package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName
import org.telegram.divo.dal.dto.common.UuidContainerDto
import org.telegram.divo.entity.Agency
import org.telegram.divo.entity.AgencyAddress

class UpdateProfileAgencyRequest(
    @SerializedName("agencyId") val agencyId: Int,
    @SerializedName("employeeTitle") val employeeTitle: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("address") val address: UpdateAgencyAddressDto?,
    @SerializedName("background") val background: UuidContainerDto?,
    @SerializedName("photo") val photo: UuidContainerDto?
)

class UpdateAgencyAddressDto(
    @SerializedName("street") val street: String?,
    @SerializedName("house") val house: String?,
    @SerializedName("apartment") val apartment: String?,
    @SerializedName("formatted") val formatted: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("cityId") val cityId: Int?
)

fun Agency.toDto(): UpdateProfileAgencyRequest =
    UpdateProfileAgencyRequest(
        agencyId = id,
        employeeTitle = employeeTitle,
        description = description,
        address = address?.toDto(),
        background = background?.let { UuidContainerDto(it.fileUuid) },
        photo = photo?.let { UuidContainerDto(it.fileUuid) }
    )

fun AgencyAddress.toDto(): UpdateAgencyAddressDto =
    UpdateAgencyAddressDto(
        street = street,
        house = house,
        apartment = apartment,
        formatted = formatted,
        latitude = latitude,
        longitude = longitude,
        cityId = city?.id
    )