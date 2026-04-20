package org.telegram.divo.dal.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import okhttp3.ResponseBody
import org.telegram.divo.common.utils.ThumbnailProcessor
import org.telegram.divo.dal.api.PublicationService
import org.telegram.divo.dal.dto.publication.CreatePublicationFileRequest
import org.telegram.divo.dal.dto.publication.CreatePublicationRequest
import org.telegram.divo.dal.dto.publication.LikeRequest
import org.telegram.divo.dal.dto.publication.FeedRequestDto
import org.telegram.divo.dal.dto.publication.FeedlineSearchRequest
import org.telegram.divo.dal.dto.publication.ModelParametersDto
import org.telegram.divo.dal.dto.publication.PublicationListRequest
import org.telegram.divo.dal.dto.publication.toEntities
import org.telegram.divo.dal.dto.publication.toEntity
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.entity.Feed
import org.telegram.divo.entity.FeedlineSearchResult
import org.telegram.divo.entity.Publication
import org.telegram.divo.entity.PublicationList

private const val MAX_CACHED_USERS = 5

class PublicationRepository(
    private val service: PublicationService,
    private val thumbnailProcessor: ThumbnailProcessor
) {
    private val _publicationCache = MutableStateFlow<Map<Int, PublicationList>>(emptyMap())

    suspend fun getFeed(
        requestDto: FeedRequestDto
    ): DivoResult<Feed> = resultOf {
        service.getFeed(requestDto).toEntity()
    }

    suspend fun searchFeeds(
        offset: Int = 0,
        limit: Int = 5,
        query: String,
        role: List<String>? = null,
        isSkills: Boolean? = null,
        isEvents: Boolean? = null,
        isProfiles: Boolean? = null,
        isPosts: Boolean? = null,
        withoutNfts: Boolean? = null,
        subscribedOnly: Boolean? = null,
        modelsOnly: Boolean? = null,
        modelParameters: ModelParametersDto? = null,
    ): DivoResult<FeedlineSearchResult> = resultOf {
        service.searchFeedline(
            FeedlineSearchRequest(
                offset = offset,
                limit = limit,
                query = query,
                role = role,
                isSkills = isSkills,
                isEvents = isEvents,
                isProfiles = isProfiles,
                isPosts = isPosts,
                withoutNfts = withoutNfts,
                subscribedOnly = subscribedOnly,
                modelsOnly = modelsOnly,
                modelParameters = modelParameters,
            )
        ).toEntities()
    }

    fun publicationFlow(userId: Int): Flow<PublicationList?> =
        _publicationCache.map { it[userId] }

    fun getPublicationCache(userId: Int): PublicationList? =
        _publicationCache.value[userId]

    suspend fun getPublicationList(
        offset: Int,
        limit: Int,
        userId: Int,
    ): DivoResult<PublicationList> = resultOf {
        if (offset == 0) {
            _publicationCache.value[userId]?.let { return@resultOf it }
        }

        service.getPublicationList(
            PublicationListRequest(offset = offset, limit = limit, userId = userId, type = "video")
        ).toEntities()
            .let { page -> page.copy(items = thumbnailProcessor.withThumbnails(page.items)) }
            .also { newPage -> updatePublicationCache(userId, newPage, offset) }
    }

    suspend fun like(payload: Map<String, Any?>): DivoResult<ResponseBody> {
        return resultOf { service.like(payload) }
    }

    suspend fun unlike(payload: Map<String, Any?>): DivoResult<ResponseBody> {
        return resultOf { service.unlike(payload) }
    }

    suspend fun likePost(id: Int): DivoResult<Unit> = resultOf {
        service.likePost(LikeRequest(id))
    }

    suspend fun unlikePost(id: Int): DivoResult<Unit> = resultOf {
        service.unlikePost(LikeRequest(id))
    }

    suspend fun createPublication(
        title: String,
        description: String,
        type: String,
        fileUuids: List<String>,
        userId: Int,
    ): DivoResult<Publication> = resultOf {
        val newPublication = service.createPublication(
            CreatePublicationRequest(
                title = title,
                description = description,
                type = type,
                files = fileUuids.mapIndexed { index, uuid ->
                    CreatePublicationFileRequest(order = index, fileUuid = uuid)
                }
            )
        ).toEntity()

        val publicationWithThumbnails = thumbnailProcessor
            .withThumbnails(listOf(newPublication))
            .first()

        _publicationCache.update { cache ->
            val existing = cache[userId] ?: return@update cache
            cache + (userId to existing.copy(
                items = listOf(publicationWithThumbnails) + existing.items
            ))
        }

        publicationWithThumbnails
    }

    suspend fun deletePublication(id: Int, userId: Int): DivoResult<Unit> = resultOf {
        service.deletePublication(id)
        _publicationCache.update { cache ->
            val existing = cache[userId] ?: return@update cache
            cache + (userId to existing.copy(
                items = existing.items.filter { it.id != id }
            ))
        }
    }

    private fun updatePublicationCache(userId: Int, newPage: PublicationList, offset: Int) {
        _publicationCache.update { cache ->
            val existing = cache[userId]

            val merged = if (existing == null || offset == 0) {
                newPage
            } else {
                existing.copy(
                    items = (existing.items + newPage.items).distinctBy { it.id },
                    pagination = newPage.pagination
                )
            }

            val updated = cache + (userId to merged)

            if (updated.size > MAX_CACHED_USERS) {
                updated.entries.drop(1).associate { it.key to it.value }
            } else {
                updated
            }
        }
    }
}


