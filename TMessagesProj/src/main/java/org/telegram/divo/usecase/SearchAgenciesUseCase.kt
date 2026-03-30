package org.telegram.divo.usecase

import org.telegram.divo.common.OffsetPaginator
import org.telegram.divo.common.PaginatedResult
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.dal.repository.WorkHistoryRepository
import org.telegram.divo.entity.Agency

class SearchAgenciesUseCase(
    private val queryProvider: () -> String,
    private val repository: WorkHistoryRepository = DivoApi.workHistory,
    limit: Int = 10,
) {
    val paginator = OffsetPaginator<Agency>(limit = limit) { offset, lim ->
        when (val result = repository.searchAgencies(
            offset = offset,
            limit = lim,
            query = queryProvider()
        )) {
            is DivoResult.Success -> PaginatedResult(
                items = result.value.first,
                totalCount = result.value.second
            )
            else -> throw Exception(result.getErrorMessage())
        }
    }
}
