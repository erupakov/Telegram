package org.telegram.divo.screen.event_list

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.usecase.GetEventListUseCase
import org.telegram.divo.usecase.IsModelUserUseCase

class EventListViewModel :
    BaseViewModel<EventListViewState, EventListIntent, EventListEffect>() {

    override fun createInitialState(): EventListViewState = EventListViewState()

    private val eventPaginator = GetEventListUseCase(limit = 10).paginator

    init {
        observeEvents()
        loadRoleInfo()
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
                    )
                }
                paginatorState.error?.let {
                    sendEffect(EventListEffect.ShowError(it))
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
                sendEffect(EventListEffect.ShowError(errorMsg))
            }
        }
    }
}

