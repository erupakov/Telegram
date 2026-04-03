package org.telegram.divo.dal.dto.event

import com.google.gson.annotations.SerializedName
import org.telegram.divo.dal.dto.common.PaginationDto
import org.telegram.divo.dal.dto.common.PhotoDto
import org.telegram.divo.dal.dto.common.toEntity
import org.telegram.divo.entity.Event
import org.telegram.divo.entity.EventCreator
import org.telegram.divo.entity.EventFile
import org.telegram.divo.entity.EventList

class EventListResponse(
    @SerializedName("data") val data: EventListDataDto?,
    @SerializedName("message") val message: String?,
    @SerializedName("errors") val errors: Map<String, Any>?
)

class EventListDataDto(
    @SerializedName("items") val items: List<EventDto>,
    @SerializedName("pagination") val pagination: PaginationDto?
)

class EventDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("likesCount") val likesCount: Int,
    @SerializedName("appliesCount") val appliesCount: Int,
    @SerializedName("isLikedByUser") val isLikedByUser: Boolean,
    @SerializedName("creator") val creator: EventCreatorDto?,
    @SerializedName("files") val files: List<EventFileDto>?
)

class EventCreatorDto(
    @SerializedName("id") val id: Int,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("photo") val photo: PhotoDto?,
    @SerializedName("avatar") val avatar: PhotoDto?,
    @SerializedName("roleLabel") val roleLabel: String?
)

class EventFileDto(
    @SerializedName("order") val order: Int,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("fullUrl") val fullUrl: String,
    @SerializedName("fileUuid") val fileUuid: String,
    @SerializedName("extension") val extension: String
)

fun EventListResponse.toEntity() = EventList(
    items = data?.items?.map { it.toEntity() } ?: emptyList(),
    pagination = data?.pagination?.meta?.toEntity()
)

fun EventDto.toEntity() = Event(
    id = id,
    title = title,
    description = description,
    type = type,
    likesCount = likesCount,
    appliesCount = appliesCount,
    isLikedByUser = isLikedByUser,
    creator = creator?.toEntity(),
    files = files?.map { it.toEntity() } ?: emptyList()
)

fun EventCreatorDto.toEntity() = EventCreator(
    id = id,
    fullName = fullName,
    photo = photo?.toEntity(),
    avatar = avatar?.toEntity(),
    roleLabel = roleLabel
)

fun EventFileDto.toEntity() = EventFile(
    order = order,
    fileName = fileName,
    fullUrl = fullUrl,
    fileUuid = fileUuid,
    extension = extension
)
