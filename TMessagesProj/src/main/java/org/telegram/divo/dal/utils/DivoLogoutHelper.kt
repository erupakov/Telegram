package org.telegram.divo.dal.utils

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.UserConfig
import androidx.core.content.edit
import org.telegram.divo.dal.network.DivoApi

object DivoLogoutHelper {

    /**
     * Wipes all local Divo data of the account and invalidates its token on the server.
     *
     * Local data is cleared synchronously and regardless of the server response: after the
     * account was deleted (or the token expired) the logout request fails, and keeping the
     * stale token / cached profile leaks data of the old account into the next registration
     * with the same phone number.
     */
    @JvmStatic
    fun cleanDivoData(currentAccount: Int) {
        // Capture everything before clearing: performLogout() resets UserConfig right after this call
        val tgUserId = UserConfig.getInstance(currentAccount).clientUserId
        val userRepository = DivoApi.userRepositoryFor(currentAccount)
        val divoUserId = userRepository.currentUserFlow.value?.id
        val token = DivoApi.accessTokenProvider.getAccessToken(currentAccount)

        userRepository.clearCache()
        ApplicationLoader.applicationContext.getSharedPreferences("divo_auth", Context.MODE_PRIVATE)
            .edit { remove("auth_completed_$tgUserId") }
        DivoApi.authRepository.notifyLoggedOut()

        CoroutineScope(Dispatchers.IO).launch {
            if (!token.isNullOrEmpty()) {
                DivoApi.authRepository.logout(token)
            }
            try {
                if (divoUserId != null) {
                    DivoApi.faceRecognitionRepository.clearHistory(divoUserId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
