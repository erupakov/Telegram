package org.telegram.divo.screen.similar_profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.common.utils.toAge
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.components.items.ProfileParameter
import org.telegram.divo.common.numericFilterRange
import org.telegram.divo.dal.db.entity.FaceRecognitionEntity
import org.telegram.divo.dal.dto.face.SimilarFaceDto
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.RoleType
import org.telegram.divo.entity.SearchedProfile
import org.telegram.divo.entity.UserInfo
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
    private val resultsJson: String? = null,
) : BaseViewModel<State, Intent, Effect>() {
    private var currentUserId: Int? = null
    private var pendingCountryShortNames: List<String> = emptyList()
    private var allProfiles: List<SearchedProfile> = emptyList()

    override fun createInitialState(): State = State(
        imageUrl = imageUrl,
        isHistoryMode = resultsJson == null
    )

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
                val filtered = filterProfiles(newState)
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
                val filtered = filterProfiles(newState)
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
            setState { copy(isUserLoading = true) }
            val result = DivoApi.userRepository.getCurrentUserInfo()
            val userId = (result as? DivoResult.Success)?.value?.id

            launch { loadCountries() }
            launch { loadUserProfile(result) }
            launch { loadSimilarProfiles(userId) }
        }
    }

    private suspend fun loadSimilarProfiles(userId: Int?) {
        setState { copy(isLoading = true) }

        var effectiveResultsJson = resultsJson

        if (effectiveResultsJson.isNullOrBlank() && userId != null) {
            val existing = DivoApi.faceRecognitionRepository.getByImageUri(imageUrl, userId)
            effectiveResultsJson = existing?.resultsJson
        }

        val profiles = parseResultsJson(effectiveResultsJson)

        allProfiles = profiles
        val filtered = filterProfiles(state.value.copy(profiles = profiles))
        val newState = state.value.copy(
            profiles = filtered,
            isLoading = false
        )
        setState { newState }

        saveHistory(newState)
    }

    private fun parseResultsJson(jsonToParse: String?): List<SearchedProfile> {
        if (jsonToParse.isNullOrBlank()) return emptyList()

        return try {
            val type = object : TypeToken<List<SimilarFaceDto>>() {}.type
            val dtos: List<SimilarFaceDto> = Gson().fromJson(jsonToParse, type)

            dtos.distinctBy { it.userId }.map { dto ->
                SearchedProfile(
                    id = dto.userId ?: -1,
                    name = dto.fullName.orEmpty(),
                    age = dto.birthday?.toAge(),
                    country = dto.countryName,
                    countryCode = dto.countryCode,
                    isMarked = dto.isFollowedByUser ?: false,
                    likes = dto.likedCount ?: 0,
                    isLiked = dto.isLikedByUser ?: false,
                    followersCount = dto.followersCount ?: 0,
                    photo = dto.image.orEmpty(),
                    index = dto.index,
                    isModel = RoleType.from(dto.role).isModel(),
                    roleLabel = RoleType.from(dto.role).value,
                    similarity = ((dto.score ?: 0.0) * 100).toInt(),
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun filterProfiles(currentState: State): List<SearchedProfile> {
        return allProfiles.filter { profile ->
            val countryMatch = if (currentState.selectedCountries.isEmpty()) {
                true
            } else {
                currentState.selectedCountries.any { it.shortName.equals(profile.countryCode, ignoreCase = true) }
            }

            val similarityMatch = (profile.similarity ?: 0) >= currentState.similarityPercent

            val roleMatch = if (currentState.role.value.isEmpty()) {
                true
            } else {
                val selectedRoles = currentState.role.value.split(", ")

                selectedRoles.any { selectedRole ->
                    profile.roleLabel.equals(selectedRole, ignoreCase = true)
                }
            }


            val ageParam = currentState.blockParams.find { it.type == ParametersType.AGE }?.value
            val ageMatch = if (ageParam.isNullOrEmpty()) {
                true
            } else {
                val ageBounds = ParametersType.AGE.numericFilterRange() ?: (16..45)
                val parts = ageParam.split("-")
                val minAge = parts.getOrNull(0)?.toIntOrNull() ?: ageBounds.first
                val maxAge = parts.getOrNull(1)?.toIntOrNull() ?: ageBounds.last

                val profileAge = profile.age
                profileAge != null && profileAge in minAge..maxAge
            }

            countryMatch && similarityMatch && roleMatch && ageMatch
        }
    }

    private val toggleLikeUseCase = org.telegram.divo.usecase.ToggleLikeUseCase()
    private val toggleBookmarkUseCase = org.telegram.divo.usecase.ToggleBookmarkUseCase()

    private fun onLikeChange(id: Int) {
        val targetProfile = state.value.profiles.find { it.id == id } ?: return
        val savedProfiles = state.value.profiles

        viewModelScope.launch {
            toggleLikeUseCase.execute(
                userId = targetProfile.id,
                isLiked = targetProfile.isLiked,
                currentCount = targetProfile.likes,
                onUpdate = { newLiked, newCount ->
                    setState {
                        copy(profiles = profiles.map { if (it.id == id) it.copy(isLiked = newLiked, likes = newCount) else it })
                    }
                },
                onRollback = {
                    setState { copy(profiles = savedProfiles) }
                },
                onSuccess = { newLiked ->
                    // Optinal effect if needed
                },
                onError = { sendEffect(ShowError(it)) }
            )
        }
    }

    private fun onMarkChange(id: Int) {
        val targetProfile = state.value.profiles.find { it.id == id } ?: return
        val savedProfiles = state.value.profiles

        viewModelScope.launch {
            toggleBookmarkUseCase.execute(
                userId = targetProfile.id,
                isFollowed = targetProfile.isMarked,
                currentFollowersCount = targetProfile.followersCount,
                onUpdate = { newFollowed, newCount ->
                    setState {
                        copy(profiles = profiles.map { if (it.id == id) it.copy(isMarked = newFollowed, followersCount = newCount) else it })
                    }
                },
                onRollback = {
                    setState { copy(profiles = savedProfiles) }
                },
                onSuccess = { newFollowed ->
                    // Optional effect if needed
                },
                onError = { sendEffect(ShowError(it)) }
            )
        }
    }

    private fun loadCountries() {
        viewModelScope.launch {
            val list = DivoApi.locationRepository.getCountries()
            setState { copy(allCountries = list) }
            if (pendingCountryShortNames.isNotEmpty()) {
                val selected = list.filter { it.shortName in pendingCountryShortNames }
                if (selected.isNotEmpty()) {
                    val newState = state.value.copy(selectedCountries = selected)
                    val filtered = filterProfiles(newState)
                    setState { newState.copy(profiles = filtered) }
                    saveHistory(newState.copy(profiles = filtered))
                }
                pendingCountryShortNames = emptyList()
            }
        }
    }

    private fun loadUserProfile(result: DivoResult<UserInfo>) {
        viewModelScope.launch {

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
        val filteredByCore = filterProfiles(stateWithCoreFilters)
        val finalInitialState = stateWithCoreFilters.copy(profiles = filteredByCore)

        setState { finalInitialState }

        // Обработка стран
        if (payload.countryShortNames.isNotEmpty()) {
            if (state.value.allCountries.isNotEmpty()) {
                val selected = state.value.allCountries.filter { it.shortName in payload.countryShortNames }
                if (selected.isNotEmpty()) {
                    val withCountries = finalInitialState.copy(selectedCountries = selected)
                    val filtered = filterProfiles(withCountries)
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
                    resultsJson = resultsJson ?: existing?.resultsJson,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    companion object {
        fun factory(
            uri: String,
            filtersJson: String? = null,
            resultsJson: String? = null
        ) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SimilarProfilesViewModel(uri, filtersJson, resultsJson) as T
            }
        }
    }
}
