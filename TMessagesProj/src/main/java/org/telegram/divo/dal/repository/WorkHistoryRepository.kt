package org.telegram.divo.dal.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

data class AgencySelection(val name: String, val avatarUrl: String?)

class WorkHistoryRepository(
    val service: WorkHistory
) {
    private val _cache = MutableStateFlow<List<WorkExperience>?>(null)
    val cache: StateFlow<List<WorkExperience>?> = _cache.asStateFlow()

    private val _selectedAgency = MutableStateFlow<AgencySelection?>(null)
    val selectedAgency: StateFlow<AgencySelection?> = _selectedAgency.asStateFlow()

    suspend fun getWorkHistory(userId: Int): DivoResult<List<WorkExperience>> {
        _cache.value?.let { return DivoResult.Success(it) }

        return resultOf {
            val items = service.getWorkHistory(userId)
                .data
                ?.items
                ?.map { it.toEntity() }
                ?: emptyList()

            coroutineScope {
                val avatarJobs = items.distinctBy { it.agencyName }.filter { !it.agencyName.isNullOrBlank() }.map {
                    async {
                        try {
                            val agencyResponse = service.getAgencyList(
                                AgencyListRequest(offset = 0, limit = 5, title = it.agencyName.orEmpty())
                            )
                            val avatar = agencyResponse.data.items.firstOrNull { agency -> 
                                agency.title.equals(it.agencyName, ignoreCase = true) 
                            }?.agencyAvatarLink
                            it.agencyName to avatar
                        } catch (e: Exception) {
                            it.agencyName to null
                        }
                    }
                }
                val avatarMap = avatarJobs.associate { it.await() }
                val updatedItems = items.map { item ->
                    if (!item.agencyName.isNullOrBlank() && item.agencyAvatarLink == null) {
                        item.copy(agencyAvatarLink = avatarMap[item.agencyName])
                    } else item
                }
                _cache.value = updatedItems
                updatedItems
            }
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

    fun selectAgency(selection: AgencySelection) {
        _selectedAgency.value = selection
    }

    fun clearSelectedAgency() {
        _selectedAgency.value = null
    }
}