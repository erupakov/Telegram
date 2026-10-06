package org.telegram.divo.apitest

import com.google.gson.JsonParser
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.telegram.divo.dal.api.AuthService
import org.telegram.divo.dal.api.UserService
import org.telegram.divo.dal.dto.auth.TelegramLinkRequest
import org.telegram.divo.dal.dto.user.UserBlockRequest
import org.telegram.divo.dal.dto.user.UserBlockedListRequest
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.repository.AuthRepository
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/** Level 2: what the client actually puts on the wire, checked against openapi.json and the backend notes. */
class RequestContractTest {

    private val tokens = FakeTokenProvider(token = "user-token")
    private val backend = FakeBackend(tokens)
    private val users = backend.service<UserService>()
    private val auth = AuthRepository(backend.service<AuthService>(), tokens)

    @AfterEach
    fun tearDown() = backend.close()

    // The recorded body is a one-shot buffer: read it once per request
    private fun body(request: okhttp3.mockwebserver.RecordedRequest) =
        JsonParser.parseString(request.body.readUtf8()).asJsonObject

    @Test
    fun `block and unblock post userId`() = runTest {
        backend.enqueueJson("""{"status":"success"}""")
        backend.enqueueJson("""{"status":"success"}""")
        users.blockUser(UserBlockRequest(77))
        users.unblockUser(UserBlockRequest(77))

        val block = backend.takeRequest()
        assertEquals("POST", block.method)
        assertEquals("/api/user/block", block.path)
        val json = body(block)
        assertEquals(77, json["userId"].asInt)
        assertEquals(setOf("userId"), json.keySet(), "UserBlockRequest has additionalProperties: false")
        assertEquals("/api/user/unblock", backend.takeRequest().path)
    }

    @Test
    fun `blocked list posts offset and limit`() = runTest {
        backend.enqueueJson("""{"data":{"items":[]}}""")
        users.getBlockedUsers(UserBlockedListRequest(offset = 40, limit = 20))
        val request = backend.takeRequest()
        assertEquals("/api/user/blocked", request.path)
        val json = body(request)
        assertEquals(40, json["offset"].asInt)
        assertEquals(20, json["limit"].asInt)
    }

    @Test
    fun `regular requests carry the user token, platform and language`() = runTest {
        backend.enqueueJson(fixture("user_model.json"))
        users.getUserById(32003)
        val request = backend.takeRequest()
        assertEquals("/api/user/32003", request.path)
        assertEquals("Bearer user-token", request.getHeader("Authorization"))
        assertEquals("android", request.getHeader("App-Platform"))
        assertEquals("en", request.getHeader("Accept-Language"))
    }

    @Test
    fun `no token means no Authorization header`() = runTest {
        tokens.token = null
        backend.enqueueJson(fixture("user_model.json"))
        users.getUserById(1)
        assertNull(backend.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `telegram-link sends proof, the proof phone and the current token`() = runTest {
        backend.enqueueJson("""{"status":"success","data":{"accessToken":"new-token","type":"bearer","user":null},"message":null}""")
        val result = auth.linkTelegramAccount(
            TelegramLinkRequest(divoUserId = 42, telegramUserId = 123456789, phone = "79990000000", proof = "1700000000.abcdef", deviceId = "dev")
        )
        assertIs<DivoResult.Success<*>>(result)

        val request = backend.takeRequest()
        assertEquals("/api/auth/telegram-link", request.path)
        assertEquals("Bearer user-token", request.getHeader("Authorization"))
        val json = body(request)
        assertEquals("1700000000.abcdef", json["proof"].asString)
        assertEquals("79990000000", json["phone"].asString)
        assertEquals(123456789L, json["telegramUserId"].asLong)
        assertEquals(42L, json["divoUserId"].asLong)
        assertEquals("new-token", tokens.token, "the linked account's token replaces the old one")
    }

    @Test
    fun `telegram-link without a token sends no Authorization header`() = runTest {
        tokens.token = null
        backend.enqueueJson("""{"status":"success","data":{"accessToken":"t","type":"bearer","user":null},"message":null}""")
        auth.linkTelegramAccount(TelegramLinkRequest(telegramUserId = 1, phone = "7999", proof = "p"))
        assertNull(backend.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `a failed telegram-link keeps the current token`() = runTest {
        backend.enqueueJson(fixture("error_422_proof.json"), code = 422)
        auth.linkTelegramAccount(TelegramLinkRequest(telegramUserId = 1, phone = "7999", proof = null))
        assertEquals("user-token", tokens.token)
        assertEquals(emptyList(), tokens.saved)
    }

    @Test
    fun `a successful telegram-link without a token in the response keeps the current token`() = runTest {
        // A 200 that carries no accessToken must not log the user out by wiping the stored token
        backend.enqueueJson("""{"status":"success","data":null,"message":null}""")
        auth.linkTelegramAccount(TelegramLinkRequest(telegramUserId = 1, phone = "7999", proof = "p"))
        assertEquals("user-token", tokens.token)
    }

    @Test
    fun `logout sends the token it was given, not the stored one`() = runTest {
        tokens.token = "token-of-another-account"
        backend.enqueueJson("""{"status":"success"}""")
        auth.logout("old-account-token")
        assertEquals("Bearer old-account-token", backend.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `login and registration are not sent with a stale user token`() = runTest {
        // /auth/* endpoints are public; a leftover token from another account must not ride along
        backend.enqueueJson("""{"status":"success","data":{"accessToken":"x"}}""")
        backend.service<AuthService>().login(
            appGson.fromJson("""{"email":"a@b.c","password":"p"}""", org.telegram.divo.dal.dto.auth.LoginRequest::class.java)
        )
        assertNull(backend.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `file uploads still carry the user token`() = runTest {
        backend.enqueueJson("""{"data":{"uuid":"u","fullUrl":"https://x/u.png"}}""")
        val part = okhttp3.MultipartBody.Part.createFormData(
            "file", "a.jpg", okhttp3.RequestBody.create(null, byteArrayOf(1, 2, 3))
        )
        users.uploadFile(part)
        val request = backend.takeRequest()
        assertEquals("/api/file/upload-file", request.path)
        assertEquals("Bearer user-token", request.getHeader("Authorization"))
    }

    @Test
    fun `logout keeps its Authorization even though it is under auth`() = runTest {
        tokens.token = null
        backend.enqueueJson("""{"status":"success"}""")
        auth.logout("t")
        assertEquals("Bearer t", backend.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `geo search is public and goes without the token`() = runTest {
        backend.enqueueJson("""{"data":[]}""")
        backend.service<org.telegram.divo.dal.api.GeoService>().searchByAddressName("Moscow")
        val request = backend.takeRequest()
        assertEquals("/api/geo/search-by-address-name?query=Moscow", request.path)
        assertNull(request.getHeader("Authorization"))
    }
}
