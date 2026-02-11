package org.telegram.divo.dal.dao

import okhttp3.ResponseBody
import org.telegram.divo.dal.DivoResult
import org.telegram.divo.dal.safeCall
import org.telegram.divo.dal.api.PublicationService

class PublicationDao(
    private val service: PublicationService
) {

    suspend fun getFeed(filters: Map<String, Any?> = emptyMap()): DivoResult<ResponseBody> {
        return safeCall { service.getFeed(filters) }
    }

    suspend fun like(payload: Map<String, Any?>): DivoResult<ResponseBody> {
        return safeCall { service.like(payload) }
    }

    suspend fun unlike(payload: Map<String, Any?>): DivoResult<ResponseBody> {
        return safeCall { service.unlike(payload) }
    }
}


