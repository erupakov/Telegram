package org.telegram.divo.apitest

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.SocketPolicy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.telegram.divo.dal.api.UserService
import org.telegram.divo.dal.dto.user.UserBlockRequest
import org.telegram.divo.dal.dto.user.toEntity
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.dal.network.isLinkProofMissing
import org.telegram.divo.dal.network.resultOf
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Level 2: real Retrofit + OkHttp + resultOf against a fake server. Every failure must become a
 * DivoResult the UI can show, never an exception.
 */
class ErrorHandlingTest {

    private val backend = FakeBackend(readTimeoutMs = 500)
    private val users = backend.service<UserService>()

    @AfterEach
    fun tearDown() = backend.close()

    @Test
    fun `422 with field errors becomes HttpError with a readable message`() = runTest {
        backend.enqueueJson(fixture("error_422_proof.json"), code = 422)
        val result = resultOf { users.blockUser(UserBlockRequest(1)) }
        val error = assertIs<DivoResult.HttpError>(result)
        assertEquals(422, error.code)
        val message = result.getErrorMessage()
        assertTrue(message.startsWith("Указанные данные неверны."), message)
        assertTrue("proof: Поле proof обязательно для заполнения." in message, message)
        assertTrue(result.isLinkProofMissing())
    }

    @Test
    fun `422 for an inactive user is not mistaken for a missing proof`() = runTest {
        backend.enqueueJson(fixture("error_422_inactive_user.json"), code = 422)
        val result = resultOf { users.getUserById(90).toEntity() }
        assertEquals("Active user with this email does not exist", result.getErrorMessage())
        assertTrue(!result.isLinkProofMissing())
    }

    @Test
    fun `5xx with an HTML body still yields an HttpError and a fallback message`() = runTest {
        backend.enqueue(MockResponse().setResponseCode(502).setHeader("Content-Type", "text/html").setBody(fixture("error_500.html")))
        val result = resultOf { users.getUserById(1).toEntity() }
        val error = assertIs<DivoResult.HttpError>(result)
        assertEquals(502, error.code)
        assertEquals("HTTP error: 502", result.getErrorMessage())
    }

    @Test
    fun `error with an empty body`() = runTest {
        backend.enqueue(MockResponse().setResponseCode(401))
        val result = resultOf { users.getUserById(1).toEntity() }
        assertEquals("HTTP error: 401", result.getErrorMessage())
    }

    @Test
    fun `200 with broken JSON is reported as an error, not a crash`() = runTest {
        backend.enqueueJson("""{"data": {"id": 1, "fullName": """)
        val result = resultOf { users.getUserById(1).toEntity() }
        assertTrue(result !is DivoResult.Success, "got $result")
    }

    @Test
    fun `200 with an HTML page instead of JSON is reported as an error`() = runTest {
        backend.enqueue(MockResponse().setResponseCode(200).setBody("<html>maintenance</html>"))
        val result = resultOf { users.getUserById(1).toEntity() }
        assertTrue(result !is DivoResult.Success, "got $result")
    }

    @Test
    fun `200 without data is reported as an error, not a crash`() = runTest {
        backend.enqueueJson("""{"status":"success","data":null}""")
        val result = resultOf { users.getUserById(1).toEntity() }
        assertTrue(result !is DivoResult.Success, "got $result")
    }

    @Test
    fun `connection dropped by the server becomes NetworkError`() = runTest {
        backend.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        assertIs<DivoResult.NetworkError>(resultOf { users.getUserById(1) })
    }

    @Test
    fun `read timeout becomes NetworkError`() = runTest {
        backend.enqueue(MockResponse().setBody(fixture("user_model.json")).setHeadersDelay(2, TimeUnit.SECONDS))
        assertIs<DivoResult.NetworkError>(resultOf { users.getUserById(1) })
    }

    @Test
    fun `block and unblock accept a JSend success with empty data`() = runTest {
        backend.enqueueJson("""{"status":"success","data":null,"message":null}""")
        backend.enqueueJson("""{"status":"success","data":{},"message":"ok"}""")
        assertIs<DivoResult.Success<*>>(resultOf { users.blockUser(UserBlockRequest(5)) })
        assertIs<DivoResult.Success<*>>(resultOf { users.unblockUser(UserBlockRequest(5)) })
    }

    @Test
    fun `block treats an empty 200 or a 204 as success`() = runTest {
        // The action succeeded on the server: the user must not see an error for it
        backend.enqueue(MockResponse().setResponseCode(204))
        backend.enqueue(MockResponse().setResponseCode(200))
        assertIs<DivoResult.Success<*>>(resultOf { users.blockUser(UserBlockRequest(5)) }, "204")
        assertIs<DivoResult.Success<*>>(resultOf { users.blockUser(UserBlockRequest(5)) }, "empty 200")
    }
}
