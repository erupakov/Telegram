package org.telegram.divo.screen.event_list

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.common.OffsetPaginator
import org.telegram.messenger.NotificationCenter
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.RoleType
import org.telegram.divo.usecase.GetEventListUseCase

class EventListViewModel :
    BaseViewModel<EventListViewState, EventListIntent, EventListEffect>() {

    override fun createInitialState(): EventListViewState = EventListViewState()

    private val allEventsPaginator = GetEventListUseCase(limit = 10).paginator
    private var myEventsPaginator: OffsetPaginator<org.telegram.divo.entity.Event>? = null

    private var currentUserId: Int? = null

    private val activePaginator: OffsetPaginator<org.telegram.divo.entity.Event>
        get() = if (state.value.selectedTab == 0 && myEventsPaginator != null) {
            myEventsPaginator!!
        } else {
            allEventsPaginator
        }

    private var currentLanguage = org.telegram.messenger.LocaleController.getInstance().currentLocale?.language ?: ""

    private val languageObserver = NotificationCenter.NotificationCenterDelegate { id, _, _ ->
        if (id == NotificationCenter.reloadInterface) {
            val newLanguage = org.telegram.messenger.LocaleController.getInstance().currentLocale?.language ?: ""
            if (currentLanguage != newLanguage) {
                currentLanguage = newLanguage
                allEventsPaginator.reset()
                myEventsPaginator?.reset()
                viewModelScope.launch {
                    activePaginator.loadInitial()
                }
            }
        }
    }

    init {
        NotificationCenter.getGlobalInstance().addObserver(languageObserver, NotificationCenter.reloadInterface)
        setIntent(EventListIntent.OnLoad)
        viewModelScope.launch {
            DivoApi.eventRepository.eventsUpdatedFlow.collect {
                allEventsPaginator.reset()
                allEventsPaginator.loadInitial()
                myEventsPaginator?.let {
                    it.reset()
                    it.loadInitial()
                }
            }
        }
        viewModelScope.launch {
            DivoApi.eventRepository.eventParticipationFlow.collect { update ->
                updateEventInList(update.eventId) {
                    it.copy(isApplied = update.isApplied, appliesCount = update.appliesCount)
                }
            }
        }
        viewModelScope.launch {
            DivoApi.authRepository.authStateFlow.collect { isLoggedIn ->
                // When auth state changes (login, logout, switch role), reset data
                allEventsPaginator.reset()
                myEventsPaginator?.reset()
                setState { EventListViewState() } // Reset UI state
                if (isLoggedIn) {
                    setIntent(EventListIntent.OnLoad) // Re-fetch role and events
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        NotificationCenter.getGlobalInstance().removeObserver(languageObserver, NotificationCenter.reloadInterface)
    }

    override fun handleIntent(intent: EventListIntent) {
        when (intent) {
            EventListIntent.OnLoad -> {
                loadData()
            }
            EventListIntent.OnLoadMore -> {
                viewModelScope.launch {
                    activePaginator.loadMore()
                }
            }
            EventListIntent.OnAddEventClicked -> sendEffect(EventListEffect.NavigateToCreateEvent)
            is EventListIntent.OnEventCardClicked -> sendEffect(
                EventListEffect.NavigateToEventDetails(intent.eventId)
            )
            is EventListIntent.OnEventCtaClicked -> onCtaClicked(intent.eventId)
            EventListIntent.OnSearchClicked -> sendEffect(EventListEffect.NavigateToSearch)
            is EventListIntent.OnTabSelected -> {
                val tabIndex = intent.tabIndex
                setState { copy(selectedTab = tabIndex) }
                viewModelScope.launch {
                    val paginator = if (tabIndex == 0) myEventsPaginator else allEventsPaginator
                    if (paginator != null) {
                        syncStateFromPaginator(paginator)
                        // Load if empty
                        if (paginator.state.value.items.isEmpty() && !paginator.state.value.isLoading) {
                            paginator.loadInitial()
                        }
                    }
                }
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            loadRoleInfo()
            observeAllEvents()
            allEventsPaginator.loadInitial()
        }
    }

    private fun observeAllEvents() {
        viewModelScope.launch {
            allEventsPaginator.state.collect { paginatorState ->
                if (state.value.selectedTab == 1 || !state.value.isAgency) {
                    setState {
                        copy(
                            events = paginatorState.items,
                            isLoading = paginatorState.isLoading,
                            isLoadingMore = paginatorState.isLoadingMore,
                            hasMore = paginatorState.hasMore,
                        )
                    }
                }
                paginatorState.error?.let {
                    sendEffect(EventListEffect.ShowError(it))
                }
            }
        }
    }

    private fun observeMyEvents() {
        val paginator = myEventsPaginator ?: return
        viewModelScope.launch {
            paginator.state.collect { paginatorState ->
                if (state.value.selectedTab == 0 && state.value.isAgency) {
                    setState {
                        copy(
                            events = paginatorState.items,
                            isLoading = paginatorState.isLoading,
                            isLoadingMore = paginatorState.isLoadingMore,
                            hasMore = paginatorState.hasMore,
                        )
                    }
                }
                paginatorState.error?.let {
                    sendEffect(EventListEffect.ShowError(it))
                }
            }
        }
    }

    private fun syncStateFromPaginator(paginator: OffsetPaginator<org.telegram.divo.entity.Event>) {
        val paginatorState = paginator.state.value
        setState {
            copy(
                events = paginatorState.items,
                isLoading = paginatorState.isLoading,
                isLoadingMore = paginatorState.isLoadingMore,
                hasMore = paginatorState.hasMore,
            )
        }
    }

    private fun onCtaClicked(eventId: Int) {
        val event = state.value.events.find { it.id == eventId } ?: return
        val isCurrentlyApplied = event.isApplied

        // Optimistic UI update
        updateEventInList(eventId) { 
            val newAppliesCount = if (!isCurrentlyApplied) it.appliesCount + 1 else it.appliesCount - 1
            it.copy(isApplied = !isCurrentlyApplied, appliesCount = newAppliesCount) 
        }
        val expectedNewCount = if (!isCurrentlyApplied) event.appliesCount + 1 else event.appliesCount - 1
        DivoApi.eventRepository.notifyEventParticipationChanged(eventId, !isCurrentlyApplied, expectedNewCount)

        viewModelScope.launch {
            val result = if (isCurrentlyApplied) {
                DivoApi.eventRepository.unapplyEvent(eventId)
            } else {
                DivoApi.eventRepository.applyEvent(eventId)
            }

            if (result !is DivoResult.Success) {
                // Revert optimistic update
                updateEventInList(eventId) { 
                    it.copy(isApplied = isCurrentlyApplied, appliesCount = event.appliesCount) 
                }
                DivoApi.eventRepository.notifyEventParticipationChanged(eventId, isCurrentlyApplied, event.appliesCount)
                sendEffect(EventListEffect.ShowError(result.getErrorMessage()))
            }
        }
    }

    private fun updateEventInList(eventId: Int, updater: (org.telegram.divo.entity.Event) -> org.telegram.divo.entity.Event) {
        setState {
            copy(
                events = events.map { if (it.id == eventId) updater(it) else it }
            )
        }
        myEventsPaginator?.updateItem({ it.id == eventId }, updater)
        allEventsPaginator.updateItem({ it.id == eventId }, updater)
    }

    private fun loadRoleInfo() {
        viewModelScope.launch {
            setState { copy(isRoleLoading = true) }
            val result = DivoApi.userRepository.getCurrentUserInfo()

            if (result is DivoResult.Success) {
                val role = result.value.role
                val isAgency = role == RoleType.AGENCY
                currentUserId = result.value.id

                if (isAgency) {
                    myEventsPaginator = GetEventListUseCase(
                        limit = 10,
                        creatorId = result.value.id
                    ).paginator
                    observeMyEvents()
                    // For agency, default tab is MY_EVENTS (0), load it
                    viewModelScope.launch {
                        myEventsPaginator?.loadInitial()
                    }
                }

                setState {
                    copy(
                        isModel = role.isModel(),
                        isAgency = isAgency,
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
