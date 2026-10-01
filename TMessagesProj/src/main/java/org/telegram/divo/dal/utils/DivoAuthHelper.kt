package org.telegram.divo.dal.utils

import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.telegram.divo.analytics.AnalyticsEvent
import org.telegram.divo.analytics.DivoAnalytics
import org.telegram.divo.dal.dto.auth.LoginRequest
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.common.utils.TelegramProfileHelper
import org.telegram.messenger.FileLog
import org.telegram.messenger.UserConfig
import java.security.MessageDigest

object DivoAuthHelper {

    /** First name LoginActivity uses for auth.signUp before the Divo registration form is filled. */
    const val TELEGRAM_PLACEHOLDER_FIRST_NAME = "Divo User"

    interface DivoAuthCallback {
        fun onSuccess()
        fun onUserNotFound()
        fun onError(error: String)
    }

    /**
     * Logs into the Divo account bound to [phone] (the phone flow derives Divo credentials from it).
     *
     * If the account exists but is linked to another Telegram user (the Telegram account with this
     * phone was deleted and created again), it is re-linked to [telegramUserId], so the user gets
     * their existing Divo profile instead of a mix of a new profile and the old one.
     */
    @JvmStatic
    fun checkDivoUserExists(
        account: Int,
        phone: String?,
        telegramUserId: Long,
        callback: DivoAuthCallback
    ): Runnable {
        if (phone.isNullOrBlank()) {
            callback.onError("Phone number is empty")
            return Runnable {}
        }

        // Drop whatever is left from a previous session in this slot (token, cached profile/avatar),
        // otherwise it leaks into the account we are logging into now
        DivoApi.userRepositoryFor(account).clearCache()

        val job = CoroutineScope(Dispatchers.IO).launch {
            val cleanPhone = phone.replace("+", "").trim()
            val email = "$cleanPhone@divo.global"
            val password = generatePassword(phone)
            val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
            val model = Build.MODEL ?: "Android Device"
            val deviceId = "$manufacturer $model"
            val deviceType = "android"

            val request = LoginRequest(
                email = email,
                password = password,
                deviceId = deviceId,
                deviceType = deviceType
            )

            DivoAnalytics.logEvent(AnalyticsEvent.SignInStart("phone"))
            val loginResult = DivoApi.authRepository.login(request)
            val result = if (loginResult is DivoResult.Success && telegramUserId != 0L) {
                ensureTelegramLink(account, phone, telegramUserId, deviceId, deviceType)
            } else {
                loginResult
            }
            withContext(Dispatchers.Main) {
                if (result is DivoResult.Success) {
                    DivoApi.accessTokenProvider.setGoogleLogin(false)
                    DivoAnalytics.logEvent(AnalyticsEvent.SignInComplete("phone"))
                    callback.onSuccess()
                } else {
                    val errorMsg = result.getErrorMessage()
                    if (errorMsg.contains("User not found", ignoreCase = true)) {
                        callback.onUserNotFound()
                    } else {
                        callback.onError(errorMsg)
                    }
                }
            }
        }
        return Runnable { job.cancel() }
    }

    private suspend fun ensureTelegramLink(
        account: Int,
        phone: String,
        telegramUserId: Long,
        deviceId: String,
        deviceType: String
    ): DivoResult<*> {
        val userResult = DivoApi.userRepository.getCurrentUserInfo(forceRefresh = true)
        if (userResult !is DivoResult.Success) {
            // Can't verify the link right now; don't block the login because of it
            return DivoResult.Success(Unit)
        }
        val divoUser = userResult.value
        if (divoUser.telegramId == telegramUserId) {
            return userResult
        }
        val linkResult = DivoTelegramLinker.link(
            account = account,
            divoUserId = divoUser.id.toLong(),
            telegramUserId = telegramUserId,
            fallbackPhone = phone,
            deviceId = deviceId,
            deviceType = deviceType
        )
        if (linkResult !is DivoResult.Success) {
            // The backend can't re-link a Divo account already linked to another Telegram user (409) and
            // has no unlink API; 403/404 mean no valid proof or no account for the phone. The login itself
            // succeeded, so keep the user in their Divo account instead of failing the sign-in.
            FileLog.e("Divo telegram re-link failed: ${linkResult.getErrorMessage()}")
            return DivoResult.Success(Unit)
        }
        DivoApi.userRepository.getCurrentUserInfo(forceRefresh = true)
        syncTelegramName(divoUser.fullName)
        return linkResult
    }

    /**
     * A freshly signed up Telegram account carries a placeholder name; take the name from the
     * existing Divo profile instead.
     */
    private suspend fun syncTelegramName(fullName: String) {
        if (fullName.isBlank()) return
        val account = UserConfig.selectedAccount
        val tgUser = UserConfig.getInstance(account).currentUser ?: return
        if (tgUser.first_name != TELEGRAM_PLACEHOLDER_FIRST_NAME || !tgUser.last_name.isNullOrEmpty()) return
        val parts = fullName.trim().split(" ", limit = 2)
        TelegramProfileHelper.updateTelegramName(account, parts[0], parts.getOrElse(1) { "" })
    }

    fun generatePassword(phoneDigits: String): String {
        val source = "divo-stage-pw::$phoneDigits"

        val hash = MessageDigest
            .getInstance("SHA-256")
            .digest(source.toByteArray())
            .joinToString("") { "%02x".format(it) }

        return "Dv9!${hash.take(20)}"
    }
}
