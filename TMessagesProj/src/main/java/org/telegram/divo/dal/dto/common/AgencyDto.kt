package org.telegram.divo.dal.dto.common

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.Agency

class AgencyDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("site") val site: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("employeeTitle") val employeeTitle: String?,
    @SerializedName("address") val address: AgencyAddressDto?,
    @SerializedName("photo") val photo: PhotoDto?,
    @SerializedName("background") val background: PhotoDto?,
    @SerializedName("tiktokUrl") val tiktokUrl: String?,
    @SerializedName("youtubeUrl") val youtubeUrl: String?,
    @SerializedName("telegramUrl") val telegramUrl: String?,
    @SerializedName("instagramUrl") val instagramUrl: String?,
    @SerializedName("websiteUrl") val websiteUrl: String?,
    @SerializedName("socialNetworks") val socialNetworks: List<UserSocialNetworkDto>?,
)

fun AgencyDto.toEntity(): Agency =
    Agency(
        id = id,
        title = title,
        site = site.orEmpty(),
        email = email.orEmpty(),
        description = description.orEmpty(),
        employeeTitle = employeeTitle.orEmpty(),
        address = address?.toEntity(),
        photo = photo?.toEntity(),
        background = background?.toEntity(),
        tiktokUrl = tiktokUrl,
        youtubeUrl = youtubeUrl,
        telegramUrl = telegramUrl,
        instagramUrl = instagramUrl,
        websiteUrl = websiteUrl,
        socialNetworks = socialNetworks?.toEntities().orEmpty()
    )
