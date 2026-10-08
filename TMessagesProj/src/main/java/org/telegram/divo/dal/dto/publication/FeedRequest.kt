package org.telegram.divo.dal.dto.publication

import com.google.gson.annotations.SerializedName

data class FeedRequestDto(
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("role") val role: String,
    @SerializedName("withoutNfts") val withoutNfts: Boolean = true,
    @SerializedName("subscribedOnly") val subscribedOnly: Boolean = false,
    @SerializedName("modelsOnly") val modelsOnly: Boolean = false,
    // Only profile cards (entity "users"), one per person; otherwise the feed also has publications/posts
    @SerializedName("isProfiles") val isProfiles: Boolean = false
)