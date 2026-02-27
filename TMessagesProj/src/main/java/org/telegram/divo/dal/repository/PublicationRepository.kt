package org.telegram.divo.dal.repository

import okhttp3.ResponseBody
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.dal.api.PublicationService
import org.telegram.divo.dal.dto.publication.FeedRequestDto
import org.telegram.divo.dal.dto.publication.FeedlineSearchRequest
import org.telegram.divo.dal.dto.publication.toEntities
import org.telegram.divo.dal.dto.publication.toEntity
import org.telegram.divo.entity.Feed
import org.telegram.divo.entity.FeedlineSearchResult

class PublicationRepository(
    private val service: PublicationService
) {

    suspend fun getFeed(
        offset: Int = 0,
        limit: Int = 5,
    ): DivoResult<Feed> = resultOf {
        service.getFeed(
            FeedRequestDto(offset, limit)
        ).toEntity()
    }

    suspend fun searchFeeds(
        offset: Int = 0,
        limit: Int = 5,
        query: String,
    ): DivoResult<FeedlineSearchResult> = resultOf {
        service.searchFeedline(
            FeedlineSearchRequest(
                offset = offset,
                limit = limit,
                query = query,
            )
        ).toEntities()
    }

    suspend fun like(payload: Map<String, Any?>): DivoResult<ResponseBody> {
        return resultOf { service.like(payload) }
    }

    suspend fun unlike(payload: Map<String, Any?>): DivoResult<ResponseBody> {
        return resultOf { service.unlike(payload) }
    }
}


