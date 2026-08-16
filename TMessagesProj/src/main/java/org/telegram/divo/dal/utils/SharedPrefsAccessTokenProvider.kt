package org.telegram.divo.dal.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

import org.telegram.messenger.UserConfig
import androidx.core.content.edit
import org.telegram.divo.dal.utils.AccessTokenProvider

private const val PREFS_NAME_PREFIX = "divo_auth_"
private const val KEY_ACCESS_TOKEN = "access_token"
private const val KEY_IS_GOOGLE_LOGIN = "is_google_login"

/**
 * EncryptedSharedPreferences-based implementation of [AccessTokenProvider].
 */
class SharedPrefsAccessTokenProvider(
    val context: Context
) : AccessTokenProvider {

    private val masterKey by lazy {
        MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private var cachedAccount: Int = -1
    private var cachedPrefs: SharedPreferences? = null

    private fun getPrefsForAccount(account: Int): SharedPreferences {
        val currentPrefs = cachedPrefs
        
        if (account == cachedAccount && currentPrefs != null) {
            return currentPrefs
        }
        
        val newPrefs = try {
            EncryptedSharedPreferences.create(
                context.applicationContext,
                PREFS_NAME_PREFIX + account,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // In case of keystore corruption, fallback or clear data
            context.applicationContext.getSharedPreferences(PREFS_NAME_PREFIX + account, Context.MODE_PRIVATE).edit { clear() }
            EncryptedSharedPreferences.create(
                context.applicationContext,
                PREFS_NAME_PREFIX + account,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        
        if (account == UserConfig.selectedAccount) {
            cachedPrefs = newPrefs
            cachedAccount = account
        }
        
        return newPrefs
    }

    private val prefs: SharedPreferences
        get() = getPrefsForAccount(UserConfig.selectedAccount)

    override fun getAccessToken(): String? = getAccessToken(UserConfig.selectedAccount)

    override fun getAccessToken(account: Int): String? = getPrefsForAccount(account).getString(KEY_ACCESS_TOKEN, null)

    override fun setAccessToken(token: String?) {
        prefs.edit {
            if (token == null) {
                remove(KEY_ACCESS_TOKEN)
                remove(KEY_IS_GOOGLE_LOGIN)
            } else {
                putString(KEY_ACCESS_TOKEN, token)
            }
        }
    }

    override fun isGoogleLogin(): Boolean {
        return prefs.getBoolean(KEY_IS_GOOGLE_LOGIN, false)
    }

    override fun setGoogleLogin(isGoogle: Boolean) {
        prefs.edit {
            putBoolean(KEY_IS_GOOGLE_LOGIN, isGoogle)
        }
    }
}

