package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName
import org.telegram.divo.dal.dto.common.UuidContainerDto
import org.telegram.divo.entity.Agency
import org.telegram.divo.entity.AgencyAddress

class UpdateProfileAgencyRequest(
    @SerializedName("agencyId") val agencyId: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("site") val site: String?,
    @SerializedName("address") val address: UpdateAgencyAddressDto?,
    @SerializedName("background") val background: UuidContainerDto?,
    @SerializedName("photo") val photo: UuidContainerDto?,
    @SerializedName("tiktokUrl") val tiktokUrl: String?,
    @SerializedName("youtubeUrl") val youtubeUrl: String?,
    @SerializedName("telegramUrl") val telegramUrl: String?,
    @SerializedName("instagramUrl") val instagramUrl: String?,
    @SerializedName("websiteUrl") val websiteUrl: String?
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
        title = title,
        description = description,
        site = site,
        address = address?.toDto(),
        background = background?.let { UuidContainerDto(it.fileUuid) },
        photo = photo?.let { UuidContainerDto(it.fileUuid) },
        tiktokUrl = tiktokUrl,
        youtubeUrl = youtubeUrl,
        telegramUrl = telegramUrl,
        instagramUrl = instagramUrl,
        websiteUrl = websiteUrl
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