package org.telegram.divo.dal.dto.publication

import com.google.gson.annotations.SerializedName

data class UserLikeRequest(
    @SerializedName("userId")
    val userId: Int
)
