package org.telegram.divo.entity

data class EventList(
    val items: List<Event> = listOf(),
    val pagination: Pagination? = null
)

data class Event(
    val id: Int,
    val title: String?,
    val description: String?,
    val date: String,
    val dateTo: String,
    val applicationDeadline: String?,
    val isPublic: Boolean = true,
    val ndaRequired: Boolean = false,
    val city: String,
    val countryCode: String,
    val type: String?,
    val typeId: Int? = null,
    val paymentType: String?,
    val paymentTypeId: Int? = null,
    val modelAttributes: EventModelAttributes? = null,
    val maxAttendees: Int?,
    val likesCount: Int,
    val appliesCount: Int,
    val isApplied: Boolean,
    val isLikedByUser: Boolean,
    val isFavourite: Boolean = false,
    val favoritesCount: Int = 0,
    val creator: EventCreator?,
    val files: List<EventFile>,
)

data class EventCreator(
    val id: Int,
    val fullName: String?,
    val photo: Photo?,
    val avatar: Photo?,
    val roleLabel: String?,
    val isVerified: Boolean = false
)

data class EventFile(
    val order: Int,
    val fileName: String,
    val fullUrl: String,
    val fileUuid: String,
    val extension: String
) {
    val isVideo: Boolean get() = extension == "mp4"
    val isImage: Boolean get() = extension in listOf("jpg", "jpeg", "png", "webp")
}
