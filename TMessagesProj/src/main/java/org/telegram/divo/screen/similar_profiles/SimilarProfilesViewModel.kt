package org.telegram.divo.screen.similar_profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.components.items.ProfileParameter
import org.telegram.divo.dal.db.entity.FaceRecognitionEntity
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.SearchedProfile
import org.telegram.divo.screen.add_model.LocalCountry
import org.telegram.divo.screen.similar_profiles.Effect.NavigateBack
import org.telegram.divo.screen.similar_profiles.Effect.NavigateToProfile
import org.telegram.divo.screen.similar_profiles.Effect.ShowError
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController
import java.io.BufferedReader
import java.io.InputStreamReader

class SimilarProfilesViewModel(
    val imageUrl: String,
    private val initialFiltersJson: String? = null,
) : BaseViewModel<State, Intent, Effect>() {
    private var currentUserId: Int? = null
    private var pendingCountryShortNames: List<String> = emptyList()

    override fun createInitialState(): State = State(imageUrl = imageUrl)

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.OnBackClicked -> sendEffect(NavigateBack)
            is Intent.OnLikeChanged -> onLikeChange(intent.id)
            is Intent.OnMarkChanged -> onMarkChange(intent.id)
            is Intent.OnProfileClicked -> sendEffect(NavigateToProfile(intent.id))
            is Intent.OnParamValueChanged -> {}
            Intent.OnParamsReset -> {
                val newState = state.value.copy(
                    selectedCountries = emptyList(),
                    similarityPercent = MIN_SIMILARITY,
                    role = ProfileParameter(ParametersType.ROLE, ""),
                    blockParams = state.value.getDefaultBlockParams()
                )
                val filtered = filterMockProfiles(newState)
                val updatedState = newState.copy(profiles = filtered)
                setState { updatedState }
                saveHistory(updatedState)
            }

            is Intent.OnApplyFilters -> {
                val newState = state.value.copy(
                    selectedCountries = intent.countries,
                    similarityPercent = intent.similarityPercent,
                    role = intent.role,
                    blockParams = intent.blockParams
                )
                val filtered = filterMockProfiles(newState)
                val updatedState = newState.copy(profiles = filtered)
                setState { updatedState }
                saveHistory(updatedState)
            }
        }
    }

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            launch { loadSimilarProfiles() }
            launch { loadCountries() }
            launch { loadUserProfile() }
        }
    }

    //TODO временно
    private suspend fun loadSimilarProfiles() {
        setState { copy(isLoading = true) }

        // Имитация загрузки
        val filtered = filterMockProfiles(state.value.copy(profiles = mockProfiles))
        val newState = state.value.copy(
            profiles = filtered,
            isLoading = false
        )
        setState { newState }

        // Пытаемся сохранить. Если currentUserId еще null,
        // история сохранится чуть позже в loadUserProfile
        saveHistory(newState)
    }

    private fun filterMockProfiles(currentState: State): List<SearchedProfile> {
        return mockProfiles.filter { profile ->
            val countryMatch = if (currentState.selectedCountries.isEmpty()) {
                true
            } else {
                currentState.selectedCountries.any { it.shortName.equals(profile.countryCode, ignoreCase = true) }
            }

            val similarityMatch = (profile.similarity ?: 0) >= currentState.similarityPercent

            val roleMatch = if (currentState.role.value.isEmpty()) {
                true
            } else {
                // Разбиваем строку на список выбранных ролей
                val selectedRoles = currentState.role.value.split(", ")
                // Проверяем, есть ли роль профиля в списке выбранных (Игнорируя регистр)
                selectedRoles.any { selectedRole ->
                    profile.roleLabel.equals(selectedRole, ignoreCase = true)
                }
            }


            val ageParam = currentState.blockParams.find { it.type == ParametersType.AGE }?.value
            val ageMatch = if (ageParam.isNullOrEmpty()) {
                true
            } else {
                val parts = ageParam.split("-")
                val minAge = parts.getOrNull(0)?.toIntOrNull() ?: 14
                val maxAge = parts.getOrNull(1)?.toIntOrNull() ?: 45

                val profileAge = profile.age
                profileAge in minAge..maxAge
            }

            countryMatch && similarityMatch && roleMatch && ageMatch
        }
    }


//    private fun calculateAgeFromDate(birthDateString: String?): Int {
//        if (birthDateString.isNullOrEmpty()) return 0
//        val birthYear = birthDateString.substringBefore("-").toIntOrNull() ?: return 0
//        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
//        return currentYear - birthYear
//    }

    private fun onLikeChange(id: Int) {
        val oldProfile = state.value.profiles.find { it.id == id } ?: return

        setState {
            val updatedProfiles = profiles.map { profile ->
                if (profile.id == id) {
                    val newIsLiked = !profile.isLiked
                    val newLikesCount = if (newIsLiked) profile.likes + 1 else profile.likes - 1

                    profile.copy(
                        isLiked = newIsLiked,
                        likes = newLikesCount
                    )
                } else {
                    profile
                }
            }

            copy(profiles = updatedProfiles)
        }

        //TODO если запрос упадет надо будет откатить не забыть
//        viewModelScope.launch {
//            setState {
//                val restoredProfiles = profiles.map { profile ->
//                    if (profile.id == id) oldProfile else profile
//                }
//                copy(profiles = restoredProfiles)
//            }
//            sendEffect(Effect.ShowError("Не удалось поставить лайк. Проверьте интернет."))
//        }
    }

    private fun onMarkChange(id: Int) {
        val oldProfile = state.value.profiles.find { it.id == id } ?: return
        setState {
            val updatedProfiles = profiles.map { profile ->
                if (profile.id == id) {
                    profile.copy(isMarked = !profile.isMarked)
                } else {
                    profile
                }
            }

            copy(profiles = updatedProfiles)
        }


        //TODO если запрос упадет надо будет откатить не забыть
//        viewModelScope.launch {
//            setState {
//                val restoredProfiles = profiles.map { profile ->
//                    if (profile.id == id) oldProfile else profile
//                }
//                copy(profiles = restoredProfiles)
//            }
//            sendEffect(Effect.ShowError("Не удалось добавить в закладки"))
//        }
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
                if (pendingCountryShortNames.isNotEmpty()) {
                    val selected = list.filter { it.shortName in pendingCountryShortNames }
                    if (selected.isNotEmpty()) {
                        val newState = state.value.copy(selectedCountries = selected)
                        val filtered = filterMockProfiles(newState)
                        setState { newState.copy(profiles = filtered) }
                        saveHistory(newState.copy(profiles = filtered))
                    }
                    pendingCountryShortNames = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            setState { copy(isUserLoading = true) }
            val result = DivoApi.userRepository.getCurrentUserInfo()

            if (result is DivoResult.Success) {
                currentUserId = result.value.id
                setState {
                    copy(
                        isUserLoading = false,
                        isModel = result.value.role.isModel(),
                        blockParams = getDefaultBlockParams(),
                    )
                }
                if (initialFiltersJson != null) {
                    applyInitialFiltersIfNeeded()
                } else {
                    saveHistory(state.value)
                }
            } else {
                setState { copy(isUserLoading = false) }
                sendEffect(ShowError(result.getErrorMessage()))
            }
        }
    }

    private fun applyInitialFiltersIfNeeded() {
        val payload = SimilarFiltersSerializer.deserialize(initialFiltersJson)

        if (payload == null) {
            saveHistory(state.value)
            return
        }

        val stateWithCoreFilters = state.value.copy(
            similarityPercent = payload.similarityPercent,
            role = ProfileParameter(ParametersType.ROLE, payload.roleValue),
            blockParams = mergeBlockParams(state.value.getDefaultBlockParams(), payload.blockParams)
        )
        val filteredByCore = filterMockProfiles(stateWithCoreFilters)
        val finalInitialState = stateWithCoreFilters.copy(profiles = filteredByCore)

        setState { finalInitialState }

        // Обработка стран
        if (payload.countryShortNames.isNotEmpty()) {
            if (state.value.allCountries.isNotEmpty()) {
                val selected = state.value.allCountries.filter { it.shortName in payload.countryShortNames }
                if (selected.isNotEmpty()) {
                    val withCountries = finalInitialState.copy(selectedCountries = selected)
                    val filtered = filterMockProfiles(withCountries)
                    setState { withCountries.copy(profiles = filtered) }
                    saveHistory(withCountries.copy(profiles = filtered))
                } else {
                    saveHistory(finalInitialState)
                }
            } else {
                pendingCountryShortNames = payload.countryShortNames
                saveHistory(finalInitialState) // Сохраняем пока без стран, потом loadCountries обновит
            }
        } else {
            saveHistory(finalInitialState)
        }
    }

    private fun mergeBlockParams(
        defaults: List<ProfileParameter>,
        restored: List<ProfileParameter>
    ): List<ProfileParameter> {
        val restoredByType = restored.associateBy { it.type }
        return defaults.map { default ->
            restoredByType[default.type]?.let {
                default.copy(value = it.value)
            } ?: default
        }
    }

    private fun saveHistory(currentState: State) {
        val userId = currentUserId ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val filtersJson = SimilarFiltersSerializer.serialize(currentState)

            val existing = DivoApi.faceRecognitionRepository.getByImageUri(currentState.imageUrl, userId)

            DivoApi.faceRecognitionRepository.save(
                FaceRecognitionEntity(
                    id = existing?.id ?: java.util.UUID.randomUUID().toString(),
                    imageUri = currentState.imageUrl,
                    userId = userId,
                    resultsCount = currentState.profiles.size,
                    filtersJson = filtersJson,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    companion object {
        fun factory(uri: String, filtersJson: String? = null) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SimilarProfilesViewModel(uri, filtersJson) as T
            }
        }

        val mockProfiles = listOf(
            SearchedProfile(
                id = 1,
                name = "Emma Johnson",
                age = 25,
                country = "USA",
                countryCode = "US",
                isMarked = false,
                likes = 120,
                isLiked = false,
                photo = "https://randomuser.me/api/portraits/women/1.jpg",
                roleLabel = "Model",
                similarity = 92
            ),
            SearchedProfile(
                id = 2,
                name = "Olivia Smith",
                age = 36,
                country = "United Kingdom",
                countryCode = "GB",
                isMarked = true,
                likes = 340,
                isLiked = true,
                photo = "https://randomuser.me/api/portraits/women/2.jpg",
                roleLabel = "Fun",
                similarity = 87
            ),
            SearchedProfile(
                id = 3,
                name = "Liam Brown",
                age = 18,
                country = "Germany",
                countryCode = "DE",
                isMarked = false,
                likes = 210,
                isLiked = false,
                photo = "https://randomuser.me/api/portraits/men/3.jpg",
                roleLabel = "New talent",
                similarity = 56
            ),
            SearchedProfile(
                id = 4,
                name = "Noah Davis",
                age = 21,
                country = "France",
                countryCode = "FR",
                isMarked = false,
                likes = 560,
                isLiked = true,
                photo = "https://randomuser.me/api/portraits/men/4.jpg",
                roleLabel = "Model",
                similarity = 35
            ),
            SearchedProfile(
                id = 5,
                name = "Liam Brown",
                age = 31,
                country = "Germany",
                countryCode = "DE",
                isMarked = false,
                likes = 210,
                isLiked = false,
                photo = "https://randomuser.me/api/portraits/men/3.jpg",
                roleLabel = "New talent",
                similarity = 56
            ),
            SearchedProfile(
                id = 6,
                name = "Noah Davis",
                age = 19,
                country = "France",
                countryCode = "FR",
                isMarked = false,
                likes = 560,
                isLiked = true,
                photo = "https://randomuser.me/api/portraits/men/4.jpg",
                roleLabel = "Model",
                similarity = 47
            )
        )
    }
}