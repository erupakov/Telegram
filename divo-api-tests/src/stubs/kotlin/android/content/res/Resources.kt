package android.content.res

import org.telegram.messenger.R

/** Returns the English values of the string arrays the mappers read. */
open class Resources {
    open fun getStringArray(id: Int): Array<String> = when (id) {
        R.array.GenderItems -> arrayOf("All", "Female", "Male")
        R.array.ModelNewTalentAgency -> arrayOf("Model", "New talent", "Agency")
        // Order must match SelectionFormat.ids / TargetGender.ids in entity/EventCastingOptions.kt
        R.array.SelectionFormatItems -> arrayOf(
            "Open Call",
            "Go-See / Invite Only",
            "Online / Self-Tape",
            "Client Casting",
            "Agency Casting",
        )
        R.array.TargetGenderItems -> arrayOf(
            "Female",
            "Male",
            "Non-binary",
            "Transgender female",
            "Transgender male",
            "Custom",
            "Other",
        )
        else -> emptyArray()
    }
}
