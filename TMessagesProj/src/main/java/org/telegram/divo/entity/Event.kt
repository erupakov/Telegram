package org.telegram.divo.entity

data class EventList(
    val items: List<Event> = listOf(),
    val pagination: Pagination? = null
)

data class Event(
    val id: Int,
    val title: String?,
    val description: String?,
    val type: String?,
    val likesCount: Int,
    val appliesCount: Int,
    val isLikedByUser: Boolean,
    val creator: EventCreator?,
    val files: List<EventFile>
)

data class EventCreator(
    val id: Int,
    val fullName: String?,
    val photo: Photo?,
    val avatar: Photo?,
    val roleLabel: String?
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
