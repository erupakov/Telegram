package org.telegram.divo.dal.utils

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.UserConfig
import androidx.core.content.edit
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult

object DivoLogoutHelper {

    @JvmStatic
    fun cleanDivoData(currentAccount: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = DivoApi.authRepository.logout()
            if (result is DivoResult.Success) {
                val userId = UserConfig.getInstance(currentAccount).clientUserId
                val prefs = ApplicationLoader.applicationContext.getSharedPreferences("divo_auth", Context.MODE_PRIVATE)
                prefs.edit { remove("auth_completed_$userId") }
            }
        }
    }
}