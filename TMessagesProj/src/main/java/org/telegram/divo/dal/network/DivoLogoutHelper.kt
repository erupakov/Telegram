package org.telegram.divo.dal.network

import android.content.Context
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.UserConfig
import androidx.core.content.edit

object DivoLogoutHelper {

    @JvmStatic
    fun cleanDivoData(currentAccount: Int) {
        DivoApi.userRepository.clearCache()

        val userId = UserConfig.getInstance(currentAccount).clientUserId
        val prefs = ApplicationLoader.applicationContext.getSharedPreferences("divo_auth", Context.MODE_PRIVATE)
        prefs.edit { remove("auth_completed_$userId") }
    }
}