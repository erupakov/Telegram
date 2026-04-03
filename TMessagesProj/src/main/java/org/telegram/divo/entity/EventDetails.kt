package org.telegram.divo.entity

data class EventDetails(
    val id: Int,
    val title: String?,
    val description: String?,
    val type: String?,
    val isApplied: Boolean,
    val appliesCount: Int,
    val viewsCount: Int,
    val userReachCount: Int,
    val date: String?,
    val dateTo: String?,
    val paymentType: String?,
    val paymentFrequency: String?,
    val cost: String?,
    val address: EventAddress?,
    val files: List<EventFile>,
    val modelAttributes: EventModelAttributes?,
    val creator: EventCreator?
)

data class EventAddress(
    val street: String?,
    val house: String?,
    val apartment: String?,
    val formatted: String?,
    val latitude: Double?,
    val longitude: Double?,
    val cityName: String,
    val countryName: String,
    val countryCode: String,
)

data class EventModelAttributes(
    val roles: List<String>,
    val ageFrom: Int?,
    val ageTo: Int?,
    val genders: List<String>,
    val heightFrom: Int?,
    val heightTo: Int?,
    val weightFrom: Int?,
    val weightTo: Int?,
    val breastSizeFrom: Int?,
    val breastSizeTo: Int?,
    val waistFrom: Int?,
    val waistTo: Int?,
    val hipsFrom: Int?,
    val hipsTo: Int?,
    val shoesSizeFrom: Int?,
    val shoesSizeTo: Int?,
    val hairColors: List<String>,
    val hairLengths: List<String>,
    val eyeColors: List<String>,
    val skinColors: List<String>,
    val measuringSystem: String?
)
