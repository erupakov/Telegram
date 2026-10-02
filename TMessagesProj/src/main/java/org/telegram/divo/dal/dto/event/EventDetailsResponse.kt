package org.telegram.divo.dal.dto.event

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.EventAddress
import org.telegram.divo.entity.EventDetails
import org.telegram.divo.entity.EventModelAttributes

class EventDetailsResponse(
    @SerializedName("data") val data: EventDetailsDto?,
    @SerializedName("message") val message: String?,
    @SerializedName("errors") val errors: Map<String, Any>?
)

class EventDetailsDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("type") val type: EventTypeDto?,
    @SerializedName("isApplied") val isApplied: Boolean?,
    @SerializedName("applicationDate") val applicationDate: String?,
    @SerializedName("isLiked") val isLiked: Boolean?,
    @SerializedName("likesCount") val likesCount: Int?,
    @SerializedName("favoritesCount") val favoritesCount: Int?,
    @SerializedName("isFavourite") val isFavourite: Boolean?,
    @SerializedName("appliesCount") val appliesCount: Int?,
    @SerializedName("viewsCount") val viewsCount: Int?,
    @SerializedName("userReachCount") val userReachCount: Int?,
    @SerializedName("date") val date: String?,
    @SerializedName("dateTo") val dateTo: String?,
    @SerializedName("paymentType") val paymentType: EventTypeDto?,
    @SerializedName("paymentFrequency") val paymentFrequency: EventTypeDto?,
    @SerializedName("cost") val cost: String?,
    @SerializedName("isPublic") val isPublic: Boolean?,
    @SerializedName("ndaRequired") val ndaRequired: Boolean?,
    @SerializedName("applicationDeadline") val applicationDeadline: String?,
    @SerializedName("maxAttendees") val maxAttendees: Int?,
    @SerializedName("requirements") val requirements: String?,
    @SerializedName("address") val address: EventAddressDto?,
    @SerializedName("files") val files: List<EventFileDto>?,
    @SerializedName("modelAttributes") val modelAttributes: EventModelAttributesDto?,
    @SerializedName("creator") val creator: EventCreatorDto?,
    @SerializedName("previousEventsFromSameOrigin") val previousEventsFromSameOrigin: List<PreviousEventDto>?
)

class EventTypeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?
)

class EventGenderDto(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?
)

class EventAddressDto(
    @SerializedName("street") val street: String?,
    @SerializedName("house") val house: String?,
    @SerializedName("apartment") val apartment: String?,
    @SerializedName("formatted") val formatted: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("city") val city: EventCityDto?
)

class EventCityDto(
    @SerializedName("id") val id: Int,
    @SerializedName("countryCode") val countryCode: String?,
    @SerializedName("countryName") val countryName: String?,
    @SerializedName("areaName") val areaName: String?,
    @SerializedName("name") val name: String?
)

class EventModelAttributesDto(
    @SerializedName("role") val role: List<String>?,
    @SerializedName("age") val age: EventRangeDto?,
    @SerializedName("gender") val gender: List<EventGenderDto>?,
    @SerializedName("height") val height: EventRangeDto?,
    @SerializedName("weight") val weight: EventRangeDto?,
    @SerializedName("breastSize") val breastSize: EventRangeDto?,
    @SerializedName("waist") val waist: EventRangeDto?,
    @SerializedName("hips") val hips: EventRangeDto?,
    @SerializedName("shoesSize") val shoesSize: EventRangeDto?,
    @SerializedName("hairColor") val hairColor: List<EventTypeDto>?,
    @SerializedName("hairLength") val hairLength: List<EventTypeDto>?,
    @SerializedName("eyeColor") val eyeColor: List<EventTypeDto>?,
    @SerializedName("skinColor") val skinColor: List<EventTypeDto>?,
    @SerializedName("measuringSystem") val measuringSystem: String?
)

class EventRangeDto(
    @SerializedName("from") val from: Double?,
    @SerializedName("to") val to: Double?
)

fun EventDetailsResponse.toEntity(): EventDetails? {
    return data?.toEntity()
}

fun EventDetailsDto.toEntity() = EventDetails(
    id = id,
    title = title,
    description = description,
    type = type?.title,
    typeId = type?.id,
    isApplied = isApplied ?: false,
    appliedDate = applicationDate,
    isLiked = isLiked ?: false,
    likesCount = likesCount ?: 0,
    isFavourite = isFavourite ?: false,
    favoritesCount = favoritesCount ?: 0,
    appliesCount = appliesCount ?: 0,
    viewsCount = viewsCount ?: 0,
    userReachCount = userReachCount ?: 0,
    date = date,
    dateTo = dateTo,
    paymentType = paymentType?.title,
    paymentTypeId = paymentType?.id,
    paymentFrequency = paymentFrequency?.title,
    cost = cost,
    isPublic = isPublic ?: true,
    ndaRequired = ndaRequired ?: false,
    applicationDeadline = applicationDeadline,
    maxAttendees = maxAttendees,
    requirements = requirements,
    address = address?.toEntity(),
    files = files?.map { it.toEntity() } ?: emptyList(),
    modelAttributes = modelAttributes?.toEntity(),
    creator = creator?.toEntity(),
    previousEventsFromSameOrigin = previousEventsFromSameOrigin?.map { it.toEntity() } ?: emptyList()
)

fun EventAddressDto.toEntity() = EventAddress(
    street = street,
    house = house,
    apartment = apartment,
    formatted = formatted,
    latitude = latitude,
    longitude = longitude,
    cityName = city?.name.orEmpty(),
    countryName = city?.countryName.orEmpty(),
    countryCode = city?.countryCode.orEmpty()
)

fun EventModelAttributesDto.toEntity() = EventModelAttributes(
    roles = role ?: emptyList(),
    ageFrom = age?.from?.toInt(),
    ageTo = age?.to?.toInt(),
    genders = gender?.mapNotNull { it.title } ?: emptyList(),
    heightFrom = height?.from?.toInt(),
    heightTo = height?.to?.toInt(),
    weightFrom = weight?.from?.toInt(),
    weightTo = weight?.to?.toInt(),
    breastSizeFrom = breastSize?.from?.toInt(),
    breastSizeTo = breastSize?.to?.toInt(),
    waistFrom = waist?.from?.toInt(),
    waistTo = waist?.to?.toInt(),
    hipsFrom = hips?.from?.toInt(),
    hipsTo = hips?.to?.toInt(),
    shoesSizeFrom = shoesSize?.from?.toInt(),
    shoesSizeTo = shoesSize?.to?.toInt(),
    hairColors = hairColor?.mapNotNull { it.title } ?: emptyList(),
    hairLengths = hairLength?.mapNotNull { it.title } ?: emptyList(),
    eyeColors = eyeColor?.mapNotNull { it.title } ?: emptyList(),
    skinColors = skinColor?.mapNotNull { it.title } ?: emptyList(),
    measuringSystem = measuringSystem
)
