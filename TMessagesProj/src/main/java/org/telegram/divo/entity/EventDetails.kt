package org.telegram.divo.entity

data class EventDetails(
    val id: Int,
    val title: String?,
    val description: String?,
    val type: String?,
    val typeId: Int?,
    val isApplied: Boolean,
    val appliedDate: String?,
    val isLiked: Boolean,
    val likesCount: Int,
    val isFavourite: Boolean,
    val favoritesCount: Int,
    val appliesCount: Int,
    val viewsCount: Int,
    val userReachCount: Int,
    val date: String?,
    val dateTo: String?,
    val paymentType: String?,
    val paymentTypeId: Int?,
    val paymentFrequency: String?,
    val cost: String?,
    val isPublic: Boolean,
    val ndaRequired: Boolean,
    val applicationDeadline: String?,
    val maxAttendees: Int?,
    val requirements: String?,
    val address: EventAddress?,
    val files: List<EventFile>,
    val modelAttributes: EventModelAttributes?,
    /** The real author: "my event" checks and the creatorId filter use it. */
    val creator: EventCreator?,
    /** Who the event is shown as organized by (the agency for an agency employee's event); null on old backends. */
    val organizer: EventCreator? = null,
    val previousEventsFromSameOrigin: List<PreviousEvent>
) {
    /** Organizer card (name, avatar, verified badge): the organizer, else the creator. */
    val displayOrganizer: EventCreator? get() = organizer ?: creator
}

data class PreviousEvent(
    val id: Int,
    val title: String?,
    val date: String,
    val photo: Photo?,
    val type: String?,
    val address: EventAddress?
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
    val measuringSystem: String?,
    /** How the casting takes place: open_call, go_see_invite_only, online_self_tape, client_casting, agency_casting */
    val selectionFormat: String? = null,
    /** Target gender of the casting: female, male, non_binary, transgender_female, transgender_male, custom, other */
    val targetGender: String? = null
)
