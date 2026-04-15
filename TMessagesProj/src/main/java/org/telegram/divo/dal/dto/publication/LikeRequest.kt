package org.telegram.divo.dal.dto.publication

import com.google.gson.annotations.SerializedName

data class LikeRequest(
    @SerializedName("id")
    val id: Int
)
