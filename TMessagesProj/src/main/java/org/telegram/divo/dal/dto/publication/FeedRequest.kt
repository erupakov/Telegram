package org.telegram.divo.dal.dto.publication

import com.google.gson.annotations.SerializedName

data class FeedRequestDto(
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("withoutNfts") val withoutNfts: Boolean = false,
    @SerializedName("subscribedOnly") val subscribedOnly: Boolean = false,
    @SerializedName("modelsOnly") val modelsOnly: Boolean = false
)