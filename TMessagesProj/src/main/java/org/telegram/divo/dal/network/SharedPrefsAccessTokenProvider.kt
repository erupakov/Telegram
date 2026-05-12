package org.telegram.divo.dal.network

import android.content.Context
import android.content.SharedPreferences

import org.telegram.messenger.UserConfig
import androidx.core.content.edit

private const val PREFS_NAME_PREFIX = "divo_auth_"
private const val KEY_ACCESS_TOKEN = "access_token"

/**
 * Simple SharedPreferences-based implementation of [AccessTokenProvider].
 */
class SharedPrefsAccessTokenProvider(
    val context: Context
) : AccessTokenProvider {

    private val prefs: SharedPreferences
        get() = context.applicationContext.getSharedPreferences(PREFS_NAME_PREFIX + UserConfig.selectedAccount, Context.MODE_PRIVATE)

    override fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    override fun setAccessToken(token: String?) {
        prefs.edit {
            if (token == null) {
                remove(KEY_ACCESS_TOKEN)
            } else {
                putString(KEY_ACCESS_TOKEN, token)
            }
        }
    }
}

