package org.telegram.divo.dal.network

import android.content.res.Resources
import android.os.Build
import android.util.Log
import java.util.Locale

/**
 * Resolves the correct API language code to send in Accept-Language.
 *
 * Supported languages: ru, es, pt, zh, en (default fallback).
 *
 * Key insight: Resources.getSystem() always reflects the REAL device locale
 * and is NOT affected by Telegram's Locale.setDefault() / resources.updateConfiguration() calls.
 */
object DivoLanguageManager {

    /** Languages supported by the Divo backend. */
    private val SUPPORTED_LANGUAGES = setOf("ru", "es", "pt", "zh")
    private const val DEFAULT_LANGUAGE = "en"

    /**
     * Returns the language code to use in Accept-Language header.
     * Maps device system locale → supported language, falls back to "en".
     */
    fun getLanguageCode(): String {
        val systemLang = getSystemLanguage()
        val result = if (systemLang in SUPPORTED_LANGUAGES) systemLang else DEFAULT_LANGUAGE
        Log.d("DivoLanguage", "System lang: $systemLang → API lang: $result")
        return result
    }

    fun getSystemLocale(): Locale {
        return try {
            val config = Resources.getSystem().configuration

            val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.locales.get(0) ?: Locale.getDefault()
            } else {
                @Suppress("DEPRECATION")
                config.locale ?: Locale.getDefault()
            }

            // Специальная обработка китайского
            if (locale.language == "zh") {
                return when (locale.script?.lowercase()) {
                    "hans", "cn", "sg" -> Locale.SIMPLIFIED_CHINESE
                    "hant", "tw", "hk", "mo" -> Locale.TRADITIONAL_CHINESE
                    else -> Locale.SIMPLIFIED_CHINESE // по умолчанию упрощённый
                }
            }

            locale
        } catch (e: Exception) {
            Log.e("DivoLanguage", "Failed to get system locale", e)
            Locale.ENGLISH
        }
    }

    /**
     * Reads the REAL device locale via Resources.getSystem().
     * This is the only locale source that Telegram cannot override.
     */
    private fun getSystemLanguage(): String {
        return try {
            val config = Resources.getSystem().configuration
            val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.locales[0] ?: Locale.getDefault()
            } else {
                @Suppress("DEPRECATION")
                config.locale ?: Locale.getDefault()
            }
            // Normalize: "zh-Hans", "zh-Hant", "zh-CN" etc. all → "zh"
            locale.language.lowercase().ifEmpty { DEFAULT_LANGUAGE }
        } catch (e: Exception) {
            Log.e("DivoLanguage", "Failed to get system locale", e)
            DEFAULT_LANGUAGE
        }
    }
}
