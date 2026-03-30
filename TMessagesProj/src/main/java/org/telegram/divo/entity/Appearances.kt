package org.telegram.divo.entity

data class Appearances(
    val hairLength: List<AppearanceItem>?,
    val hairColor: List<AppearanceItem>?,
    val eyeColor: List<AppearanceItem>?,
    val skinColor: List<AppearanceItem>?
)

data class AppearanceItem(
    val id: Int?,
    val title: String?
)