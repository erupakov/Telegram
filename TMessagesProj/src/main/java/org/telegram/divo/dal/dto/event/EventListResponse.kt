package org.telegram.divo.dal.dto.event

import com.google.gson.annotations.SerializedName
import org.telegram.divo.dal.dto.common.AgencyAddressDto
import org.telegram.divo.dal.dto.common.PaginationDto
import org.telegram.divo.dal.dto.common.PhotoDto
import org.telegram.divo.dal.dto.common.toEntity
import org.telegram.divo.entity.Event
import org.telegram.divo.entity.EventCreator
import org.telegram.divo.entity.EventFile
import org.telegram.divo.entity.EventList
import org.telegram.divo.entity.PreviousEvent

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
    @SerializedName("type") val type: EventTypeDto?,
    @SerializedName("paymentType") val paymentType: EventTypeDto?,
    @SerializedName("maxAttendees") val maxAttendees: Int?,
    @SerializedName("likesCount") val likesCount: Int,
    @SerializedName("isApplied") val isApplied: Boolean,
    @SerializedName("appliesCount") val appliesCount: Int,
    @SerializedName("date") val date: String?,
    @SerializedName("dateTo") val dateTo: String?,
    @SerializedName("applicationDeadline") val applicationDeadline: String?,
    @SerializedName("isLikedByUser") val isLikedByUser: Boolean,
    @SerializedName("creator") val creator: EventCreatorDto?,
    @SerializedName("files") val files: List<EventFileDto>?,
    @SerializedName("address") val address: AgencyAddressDto?,
    @SerializedName("modelAttributes") val modelAttributes: EventModelAttributesDto?
)

class EventCreatorDto(
    @SerializedName("id") val id: Int,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("photo") val photo: PhotoDto?,
    @SerializedName("avatar") val avatar: PhotoDto?,
    @SerializedName("roleLabel") val roleLabel: String?,
    @SerializedName("isVerified") val isVerified: Boolean?
)

class EventFileDto(
    @SerializedName("order") val order: Int,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("fullUrl") val fullUrl: String,
    @SerializedName("fileUuid") val fileUuid: String,
    @SerializedName("extension") val extension: String
)

class PreviousEventDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("photo") val photo: PhotoDto?,
    @SerializedName("type") val type: EventTypeDto?,
    @SerializedName("address") val address: EventAddressDto?
)

fun EventListResponse.toEntity() = EventList(
    items = data?.items?.map { it.toEntity() } ?: emptyList(),
    pagination = data?.pagination?.meta?.toEntity()
)

fun EventDto.toEntity() = Event(
    id = id,
    title = title,
    description = description,
    type = type?.title,
    typeId = type?.id,
    paymentType = paymentType?.title,
    paymentTypeId = paymentType?.id,
    modelAttributes = modelAttributes?.toEntity(),
    maxAttendees = maxAttendees,
    likesCount = likesCount,
    appliesCount = appliesCount,
    isApplied = isApplied,
    date = date.orEmpty(),
    dateTo = dateTo.orEmpty(),
    applicationDeadline = applicationDeadline,
    city = address?.city?.name.orEmpty(),
    countryCode = address?.city?.countryCode.orEmpty(),
    isLikedByUser = isLikedByUser,
    creator = creator?.toEntity(),
    files = files?.map { it.toEntity() } ?: emptyList(),
)

fun EventCreatorDto.toEntity() = EventCreator(
    id = id,
    fullName = fullName,
    photo = photo?.toEntity(),
    avatar = avatar?.toEntity(),
    roleLabel = roleLabel,
    isVerified = isVerified ?: false
)

fun EventFileDto.toEntity() = EventFile(
    order = order,
    fileName = fileName,
    fullUrl = fullUrl,
    fileUuid = fileUuid,
    extension = extension
)

fun PreviousEventDto.toEntity() = PreviousEvent(
    id = id,
    title = title,
    date = date.orEmpty(),
    photo = photo?.toEntity(),
    type = type?.title,
    address = address?.toEntity()
)
