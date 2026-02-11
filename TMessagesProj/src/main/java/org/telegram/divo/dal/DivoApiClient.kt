package org.telegram.divo.dal

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.telegram.messenger.BuildVars
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson

/**
 * Builds shared OkHttp and Retrofit instances for talking to the Divo backend.
 */
object DivoApiClient {

    private val gson: Gson by lazy {
        Gson()
    }

    fun createOkHttpClient(accessTokenProvider: AccessTokenProvider): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(DivoApiConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(DivoApiConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        // Authorization header interceptor
        builder.addInterceptor(AuthorizationInterceptor(accessTokenProvider))

        // Logging interceptor for debug builds
        if (BuildVars.LOGS_ENABLED) {
            val logging = HttpLoggingInterceptor()
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
     * For now, it skips auth for clearly public endpoints like /auth/* without security
     * and /geo/* and /crypto/webhook*, based on the OpenAPI definition.
     */
    private class AuthorizationInterceptor(
        private val accessTokenProvider: AccessTokenProvider
    ) : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val url = originalRequest.url
            val path = url.encodedPath

            if (shouldSkipAuth(path)) {
                return chain.proceed(originalRequest)
            }

            val token = runBlocking { accessTokenProvider.getAccessToken() }
            if (token.isNullOrEmpty()) {
                return chain.proceed(originalRequest)
            }

            val newRequest: Request = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()

            return chain.proceed(newRequest)
        }

        private fun shouldSkipAuth(path: String): Boolean {
            if (path.startsWith("/auth/")) {
                return true
            }
            if (path.startsWith("/geo/")) {
                return true
            }
            if (path.startsWith("/file/upload-file") || path.startsWith("/file/upload-files")) {
                return true
            }
            if (path.startsWith("/crypto/webhook")) {
                return true
            }
            return false
        }
    }
}

