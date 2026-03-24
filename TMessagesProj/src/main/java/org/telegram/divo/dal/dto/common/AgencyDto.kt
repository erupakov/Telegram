package org.telegram.divo.dal.dto.common

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.Agency

class AgencyDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("site") val site: String?,
    @SerializedName("email") val email: String?,
    //@SerializedName("socialNetworks") val socialNetworks: List<UserSocialNetworkDto>?,
    @SerializedName("description") val description: String?,
    @SerializedName("employeeTitle") val employeeTitle: String?,
    @SerializedName("address") val address: AgencyAddressDto?,
    @SerializedName("photo") val photo: PhotoDto?,
    @SerializedName("background") val background: PhotoDto?,
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
    )
