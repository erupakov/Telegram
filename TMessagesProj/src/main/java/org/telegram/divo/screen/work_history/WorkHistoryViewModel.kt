package org.telegram.divo.screen.work_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage

class WorkHistoryViewModel(
    private val userId: Int
) : BaseViewModel<State, Intent, Effect>() {

    init {
        viewModelScope.launch {
            DivoApi.workHistory.cache.collect { cached ->
                cached?.let { setState { copy(experiences = it) } }
            }
        }

        loadWorkExperience()
    }

    override fun createInitialState(): State = State()

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.OnBackClicked -> sendEffect(Effect.NavigateBack)
            is Intent.OnDeleteClicked -> deleteWorkExperience(intent.id)
            is Intent.OnCreateClicked -> sendEffect(Effect.NavigateToCreate(null))
            Intent.Load -> {}
            is Intent.OnEditClicked -> { sendEffect(Effect.NavigateToCreate(intent.id)) }
        }
    }

    private fun loadWorkExperience() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val result = DivoApi.workHistory.getWorkHistory(userId)
            setState { copy(isLoading = false) }
            if (result !is DivoResult.Success) {
                sendEffect(Effect.ShowError(result.getErrorMessage()))
            }
        }
    }

    private fun deleteWorkExperience(id: Int) {
        viewModelScope.launch {
            setState { copy(deletingId = id) }
            val result = DivoApi.workHistory.deleteWorkExperience(id)
            setState { copy(deletingId = null) }
            if (result !is DivoResult.Success) {
                sendEffect(Effect.ShowError(result.getErrorMessage()))
            }
        }
    }

    companion object {
        fun factory(userId: Int) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return WorkHistoryViewModel(userId) as T
            }
        }
    }
}
