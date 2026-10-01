package org.telegram.divo.common.utils

/**
 * The backend validates social/website links as URLs with a scheme (`instagram.com/x` -> 422),
 * so prepend https:// to anything the user typed without one. Blank stays blank.
 */
fun String.withHttpsScheme(): String {
    val trimmed = trim()
    if (trimmed.isEmpty()) return trimmed
    return if (trimmed.contains("://")) trimmed else "https://$trimmed"
}
