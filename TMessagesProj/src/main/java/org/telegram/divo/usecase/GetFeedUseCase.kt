package org.telegram.divo.usecase

import org.telegram.divo.common.OffsetPaginator
import org.telegram.divo.common.PaginatedResult
import org.telegram.divo.dal.dto.publication.FeedRequestDto
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.dal.repository.PublicationRepository
import org.telegram.divo.entity.FeedItem

class GetFeedUseCase(
    private val repository: PublicationRepository = DivoApi.publicationRepository,
    limit: Int = 5,
    subscribedOnly: Boolean = false,
    modelsOnly: Boolean = false,
) {
    val paginator = OffsetPaginator<FeedItem>(limit = limit) { offset, lim ->
        val request = FeedRequestDto(
            offset = offset,
            limit = lim,
            subscribedOnly = subscribedOnly,
            modelsOnly = modelsOnly,
        )
        when (val result = repository.getFeed(request)) {
            is DivoResult.Success -> PaginatedResult(
                items = result.value.items,
                totalCount = result.value.pagination?.totalCount ?: result.value.items.size
            )
            else -> throw Exception(result.getErrorMessage())
        }
    }
}
