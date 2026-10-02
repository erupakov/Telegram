package org.telegram.divo.entity

enum class RoleType(val value: String) {
    MODEL("model"),
    NEW_FACE("new_face"),
    NEW_TALENT("new_talent"),
    AGENCY("agency_employee"),
    PHOTOGRAPHER("photographer"),
    MEDIA("media"),
    BRAND("brand"),
    STYLIST("stylist"),
    PLACE("place"),
    CUSTOMER("customer"),
    FAN("fan"),
    UNKNOWN("");

    fun isModel(): Boolean = this != AGENCY

    companion object {
        fun from(value: String?): RoleType {
            return entries.firstOrNull { it.value == value } ?: UNKNOWN
        }
    }
}