package org.telegram.divo.screen.search

import android.net.Uri
import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.components.items.ProfileParameter
import org.telegram.divo.entity.AppearanceItem
import org.telegram.divo.entity.SearchedProfile
import org.telegram.divo.screen.add_model.LocalCountry
import org.telegram.divo.dal.db.entity.FaceRecognitionEntity

data class State(
    val frSearchHistory: List<FaceRecognitionEntity> = emptyList(),
    val isModel: Boolean = true,
    val isUserLoading: Boolean = false,

    val query: String = "",
    val searchResults: List<SearchedProfile> = emptyList(),
    val totalProfiles: Int = 0,
    val isSearchConfirmed: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasSearched: Boolean = false,
    val hasMore: Boolean = false,

    val queryFR: String = "",
    val searchResultsFR: List<SearchedProfile> = emptyList(),
    val isLoadingFR: Boolean = false,
    val isLoadingMoreFR: Boolean = false,
    val hasMoreFR: Boolean = false,

    val allCities: List<LocalCity> = emptyList(),
    val selectedCity: LocalCity? = null,

    val allCountries: List<LocalCountry> = emptyList(),
    val selectedCountries: List<LocalCountry> = emptyList(),

    val gender: ProfileParameter = ProfileParameter(ParametersType.GENDER, ""),
    val role: ProfileParameter = ProfileParameter(ParametersType.ROLE, ""),
    val hairLength: ProfileParameter = ProfileParameter(ParametersType.HAIR_LENGTH, ""),
    val hairColor: ProfileParameter = ProfileParameter(ParametersType.HAIR_COLOR, ""),
    val eyeColor: ProfileParameter = ProfileParameter(ParametersType.EYE_COLOR, ""),
    val skinColor: ProfileParameter = ProfileParameter(ParametersType.SKIN_COLOR, ""),

    val blockParams: List<ProfileParameter> = listOf(),
    val hairLengthOptions: List<AppearanceItem> = emptyList(),
    val hairColorOptions: List<AppearanceItem> = emptyList(),
    val eyeColorOptions: List<AppearanceItem> = emptyList(),
    val skinColorOptions: List<AppearanceItem> = emptyList(),
) : ViewState {

    val activeFiltersCount: Int
        get() {
            var count = 0
            if (selectedCountries.isNotEmpty()) count++
            if (selectedCity != null) count++
            if (gender.value.isNotEmpty()) count++
            if (role.value.isNotEmpty()) count++
            if (hairColor.ids.isNotEmpty()) count++
            if (hairLength.ids.isNotEmpty()) count++
            if (eyeColor.ids.isNotEmpty()) count++
            if (skinColor.ids.isNotEmpty()) count++
            count += blockParams.count { it.value.isNotEmpty() }
            return count
        }

    fun getDefaultBlockParams(): List<ProfileParameter> =
        listOf(
            ProfileParameter(ParametersType.AGE, ""),
            ProfileParameter(ParametersType.HEIGHT, ""),
            ProfileParameter(ParametersType.WEIGHT, ""),
            ProfileParameter(ParametersType.WAIST, ""),
            ProfileParameter(ParametersType.HIPS, ""),
            ProfileParameter(ParametersType.SHOE_SIZE, ""),
            ProfileParameter(ParametersType.BREAST_SIZE, ""),
        )

    fun getFormatedCountries(countries: List<LocalCountry>): String =
        if (countries.isEmpty()) ""
        else {
            val first = countries.first()
            val flag = first.flag ?: ""
            if (countries.size == 1) "$flag ${first.name}" else "$flag ${first.name} +${countries.size - 1}"
        }

    fun getFormatedOptions(item: ProfileParameter): String =
        if (item.value.isEmpty()) ""
        else {
            val list = item.value.split(", ")
            val first = list.first()
            if (list.size == 1) first else "$first +${list.size - 1}"
        }

    val hasActiveFilters: Boolean
        get() = activeFiltersCount > 0
}

sealed interface Intent : ViewIntent {
    data object OnBackClicked : Intent
    data class OnQueryChanged(val value: String) : Intent
    data class OnQueryFRChanged(val value: String) : Intent
    data class OnPhotoSelected(val uri: Uri) : Intent
    data object OnLoadMore : Intent
    data object OnLoadMoreFR : Intent
    data class OnItemClicked(val user: SearchedProfile, val isSearchMode: Boolean) : Intent
    data object OnSearchConfirmed : Intent
    data object OnResetFilters : Intent
    data object OnFaceSearchHistoryClicked : Intent
    data class OnSimilarProfilesClicked(val photo: String, val filters: String?) : Intent

    data class OnApplyFilters(
        val countries: List<LocalCountry>,
        val city: LocalCity?,
        val role: ProfileParameter,
        val gender: ProfileParameter,
        val hairLength: ProfileParameter,
        val hairColor: ProfileParameter,
        val eyeColor: ProfileParameter,
        val skinColor: ProfileParameter,
        val blockParams: List<ProfileParameter>
    ) : Intent
}

sealed interface Effect : ViewEffect {
    data object NavigateBack : Effect
    data class ShowError(val message: String) : Effect
    data class NavigateToFaceSearch(val uri: String) : Effect
    data class NavigateToProfile(val user: SearchedProfile) : Effect
    data object NavigateToFaceSearchHistory : Effect
    data class NavigateToSimilarProfiles(val photo: String, val filters: String?) : Effect
}

data class LocalCity(
    val id: Long,
    val name: String,
    val asciiName: String,
    val countryCode: String,
    val population: Int
)
