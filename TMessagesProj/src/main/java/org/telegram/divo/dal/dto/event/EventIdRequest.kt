package org.telegram.divo.dal.dto.event

import com.google.gson.annotations.SerializedName

class EventIdRequest(
    @SerializedName("eventId") val eventId: Int
)
