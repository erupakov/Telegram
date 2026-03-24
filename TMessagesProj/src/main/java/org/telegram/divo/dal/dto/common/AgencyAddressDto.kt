package org.telegram.divo.dal.dto.common

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.AgencyAddress

class AgencyAddressDto(
    @SerializedName("street") val street: String?,
    @SerializedName("house") val house: String?,
    @SerializedName("apartment") val apartment: String?,
    @SerializedName("formatted") val formatted: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("city") val city: CityDto?
)

fun AgencyAddressDto.toEntity(): AgencyAddress =
    AgencyAddress(
        street = street.orEmpty(),
        house = house.orEmpty(),
        apartment = apartment.orEmpty(),
        formatted = formatted.orEmpty(),
        latitude = latitude ?: 0.0,
        longitude = longitude ?: 0.0,
        city = city?.toEntity()
    )

fun AgencyAddress.toDto(): AgencyAddressDto =
    AgencyAddressDto(
        street = street,
        house = house,
        apartment = apartment,
        formatted = formatted,
        latitude = latitude,
        longitude = longitude,
        city = city?.toDto()
    )