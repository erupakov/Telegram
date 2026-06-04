package org.telegram.divo.screen.event_list

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.common.OffsetPaginator
import org.telegram.messenger.NotificationCenter
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.RoleType
import org.telegram.divo.screen.add_model.LocalCountry
import org.telegram.divo.screen.search.LocalCity
import org.telegram.divo.usecase.GetEventListUseCase
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers

class EventListViewModel :
    BaseViewModel<EventListViewState, EventListIntent, EventListEffect>() {

    override fun createInitialState(): EventListViewState = EventListViewState()

    private val allEventsPaginator = GetEventListUseCase(limit = 10).paginator
    private var myEventsPaginator: OffsetPaginator<org.telegram.divo.entity.Event>? = null
    private var currentUserId: Int? = null

    private val activePaginator: OffsetPaginator<org.telegram.divo.entity.Event>
        get() {
            return if (state.value.isSearchMode) {
                allEventsPaginator
            } else if (state.value.selectedTab == 0 && myEventsPaginator != null) {
                myEventsPaginator!!
            } else {
                allEventsPaginator
            }
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
            launch { loadCountries() }
            launch { loadCities() }
        }
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
            is EventListIntent.ConfirmWithdraw -> confirmWithdraw(intent.eventId)
            EventListIntent.OnAddEventClicked -> sendEffect(EventListEffect.NavigateToCreateEvent)
            is EventListIntent.OnEventCardClicked -> sendEffect(
                EventListEffect.NavigateToEventDetails(intent.eventId)
            )
            is EventListIntent.OnEventCtaClicked -> onCtaClicked(intent.eventId)
            EventListIntent.OnSearchClicked -> {
                setState { copy(isSearchMode = true) }
                performSearch()
                sendEffect(EventListEffect.NavigateToSearch)
            }
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
            // Search intents
            EventListIntent.OnOpenSearch -> {
                setState { copy(isSearchMode = true) }
                performSearch()
            }
            EventListIntent.OnCloseSearch -> {
                setState {
                    copy(
                        isSearchMode = false,
                        searchFilters = EventSearchFilters(),
                        totalSearchResults = 0,
                    )
                }
                // Restore normal list
                viewModelScope.launch {
                    syncStateFromPaginator(activePaginator)
                }
            }
            is EventListIntent.OnSearchQueryChanged -> {
                val newFilters = state.value.searchFilters.copy(query = intent.query)
                setState { copy(searchFilters = newFilters) }
                performSearch()
            }
            EventListIntent.OnSearchConfirmed -> {
                performSearch()
            }
            is EventListIntent.OnApplyFilters -> {
                setState { copy(searchFilters = intent.filters) }
                performSearch()
            }
            EventListIntent.OnResetFilters -> {
                val resetFilters = EventSearchFilters(query = state.value.searchFilters.query)
                setState { copy(searchFilters = resetFilters) }
                performSearch()
            }
        }
    }

    private fun performSearch() {
        syncStateFromPaginator(activePaginator)
    }

    fun loadData() {
        viewModelScope.launch {
            loadRoleInfo()
            observeAllEvents()
            loadEventTypes()
            loadAppearances()
            allEventsPaginator.loadInitial()
        }
    }

    private fun observeAllEvents() {
        viewModelScope.launch {
            allEventsPaginator.state.collect { paginatorState ->
                if (state.value.selectedTab == 1 || !state.value.isAgency || state.value.isSearchMode) {
                    syncStateFromPaginator(allEventsPaginator)
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
                if (state.value.selectedTab == 0 && state.value.isAgency && !state.value.isSearchMode) {
                    syncStateFromPaginator(paginator)
                }
                paginatorState.error?.let {
                    sendEffect(EventListEffect.ShowError(it))
                }
            }
        }
    }

    private fun syncStateFromPaginator(paginator: OffsetPaginator<org.telegram.divo.entity.Event>) {
        val paginatorState = paginator.state.value
        
        var filteredItems = paginatorState.items
        if (state.value.isSearchMode && state.value.searchFilters.hasAnyFilterOrQuery) {
            val filters = state.value.searchFilters
            
            // Helper functions for matching logic
            fun matchRange(
                filterFrom: Int?, filterTo: Int?,
                eventFrom: Int?, eventTo: Int?,
                defaultMin: Int = 0, defaultMax: Int = 1000
            ): Boolean {
                if (filterFrom == null && filterTo == null) return true
                val eMin = eventFrom ?: defaultMin
                val eMax = eventTo ?: defaultMax
                val fMin = filterFrom ?: 0
                val fMax = filterTo ?: 1000
                return maxOf(eMin, fMin) <= minOf(eMax, fMax)
            }

            fun matchValues(
                filterValue: String,
                eventValues: List<String>
            ): Boolean {
                if (filterValue.isBlank()) return true
                // If the event doesn't specify any requirement for this parameter, we let it pass
                if (eventValues.isEmpty()) return true
                
                val selectedTitles = filterValue.split(",").map { it.trim().lowercase() }.toSet()
                return eventValues.any { it.trim().lowercase() in selectedTitles }
            }

            fun getRange(type: ParametersType): Pair<Int?, Int?> {
                val value = filters.blockParams.find { it.type == type }?.value
                if (value.isNullOrBlank()) return null to null
                if ("-" in value) {
                    val parts = value.split("-")
                    return parts.getOrNull(0)?.trim()?.toIntOrNull() to parts.getOrNull(1)?.trim()?.toIntOrNull()
                }
                val intVal = value.toIntOrNull()
                return intVal to intVal
            }

            filteredItems = filteredItems.filter { event ->
                val matchesQuery = filters.query.isBlank() || event.title?.contains(filters.query, ignoreCase = true) == true
                val matchesType = filters.typeIds?.let { event.typeId in it } ?: true
                val matchesCity = if (filters.city == null) true else {
                    val eventCity = event.city.lowercase()
                    eventCity.contains(filters.city.name.lowercase()) ||
                    eventCity.contains(filters.city.asciiName.lowercase()) ||
                    filters.city.alternateNames.lowercase().contains(eventCity)
                }
                val matchesPaid = if (filters.paidOnly) event.paymentTypeId == 1 else true
                
                val attrs = event.modelAttributes
                
                val matchesAge = matchRange(filters.ageFrom, filters.ageTo, attrs?.ageFrom, attrs?.ageTo, 0, 100)
                
                val (heightFrom, heightTo) = getRange(ParametersType.HEIGHT)
                val matchesHeight = matchRange(heightFrom, heightTo, attrs?.heightFrom, attrs?.heightTo, 0, 300)

                val (waistFrom, waistTo) = getRange(ParametersType.WAIST)
                val matchesWaist = matchRange(waistFrom, waistTo, attrs?.waistFrom, attrs?.waistTo, 0, 300)

                val (hipsFrom, hipsTo) = getRange(ParametersType.HIPS)
                val matchesHips = matchRange(hipsFrom, hipsTo, attrs?.hipsFrom, attrs?.hipsTo, 0, 300)

                val (shoeFrom, shoeTo) = getRange(ParametersType.SHOE_SIZE)
                val matchesShoe = matchRange(shoeFrom, shoeTo, attrs?.shoesSizeFrom, attrs?.shoesSizeTo, 0, 100)

                val (breastFrom, breastTo) = getRange(ParametersType.BREAST_SIZE)
                val matchesBreast = matchRange(breastFrom, breastTo, attrs?.breastSizeFrom, attrs?.breastSizeTo, 0, 20)

                val matchesHairLength = matchValues(filters.hairLength.value, attrs?.hairLengths ?: emptyList())
                val matchesHairColor = matchValues(filters.hairColor.value, attrs?.hairColors ?: emptyList())
                val matchesEyeColor = matchValues(filters.eyeColor.value, attrs?.eyeColors ?: emptyList())
                val matchesSkinColor = matchValues(filters.skinColor.value, attrs?.skinColors ?: emptyList())

                matchesQuery && matchesType && matchesCity && matchesPaid && 
                matchesAge && matchesHeight && matchesWaist && matchesHips && 
                matchesShoe && matchesBreast && matchesHairLength && 
                matchesHairColor && matchesEyeColor && matchesSkinColor
            }
        }
        
        setState {
            copy(
                events = filteredItems,
                isLoading = paginatorState.isLoading,
                isLoadingMore = paginatorState.isLoadingMore,
                hasMore = paginatorState.hasMore,
                totalSearchResults = if (isSearchMode && searchFilters.hasAnyFilterOrQuery) filteredItems.size else 0
            )
        }
    }

    private fun onCtaClicked(eventId: Int) {
        val event = state.value.events.find { it.id == eventId } ?: return
        val isCurrentlyApplied = event.isApplied

        if (isCurrentlyApplied) {
            sendEffect(EventListEffect.ShowWithdrawConfirmation(eventId))
        } else {
            sendEffect(EventListEffect.NavigateToApplyConfirmation(eventId))
        }
    }

    private fun confirmWithdraw(eventId: Int) {
        val event = state.value.events.find { it.id == eventId } ?: return
        val isCurrentlyApplied = event.isApplied
        if (!isCurrentlyApplied) return

        // Optimistic UI update
        updateEventInList(eventId) { 
            val newAppliesCount = it.appliesCount - 1
            it.copy(isApplied = false, appliesCount = newAppliesCount) 
        }
        val expectedNewCount = event.appliesCount - 1
        DivoApi.eventRepository.notifyEventParticipationChanged(eventId, false, expectedNewCount)

        viewModelScope.launch {
            val result = DivoApi.eventRepository.unapplyEvent(eventId)

            if (result !is DivoResult.Success) {
                // Revert optimistic update
                updateEventInList(eventId) { 
                    it.copy(isApplied = true, appliesCount = event.appliesCount) 
                }
                DivoApi.eventRepository.notifyEventParticipationChanged(eventId, true, event.appliesCount)
                sendEffect(EventListEffect.ShowError(result.getErrorMessage()))
            }
        }
    }

    private fun updateEventInList(eventId: Int, updater: (org.telegram.divo.entity.Event) -> org.telegram.divo.entity.Event) {
        myEventsPaginator?.updateItem({ it.id == eventId }, updater)
        allEventsPaginator.updateItem({ it.id == eventId }, updater)
        
        // Let the state update via paginator flow collection
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

    private fun loadEventTypes() {
        viewModelScope.launch {
            val result = DivoApi.eventRepository.getEventTypes()
            if (result is DivoResult.Success) {
                setState { copy(eventTypes = result.value) }
            }
        }
    }

    private fun loadAppearances() {
        viewModelScope.launch {
            val result = DivoApi.userRepository.getAppearances()
            if (result is DivoResult.Success) {
                val dict = result.value
                setState {
                    copy(
                        hairLengthOptions = dict.hairLength.orEmpty(),
                        hairColorOptions = dict.hairColor.orEmpty(),
                        eyeColorOptions = dict.eyeColor.orEmpty(),
                        skinColorOptions = dict.skinColor.orEmpty()
                    )
                }
            }
        }
    }
    private fun loadCountries() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = mutableListOf<LocalCountry>()
            try {
                val stream = ApplicationLoader.applicationContext.assets.open("countries.txt")
                val reader = BufferedReader(InputStreamReader(stream))
                reader.forEachLine { line ->
                    val args = line.split(";")
                    if (args.size >= 3) {
                        val code = args[0]
                        val shortname = args[1]
                        val defaultName = args[2]
                        val locName = LocaleController.getCountryName(shortname)
                        val name = if (!locName.isNullOrEmpty()) locName else defaultName
                        val flag = LocaleController.getLanguageFlag(shortname)
                        list.add(
                            LocalCountry(
                                code = code,
                                shortName = shortname,
                                name = name,
                                flag = flag
                            )
                        )
                    }
                }
                reader.close()
                stream.close()

                list.sortBy { it.name }

                setState { copy(allCountries = list) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadCities() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cities = mutableListOf<LocalCity>()
                val stream = ApplicationLoader.applicationContext.assets.open("cities.txt")
                stream.bufferedReader().forEachLine { line ->
                    val cols = line.split("\t")
                    if (cols.size > 14) {
                        cities.add(
                            LocalCity(
                                id = cols[0].toLongOrNull() ?: return@forEachLine,
                                name = cols[1],
                                asciiName = cols[2],
                                alternateNames = cols[3],
                                countryCode = cols[8],
                                population = cols[14].toIntOrNull() ?: 0
                            )
                        )
                    }
                }
                stream.close()
                setState { copy(allCities = cities) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
