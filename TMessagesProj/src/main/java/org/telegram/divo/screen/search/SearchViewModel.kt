package org.telegram.divo.screen.search

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.common.OffsetPaginator
import org.telegram.divo.common.PaginatedResult
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.components.items.ProfileParameter
import org.telegram.divo.dal.dto.publication.ModelParametersDto
import org.telegram.divo.dal.dto.publication.RangeParamDto
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.AppearanceItem
import org.telegram.divo.entity.FeedlineItem
import org.telegram.divo.entity.SearchedProfile
import org.telegram.divo.screen.add_model.LocalCountry
import org.telegram.divo.screen.search.Effect.*
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController
import java.io.BufferedReader
import java.io.InputStreamReader

class SearchViewModel : BaseViewModel<State, Intent, Effect>() {
    private var searchFRJob: Job? = null
    private var searchJob: Job? = null

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

        val roleValues = s.role.value
            .split(",")
            .map { it.trim().lowercase().replace(" ", "_") }
            .filter { it.isNotEmpty() }
            .ifEmpty { null }

        when (val result = DivoApi.publicationRepository.searchFeeds(
            offset = offset,
            limit = limit,
            query = s.query,
            role = roleValues,
            modelParameters = s.buildModelParameters()
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
            is Intent.OnPhotoSelected -> sendEffect(NavigateToFaceSearch(intent.uri.toString()))
            Intent.OnLoadMore -> loadMore()
            is Intent.OnItemClicked -> {
                if (intent.isSearchMode)
                    sendEffect(NavigateToProfile(intent.user))
                else {
                    sendEffect(NavigateToFaceSearch(intent.user.photo))
                }
            }
            is Intent.OnSearchConfirmed -> setState { copy(isSearchConfirmed = true) }
            is Intent.OnApplyFilters -> {
                setState {
                    copy(
                        isSearchConfirmed = true,
                        selectedCountries = intent.countries,
                        selectedCity = intent.city,
                        role = intent.role,
                        gender = intent.gender,
                        hairLength = intent.hairLength,
                        hairColor = intent.hairColor,
                        eyeColor = intent.eyeColor,
                        skinColor = intent.skinColor,
                        blockParams = intent.blockParams
                    )
                }

                searchJob?.cancel()
                searchPaginator.reset()
                viewModelScope.launch {
                    setState { copy(isLoading = true) }
                    searchPaginator.loadInitial()
                    setState { copy(isLoading = false, hasSearched = true) }
                }
            }

            Intent.OnResetFilters -> {
                setState {
                    copy(
                        selectedCountries = emptyList(),
                        selectedCity = null,
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
                    setState { copy(isLoading = false, hasSearched = true) }
                }
            }
            Intent.OnFaceSearchHistoryClicked -> { sendEffect(NavigateToFaceSearchHistory) }
            is Intent.OnSimilarProfilesClicked -> sendEffect(NavigateToSimilarProfiles(intent.photo, intent.filters))
            Intent.OnLoadMoreFR -> loadMoreFR()
            is Intent.OnQueryFRChanged -> onQueryFRChanged(intent.value)
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
            setState { copy(isLoading = true) }
            searchPaginator.reset()
            searchPaginator.loadInitial()
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
                        val name = args[2]
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
                                countryCode = cols[8],
                                population = cols[14].toIntOrNull() ?: 0
                            )
                        )
                    }
                }
                stream.close()
                // Просто сохраняем список в State. Дальше с ним будет работать CityPickerSheet.
                setState { copy(allCities = cities) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun State.buildModelParameters(): ModelParametersDto? {
        // Gender: value хранит строки типа "Male, Female" -> маппим в ["male", "female"]
        val genderValues = gender.value
            .split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .ifEmpty { null }

        // Appearance: маппим titles обратно в ids через options
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

        // blockParams: value хранится как "25, 40"
        fun ProfileParameter.toRange(): RangeParamDto? {
            val parts = value.split(",").mapNotNull { it.trim().toIntOrNull() }
            return if (parts.size >= 2) RangeParamDto(from = parts[0], to = parts[1]) else null
        }

        val ageRange     = blockParams.find { it.type == ParametersType.AGE }?.toRange()
        val heightRange  = blockParams.find { it.type == ParametersType.HEIGHT }?.toRange()
        val waistRange   = blockParams.find { it.type == ParametersType.WAIST }?.toRange()
        val hipsRange    = blockParams.find { it.type == ParametersType.HIPS }?.toRange()
        val shoeRange    = blockParams.find { it.type == ParametersType.SHOE_SIZE }?.toRange()
        val breastRange  = blockParams.find { it.type == ParametersType.BREAST_SIZE }?.toRange()

        val hasAnyFilter = listOf(
            genderValues, hairColorIds, hairLengthIds, eyeColorIds, skinColorIds
        ).any { it != null } || listOf(
            ageRange, heightRange, waistRange, hipsRange, shoeRange, breastRange
        ).any { it != null }

        if (!hasAnyFilter) return null

        return ModelParametersDto(
            gender = genderValues,
            age = ageRange,
            height = heightRange,
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
        name = this.user?.fullName.orEmpty(),
        age = this.user?.age,
        country = this.user?.city?.countryName,
        countryCode = this.user?.city?.countryCode,
        isMarked = this.isFavoriteByUser,
        likes = this.likesCount,
        isLiked = this.isLikedByUser,
        photo = this.searchImageUrl.orEmpty(),
        roleLabel = this.user?.roleLabel.orEmpty(),
        similarity = null
    )

    companion object {
        private const val PAGE_SIZE = 10
        private const val SEARCH_DEBOUNCE_MS = 400L
    }
}