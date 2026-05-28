package org.telegram.divo.dal.network

import android.content.res.Resources
import android.os.Build
import android.util.Log
import org.telegram.messenger.LocaleController
import java.util.Locale

/**
 * Resolves the correct API language code to send in Accept-Language.
 *
 * Supported languages: ru, es, pt, zh, en (default fallback).
 *
 * Priority: Telegram's LocaleController (in-app language) → system locale fallback.
 */
object DivoLanguageManager {

    /** Languages supported by the Divo backend. */
    private val SUPPORTED_LANGUAGES = setOf("ru", "es", "pt", "zh")
    private const val DEFAULT_LANGUAGE = "en"

    /**
     * Returns the language code to use in Accept-Language header.
     * Reads from Telegram's LocaleController so it respects the in-app language setting.
     */
    fun getLanguageCode(): String {
        val systemLang = getTelegramLanguage()
        val result = if (systemLang in SUPPORTED_LANGUAGES) systemLang else DEFAULT_LANGUAGE
        Log.d("DivoLanguage", "Telegram lang: $systemLang → API lang: $result")
        return result
    }

    /**
     * Returns the current locale respecting Telegram's in-app language selection.
     * Falls back to system locale if LocaleController has no selection.
     */
    fun getSystemLocale(): Locale {
        return try {
            // Prefer Telegram's selected locale over system locale
            val telegramLocale = LocaleController.getInstance()?.getCurrentLocale()
            val locale = telegramLocale ?: getDeviceLocale()

            // Специальная обработка китайского
            if (locale.language == "zh") {
                return when (locale.script?.lowercase()) {
                    "hans", "cn", "sg" -> Locale.SIMPLIFIED_CHINESE
                    "hant", "tw", "hk", "mo" -> Locale.TRADITIONAL_CHINESE
                    else -> Locale.SIMPLIFIED_CHINESE
                }
            }

            locale
        } catch (e: Exception) {
            Log.e("DivoLanguage", "Failed to get locale", e)
            Locale.ENGLISH
        }
    }

    /**
     * Reads the language code from Telegram's LocaleController.
     * Falls back to system locale if not available.
     */
    private fun getTelegramLanguage(): String {
        return try {
            val telegramLocale = LocaleController.getInstance()?.getCurrentLocale()
            val locale = telegramLocale ?: getDeviceLocale()
            locale.language.lowercase().ifEmpty { DEFAULT_LANGUAGE }
        } catch (e: Exception) {
            Log.e("DivoLanguage", "Failed to get Telegram locale", e)
            getDeviceLanguage()
        }
    }

    /** Reads the physical device locale (unaffected by Telegram's in-app language). */
    private fun getDeviceLocale(): Locale {
        return try {
            val config = Resources.getSystem().configuration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.locales.get(0) ?: Locale.getDefault()
            } else {
                @Suppress("DEPRECATION")
                config.locale ?: Locale.getDefault()
            }
        } catch (e: Exception) {
            Locale.getDefault()
        }
    }

    private fun getDeviceLanguage(): String {
        return try {
            getDeviceLocale().language.lowercase().ifEmpty { DEFAULT_LANGUAGE }
        } catch (e: Exception) {
            DEFAULT_LANGUAGE
        }
    }
}
