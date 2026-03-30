package org.telegram.divo.usecase

import org.telegram.divo.common.OffsetPaginator
import org.telegram.divo.common.PaginatedResult
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.dal.repository.UserRepository
import org.telegram.divo.entity.EngagementUser

class EngagementInteractor(
    private val userId: Int,
    private val repository: UserRepository = DivoApi.userRepository,
    limit: Int = 10,
    private val onFollowersCount: ((Int) -> Unit)? = null,
    private val onViewsCount: ((Int) -> Unit)? = null,
    private val onFollowingCount: ((Int) -> Unit)? = null,
) {
    val likedPaginator = OffsetPaginator<EngagementUser>(limit = limit) { offset, lim ->
        when (val result = repository.getEngagement(userId = userId, offset = offset, limit = lim)) {
            is DivoResult.Success -> {
                onFollowersCount?.invoke(result.value.liked.totalCount)
                PaginatedResult(
                    items = result.value.liked.items,
                    totalCount = result.value.liked.totalCount
                )
            }
            else -> throw Exception(result.getErrorMessage())
        }
    }

    val viewedPaginator = OffsetPaginator<EngagementUser>(limit = limit) { offset, lim ->
        when (val result = repository.getEngagement(userId = userId, offset = offset, limit = lim)) {
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
        when (val result = repository.getEngagement(userId = userId, offset = offset, limit = lim)) {
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
}
