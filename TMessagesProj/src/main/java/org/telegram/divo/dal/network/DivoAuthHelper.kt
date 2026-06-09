package org.telegram.divo.dal.network

import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.telegram.divo.dal.dto.auth.LoginRequest
import java.security.MessageDigest

object DivoAuthHelper {

    interface DivoAuthCallback {
        fun onSuccess()
        fun onUserNotFound()
        fun onError(error: String)
    }

    @JvmStatic
    fun checkDivoUserExists(
        phone: String?,
        callback: DivoAuthCallback
    ): Runnable {
        if (phone.isNullOrBlank()) {
            callback.onError("Phone number is empty")
            return Runnable {}
        }
        Log.d("VideoGrid", generatePassword(phone))
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

            val result = DivoApi.authRepository.login(request)
            withContext(Dispatchers.Main) {
                if (result is DivoResult.Success) {
                    DivoApi.accessTokenProvider.setGoogleLogin(false)
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

    fun generatePassword(phoneDigits: String): String {
        val source = "divo-stage-pw::$phoneDigits"

        val hash = MessageDigest
            .getInstance("SHA-256")
            .digest(source.toByteArray())
            .joinToString("") { "%02x".format(it) }

        return "Dv9!${hash.take(20)}"
    }
}
