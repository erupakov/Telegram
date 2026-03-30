package org.telegram.divo.dal.dto.common

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.UserSocialNetwork

class UserSocialNetworkDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("link") val link: String,
    @SerializedName("socialNetwork") val socialNetwork: SocialNetworkDto
)

class SocialNetworkDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("provider") val provider: String
)

fun List<UserSocialNetworkDto>.toEntities(): List<UserSocialNetwork> = map { dto ->
    UserSocialNetwork(
        id = dto.socialNetwork.id,
        name = dto.nickname,
        link = dto.link,
        provider = dto.socialNetwork.provider
    )
}