package org.telegram.divo.common

import java.time.LocalDate
import java.time.Period
import java.util.Locale

fun String.toCountryFlagEmoji() =
    this.uppercase()
        .map { char -> Character.toCodePoint('\uD83C', '\uDDE6' + (char - 'A')) }
        .joinToString("") { String(Character.toChars(it)) }

fun String.formattedAge(locale: Locale = Locale.getDefault()): String {
    try {
        val birthDate = LocalDate.parse(this)
        val age = Period.between(birthDate, LocalDate.now()).years

        return "$age y.o."
    } catch (_: Exception) {
        return ""
    }
}