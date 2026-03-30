package org.telegram.divo.usecase

import org.telegram.divo.common.OffsetPaginator
import org.telegram.divo.common.PaginatedResult
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.dal.repository.PublicationRepository
import org.telegram.divo.entity.Publication

class GetUserVideosUseCase(
    private val userId: Int,
    private val repository: PublicationRepository = DivoApi.publicationRepository,
    limit: Int = 20,
) {
    val paginator = OffsetPaginator<Publication>(limit = limit) { offset, lim ->
        when (val result = repository.getPublicationList(
            offset = offset,
            limit = lim,
            userId = userId,
        )) {
            is DivoResult.Success -> PaginatedResult(
                items = result.value.items.filter { it.files.any { f -> f.isVideo } },
                totalCount = result.value.pagination?.totalCount ?: result.value.items.size
            )
            else -> throw Exception(result.getErrorMessage())
        }
    }
}
