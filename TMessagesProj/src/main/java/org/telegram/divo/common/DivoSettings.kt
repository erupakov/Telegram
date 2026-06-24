package org.telegram.divo.common

import android.content.Context
import android.content.SharedPreferences
import org.telegram.messenger.ApplicationLoader
import java.util.Locale

object DivoSettings {
    private const val PREFS_NAME = "divo_settings"
    private const val KEY_MEASURING_SYSTEM = "measuring_system"

    const val SYSTEM_METRIC = "metric"
    const val SYSTEM_IMPERIAL = "imperial"

    private val prefs: SharedPreferences by lazy {
        ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var measuringSystem: String
        get() {
            return prefs.getString(KEY_MEASURING_SYSTEM, null) ?: getDefaultMeasuringSystem()
        }
        set(value) {
            prefs.edit().putString(KEY_MEASURING_SYSTEM, value).apply()
        }

    private fun getDefaultMeasuringSystem(): String {
        val country = Locale.getDefault().country
        return if (country == "US" || country == "LR" || country == "MM") {
            SYSTEM_IMPERIAL
        } else {
            SYSTEM_METRIC
        }
    }
}
