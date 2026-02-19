package org.telegram.divo.dal.repository

import okhttp3.ResponseBody
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.dal.api.PublicationService

class PublicationRepository(
    private val service: PublicationService
) {

    suspend fun getFeed(filters: Map<String, Any?> = emptyMap()): DivoResult<ResponseBody> {
        return resultOf { service.getFeed(filters) }
    }

    suspend fun like(payload: Map<String, Any?>): DivoResult<ResponseBody> {
        return resultOf { service.like(payload) }
    }

    suspend fun unlike(payload: Map<String, Any?>): DivoResult<ResponseBody> {
        return resultOf { service.unlike(payload) }
    }
}


