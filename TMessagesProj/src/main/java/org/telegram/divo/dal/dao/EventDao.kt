package org.telegram.divo.dal.dao

import okhttp3.ResponseBody
import org.telegram.divo.dal.DivoResult
import org.telegram.divo.dal.safeCall
import org.telegram.divo.dal.api.EventService

class EventDao(
    private val service: EventService
) {

    suspend fun listEvents(filters: Map<String, Any?> = emptyMap()): DivoResult<ResponseBody> {
        return safeCall { service.listEvents(filters) }
    }

    suspend fun getEvent(id: Long): DivoResult<ResponseBody> {
        return safeCall { service.getEvent(id) }
    }
}

