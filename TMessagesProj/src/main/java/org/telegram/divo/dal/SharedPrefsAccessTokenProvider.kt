package org.telegram.divo.dal

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val PREFS_NAME = "divo_auth"
private const val KEY_ACCESS_TOKEN = "access_token"

/**
 * Simple SharedPreferences-based implementation of [AccessTokenProvider].
 */
class SharedPrefsAccessTokenProvider(
    context: Context
) : AccessTokenProvider {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    override suspend fun setAccessToken(token: String?) {
        withContext(Dispatchers.IO) {
            prefs.edit().apply {
                if (token == null) {
                    remove(KEY_ACCESS_TOKEN)
                } else {
                    putString(KEY_ACCESS_TOKEN, token)
                }
            }.apply()
        }
    }
}

