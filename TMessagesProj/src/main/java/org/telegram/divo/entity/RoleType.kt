package org.telegram.divo.entity

enum class RoleType(val value: String) {
    MODEL("model"),
    NEW_FACE("new_face"),
    AGENCY("agency_employee"),
    UNKNOWN("");

    fun isModel(): Boolean = this == NEW_FACE || this == MODEL

    companion object {
        fun from(value: String?): RoleType {
            return entries.firstOrNull { it.value == value } ?: UNKNOWN
        }
    }
}