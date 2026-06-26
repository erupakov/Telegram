package org.telegram.divo.screen.work_create_edit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.telegram.divo.analytics.AnalyticsEvent
import org.telegram.divo.analytics.DivoAnalytics
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.dal.repository.AgencySelection
import org.telegram.messenger.R
import java.time.LocalDate
import java.time.ZoneOffset

class CreateWorkHistoryViewModel(
    private val editId: Int?
) : BaseViewModel<State, Intent, Effect>() {

    init {
        observeSelectedAgency()
        onLoad(editId)
    }

    override fun createInitialState(): State = State()

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.OnBackClicked -> { sendEffect(Effect.NavigateBack) }
            is Intent.OnStartDateSelected -> {
                setState {
                    copy(
                        startDate = intent.date,
                        startDateMil = intent.mil
                    )
                }
            }
            is Intent.OnEndDateSelected -> {
                setState {
                    copy(
                        endDate = intent.date,
                        endDateMil = intent.mil
                    )
                }
            }
            is Intent.OnCurrentChanged -> {
                setState {
                    copy(
                        isCurrent = intent.isCurrent
                    )
                }
            }
            is Intent.OnCreateExperience ->{
                if (state.value.isEditMode) updateWorkExperience()
                else createWorkExperience()
            }
            Intent.OnSearchSelected -> {
                DivoApi.workHistory.selectAgency(
                    AgencySelection(
                        state.value.agencyName,
                        state.value.avatarUrl
                    )
                )
                sendEffect(Effect.NavigateToSearch)
            }
        }
    }

    private fun onLoad(editId: Int?) {
        viewModelScope.launch {
            if (editId != null) {
                val item = DivoApi.workHistory.cache.value?.find { it.id == editId }
                if (item != null) {
                    DivoAnalytics.logEvent(AnalyticsEvent.WorkHistoryEditOpened(editId))
                    setState {
                        copy(
                            isEditMode = true,
                            agencyName = item.agencyName.orEmpty(),
                            editId = editId,
                            startDate = item.startDate,
                            startDateMil = LocalDate.parse(item.startDate)
                                .atStartOfDay(ZoneOffset.UTC)
                                .toInstant()
                                .toEpochMilli(),
                            endDate = item.endDate,
                            endDateMil = item.endDate?.let { d ->
                                LocalDate.parse(d)
                                    .atStartOfDay(ZoneOffset.UTC)
                                    .toInstant()
                                    .toEpochMilli()
                            } ?: System.currentTimeMillis(),
                            isCurrent = item.isCurrent
                        )
                    }
                } else {
                    DivoAnalytics.logEvent(AnalyticsEvent.WorkHistoryCreateOpened())
                }
            } else {
                DivoAnalytics.logEvent(AnalyticsEvent.WorkHistoryCreateOpened())
            }
        }
    }

    private fun createWorkExperience() {
        viewModelScope.launch {
            val s = state.value
            val startDate = s.startDate ?: return@launch
            val endDate = if (s.isCurrent) null else s.endDate

            if (endDate != null && endDate < startDate) {
                sendEffect(Effect.ShowError(""))
                return@launch
            }

            val result = DivoApi.workHistory.createWorkExperience(
                agencyId = null,
                agencyName = s.agencyName,
                startDate = startDate,
                endDate = endDate,
                isCurrent = s.isCurrent,
            )
            when (result) {
                is DivoResult.Success -> {
                    DivoAnalytics.logEvent(AnalyticsEvent.WorkHistoryCreated())
                    sendEffect(Effect.ShowSuccess)
                }
                else -> sendEffect(Effect.ShowError(result.getErrorMessage()))
            }
        }
    }

    private fun updateWorkExperience() {
        viewModelScope.launch {
            val s = state.value
            val id = s.editId ?: return@launch
            val startDate = s.startDate ?: return@launch
            val endDate = if (s.isCurrent) null else s.endDate

            if (endDate != null && endDate < startDate) {
                sendEffect(Effect.ShowError(""))
                return@launch
            }

            val result = DivoApi.workHistory.updateWorkExperience(
                id = id,
                agencyId = null,
                agencyName = s.agencyName,
                startDate = startDate,
                endDate = endDate,
                isCurrent = s.isCurrent,
            )
            when (result) {
                is DivoResult.Success -> {
                    DivoAnalytics.logEvent(AnalyticsEvent.WorkHistoryEdited())
                    sendEffect(Effect.ShowSuccess)
                }
                else -> sendEffect(Effect.ShowError(result.getErrorMessage()))
            }
        }
    }

    private fun observeSelectedAgency() {
        viewModelScope.launch {
            DivoApi.workHistory.selectedAgency
                .onStart { DivoApi.workHistory.clearSelectedAgency() }
                .collect { agencySelection ->
                    setState { 
                        copy(
                            agencyName = agencySelection?.name ?: "",
                            avatarUrl = agencySelection?.avatarUrl ?: ""
                        ) 
                    }
                }
        }
    }

    companion object {
        fun factory(editId: Int?) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CreateWorkHistoryViewModel(editId) as T
            }
        }
    }
}
