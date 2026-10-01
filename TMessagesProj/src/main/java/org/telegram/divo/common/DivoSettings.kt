package org.telegram.divo.common

import android.content.Context
import android.content.SharedPreferences
import org.telegram.messenger.ApplicationLoader
import java.util.Locale

object DivoSettings {
    private const val PREFS_NAME = "divo_settings"
    private const val KEY_MEASURING_SYSTEM = "measuring_system"
    private const val KEY_FACE_SEARCH_SIMILARITY = "face_search_similarity_percent"

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

    /** Last similarity threshold chosen in the Face Recognition results filter, null if never set. */
    var faceSearchSimilarityPercent: Int?
        get() = if (prefs.contains(KEY_FACE_SEARCH_SIMILARITY)) prefs.getInt(KEY_FACE_SEARCH_SIMILARITY, 0) else null
        set(value) {
            prefs.edit().apply {
                if (value == null) remove(KEY_FACE_SEARCH_SIMILARITY) else putInt(KEY_FACE_SEARCH_SIMILARITY, value)
            }.apply()
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
