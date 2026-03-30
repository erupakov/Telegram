package org.telegram.divo.dal.dto.publication

import com.google.gson.annotations.SerializedName

class FeedlineSearchRequest(
    @SerializedName("offset") val offset: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("query") val query: String
)