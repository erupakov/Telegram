package org.telegram.divo.dal.utils

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.telegram.divo.dal.dto.auth.TelegramLinkRequest
import org.telegram.divo.dal.dto.auth.TelegramLinkResponse
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.messenger.FileLog
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.TLRPC
import kotlin.coroutines.resume

/**
 * Links the current Divo account to a Telegram (teamgram) user via `POST /auth/telegram-link`.
 *
 * The backend requires a proof signed by the teamgram server: `divo_link_proof` (valid 10 minutes)
 * and `divo_link_phone` (the phone the proof was made for) from a fresh `help.getAppConfig`.
 * The proof is never cached: it is fetched right before each attempt.
 *
 * While the backend/teamgram changes are not rolled out, the proof is absent and the request is
 * sent the old way (without `proof`, with the phone known to the client).
 */
object DivoTelegramLinker {

    private const val KEY_PROOF = "divo_link_proof"
    private const val KEY_PHONE = "divo_link_phone"
    private const val APP_CONFIG_TIMEOUT_MS = 10_000L

    data class LinkProof(val proof: String, val phone: String)

    suspend fun link(
        account: Int,
        divoUserId: Long?,
        telegramUserId: Long,
        fallbackPhone: String,
        deviceId: String?,
        deviceType: String
    ): DivoResult<TelegramLinkResponse> {
        val proof = fetchProof(account)
        val result = send(proof, divoUserId, telegramUserId, fallbackPhone, deviceId, deviceType)
        // 403 with a proof may mean it expired between fetching and sending: retry once with a fresh one.
        // Without a proof, or on 404 / 409 / 422, retrying is pointless.
        if (proof != null && result is DivoResult.HttpError && result.code == 403) {
            val freshProof = fetchProof(account) ?: return result
            return send(freshProof, divoUserId, telegramUserId, fallbackPhone, deviceId, deviceType)
        }
        return result
    }

    private suspend fun send(
        proof: LinkProof?,
        divoUserId: Long?,
        telegramUserId: Long,
        fallbackPhone: String,
        deviceId: String?,
        deviceType: String
    ): DivoResult<TelegramLinkResponse> {
        val request = TelegramLinkRequest(
            divoUserId = divoUserId,
            telegramUserId = telegramUserId,
            // The proof is bound to this exact phone; any other value fails the signature check
            phone = proof?.phone ?: fallbackPhone,
            proof = proof?.proof,
            deviceId = deviceId,
            deviceType = deviceType
        )
        return DivoApi.authRepository.linkTelegramAccount(request)
    }

    /**
     * Requests a fresh `help.getAppConfig` (hash 0, bypassing the cached config) and extracts the link
     * proof. Returns null when teamgram doesn't issue one (feature off, not logged in, 2FA enabled,
     * no phone) or the request fails.
     */
    suspend fun fetchProof(account: Int): LinkProof? {
        val config = withTimeoutOrNull(APP_CONFIG_TIMEOUT_MS) {
            suspendCancellableCoroutine<TLRPC.TL_help_appConfig?> { continuation ->
                val req = TLRPC.TL_help_getAppConfig().apply { hash = 0 }
                val reqId = ConnectionsManager.getInstance(account).sendRequest(req) { response, error ->
                    if (error != null) {
                        FileLog.e("divo link proof: getAppConfig error ${error.code} ${error.text}")
                    }
                    continuation.resume(response as? TLRPC.TL_help_appConfig)
                }
                continuation.invokeOnCancellation {
                    ConnectionsManager.getInstance(account).cancelRequest(reqId, true)
                }
            }
        } ?: return null

        val json = config.config as? TLRPC.TL_jsonObject
        if (json == null) {
            FileLog.d("divo link proof: getAppConfig returned no json object")
            return null
        }
        val proof = json.stringValue(KEY_PROOF)
        val phone = json.stringValue(KEY_PHONE)
        // Presence only, never the values: tells whether teamgram issues the proof at all
        FileLog.d("divo link proof: $KEY_PROOF=${!proof.isNullOrBlank()} $KEY_PHONE=${!phone.isNullOrBlank()} (${json.value.size} config keys)")
        if (proof.isNullOrBlank() || phone.isNullOrBlank()) return null
        return LinkProof(proof, phone)
    }

    /**
     * True when the backend rejected the link because no proof was sent: teamgram didn't issue
     * `divo_link_proof` (link secret not configured, 2FA, no phone), not a problem with the user's data.
     */
    fun isProofMissing(result: DivoResult<*>): Boolean =
        result is DivoResult.HttpError && result.code == 422 && result.body?.errors?.containsKey("proof") == true

    private fun TLRPC.TL_jsonObject.stringValue(key: String): String? =
        when (val v = value.firstOrNull { it.key == key }?.value) {
            is TLRPC.TL_jsonString -> v.value
            // A phone may come as a JSON number; render it without exponent or fraction
            is TLRPC.TL_jsonNumber -> java.math.BigDecimal(v.value).toBigInteger().toString()
            else -> null
        }
}
