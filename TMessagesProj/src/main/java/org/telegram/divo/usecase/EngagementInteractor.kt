package org.telegram.divo.usecase

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.telegram.divo.common.OffsetPaginator
import org.telegram.divo.common.PaginatedResult
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.dal.repository.UserRepository
import org.telegram.divo.entity.Engagement
import org.telegram.divo.entity.EngagementUser

class EngagementInteractor(
    private val userId: Int,
    private val repository: UserRepository = DivoApi.userRepository,
    limit: Int = 10,
    private val onFollowersCount: ((Int) -> Unit)? = null,
    private val onViewsCount: ((Int) -> Unit)? = null,
    private val onFollowingCount: ((Int) -> Unit)? = null,
) {

    private val initialFetchMutex = Mutex()
    private var initialResponse: DivoResult.Success<Engagement>? = null

    private suspend fun fetchEngagementData(offset: Int, limit: Int, search: String = ""): DivoResult<Engagement> {
        if (offset == 0 && search.isBlank()) {
            initialFetchMutex.withLock {
                val cached = initialResponse
                if (cached != null) return cached

                val result = repository.getEngagement(userId = userId, offset = offset, limit = limit)
                if (result is DivoResult.Success) {
                    initialResponse = result
                }
                return result
            }
        }

        return repository.getEngagement(userId = userId, offset = offset, limit = limit, search = search)
    }

    val likedPaginator = OffsetPaginator<EngagementUser>(limit = limit) { offset, lim ->
        when (val result = fetchEngagementData(offset, lim)) {
            is DivoResult.Success -> {
                onFollowersCount?.invoke(result.value.liked.totalCount) // или какая там структура в ответе
                PaginatedResult(
                    items = result.value.liked.items,
                    totalCount = result.value.liked.totalCount
                )
            }
            else -> throw Exception(result.getErrorMessage())
        }
    }

    val viewedPaginator = OffsetPaginator<EngagementUser>(limit = limit) { offset, lim ->
        when (val result = fetchEngagementData(offset, lim)) {
            is DivoResult.Success -> {
                onViewsCount?.invoke(result.value.viewed.totalCount)
                PaginatedResult(
                    items = result.value.viewed.items,
                    totalCount = result.value.viewed.totalCount
                )
            }
            else -> throw Exception(result.getErrorMessage())
        }
    }

    val followedPaginator = OffsetPaginator<EngagementUser>(limit = limit) { offset, lim ->
        when (val result = fetchEngagementData(offset, lim)) {
            is DivoResult.Success -> {
                onFollowingCount?.invoke(result.value.followed.totalCount)
                PaginatedResult(
                    items = result.value.followed.items,
                    totalCount = result.value.followed.totalCount
                )
            }
            else -> throw Exception(result.getErrorMessage())
        }
    }

    var currentSearchQuery: String = ""
    var currentStatsType: String = "liked"

    val searchPaginator = OffsetPaginator<EngagementUser>(limit = limit) { offset, lim ->
        when (val result = repository.getEngagement(
            userId = userId,
            offset = offset,
            limit = lim,
            search = currentSearchQuery
        )) {
            is DivoResult.Success -> {
                val section = when (currentStatsType) {
                    "viewed" -> result.value.viewed
                    "followed" -> result.value.followed
                    else -> result.value.liked
                }
                PaginatedResult(items = section.items, totalCount = section.totalCount)
            }
            else -> throw Exception(result.getErrorMessage())
        }
    }
}
