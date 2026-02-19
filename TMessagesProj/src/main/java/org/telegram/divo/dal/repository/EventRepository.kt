package org.telegram.divo.dal.repository

import okhttp3.ResponseBody
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.dal.api.EventService

class EventRepository(
    private val service: EventService
) {

    suspend fun listEvents(filters: Map<String, Any?> = emptyMap()): DivoResult<ResponseBody> {
        return resultOf { service.listEvents(filters) }
    }

    suspend fun getEvent(id: Long): DivoResult<ResponseBody> {
        return resultOf { service.getEvent(id) }
    }
}

