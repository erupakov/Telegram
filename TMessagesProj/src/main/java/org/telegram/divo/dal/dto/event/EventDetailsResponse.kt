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
    @SerializedName("appliesCount") val appliesCount: Int?,
    @SerializedName("viewsCount") val viewsCount: Int?,
    @SerializedName("userReachCount") val userReachCount: Int?,
    @SerializedName("date") val date: String?,
    @SerializedName("dateTo") val dateTo: String?,
    @SerializedName("paymentType") val paymentType: EventTypeDto?,
    @SerializedName("paymentFrequency") val paymentFrequency: EventTypeDto?,
    @SerializedName("cost") val cost: String?,
    @SerializedName("address") val address: EventAddressDto?,
    @SerializedName("files") val files: List<EventFileDto>?,
    @SerializedName("modelAttributes") val modelAttributes: EventModelAttributesDto?,
    @SerializedName("creator") val creator: EventCreatorDto?
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
    @SerializedName("from") val from: Int?,
    @SerializedName("to") val to: Int?
)

fun EventDetailsResponse.toEntity(): EventDetails? {
    return data?.toEntity()
}

fun EventDetailsDto.toEntity() = EventDetails(
    id = id,
    title = title,
    description = description,
    type = type?.title,
    isApplied = isApplied ?: false,
    appliesCount = appliesCount ?: 0,
    viewsCount = viewsCount ?: 0,
    userReachCount = userReachCount ?: 0,
    date = date,
    dateTo = dateTo,
    paymentType = paymentType?.title,
    paymentFrequency = paymentFrequency?.title,
    cost = cost,
    address = address?.toEntity(),
    files = files?.map { it.toEntity() } ?: emptyList(),
    modelAttributes = modelAttributes?.toEntity(),
    creator = creator?.toEntity()
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
    ageFrom = age?.from,
    ageTo = age?.to,
    genders = gender?.mapNotNull { it.title } ?: emptyList(),
    heightFrom = height?.from,
    heightTo = height?.to,
    weightFrom = weight?.from,
    weightTo = weight?.to,
    breastSizeFrom = breastSize?.from,
    breastSizeTo = breastSize?.to,
    waistFrom = waist?.from,
    waistTo = waist?.to,
    hipsFrom = hips?.from,
    hipsTo = hips?.to,
    shoesSizeFrom = shoesSize?.from,
    shoesSizeTo = shoesSize?.to,
    hairColors = hairColor?.mapNotNull { it.title } ?: emptyList(),
    hairLengths = hairLength?.mapNotNull { it.title } ?: emptyList(),
    eyeColors = eyeColor?.mapNotNull { it.title } ?: emptyList(),
    skinColors = skinColor?.mapNotNull { it.title } ?: emptyList(),
    measuringSystem = measuringSystem
)
