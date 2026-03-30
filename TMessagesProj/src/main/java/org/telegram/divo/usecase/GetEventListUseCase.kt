package org.telegram.divo.usecase

import org.telegram.divo.common.OffsetPaginator
import org.telegram.divo.common.PaginatedResult
import org.telegram.divo.dal.dto.event.EventListRequest
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.dal.repository.EventRepository

class GetEventListUseCase(
    private val repository: EventRepository = DivoApi.eventRepository,
    limit: Int = 20
) {
    val paginator = OffsetPaginator(limit = limit) { offset, lim ->
        val request = EventListRequest(offset = offset, limit = lim)
        when (val result = repository.listEvents(request)) {
            is DivoResult.Success -> PaginatedResult(
                items = result.value.items,
                totalCount = result.value.pagination?.totalCount ?: result.value.items.size
            )

            else -> throw Exception(result.getErrorMessage())
        }
    }
}