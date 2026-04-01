package org.telegram.divo.dal.repository

import org.telegram.divo.dal.api.EventService
import org.telegram.divo.dal.dto.event.EventListRequest
import org.telegram.divo.dal.dto.event.toEntity
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.map
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.entity.EventDetails
import org.telegram.divo.entity.EventList

class EventRepository(
    private val service: EventService
) {

    suspend fun listEvents(request: EventListRequest): DivoResult<EventList> =
        resultOf { service.listEvents(request) }.map { it.toEntity() }

    suspend fun getEvent(id: Int): DivoResult<EventDetails> = resultOf {
        service.getEvent(id)
    }.map {
        requireNotNull(it.toEntity()) { it.message ?: "Event data is null" }
    }
}

