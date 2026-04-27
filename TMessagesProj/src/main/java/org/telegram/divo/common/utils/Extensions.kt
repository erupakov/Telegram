package org.telegram.divo.common.utils

import android.content.Context
import android.net.Uri
import org.telegram.messenger.R
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun String.toCountryFlagEmoji() =
    this.uppercase()
        .map { char -> Character.toCodePoint('\uD83C', '\uDDE6' + (char - 'A')) }
        .joinToString("") { String(Character.toChars(it)) }

fun String.formattedAge(context: Context, locale: Locale = Locale.getDefault()): String {
    try {
        val birthDate = LocalDate.parse(this)
        val age = Period.between(birthDate, LocalDate.now()).years

        return "$age ${context.getString(R.string.YearsOld)}"
    } catch (_: Exception) {
        return ""
    }
}

fun String.toAge(): Int? {
    return try {
        val birthDate = LocalDate.parse(this, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        Period.between(birthDate, LocalDate.now()).years
    } catch (e: Exception) {
        null
    }
}

fun String?.toDateFloat(): Float? {
    try {
        val birthDate = LocalDate.parse(this)
        val age = Period.between(birthDate, LocalDate.now()).years

        return age.toFloat()
    } catch (_: Exception) {
        return null
    }
}

fun String.formatWeird(): String {
    val cleaned = this.trim().removeSuffix(".")

    val parts = cleaned.split(".")
    if (parts.size < 2) return cleaned

    val integer = parts[0]
    val decimal = parts[1]

    if (decimal.all { it == '0' }) return integer

    if (decimal.endsWith("0")) return "$integer.$decimal"

    val normalizedDecimal = decimal.trimStart('0')
    return "$integer.$normalizedDecimal"
}

fun String.formatDate(): String = try {
    val localDate = LocalDate.parse(this)
    String.format(Locale.getDefault(), "%02d.%02d.%d", localDate.dayOfMonth, localDate.monthValue, localDate.year)
} catch (_: Exception) {
    this
}

fun Long.toFormattedDate(): String = Instant.ofEpochMilli(this)
    .atZone(ZoneId.systemDefault())
    .toLocalDate()
    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault()))

fun Int.toShortString(): String = when {
    this >= 1_000_000 -> {
        val value = this / 100_000 / 10.0
        if (value % 1 == 0.0) "${value.toInt()}M" else "${value}M"
    }
    this >= 1_000 -> {
        val value = this / 100 / 10.0
        if (value % 1 == 0.0) "${value.toInt()}K" else "${value}K"
    }
    else -> this.toString()
}

fun Context.uriToFile(uri: Uri): Result<File> {
    return runCatching {
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Cannot open input stream")
        val extension = getExtension(uri)
        val file = File.createTempFile("upload_", ".$extension", cacheDir)

        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        file
    }
}

fun String.toEventDisplayDate(
    countryCode: String? = null,
    city: String? = null,
    showTime: Boolean = true
): String = try {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val localDateTime = LocalDateTime.parse(this, formatter)

    val date = localDateTime.format(DateTimeFormatter.ofPattern("MMMM d", Locale.ENGLISH))
    val flag = countryCode?.toCountryFlagEmoji().orEmpty()

    if (showTime) {
        val time = localDateTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH))
        "$date · $time · $flag $city"
    } else {
        "$date · $flag $city"
    }
} catch (_: Exception) {
    this
}

fun String.getInitials(): String = this
    .trim()
    .split(" ")
    .filter { it.isNotBlank() }
    .take(2)
    .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
    .joinToString("")
    .ifBlank { "A" }

private fun Context.getExtension(uri: Uri): String {
    return contentResolver.getType(uri)
        ?.substringAfter("/")
        ?: "jpg"
}