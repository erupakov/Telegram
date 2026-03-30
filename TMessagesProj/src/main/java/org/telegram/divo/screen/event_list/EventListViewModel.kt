package org.telegram.divo.screen.event_list

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.usecase.GetEventListUseCase

class EventListViewModel :
    BaseViewModel<EventListViewState, EventListIntent, EventListEffect>() {

    override fun createInitialState(): EventListViewState = EventListViewState()

    private val eventPaginator = GetEventListUseCase(limit = 10).paginator

    init {
        observeOwnProfile()
        observeEvents()
        setIntent(EventListIntent.OnLoad)
    }

    override fun handleIntent(intent: EventListIntent) {
        when (intent) {
            EventListIntent.OnLoad -> {
                viewModelScope.launch {
                    eventPaginator.loadInitial()
                }
            }
            EventListIntent.OnLoadMore -> {
                 viewModelScope.launch {
                    eventPaginator.loadMore()
                }
            }
            EventListIntent.OnAddEventClicked -> sendEffect(EventListEffect.NavigateToCreateEvent)
            is EventListIntent.OnEventCardClicked -> sendEffect(
                EventListEffect.NavigateToEventDetails(intent.eventId)
            )
            is EventListIntent.OnEventCtaClicked -> sendEffect(
                EventListEffect.NavigateToEventDetails(intent.eventId)
            )
            EventListIntent.OnSearchClicked -> sendEffect(EventListEffect.NavigateToSearch)
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            eventPaginator.state.collect { paginatorState ->
                setState {
                    copy(
                        events = paginatorState.items,
                        isLoading = paginatorState.isLoading,
                        isLoadingMore = paginatorState.isLoadingMore,
                        hasMore = paginatorState.hasMore,
                        errorMessage = paginatorState.error
                    )
                }
            }
        }
    }

    private fun observeOwnProfile() {
        setState { copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            DivoApi.userRepository.currentUserFlow
                .filterNotNull()
                .collect { userData ->
                    setState {
                        copy(
                            isLoading = false,
                            userInfo = userData,
                        )
                    }
                }
        }

        viewModelScope.launch {
            if (DivoApi.userRepository.currentUserFlow.value == null) {
                val result = DivoApi.userRepository.getCurrentUserInfo()
                if (result !is DivoResult.Success) {
                    val errorMsg = result.getErrorMessage()
                    setState { copy(isLoading = false, errorMessage = errorMsg) }
                    //sendEffect(ProfileEffect.ShowError(errorMsg))
                }
            }
        }
    }
}

