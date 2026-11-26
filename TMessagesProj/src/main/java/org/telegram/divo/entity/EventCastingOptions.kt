package org.telegram.divo.entity

import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.R

/**
 * Event casting attributes are plain string identifiers on the server
 * (see app-backend SelectionFormatType / TargetGenderType).
 *
 * The localized labels live in string arrays whose order matches [ids],
 * so index-alignment is what maps an id to a label and back.
 */
object SelectionFormat {
    val ids = listOf(
        "open_call",
        "go_see_invite_only",
        "online_self_tape",
        "client_casting",
        "agency_casting",
    )

    private fun labels(): List<String> =
        ApplicationLoader.applicationContext.resources
            .getStringArray(R.array.SelectionFormatItems)
            .toList()

    /** Localized option labels in [ids] order, for the picker sheet. */
    fun options(): List<String> = labels()

    fun labelFor(id: String?): String? {
        if (id.isNullOrBlank()) return null
        return labels().getOrNull(ids.indexOf(id))
    }

    fun idFor(label: String?): String? {
        if (label.isNullOrBlank()) return null
        return ids.getOrNull(labels().indexOf(label))
    }
}

object TargetGender {
    val ids = listOf(
        "female",
        "male",
        "non_binary",
        "transgender_female",
        "transgender_male",
        "custom",
        "other",
    )

    private fun labels(): List<String> =
        ApplicationLoader.applicationContext.resources
            .getStringArray(R.array.TargetGenderItems)
            .toList()

    /** Localized option labels in [ids] order, for the picker sheet. */
    fun options(): List<String> = labels()

    fun labelFor(id: String?): String? {
        if (id.isNullOrBlank()) return null
        return labels().getOrNull(ids.indexOf(id))
    }

    fun idFor(label: String?): String? {
        if (label.isNullOrBlank()) return null
        return ids.getOrNull(labels().indexOf(label))
    }
}