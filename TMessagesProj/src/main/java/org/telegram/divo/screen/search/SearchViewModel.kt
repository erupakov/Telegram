package org.telegram.divo.screen.search

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.divo.analytics.AnalyticsEvent
import org.telegram.divo.analytics.DivoAnalytics
import org.telegram.divo.common.arch.BaseViewModel
import org.telegram.divo.common.arch.OffsetPaginator
import org.telegram.divo.common.arch.PaginatedResult
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.components.items.ProfileParameter
import org.telegram.divo.dal.dto.publication.ModelParametersDto
import org.telegram.divo.dal.dto.publication.RangeParamDto
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.AppearanceItem
import org.telegram.divo.entity.FeedlineItem
import org.telegram.divo.entity.LocalCountry
import org.telegram.divo.entity.SearchedProfile
import org.telegram.divo.screen.search.Effect.*
import org.telegram.divo.usecase.ToggleBookmarkUseCase
import org.telegram.divo.usecase.ToggleLikeUseCase
import org.telegram.messenger.R

class SearchViewModel : BaseViewModel<State, Intent, Effect>() {
    private var searchFRJob: Job? = null
    private var searchJob: Job? = null

    private val toggleLikeUseCase = ToggleLikeUseCase()
    private val toggleBookmarkUseCase = ToggleBookmarkUseCase()

    private val searchFRPaginator = OffsetPaginator(limit = PAGE_SIZE) { offset, limit ->
        when (val result = DivoApi.publicationRepository.searchFeeds(
            offset = offset,
            limit = limit,
            query = state.value.queryFR,
        )) {
            is DivoResult.Success -> {
                PaginatedResult(
                    items = result.value.items,
                    totalCount = result.value.pagination?.totalCount ?: result.value.items.size
                )
            }
            else -> throw Exception(result.getErrorMessage())
        }
    }

    private val searchPaginator = OffsetPaginator(limit = PAGE_SIZE) { offset, limit ->
        val s = state.value

        val mappedRole = org.telegram.divo.entity.mapRoleToEnglish(s.role.value)
        val roleValues = mappedRole
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() && it != "all" }
            ?.map { if (it == "agency") "agency_employee" else it }
            ?.ifEmpty { null }

        when (val result = DivoApi.publicationRepository.searchFeeds(
            offset = offset,
            limit = limit,
            query = s.query,
            role = roleValues,
            modelParameters = s.buildModelParameters(),
            geoCityId = s.resolvedGeoCityId,
            countryCode = s.selectedCountries.firstOrNull()?.shortName
        )) {
            is DivoResult.Success -> {
                val data = result.value
                setState { copy(totalProfiles = data.pagination?.totalCount ?: data.items.size) }
                PaginatedResult(
                    items = data.items,
                    totalCount = data.pagination?.totalCount ?: data.items.size
                )
            }
            else -> throw Exception(result.getErrorMessage())
        }
    }

    override fun createInitialState(): State = State()

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.OnBackClicked -> sendEffect(NavigateBack)
            is Intent.OnQueryChanged -> onQueryChanged(intent.value)
            is Intent.OnPhotoSelected -> {
                DivoAnalytics.logEvent(AnalyticsEvent.FaceRecognitionOpened("search"))
                sendEffect(NavigateToFaceSearch(intent.uri.toString()))
            }
            Intent.OnLoadMore -> loadMore()
            is Intent.OnItemClicked -> {
                DivoAnalytics.logEvent(AnalyticsEvent.SearchModelTapped(intent.user.id))
                if (intent.isSearchMode)
                    sendEffect(NavigateToProfile(intent.user))
                else {
                    DivoAnalytics.logEvent(AnalyticsEvent.FaceRecognitionOpened("search"))
                    sendEffect(NavigateToFaceSearch(intent.user.photo))
                }
            }
            is Intent.OnSearchConfirmed -> setState { copy(isSearchConfirmed = true) }
            is Intent.OnApplyFilters -> {
                searchJob?.cancel()
                searchPaginator.reset()
                viewModelScope.launch {
                    setState { copy(isLoading = true) }

                    var newGeoCityId: Int? = null
                    if (intent.city != null) {
                        try {
                            val geoResponse = DivoApi.geoService.searchByAddressName(intent.city.name)
                            newGeoCityId = geoResponse.data?.firstOrNull()?.city?.id
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    setState {
                        copy(
                            isSearchConfirmed = true,
                            selectedCountries = intent.countries,
                            selectedCity = intent.city,
                            resolvedGeoCityId = newGeoCityId,
                            role = intent.role,
                            gender = intent.gender,
                            hairLength = intent.hairLength,
                            hairColor = intent.hairColor,
                            eyeColor = intent.eyeColor,
                            skinColor = intent.skinColor,
                            blockParams = intent.blockParams
                        )
                    }

                    searchPaginator.loadInitial()
                    val hasResults = searchPaginator.state.value.items.isNotEmpty()
                    val filtersStr = getActiveFiltersString(state.value)
                    DivoAnalytics.logEvent(AnalyticsEvent.SearchFiltersApplied("models", filtersStr))
                    DivoAnalytics.logEvent(AnalyticsEvent.SearchPerformed("models", hasResults, state.value.query, filtersStr))
                    setState { copy(isLoading = false, hasSearched = true) }
                }
            }

            Intent.OnResetFilters -> {
                setState {
                    copy(
                        selectedCountries = emptyList(),
                        selectedCity = null,
                        resolvedGeoCityId = null,
                        role = ProfileParameter(ParametersType.ROLE, ""),
                        gender = ProfileParameter(ParametersType.GENDER, ""),
                        hairLength = ProfileParameter(ParametersType.HAIR_LENGTH, ""),
                        hairColor = ProfileParameter(ParametersType.HAIR_COLOR, ""),
                        eyeColor = ProfileParameter(ParametersType.EYE_COLOR, ""),
                        skinColor = ProfileParameter(ParametersType.SKIN_COLOR, ""),
                        blockParams = getDefaultBlockParams()
                    )
                }

                searchJob?.cancel()
                searchPaginator.reset()
                viewModelScope.launch {
                    setState { copy(isLoading = true) }
                    searchPaginator.loadInitial()
                    val hasResults = searchPaginator.state.value.items.isNotEmpty()
                    DivoAnalytics.logEvent(AnalyticsEvent.SearchPerformed("models", hasResults, state.value.query, ""))
                    setState { copy(isLoading = false, hasSearched = true) }
                }
            }
            Intent.OnFaceSearchHistoryClicked -> { sendEffect(NavigateToFaceSearchHistory) }
            is Intent.OnSimilarProfilesClicked -> {
                DivoAnalytics.logEvent(AnalyticsEvent.FaceRecognitionOpened("search"))
                sendEffect(NavigateToSimilarProfiles(intent.photo, intent.filters))
            }
            Intent.OnLoadMoreFR -> loadMoreFR()
            is Intent.OnQueryFRChanged -> onQueryFRChanged(intent.value)
            is Intent.OnLikeClick -> onLikeClick(intent.userId, intent.isFrSearch)
            is Intent.OnBookmarkClick -> onBookmarkClick(intent.userId, intent.isFrSearch)
        }
    }

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            observePaginator()
            launch { loadCountries() }
            launch { loadCities() }
            launch { loadUserProfile() }
            launch { loadFrHistory() }
        }
    }

    private suspend fun loadFrHistory() {
        val userInfoResult = DivoApi.userRepository.getCurrentUserInfo()
        val userId = (userInfoResult as? DivoResult.Success)?.value?.id ?: return
        
        DivoApi.faceRecognitionRepository.getRecent(10, userId).collect { history ->
            setState { copy(frSearchHistory = history) }
        }
    }

    private fun observePaginator() {
        // Подписка на основной поиск
        viewModelScope.launch {
            searchPaginator.state.collect { pState ->
                setState {
                    copy(
                        searchResults = pState.items.map { it.toSearchedProfile() },
                        isLoadingMore = pState.isLoadingMore,
                        hasMore = pState.hasMore,
                    )
                }
                pState.error?.let { sendEffect(ShowError(it)) }
            }
        }

        viewModelScope.launch {
            searchFRPaginator.state.collect { pState ->
                setState {
                    copy(
                        searchResultsFR = pState.items.map { it.toSearchedProfile() },
                        isLoadingMoreFR = pState.isLoadingMore,
                        hasMoreFR = pState.hasMore,
                    )
                }
                pState.error?.let { sendEffect(ShowError(it)) }
            }
        }
    }


    private fun onQueryChanged(query: String) {
        searchJob?.cancel()
        setState { copy(query = query, isSearchConfirmed = false, hasSearched = false) }

        if (query.isBlank()) {
            searchPaginator.reset()
            setState { copy(isLoading = false, searchResults = emptyList()) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            DivoAnalytics.logEvent(AnalyticsEvent.SearchQueryEntered("models", query.length))
            setState { copy(isLoading = true) }
            searchPaginator.reset()
            searchPaginator.loadInitial()
            val hasResults = searchPaginator.state.value.items.isNotEmpty()
            val filtersStr = getActiveFiltersString(state.value)
            DivoAnalytics.logEvent(AnalyticsEvent.SearchPerformed("models", hasResults, query, filtersStr))
            setState { copy(isLoading = false, hasSearched = true) }
        }
    }

    private fun onQueryFRChanged(query: String) {
        searchFRJob?.cancel()
        setState { copy(queryFR = query) }

        if (query.isBlank()) {
            searchFRPaginator.reset()
            setState { copy(isLoadingFR = false, searchResultsFR = emptyList()) }
            return
        }

        searchFRJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            setState { copy(isLoadingFR = true) }
            searchFRPaginator.reset()
            searchFRPaginator.loadInitial()
            setState { copy(isLoadingFR = false) }
        }
    }

    private fun loadMore() {
        viewModelScope.launch {
            searchPaginator.loadMore()
        }
    }

    private fun loadMoreFR() {
        viewModelScope.launch {
            searchFRPaginator.loadMore()
        }
    }

    private fun onLikeClick(userId: Int, isFrSearch: Boolean) {
        val targetList = if (isFrSearch) state.value.searchResultsFR else state.value.searchResults
        val targetItem = targetList.find { it.id == userId } ?: return
        val feedId = targetItem.feedId ?: return
        
        fun updateAll(newLiked: Boolean, newCount: Int): List<SearchedProfile> {
            return targetList.map {
                if (it.id == userId) it.copy(isLiked = newLiked, likes = newCount)
                else it
            }
        }

        viewModelScope.launch {
            toggleLikeUseCase.execute(
                userId = userId,
                isLiked = targetItem.isLiked,
                currentCount = targetItem.likes,
                screenName = "search",
                onUpdate = { newLiked, newCount ->
                    if (isFrSearch) {
                        setState { copy(searchResultsFR = updateAll(newLiked, newCount)) }
                    } else {
                        setState { copy(searchResults = updateAll(newLiked, newCount)) }
                    }
                },
                onRollback = {
                    if (isFrSearch) {
                        setState { copy(searchResultsFR = targetList) }
                    } else {
                        setState { copy(searchResults = targetList) }
                    }
                },
                onSuccess = { newLiked ->
                    sendEffect(Effect.ActionChanged(
                        R.drawable.ic_divo_favorite_selected,
                        if (newLiked) R.string.Liked else R.string.Unliked
                    ))
                },
                onError = { sendEffect(Effect.ShowError(it)) }
            )
        }
    }

    private fun onBookmarkClick(userId: Int, isFrSearch: Boolean) {
        val targetList = if (isFrSearch) state.value.searchResultsFR else state.value.searchResults
        val targetItem = targetList.find { it.id == userId } ?: return

        fun updateAll(newFavorite: Boolean, newCount: Int): List<SearchedProfile> {
            return targetList.map {
                if (it.id == userId) it.copy(isMarked = newFavorite, followersCount = newCount)
                else it
            }
        }

        viewModelScope.launch {
            toggleBookmarkUseCase.execute(
                userId = userId,
                isFollowed = targetItem.isMarked,
                currentFollowersCount = targetItem.followersCount,
                screenName = "search",
                onUpdate = { newFavorite, newCount ->
                    if (isFrSearch) {
                        setState { copy(searchResultsFR = updateAll(newFavorite, newCount)) }
                    } else {
                        setState { copy(searchResults = updateAll(newFavorite, newCount)) }
                    }
                },
                onRollback = {
                    if (isFrSearch) {
                        setState { copy(searchResultsFR = targetList) }
                    } else {
                        setState { copy(searchResults = targetList) }
                    }
                },
                onSuccess = { newFavorite ->
                    sendEffect(Effect.ActionChanged(
                        R.drawable.ic_divo_bookmark_glass_selected,
                        if (newFavorite) R.string.BookmarkSaved else R.string.BookmarkUnsaved
                    ))
                },
                onError = { sendEffect(Effect.ShowError(it)) }
            )
        }
    }

    private fun loadCountries() {
        viewModelScope.launch {
            val list = DivoApi.locationRepository.getCountries()
            setState { copy(allCountries = list) }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            setState { copy(isUserLoading = true) }
            val userDeferred = async { DivoApi.userRepository.getCurrentUserInfo() }
            val dictDeferred = async { DivoApi.userRepository.getAppearances() }

            val userResult = userDeferred.await()
            val dictResult = dictDeferred.await()

            if (userResult is DivoResult.Success && dictResult is DivoResult.Success) {
                val user = userResult.value
                val dict = dictResult.value

                setState {
                    copy(
                        isUserLoading = false,
                        isModel = user.role.isModel(),
                        blockParams = getDefaultBlockParams(),
                        hairLengthOptions = dict.hairLength.orEmpty(),
                        hairColorOptions = dict.hairColor.orEmpty(),
                        eyeColorOptions = dict.eyeColor.orEmpty(),
                        skinColorOptions = dict.skinColor.orEmpty()
                    )
                }
            } else {
                val errorMessage = if (userResult !is DivoResult.Success) {
                    userResult.getErrorMessage()
                } else {
                    dictResult.getErrorMessage()
                }

                setState { copy(isUserLoading = false) }
                sendEffect(Effect.ShowError(errorMessage))
            }
        }
    }

    private fun loadCities() {
        viewModelScope.launch {
            val cities = DivoApi.locationRepository.getCities()
            setState { copy(allCities = cities) }
        }
    }

    private fun State.buildModelParameters(): ModelParametersDto? {
        val genderValues = org.telegram.divo.entity.mapGenderToEnglish(gender.value)?.split(",")
            ?.ifEmpty { null }

        fun resolveIds(param: ProfileParameter, options: List<AppearanceItem>): List<Int>? {
            if (param.value.isEmpty()) return null
            val selectedTitles = param.value.split(",").map { it.trim() }.toSet()
            return options
                .filter { it.title?.trim() in selectedTitles }
                .mapNotNull { it.id }
                .ifEmpty { null }
        }

        val hairColorIds = resolveIds(hairColor, hairColorOptions)
        val hairLengthIds = resolveIds(hairLength, hairLengthOptions)
        val eyeColorIds = resolveIds(eyeColor, eyeColorOptions)
        val skinColorIds = resolveIds(skinColor, skinColorOptions)

        fun ProfileParameter.toRange(): RangeParamDto? {
            if (value.isBlank()) return null

            if ("-" in value) {
                val parts = value.split("-").mapNotNull { it.trim().toIntOrNull() }
                return if (parts.size >= 2) RangeParamDto(from = parts[0], to = parts[1]) else null
            }

            val intValue = value.toDoubleOrNull()?.toInt() ?: value.toIntOrNull()
            return intValue?.let { RangeParamDto(from = it, to = it) }
        }

        val ageRange     = blockParams.find { it.type == ParametersType.AGE }?.toRange()
        val heightRange  = blockParams.find { it.type == ParametersType.HEIGHT }?.toRange()
        val weightRange  = blockParams.find { it.type == ParametersType.WEIGHT }?.toRange()
        val waistRange   = blockParams.find { it.type == ParametersType.WAIST }?.toRange()
        val hipsRange    = blockParams.find { it.type == ParametersType.HIPS }?.toRange()
        val shoeRange    = blockParams.find { it.type == ParametersType.SHOE_SIZE }?.toRange()
        val breastRange  = blockParams.find { it.type == ParametersType.BREAST_SIZE }?.toRange()

        val hasAnyFilter = listOf(
            genderValues, hairColorIds, hairLengthIds, eyeColorIds, skinColorIds
        ).any { it != null } || listOf(
            ageRange, heightRange, weightRange, waistRange, hipsRange, shoeRange, breastRange
        ).any { it != null }

        if (!hasAnyFilter) return null

        return ModelParametersDto(
            gender = genderValues,
            age = ageRange,
            height = heightRange,
            weight = weightRange,
            waist = waistRange,
            hips = hipsRange,
            shoesSize = shoeRange,
            breastSize = breastRange,
            hairColor = hairColorIds,
            hairLength = hairLengthIds,
            eyeColor = eyeColorIds,
            skinColor = skinColorIds,
        )
    }

    private fun FeedlineItem.toSearchedProfile() = SearchedProfile(
        id = this.id,
        feedId = this.feedId,
        role = this.user?.role.orEmpty(),
        name = this.user?.fullName.orEmpty(),
        age = this.user?.age,
        country = this.user?.city?.countryName,
        countryCode = this.user?.city?.countryCode,
        isMarked = this.isFollowedByUser,
        likes = this.likesCount,
        isLiked = this.isLikedByUser,
        followersCount = this.user?.followersCount ?: 0,
        photo = this.searchImageUrl.orEmpty(),
        index = null,
        isModel = org.telegram.divo.entity.RoleType.from(this.user?.role).isModel(),
        roleLabel = this.user?.roleLabel.orEmpty(),
        similarity = null
    )

    companion object {
        private const val PAGE_SIZE = 10
        private const val SEARCH_DEBOUNCE_MS = 400L
    }

    private fun getActiveFiltersString(s: State): String {
        val active = mutableListOf<String>()
        if (s.selectedCountries.isNotEmpty()) active.add("country")
        if (s.selectedCity != null) active.add("city")
        if (s.role.value.isNotBlank()) active.add("role")
        if (s.gender.value.isNotBlank()) active.add("gender")
        if (s.hairLength.value.isNotBlank()) active.add("hair_length")
        if (s.hairColor.value.isNotBlank()) active.add("hair_color")
        if (s.eyeColor.value.isNotBlank()) active.add("eye_color")
        if (s.skinColor.value.isNotBlank()) active.add("skin_color")
        if (s.blockParams.any { it.value.isNotBlank() }) active.add("body_params")
        return active.joinToString(",")
    }
}
