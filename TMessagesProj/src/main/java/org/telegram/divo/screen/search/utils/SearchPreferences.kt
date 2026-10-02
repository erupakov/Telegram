package org.telegram.divo.screen.search.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

private const val PREFS_NAME = "search_prefs"
private const val FIRST_SEARCH_KEY = "face_search_first_use"

class SearchPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isFaceSearchFirstUse: Boolean
        get() = prefs.getBoolean(FIRST_SEARCH_KEY, true)
        set(value) = prefs.edit { putBoolean(FIRST_SEARCH_KEY, value) }
}