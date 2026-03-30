package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName

class UpsertSocialNetworkRequest(
    @SerializedName("socialNetworkId") val socialNetworkId: Int,
    @SerializedName("nickname") val nickname: String
)