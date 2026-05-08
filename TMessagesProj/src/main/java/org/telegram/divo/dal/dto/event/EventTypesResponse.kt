package org.telegram.divo.dal.dto.event

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.EventType

class EventTypesResponse(
    @SerializedName("data") val data: EventTypesDataDto?,
)

class EventTypesDataDto(
    @SerializedName("items") val items: List<EventTypeDto>
)

fun EventTypesResponse.toEntities() = data?.items?.map { it.toEntity() } ?: emptyList()

fun EventTypeDto.toEntity() = EventType(
    id = id,
    title = title.orEmpty()
)
