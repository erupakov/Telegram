package android.content.res

import org.telegram.messenger.R

/** Returns the English values of the string arrays the mappers read. */
open class Resources {
    open fun getStringArray(id: Int): Array<String> = when (id) {
        R.array.GenderItems -> arrayOf("All", "Female", "Male")
        R.array.ModelNewTalentAgency -> arrayOf("Model", "New talent", "Agency")
        else -> emptyArray()
    }
}
