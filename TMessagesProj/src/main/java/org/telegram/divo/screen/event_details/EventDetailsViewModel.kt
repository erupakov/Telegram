package org.telegram.divo.screen.event_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.screen.event_details.EventDetailsEffect.*
import org.telegram.divo.usecase.GetEventListUseCase
import org.telegram.divo.usecase.IsModelUserUseCase

class EventDetailsViewModel(
    private val eventId: Int,
    private val isOwnProfile: Boolean
) : BaseViewModel<EventDetailsViewState, EventDetailsIntent, EventDetailsEffect>() {

    override fun createInitialState(): EventDetailsViewState = EventDetailsViewState(eventId, isOwnProfile)

    override fun handleIntent(intent: EventDetailsIntent) {
        when (intent) {
            EventDetailsIntent.OnAddEventClicked -> {}
            is EventDetailsIntent.OnEventCardClicked -> {}
            is EventDetailsIntent.OnEventCtaClicked -> {}
            EventDetailsIntent.OnSearchClicked -> {}
            EventDetailsIntent.OnBackClicked -> sendEffect(Back)
            is EventDetailsIntent.OnPhotoClick -> sendEffect(NavigateToGallery(intent.items, intent.id))
            EventDetailsIntent.OnParamsClick -> sendEffect(NavigateToParams)
            is EventDetailsIntent.OnPrevEventClicked -> sendEffect(NavigateToPrevEvent(intent.eventId))
            EventDetailsIntent.OnLoad -> loadData()
        }
    }

    private val eventPaginator = GetEventListUseCase(limit = 10).paginator

    init {
        setIntent(EventDetailsIntent.OnLoad)
    }

    fun loadData() {
        observeEvents()
        loadEvent()
        loadRoleInfo()
    }

    private fun loadEvent() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val result = DivoApi.eventRepository.getEvent(state.value.eventId)

            if (result is DivoResult.Success) {
                setState {
                    copy(
                        eventDetails = result.value,
                        isLoading = false
                    )
                }
            } else {
                val errorMsg = result.getErrorMessage()
                sendEffect(ShowError(errorMsg))
            }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            eventPaginator.loadInitial()
            eventPaginator.state.collect { paginatorState ->
                setState {
                    copy(
                        events = paginatorState.items,
                        isLoadingEvents = paginatorState.isLoading,
                        isLoadingMore = paginatorState.isLoadingMore,
                        hasMore = paginatorState.hasMore,
                    )
                }
                paginatorState.error?.let {
                    sendEffect(ShowError(it))
                }
            }
        }
    }

    private fun loadRoleInfo() {
        viewModelScope.launch {
            setState { copy(isRoleLoading = true) }
            val result = IsModelUserUseCase(DivoApi.userRepository).invoke()

            if (result is DivoResult.Success) {
                setState {
                    copy(
                        isModel = result.value,
                        isRoleLoading = false
                    )
                }
            } else {
                val errorMsg = result.getErrorMessage()
                setState { copy(isRoleLoading = false) }
                sendEffect(ShowError(errorMsg))
            }
        }
    }

    companion object {
        fun factory(eventId: Int, isOwnProfile: Boolean) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return EventDetailsViewModel(eventId, isOwnProfile) as T
            }
        }
    }
}

