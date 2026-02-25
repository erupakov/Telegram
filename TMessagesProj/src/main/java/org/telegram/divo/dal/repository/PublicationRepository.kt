package org.telegram.divo.dal.repository

import okhttp3.ResponseBody
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.dal.api.PublicationService
import org.telegram.divo.dal.dto.publication.FeedRequestDto
import org.telegram.divo.dal.dto.publication.toEntity
import org.telegram.divo.entity.Feed

class PublicationRepository(
    private val service: PublicationService
) {

    suspend fun getFeed(
        offset: Int = 0,
        limit: Int = 3,
    ): DivoResult<Feed> = resultOf {
        service.getFeed(
            FeedRequestDto(offset, limit)
        ).toEntity()
    }

    suspend fun like(payload: Map<String, Any?>): DivoResult<ResponseBody> {
        return resultOf { service.like(payload) }
    }

    suspend fun unlike(payload: Map<String, Any?>): DivoResult<ResponseBody> {
        return resultOf { service.unlike(payload) }
    }
}


