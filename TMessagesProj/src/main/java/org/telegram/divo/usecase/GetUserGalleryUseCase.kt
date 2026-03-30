package org.telegram.divo.usecase

import org.telegram.divo.common.OffsetPaginator
import org.telegram.divo.common.PaginatedResult
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.dal.repository.UserRepository
import org.telegram.divo.entity.UserGalleryItem

class GetUserGalleryUseCase(
    private val userId: Int,
    private val repository: UserRepository = DivoApi.userRepository,
    limit: Int = 12,
) {
    val paginator = OffsetPaginator<UserGalleryItem>(limit = limit) { offset, lim ->
        when (val result = repository.getUserGalleryList(
            userId = userId,
            offset = offset,
            limit = lim,
        )) {
            is DivoResult.Success -> {
                val data = result.value
                PaginatedResult(
                    items = data.items,
                    totalCount = data.pagination?.totalCount ?: data.items.size
                )
            }
            else -> throw Exception(result.getErrorMessage())
        }
    }
}
