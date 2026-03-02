package org.telegram.divo.dal.network

import com.google.gson.Gson
import java.io.IOException
import org.telegram.divo.dal.dto.common.ErrorResponse
import retrofit2.HttpException
import retrofit2.Response
import kotlin.coroutines.cancellation.CancellationException

/**
 * Unified result type for Divo network operations.
 */
sealed class DivoResult<out T> {

    data class Success<T>(val value: T) : DivoResult<T>()

    data class NetworkError(val exception: IOException) : DivoResult<Nothing>()

    data class HttpError(val code: Int, val body: ErrorResponse?) : DivoResult<Nothing>()

    data class UnknownError(val throwable: Throwable) : DivoResult<Nothing>()
}

/**
 * Helper to execute Retrofit calls and map them into [DivoResult].
 */
suspend fun <T> resultOf(
    block: suspend () -> T
): DivoResult<T> {
    return try {
        DivoResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        DivoResult.NetworkError(e)
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        val parsed = errorBody?.let {
            runCatching {
                Gson().fromJson(it, ErrorResponse::class.java)
            }.getOrNull()
        }
        DivoResult.HttpError(e.code(), parsed)
    } catch (t: Throwable) {
        DivoResult.UnknownError(t)
    }
}

inline fun <T, R> DivoResult<T>.map(
    transform: (T) -> R
): DivoResult<R> {
    return when (this) {
        is DivoResult.Success -> {
            try {
                DivoResult.Success(transform(value))
            } catch (t: Throwable) {
                DivoResult.UnknownError(t)
            }
        }
        is DivoResult.NetworkError -> this
        is DivoResult.HttpError -> this
        is DivoResult.UnknownError -> this
    }
}

fun DivoResult<*>.getErrorMessage(): String {
    return when (this) {
        is DivoResult.Success -> ""
        is DivoResult.NetworkError -> exception.message ?: "Network error"
        is DivoResult.HttpError -> "HTTP error: $code"
        is DivoResult.UnknownError -> throwable.message ?: "Unknown error"
    }
}

suspend fun <T, R> DivoResult<T>.flatMap(
    transform: suspend (T) -> DivoResult<R>
): DivoResult<R> = when (this) {
    is DivoResult.Success -> transform(value)
    is DivoResult.NetworkError -> this
    is DivoResult.HttpError -> this
    is DivoResult.UnknownError -> this
}

