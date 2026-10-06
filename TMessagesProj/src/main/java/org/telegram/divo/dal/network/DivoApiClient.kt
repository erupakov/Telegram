package org.telegram.divo.dal.network

import android.util.Log
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.telegram.messenger.BuildVars
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import org.telegram.divo.dal.utils.AccessTokenProvider
import org.telegram.divo.dal.utils.DivoLanguageManager

/**
 * Builds shared OkHttp and Retrofit instances for talking to the Divo backend.
 */
object DivoApiClient {

    private val gson: Gson by lazy {
        GsonBuilder()
            .serializeNulls()
            .create()
    }

    fun createOkHttpClient(accessTokenProvider: AccessTokenProvider): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(DivoApiConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(DivoApiConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        // Authorization header interceptor
        builder.addInterceptor(AuthorizationInterceptor(accessTokenProvider))

        //builder.authenticator(TokenAuthenticator(accessTokenProvider))

        // Logging interceptor for debug builds
        if (BuildVars.LOGS_ENABLED) {
            val logging = HttpLoggingInterceptor(LoggingInterceptor())
            logging.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(logging)
        }

        return builder.build()
    }

    fun createRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(DivoApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Simple interceptor that adds Authorization header where appropriate.
     *
     * For now, it skips auth for clearly public endpoints like /auth/ without security
     * and /geo/ and /crypto/webhook, based on the OpenAPI definition.
     */
    private class AuthorizationInterceptor(
        private val accessTokenProvider: AccessTokenProvider
    ) : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val url = originalRequest.url
            val path = url.encodedPath

            val currentLanguage = DivoLanguageManager.getLanguageCode()

            val requestBuilder = originalRequest.newBuilder()
                .addHeader("App-Platform", "android")
                .addHeader("App-Version", "(126)")
                .addHeader("Accept-Language", currentLanguage)

            if (!shouldSkipAuth(path) && originalRequest.header("Authorization") == null) {
                val token = runBlocking { accessTokenProvider.getAccessToken() }

                if (!token.isNullOrEmpty()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }
            }

            val request = requestBuilder.build()
            Log.d("DivoNetwork", "Sending request to ${request.url} with lang: ${request.header("Accept-Language")}")
            return chain.proceed(request)
        }

        private fun shouldSkipAuth(path: String): Boolean {
            // encodedPath includes the base path ("/api/auth/login" on stage, "/v2/auth/login" on prod),
            // so match on the part after it; before, none of these exclusions ever applied
            val relativePath = path.replaceFirst(BASE_PATH_PREFIX, "")
            if (relativePath.startsWith("/auth/") && !relativePath.startsWith("/auth/logout")) {
                return true
            }
            if (relativePath.startsWith("/geo/")) {
                return true
            }
            // File uploads keep the token: they are public in the spec, but have always been sent with it
            if (relativePath.startsWith("/crypto/webhook")) {
                return true
            }
            return false
        }

        private companion object {
            val BASE_PATH_PREFIX = Regex("^/(api|v\\d+)(?=/)")
        }
    }

    private class LoggingInterceptor : HttpLoggingInterceptor.Logger {
        override fun log(message: String) {
            if (message.startsWith("{") || message.startsWith("[")) {
                try {
                    val gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()
                    val jsonElement = JsonParser.parseString(message)
                    val prettyJson = gson.toJson(jsonElement)
                    Log.i("okhttp.OkHttpClient", prettyJson)
                } catch (_: Exception) {
                    Log.i("okhttp.OkHttpClient", message)
                }
            } else {
                Log.i("okhttp.OkHttpClient", message)
            }
        }
    }
}

