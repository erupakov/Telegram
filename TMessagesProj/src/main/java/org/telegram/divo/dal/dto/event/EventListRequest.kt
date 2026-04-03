package org.telegram.divo.dal.dto.event

import com.google.gson.annotations.SerializedName

data class EventListRequest(
    @SerializedName("offset") val offset: Int,
    @SerializedName("limit") val limit: Int
)
