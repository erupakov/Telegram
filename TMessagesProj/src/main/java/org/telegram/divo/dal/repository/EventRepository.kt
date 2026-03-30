package org.telegram.divo.dal.repository

import okhttp3.ResponseBody
import org.telegram.divo.dal.api.EventService
import org.telegram.divo.dal.dto.event.EventListRequest
import org.telegram.divo.dal.dto.event.toEntity
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.map
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.entity.EventList

class EventRepository(
    private val service: EventService
) {

    suspend fun listEvents(request: EventListRequest): DivoResult<EventList> {
        return resultOf { service.listEvents(request) }.map { it.toEntity() }
    }

    suspend fun getEvent(id: Long): DivoResult<ResponseBody> {
        return resultOf { service.getEvent(id) }
    }
}

