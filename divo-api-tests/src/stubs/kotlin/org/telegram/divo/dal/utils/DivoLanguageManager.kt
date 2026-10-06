package org.telegram.divo.dal.utils

/** Stand-in for the app's language manager: tests may set the language sent in Accept-Language. */
object DivoLanguageManager {
    var currentLanguage: String = "en"
    fun getLanguageCode(): String = currentLanguage
}
