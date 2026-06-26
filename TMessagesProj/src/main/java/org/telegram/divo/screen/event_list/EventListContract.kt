package org.telegram.divo.screen.event_list

import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.components.items.ProfileParameter
import org.telegram.divo.entity.Event
import org.telegram.divo.entity.EventType
import org.telegram.divo.screen.add_model.LocalCountry
import org.telegram.divo.screen.search.LocalCity

data class EventSearchFilters(
    val query: String = "",
    val selectedEventTypes: List<EventType> = emptyList(),
    val city: LocalCity? = null,
    val paidOnly: Boolean = false,
    val ageFrom: Int? = null,
    val ageTo: Int? = null,
    val blockParams: List<ProfileParameter> = getDefaultEventBlockParams(),
    val hairLength: ProfileParameter = ProfileParameter(ParametersType.HAIR_LENGTH, ""),
    val hairColor: ProfileParameter = ProfileParameter(ParametersType.HAIR_COLOR, ""),
    val eyeColor: ProfileParameter = ProfileParameter(ParametersType.EYE_COLOR, ""),
    val skinColor: ProfileParameter = ProfileParameter(ParametersType.SKIN_COLOR, ""),
) {
    val activeFiltersCount: Int
        get() {
            var count = 0
            if (selectedEventTypes.isNotEmpty()) count++
            if (city != null) count++
            if (paidOnly) count++
            if (ageFrom != null || ageTo != null) count++
            if (blockParams.any { it.value.isNotBlank() }) count++
            if (hairLength.value.isNotBlank()) count++
            if (hairColor.value.isNotBlank()) count++
            if (eyeColor.value.isNotBlank()) count++
            if (skinColor.value.isNotBlank()) count++
            return count
        }

    val hasActiveFilters: Boolean get() = activeFiltersCount > 0
    val hasAnyFilterOrQuery: Boolean get() = hasActiveFilters || query.isNotBlank()

    val typeIds: List<Int>? get() = selectedEventTypes.map { it.id }.ifEmpty { null }

    fun toActiveFiltersString(): String {
        val parts = mutableListOf<String>()
        if (selectedEventTypes.isNotEmpty()) parts.add("types")
        if (city != null) parts.add("city")
        if (paidOnly) parts.add("paid_only")
        if (ageFrom != null || ageTo != null) parts.add("age")
        if (blockParams.any { it.value.isNotBlank() }) parts.add("parameters")
        if (hairLength.value.isNotBlank()) parts.add("hair_length")
        if (hairColor.value.isNotBlank()) parts.add("hair_color")
        if (eyeColor.value.isNotBlank()) parts.add("eye_color")
        if (skinColor.value.isNotBlank()) parts.add("skin_color")
        return parts.joinToString(",")
    }
}

fun getDefaultEventBlockParams() = listOf(
    ProfileParameter(ParametersType.HEIGHT, ""),
    ProfileParameter(ParametersType.WAIST, ""),
    ProfileParameter(ParametersType.HIPS, ""),
    ProfileParameter(ParametersType.SHOE_SIZE, ""),
    ProfileParameter(ParametersType.BREAST_SIZE, "")
)

data class EventListViewState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,

    val isModel: Boolean = false,
    val isAgency: Boolean = false,
    val selectedTab: Int = 0,
    val isRoleLoading: Boolean = false,
    val filterCount: Int? = null,

    // Search
    val isSearchMode: Boolean = false,
    val searchFilters: EventSearchFilters = EventSearchFilters(),
    val totalSearchResults: Int = 0,

    // Event types for filter
    val eventTypes: List<EventType> = emptyList(),
    
    // Appearance dictionaries for filters
    val hairLengthOptions: List<org.telegram.divo.entity.AppearanceItem> = emptyList(),
    val hairColorOptions: List<org.telegram.divo.entity.AppearanceItem> = emptyList(),
    val eyeColorOptions: List<org.telegram.divo.entity.AppearanceItem> = emptyList(),
    val skinColorOptions: List<org.telegram.divo.entity.AppearanceItem> = emptyList(),

    // Locations
    val allCountries: List<LocalCountry> = emptyList(),
    val allCities: List<LocalCity> = emptyList(),
) : ViewState

enum class EventCtaType {
    Apply,
    MyEvent,
}

sealed class EventListIntent : ViewIntent {
    data object OnSearchClicked : EventListIntent()
    data object OnAddEventClicked : EventListIntent()
    data class OnEventCardClicked(val eventId: Int) : EventListIntent()
    data class OnEventCtaClicked(val eventId: Int) : EventListIntent()
    data class ConfirmWithdraw(val eventId: Int) : EventListIntent()
    data object OnLoad : EventListIntent()
    data object OnLoadMore : EventListIntent()
    data class OnTabSelected(val tabIndex: Int) : EventListIntent()

    // Search intents
    data object OnOpenSearch : EventListIntent()
    data object OnCloseSearch : EventListIntent()
    data class OnSearchQueryChanged(val query: String) : EventListIntent()
    data object OnSearchConfirmed : EventListIntent()
    data class OnApplyFilters(val filters: EventSearchFilters) : EventListIntent()
    data object OnResetFilters : EventListIntent()
}

sealed class EventListEffect : ViewEffect {
    data object NavigateToSearch : EventListEffect()
    data object NavigateToCreateEvent : EventListEffect()
    data class NavigateToEventDetails(val eventId: Int) : EventListEffect()
    data class NavigateToApplyConfirmation(val eventId: Int) : EventListEffect()
    data class ShowWithdrawConfirmation(val eventId: Int) : EventListEffect()
    data class ShowError(val message: String) : EventListEffect()
}