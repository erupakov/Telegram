package org.telegram.divo.dal.dto.publication

import com.google.gson.annotations.SerializedName

data class FeedRequestDto(
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("limit")
    val limit: Int
)