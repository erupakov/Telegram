package org.telegram.divo.screen.similar_profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.dal.dto.publication.FeedRequestDto
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.SearchedProfile
import kotlin.random.Random

class SimilarProfilesViewModel(
    val imageUrl: String
) : BaseViewModel<State, Intent, Effect>() {

    override fun createInitialState(): State = State(imageUrl = imageUrl)

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.OnBackClicked -> sendEffect(Effect.NavigateBack)
            is Intent.OnLikeChanged -> onLikeChange(intent.id)
            is Intent.OnMarkChanged -> onMarkChange(intent.id)
            is Intent.OnProfileClicked -> sendEffect(Effect.NavigateToProfile(intent.id))
        }
    }

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            launch { loadSimilarProfiles() }
        }
    }

    //TODO временно
    private suspend fun loadSimilarProfiles() {
        setState { copy(isLoading = true) }

        //if (result is DivoResult.Success) {
            setState {
                copy(
                    profiles = mockProfiles,
                    isLoading = false
                )
            }
//        } else {
//            setState { copy(isLoading = false) }
//            sendEffect(Effect.ShowError(result.getErrorMessage()))
//        }
    }

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

    companion object {
        fun factory(uri: String) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SimilarProfilesViewModel(uri) as T
            }
        }

        val mockProfiles = listOf(
            SearchedProfile(
                id = 1,
                name = "Emma Johnson",
                age = "1994-11-03",
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
                age = "1996-07-15",
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
                age = "1992-02-21",
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
                age = "1990-09-10",
                country = "France",
                countryCode = "FR",
                isMarked = false,
                likes = 560,
                isLiked = true,
                photo = "https://randomuser.me/api/portraits/men/4.jpg",
                roleLabel = "Model",
                similarity = 25
            ),
            SearchedProfile(
                id = 5,
                name = "Liam Brown",
                age = "1992-02-21",
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
                age = "1990-09-10",
                country = "France",
                countryCode = "FR",
                isMarked = false,
                likes = 560,
                isLiked = true,
                photo = "https://randomuser.me/api/portraits/men/4.jpg",
                roleLabel = "Model",
                similarity = 25
            )
        )
    }
}