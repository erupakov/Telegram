package org.telegram.divo.screen.apply_confirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.telegram.divo.analytics.AnalyticsEvent
import org.telegram.divo.analytics.DivoAnalytics
import org.telegram.divo.common.arch.BaseViewModel
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage

class ApplyConfirmationViewModel(
    private val eventId: Int
) : BaseViewModel<ApplyConfirmationViewState, ApplyConfirmationIntent, ApplyConfirmationEffect>() {

    override fun createInitialState(): ApplyConfirmationViewState = ApplyConfirmationViewState(eventId)

    init {
        setIntent(ApplyConfirmationIntent.OnLoad)
    }

    override fun handleIntent(intent: ApplyConfirmationIntent) {
        when (intent) {
            ApplyConfirmationIntent.OnLoad -> loadData()
            ApplyConfirmationIntent.OnSubmitClick -> submitApplication()
            ApplyConfirmationIntent.OnBackClick -> sendEffect(ApplyConfirmationEffect.Back)
            ApplyConfirmationIntent.OnSuccessDismiss -> sendEffect(ApplyConfirmationEffect.FinishWithSuccess)
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            
            val eventDeferred = async { DivoApi.eventRepository.getEvent(eventId) }
            val userDeferred = async { DivoApi.userRepository.getCurrentUserInfo() }
            
            val eventResult = eventDeferred.await()
            val userResult = userDeferred.await()
            
            if (eventResult is DivoResult.Success && userResult is DivoResult.Success) {
                setState { 
                    copy(
                        eventDetails = eventResult.value,
                        userInfo = userResult.value,
                        isLoading = false
                    ) 
                }
                DivoAnalytics.logEvent(AnalyticsEvent.EventApplyStarted(eventId.toLong(), userResult.value.id.toLong()))
            } else {
                setState { copy(isLoading = false) }
                val errorMsg = when {
                    eventResult !is DivoResult.Success -> eventResult.getErrorMessage()
                    userResult !is DivoResult.Success -> userResult.getErrorMessage()
                    else -> "Unknown error"
                }
                sendEffect(ApplyConfirmationEffect.ShowError(errorMsg))
            }
        }
    }

    private fun submitApplication() {
        viewModelScope.launch {
            setState { copy(isSubmitting = true) }
            val result = DivoApi.eventRepository.applyEvent(eventId)
            if (result is DivoResult.Success) {
                val userId = state.value.userInfo?.id ?: 0
                DivoAnalytics.logEvent(AnalyticsEvent.EventApplyConfirmed(eventId.toLong(), userId.toLong()))
                val event = state.value.eventDetails
                if (event != null) {
                    DivoApi.eventRepository.notifyEventParticipationChanged(eventId, true, event.appliesCount + 1)
                }
                setState { copy(isSubmitting = false, isSuccess = true) }
            } else {
                setState { copy(isSubmitting = false) }
                sendEffect(ApplyConfirmationEffect.ShowError(result.getErrorMessage()))
            }
        }
    }

    companion object {
        fun factory(eventId: Int) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ApplyConfirmationViewModel(eventId) as T
            }
        }
    }
}
