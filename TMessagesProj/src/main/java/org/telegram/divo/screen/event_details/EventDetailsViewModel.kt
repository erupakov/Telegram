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
    private var currentUserId: Int? = null

    override fun createInitialState(): EventDetailsViewState = EventDetailsViewState(eventId, isOwnProfile)

    override fun handleIntent(intent: EventDetailsIntent) {
        when (intent) {
            EventDetailsIntent.OnAddEventClicked -> {}
            is EventDetailsIntent.OnEventCardClicked -> {}
            is EventDetailsIntent.OnEventCtaClicked -> handleCtaClicked(intent.eventId)
            EventDetailsIntent.OnSearchClicked -> {}
            EventDetailsIntent.OnBackClicked -> sendEffect(Back)
            EventDetailsIntent.OnEditEventClick -> state.value.eventDetails?.id?.let { sendEffect(NavigateToEditEvent(it)) }
            EventDetailsIntent.OnDeleteEventConfirmed -> deleteEvent()
            is EventDetailsIntent.OnPhotoClick -> sendEffect(NavigateToGallery(intent.items, intent.id))
            EventDetailsIntent.OnParamsClick -> sendEffect(NavigateToParams)
            is EventDetailsIntent.OnPrevEventClicked -> sendEffect(NavigateToPrevEvent(intent.eventId))
            EventDetailsIntent.OnLoad -> loadData()
        }
    }

    private fun handleCtaClicked(eventId: Int) {
        val currentEvent = state.value.eventDetails ?: return
        if (currentEvent.id != eventId) return

        val isCurrentlyApplied = currentEvent.isApplied

        // Optimistic UI update
        val newAppliesCount = if (!isCurrentlyApplied) currentEvent.appliesCount + 1 else currentEvent.appliesCount - 1
        setState {
            copy(
                eventDetails = currentEvent.copy(isApplied = !isCurrentlyApplied, appliesCount = newAppliesCount)
            )
        }
        DivoApi.eventRepository.notifyEventParticipationChanged(eventId, !isCurrentlyApplied, newAppliesCount)

        viewModelScope.launch {
            val result = if (isCurrentlyApplied) {
                DivoApi.eventRepository.unapplyEvent(eventId)
            } else {
                DivoApi.eventRepository.applyEvent(eventId)
            }

            if (result !is DivoResult.Success) {
                // Revert optimistic update
                setState {
                    copy(
                        eventDetails = currentEvent.copy(isApplied = isCurrentlyApplied, appliesCount = currentEvent.appliesCount)
                    )
                }
                DivoApi.eventRepository.notifyEventParticipationChanged(eventId, isCurrentlyApplied, currentEvent.appliesCount)
                sendEffect(ShowError(result.getErrorMessage()))
            }
        }
    }

    private var eventPaginator = GetEventListUseCase(limit = 10).paginator

    init {
        setIntent(EventDetailsIntent.OnLoad)
        viewModelScope.launch {
            DivoApi.eventRepository.eventsUpdatedFlow.collect {
                loadData()
            }
        }
    }

    fun loadData() {
        loadEvent()
        loadRoleInfo()
        loadCurrentUserForOwnership()
    }

    private fun loadEvent() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val result = DivoApi.eventRepository.getEvent(state.value.eventId)

            if (result is DivoResult.Success) {
                setState {
                    copy(
                        eventDetails = result.value,
                        isOwnEvent = (currentUserId != null && currentUserId == result.value.creator?.id) || isOwnProfile,
                        isLoading = false
                    )
                }
                val creatorId = result.value.creator?.id ?: return@launch
                eventPaginator = GetEventListUseCase(limit = 10, creatorId = creatorId).paginator
                observeEvents()
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

    private fun loadCurrentUserForOwnership() {
        viewModelScope.launch {
            when (val userResult = DivoApi.userRepository.getCurrentUserInfo()) {
                is DivoResult.Success -> {
                    currentUserId = userResult.value.id
                    setState {
                        copy(
                            isOwnEvent = (eventDetails?.creator?.id == currentUserId) || isOwnProfile
                        )
                    }
                }
                else -> Unit
            }
        }
    }

    private fun deleteEvent() {
        state.value.eventDetails?.id?.let { id ->
            viewModelScope.launch {
                setState { copy(isLoading = true) }
                val result = DivoApi.eventRepository.deleteEvent(id)
                if (result is DivoResult.Success) {
                    sendEffect(EventDeleted)
                } else {
                    setState { copy(isLoading = false) }
                    sendEffect(ShowError(result.getErrorMessage()))
                }
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

