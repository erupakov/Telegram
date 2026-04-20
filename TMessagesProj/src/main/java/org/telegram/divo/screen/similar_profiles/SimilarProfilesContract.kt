package org.telegram.divo.screen.similar_profiles

import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.components.items.ProfileParameter
import org.telegram.divo.entity.SearchedProfile
import org.telegram.divo.screen.add_model.LocalCountry

const val HIGH_PERCENT = 85
const val MIN_SIMILARITY = 35

data class State(
    val imageUrl: String,
    val profiles: List<SearchedProfile> = emptyList(),
    val isLoading: Boolean = false,
    val isUserLoading: Boolean = false,
    val isModel: Boolean = true,

    val allCountries: List<LocalCountry> = emptyList(),
    val selectedCountries: List<LocalCountry> = emptyList(),
    val similarityPercent: Int = MIN_SIMILARITY,

    val role: ProfileParameter = ProfileParameter(ParametersType.ROLE, ""),
    val blockParams: List<ProfileParameter> = listOf(),
) : ViewState {

    val topMatches: Int
        get() = profiles.count { (it.similarity ?: 0) >= HIGH_PERCENT }

    fun getDefaultBlockParams(): List<ProfileParameter> =
        listOf(
            ProfileParameter(ParametersType.AGE, ""),
            ProfileParameter(ParametersType.HEIGHT, ""),
            ProfileParameter(ParametersType.WAIST, ""),
            ProfileParameter(ParametersType.HIPS, ""),
            ProfileParameter(ParametersType.SHOE_SIZE, ""),
        )

    fun getFormatedCountries(countries: List<LocalCountry>): String =
        if (countries.isEmpty()) ""
        else {
            val first = countries.first()
            val flag = first.flag ?: ""
            if (countries.size == 1) "$flag ${first.name}" else "$flag ${first.name} +${countries.size - 1}"
        }

    fun getFormatedRoles(role: ProfileParameter): String =
        if (role.value.isEmpty()) ""
        else {
            val list = role.value.split(", ")
            val first = list.first()
            if (list.size == 1) first else "$first +${list.size - 1}"
        }

    val activeFiltersCount: Int
        get() {
            var count = 0
            if (selectedCountries.isNotEmpty()) count++
            if (similarityPercent > 35) count++
            if (role.value.isNotEmpty()) count++
            count += blockParams.count { it.value.isNotEmpty() }

            return count
        }

    val hasActiveFilters: Boolean
        get() = activeFiltersCount > 0
}

sealed interface Intent : ViewIntent {
    data object OnBackClicked : Intent
    data class OnLikeChanged(val id: Int) : Intent
    data class OnMarkChanged(val id: Int) : Intent
    data class OnProfileClicked(val id: Int) : Intent
    data class OnApplyFilters(
        val countries: List<LocalCountry>,
        val similarityPercent: Int,
        val role: ProfileParameter,
        val blockParams: List<ProfileParameter>
    ) : Intent
    data class OnParamValueChanged(val type: ParametersType, val value: String) : Intent
    data object OnParamsReset : Intent
}

sealed interface Effect : ViewEffect {
    data object NavigateBack : Effect
    data class ShowError(val message: String) : Effect
    data class NavigateToProfile(val id: Int) : Effect
}
