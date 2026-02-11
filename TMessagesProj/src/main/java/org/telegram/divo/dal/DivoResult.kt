package org.telegram.divo.dal

import com.google.gson.Gson
import java.io.IOException
import org.telegram.divo.dto.common.ErrorResponse
import retrofit2.Response

/**
 * Unified result type for Divo network operations.
 */
sealed class DivoResult<out T> {

    data class Success<T>(val data: T) : DivoResult<T>()

    data class NetworkError(val exception: IOException) : DivoResult<Nothing>()

    data class HttpError(val code: Int, val body: ErrorResponse?) : DivoResult<Nothing>()

    data class UnknownError(val throwable: Throwable) : DivoResult<Nothing>()
}

/**
 * Helper to execute Retrofit calls and map them into [DivoResult].
 */
suspend inline fun <reified T> safeCall(
    crossinline block: suspend () -> Response<T>
): DivoResult<T> {
    return try {
        val response = block()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                DivoResult.Success(body)
            } else {
                DivoResult.UnknownError(IllegalStateException("Empty response body"))
            }
        } else {
            val errorBodyString = try {
                response.errorBody()?.string()
            } catch (e: IOException) {
                null
            }
            val error = errorBodyString?.let {
                runCatching {
                    Gson().fromJson(it, ErrorResponse::class.java)
                }.getOrNull()
            }
            DivoResult.HttpError(response.code(), error)
        }
    } catch (ioe: IOException) {
        DivoResult.NetworkError(ioe)
    } catch (t: Throwable) {
        DivoResult.UnknownError(t)
    }
}

