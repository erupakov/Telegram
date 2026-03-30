package org.telegram.divo.dal.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.telegram.divo.dal.api.WorkHistory
import org.telegram.divo.dal.dto.work_history.AgencyListRequest
import org.telegram.divo.dal.dto.work_history.CreateWorkExperienceRequest
import org.telegram.divo.dal.dto.work_history.toEntities
import org.telegram.divo.dal.dto.work_history.toEntity
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.entity.Agency
import org.telegram.divo.entity.WorkExperience

class WorkHistoryRepository(
    val service: WorkHistory
) {
    private val _cache = MutableStateFlow<List<WorkExperience>?>(null)
    val cache: StateFlow<List<WorkExperience>?> = _cache.asStateFlow()

    private val _selectedAgency = MutableStateFlow("")
    val selectedAgency: StateFlow<String> = _selectedAgency.asStateFlow()

    suspend fun getWorkHistory(): DivoResult<List<WorkExperience>> {
        _cache.value?.let { return DivoResult.Success(it) }

        return resultOf {
            service.getWorkHistory()
                .data
                ?.items
                ?.map { it.toEntity() }
                ?.also { _cache.value = it }
                ?: emptyList()
        }
    }

    suspend fun deleteWorkExperience(id: Int): DivoResult<Unit> = resultOf {
        service.deleteWorkExperience(id)
        _cache.update { it?.filter { item -> item.id != id } }
    }

    suspend fun createWorkExperience(
        agencyId: Int?,
        agencyName: String?,
        startDate: String,
        endDate: String?,
        isCurrent: Boolean,
    ): DivoResult<Unit> {
        var newItem: WorkExperience? = null

        return resultOf {
            newItem = service.createWorkExperience(
                request = CreateWorkExperienceRequest(
                    agencyId = agencyId,
                    agencyName = agencyName,
                    startDate = startDate,
                    endDate = endDate,
                    isCurrent = isCurrent,
                )
            ).data?.toEntity()
        }.also { result ->
            if (result is DivoResult.Success) {
                newItem?.let { item ->
                    _cache.update { current -> current?.plus(item) ?: listOf(item) }
                }
            }
        }
    }

    suspend fun updateWorkExperience(
        id: Int,
        agencyId: Int?,
        agencyName: String?,
        startDate: String,
        endDate: String?,
        isCurrent: Boolean,
    ): DivoResult<Unit> {
        var updated: WorkExperience? = null

        return resultOf {
            updated = service.updateWorkExperience(
                id = id,
                request = CreateWorkExperienceRequest(
                    agencyId = agencyId,
                    agencyName = agencyName,
                    startDate = startDate,
                    endDate = endDate,
                    isCurrent = isCurrent,
                )
            ).data?.toEntity()
        }.also { result ->
            if (result is DivoResult.Success) {
                updated?.let { item ->
                    _cache.update { current ->
                        current?.map { if (it.id == id) item else it }
                    }
                }
            }
        }
    }

    suspend fun searchAgencies(
        offset: Int,
        limit: Int,
        query: String,
    ): DivoResult<Pair<List<Agency>, Int>> = resultOf {
        val response = service.getAgencyList(
            AgencyListRequest(offset = offset, limit = limit, title = query)
        )
        response.toEntities() to (response.data.pagination?.meta?.totalCount ?: 0)
    }

    fun selectAgency(agency: String) {
        _selectedAgency.value = agency
    }

    fun clearSelectedAgency() {
        _selectedAgency.value = ""
    }
}