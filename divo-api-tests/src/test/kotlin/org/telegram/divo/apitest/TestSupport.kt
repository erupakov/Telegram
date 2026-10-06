package org.telegram.divo.apitest

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.telegram.divo.dal.network.DivoApiClient
import org.telegram.divo.dal.utils.AccessTokenProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Same Gson setup as DivoApiClient (serializeNulls), so request bodies match what the app sends. */
val appGson: Gson = GsonBuilder().serializeNulls().create()

fun fixture(name: String): String =
    checkNotNull(object {}.javaClass.getResource("/fixtures/$name")) { "missing fixture $name" }.readText()

fun fixtureJson(name: String): JsonObject = JsonParser.parseString(fixture(name)).asJsonObject

/** Deep copy of [base] with `data.<field>` replaced by [value] (JsonNull) or removed (null). */
fun JsonObject.withDataField(field: String, value: JsonElement?): JsonObject {
    val copy = deepCopy()
    val data = copy.getAsJsonObject("data")
    if (value == null) data.remove(field) else data.add(field, value)
    return copy
}

val NULL: JsonElement = JsonNull.INSTANCE

class FakeTokenProvider(var token: String? = null) : AccessTokenProvider {
    val saved = mutableListOf<String?>()
    override fun getAccessToken(): String? = token
    override fun getAccessToken(account: Int): String? = token
    override fun setAccessToken(token: String?) {
        saved += token
        this.token = token
    }
    override fun clearAccount(account: Int) {
        token = null
    }
    override fun isGoogleLogin(): Boolean = false
    override fun setGoogleLogin(isGoogle: Boolean) {}
}

/**
 * A MockWebServer mounted like the stage backend (`/api/`), with the app's real OkHttp client
 * (auth/language interceptors) and Retrofit + Gson setup in front of it.
 */
class FakeBackend(
    val tokens: FakeTokenProvider = FakeTokenProvider(),
    readTimeoutMs: Long? = null,
) : AutoCloseable {
    val server = MockWebServer().apply { start() }

    private val client: OkHttpClient = DivoApiClient.createOkHttpClient(tokens).let { base ->
        if (readTimeoutMs == null) base else base.newBuilder().readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS).build()
    }

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(server.url("/api/"))
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(appGson))
        .build()

    inline fun <reified T> service(): T = retrofit.create(T::class.java)

    fun enqueueJson(body: String, code: Int = 200) {
        server.enqueue(MockResponse().setResponseCode(code).setHeader("Content-Type", "application/json").setBody(body))
    }

    fun enqueue(response: MockResponse) = server.enqueue(response)

    fun takeRequest() = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS)) { "no request reached the server" }

    override fun close() = server.shutdown()
}
