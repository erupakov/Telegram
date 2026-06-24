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
    private val toggleEventFavouriteUseCase = org.telegram.divo.usecase.ToggleEventFavouriteUseCase()

    override fun createInitialState(): EventDetailsViewState = EventDetailsViewState(eventId, isOwnProfile)

    override fun handleIntent(intent: EventDetailsIntent) {
        when (intent) {
            EventDetailsIntent.OnAddEventClicked -> {}
            is EventDetailsIntent.OnEventCardClicked -> {}
            is EventDetailsIntent.OnEventCtaClicked -> handleCtaClicked(intent.eventId)
            is EventDetailsIntent.ConfirmWithdraw -> confirmWithdraw(intent.id)
            EventDetailsIntent.OnSearchClicked -> {}
            EventDetailsIntent.OnBackClicked -> sendEffect(Back)
            EventDetailsIntent.OnEditEventClick -> state.value.eventDetails?.id?.let { sendEffect(NavigateToEditEvent(it)) }
            EventDetailsIntent.OnCloseApplicationsConfirmed -> closeApplications()
            EventDetailsIntent.OnCancelEventConfirmed -> cancelEvent()
            EventDetailsIntent.OnDeleteEventConfirmed -> deleteEvent()
            is EventDetailsIntent.OnPhotoClick -> sendEffect(NavigateToGallery(intent.items, intent.id))
            EventDetailsIntent.OnParamsClick -> sendEffect(NavigateToParams)
            is EventDetailsIntent.OnPrevEventClicked -> sendEffect(NavigateToPrevEvent(intent.eventId))
            EventDetailsIntent.OnLikeClicked -> handleLikeClicked()
            EventDetailsIntent.OnFavouriteClicked -> handleFavouriteClicked()
            EventDetailsIntent.OnLoad -> loadData()
        }
    }

    private fun handleFavouriteClicked() {
        val currentEvent = state.value.eventDetails ?: return
        
        viewModelScope.launch {
            toggleEventFavouriteUseCase.execute(
                eventId = currentEvent.id,
                isFavourite = currentEvent.isFavourite,
                currentCount = currentEvent.favoritesCount,
                onUpdate = { newFavourite, newCount ->
                    setState {
                        copy(
                            eventDetails = currentEvent.copy(
                                isFavourite = newFavourite,
                                favoritesCount = newCount
                            )
                        )
                    }
                },
                onRollback = {
                    setState {
                        copy(
                            eventDetails = currentEvent
                        )
                    }
                },
                onSuccess = { newFavourite ->
                    sendEffect(
                        EventDetailsEffect.ActionChanged(
                            resDrawableId = org.telegram.messenger.R.drawable.ic_divo_bookmark_glass_selected,
                            resStringId = if (newFavourite) org.telegram.messenger.R.string.BookmarkSaved else org.telegram.messenger.R.string.BookmarkUnsaved
                        )
                    )
                },
                onError = { sendEffect(ShowError(it)) }
            )
        }
    }

    private fun handleLikeClicked() {
        val currentEvent = state.value.eventDetails ?: return
        val isCurrentlyLiked = currentEvent.isLiked

        val newLikesCount = if (isCurrentlyLiked) currentEvent.likesCount - 1 else currentEvent.likesCount + 1

        // Optimistic update
        setState {
            copy(
                eventDetails = currentEvent.copy(
                    isLiked = !isCurrentlyLiked,
                    likesCount = newLikesCount
                )
            )
        }

        viewModelScope.launch {
            val result = if (isCurrentlyLiked) {
                DivoApi.eventRepository.unlikeEvent(currentEvent.id)
            } else {
                DivoApi.eventRepository.likeEvent(currentEvent.id)
            }

            if (result is DivoResult.Success) {
                sendEffect(
                    EventDetailsEffect.ActionChanged(
                        resDrawableId = org.telegram.messenger.R.drawable.ic_divo_favorite_selected,
                        resStringId = if (!isCurrentlyLiked) org.telegram.messenger.R.string.Liked else org.telegram.messenger.R.string.Unliked
                    )
                )
            } else {
                // Revert optimistic update
                setState {
                    copy(
                        eventDetails = currentEvent.copy(
                            isLiked = isCurrentlyLiked,
                            likesCount = currentEvent.likesCount
                        )
                    )
                }
                sendEffect(ShowError(result.getErrorMessage()))
            }
        }
    }

    private fun handleCtaClicked(eventId: Int) {
        val currentEvent = state.value.eventDetails ?: return
        if (currentEvent.id != eventId) return

        val isCurrentlyApplied = currentEvent.isApplied

        if (isCurrentlyApplied) {
            sendEffect(ShowWithdrawConfirmation(eventId))
        } else {
            // Navigate to Apply Confirmation Screen
            sendEffect(NavigateToApplyConfirmation(eventId))
        }
    }

    private fun confirmWithdraw(eventId: Int) {
        val currentEvent = state.value.eventDetails ?: return
        if (currentEvent.id != eventId) return

        // Unapply immediately
        val newAppliesCount = currentEvent.appliesCount - 1
        setState {
            copy(
                eventDetails = currentEvent.copy(isApplied = false, appliesCount = newAppliesCount)
            )
        }
        DivoApi.eventRepository.notifyEventParticipationChanged(eventId, false, newAppliesCount)

        viewModelScope.launch {
            val result = DivoApi.eventRepository.unapplyEvent(eventId)

            if (result !is DivoResult.Success) {
                // Revert optimistic update
                setState {
                    copy(
                        eventDetails = currentEvent.copy(isApplied = true, appliesCount = currentEvent.appliesCount)
                    )
                }
                DivoApi.eventRepository.notifyEventParticipationChanged(eventId, true, currentEvent.appliesCount)
                sendEffect(ShowError(result.getErrorMessage()))
            }
        }
    }

    private var eventPaginator = GetEventListUseCase(limit = 10).paginator

    init {
        setIntent(EventDetailsIntent.OnLoad)
        viewModelScope.launch {
            DivoApi.eventRepository.eventsUpdatedFlow.collect {
                loadData(silent = true)
            }
        }
        viewModelScope.launch {
            DivoApi.eventRepository.eventParticipationFlow.collect { update ->
                val currentEvent = state.value.eventDetails
                if (currentEvent != null && currentEvent.id == update.eventId) {
                    setState {
                        copy(
                            eventDetails = currentEvent.copy(
                                isApplied = update.isApplied,
                                appliesCount = update.appliesCount
                            )
                        )
                    }
                }
            }
        }
    }

    fun loadData(silent: Boolean = false) {
        loadEvent(silent)
        loadRoleInfo(silent)
        loadCurrentUserForOwnership()
    }

    private fun loadEvent(silent: Boolean) {
        viewModelScope.launch {
            if (!silent) setState { copy(isLoading = true) }
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

    private fun loadRoleInfo(silent: Boolean) {
        viewModelScope.launch {
            if (!silent) setState { copy(isRoleLoading = true) }
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
                val result = DivoApi.eventRepository.deleteEvent(id)
                if (result is DivoResult.Success) {
                    sendEffect(EventDeleted)
                } else {
                    sendEffect(ShowError(result.getErrorMessage()))
                }
            }
        }
    }

    private fun closeApplications() {
        state.value.eventDetails?.id?.let { id ->
            viewModelScope.launch {
                val result = DivoApi.eventRepository.closeApplications(id)
                if (result is DivoResult.Success) {
                    setState { copy(eventDetails = result.value) }
                    sendEffect(ApplicationsClosed)
                } else {
                    sendEffect(ShowError(result.getErrorMessage()))
                }
            }
        }
    }

    private fun cancelEvent() {
        state.value.eventDetails?.id?.let { id ->
            viewModelScope.launch {
                val result = DivoApi.eventRepository.cancelEvent(id)
                if (result is DivoResult.Success) {
                    sendEffect(EventDeleted)
                } else {
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

