package org.telegram.divo.screen.event_filter

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.telegram.divo.base.BaseViewModel
import org.telegram.divo.base.ViewEffect
import org.telegram.divo.base.ViewIntent
import org.telegram.divo.base.ViewState
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.FileLog
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.RequestDelegate
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC

class EventFilterViewModel(
    private val initialFilter: TLRPC.TL_event_filter? = null,
) : BaseViewModel<EventFilterViewModel.State, EventFilterViewModel.Intent, EventFilterViewModel.Effect>() {

    private val currentAccount = UserConfig.selectedAccount

    // MemberTypeFilter flags:
    // 1  all_members
    // 2  model
    // 4  new_talents
    // 8  booker
    // 16 scout
    enum class MemberTypeOption(val mask: Int, val title: String) {
        ALL(1, "All Members"),
        MODEL(2, "Model"),
        NEW_TALENTS(4, "New Talents"),
        BOOKER(8, "Booker"),
        SCOUT(16, "Scout"),
    }

    data class State(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,

        val searchQuery: String = "",

        val countries: List<TLRPC.TL_event_country> = emptyList(),
        val selectedCountry: TLRPC.TL_event_country? = null,

        val eventTypes: List<TLRPC.TL_event_eventType> = emptyList(),
        val selectedEventType: TLRPC.TL_event_eventType? = null,

        val dateFrom: String? = null,
        val dateTo: String? = null,

        // Multi-select member types
        val memberAll: Boolean = false,
        val memberModel: Boolean = false,
        val memberNewTalents: Boolean = false,
        val memberBooker: Boolean = false,
        val memberScout: Boolean = false,
    ) : ViewState {
        fun hasAnyMemberSelected(): Boolean =
            memberAll || memberModel || memberNewTalents || memberBooker || memberScout

        fun isAllMembersReallyAll(): Boolean =
            memberAll && memberModel && memberNewTalents && memberBooker && memberScout

        fun memberSummary(): String {
            if (!hasAnyMemberSelected()) return ""
            if (isAllMembersReallyAll()) return "All Members"

            val selectedCount = listOf(memberModel, memberNewTalents, memberBooker, memberScout).count { it }
            return if (selectedCount == 0) "All Members" else "All Members • $selectedCount selected"
        }
    }

    sealed interface Intent : ViewIntent {
        data class OnSearchChanged(val value: String) : Intent

        data class OnCountrySelected(val item: TLRPC.TL_event_country?) : Intent
        data class OnEventTypeSelected(val item: TLRPC.TL_event_eventType?) : Intent

        data class OnDateFromChanged(val value: String?) : Intent
        data class OnDateToChanged(val value: String?) : Intent

        // Member type toggles (multi-select)
        data class ToggleAllMembers(val checked: Boolean) : Intent
        data class ToggleMemberModel(val checked: Boolean) : Intent
        data class ToggleMemberNewTalents(val checked: Boolean) : Intent
        data class ToggleMemberBooker(val checked: Boolean) : Intent
        data class ToggleMemberScout(val checked: Boolean) : Intent

        data object OnClearClicked : Intent
        data object OnApplyClicked : Intent
        data object OnBackClicked : Intent
    }

    sealed interface Effect : ViewEffect {
        data object NavigateBack : Effect
        data object Apply : Effect
    }

    override fun createInitialState(): State = State()

    init {
        loadCountries()
        loadEventTypes()
        initialFilter?.let { restoreFromInitial(it) }
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.OnSearchChanged -> setState { copy(searchQuery = intent.value) }

            is Intent.OnCountrySelected -> setState { copy(selectedCountry = intent.item) }
            is Intent.OnEventTypeSelected -> setState { copy(selectedEventType = intent.item) }

            is Intent.OnDateFromChanged -> setState { copy(dateFrom = intent.value) }
            is Intent.OnDateToChanged -> setState { copy(dateTo = intent.value) }

            is Intent.ToggleAllMembers -> {
                if (intent.checked) {
                    // RULE: when All Members clicked -> all becomes selected
                    setState {
                        copy(
                            memberAll = true,
                            memberModel = true,
                            memberNewTalents = true,
                            memberBooker = true,
                            memberScout = true
                        )
                    }
                } else {
                    // allow user to clear all by unchecking All Members
                    setState {
                        copy(
                            memberAll = false,
                            memberModel = false,
                            memberNewTalents = false,
                            memberBooker = false,
                            memberScout = false
                        )
                    }
                }
            }

            is Intent.ToggleMemberModel -> setState {
                val newModel = intent.checked
                val newAny = newModel && memberNewTalents && memberBooker && memberScout
                copy(
                    memberModel = newModel,
                    memberAll = newAny
                )
            }

            is Intent.ToggleMemberNewTalents -> setState {
                val newVal = intent.checked
                val newAny = memberModel && newVal && memberBooker && memberScout
                copy(
                    memberNewTalents = newVal,
                    memberAll = newAny
                )
            }

            is Intent.ToggleMemberBooker -> setState {
                val newVal = intent.checked
                val newAny = memberModel && memberNewTalents && newVal && memberScout
                copy(
                    memberBooker = newVal,
                    memberAll = newAny
                )
            }

            is Intent.ToggleMemberScout -> setState {
                val newVal = intent.checked
                val newAny = memberModel && memberNewTalents && memberBooker && newVal
                copy(
                    memberScout = newVal,
                    memberAll = newAny
                )
            }

            Intent.OnClearClicked -> setState {
                copy(
                    searchQuery = "",
                    selectedCountry = null,
                    selectedEventType = null,
                    dateFrom = null,
                    dateTo = null,
                    memberAll = false,
                    memberModel = false,
                    memberNewTalents = false,
                    memberBooker = false,
                    memberScout = false,
                    errorMessage = null
                )
            }

            Intent.OnApplyClicked -> {
                viewModelScope.launch {
                    val filter = buildFilterFromState(state.value)
                    EventFilterStore.set(filter)
                }
                sendEffect(Effect.Apply)
            }

            Intent.OnBackClicked -> sendEffect(Effect.NavigateBack)
        }
    }

    private fun buildFilterFromState(s: State): TLRPC.TL_event_filter {
        val f = TLRPC.TL_event_filter()

        if (s.searchQuery.isNotBlank()) f.search_query = s.searchQuery

        s.selectedCountry?.let { country ->
            val loc = TLRPC.TL_event_location()
            loc.country = TLRPC.TL_event_country()
            loc.country.country_id = country.country_id
            loc.country.country = country.country
            f.location = loc
        }

        s.selectedEventType?.let { f.event_type = it }

        if (!s.dateFrom.isNullOrBlank()) f.date_from = s.dateFrom
        if (!s.dateTo.isNullOrBlank()) f.date_to = s.dateTo

        f.member_type = buildMemberTypeFilterMulti(s)

        return f
    }

    private fun buildMemberTypeFilterMulti(s: State): TLRPC.TL_event_memberTypeFilter? {
        // if nothing selected -> no member filter
        val any = s.memberAll || s.memberModel || s.memberNewTalents || s.memberBooker || s.memberScout
        if (!any) return null

        // RULE: all_members must be selected when anything else is selected
        val allMembers = s.memberAll || s.memberModel || s.memberNewTalents || s.memberBooker || s.memberScout

        var flags = 0
        val filter = TLRPC.TL_event_memberTypeFilter()

        if (allMembers) {
            flags = flags or 1
            filter.all_members = TLRPC.TL_boolTrue()
        }
        if (s.memberModel) {
            flags = flags or 2
            filter.model = TLRPC.TL_boolTrue()
        }
        if (s.memberNewTalents) {
            flags = flags or 4
            filter.new_talents = TLRPC.TL_boolTrue()
        }
        if (s.memberBooker) {
            flags = flags or 8
            filter.booker = TLRPC.TL_boolTrue()
        }
        if (s.memberScout) {
            flags = flags or 16
            filter.scout = TLRPC.TL_boolTrue()
        }

        filter.flags = flags
        return filter
    }

    private fun restoreFromInitial(f: TLRPC.TL_event_filter) {
        val flags = f.member_type?.flags ?: 0

        setState {
            copy(
                searchQuery = f.search_query ?: "",
                selectedEventType = f.event_type,
                dateFrom = f.date_from,
                dateTo = f.date_to,

                memberAll = (flags and 1) != 0,
                memberModel = (flags and 2) != 0,
                memberNewTalents = (flags and 4) != 0,
                memberBooker = (flags and 8) != 0,
                memberScout = (flags and 16) != 0,
            )
        }
    }

    private fun loadCountries() {
        setState { copy(isLoading = true, errorMessage = null) }

        val req = TLRPC.TL_event_getCountries().apply {
            offset = 0
            limit = 300
        }

        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TLRPC.TL_error? ->
                AndroidUtilities.runOnUIThread {
                    if (error != null) {
                        FileLog.e("getCountries error: ${error.text} code=${error.code}")
                        setState { copy(isLoading = false, errorMessage = error.text ?: "Error") }
                        return@runOnUIThread
                    }

                    val list = (response as? TLRPC.TL_event_countries)
                        ?.countries
                        .orEmpty()
                        .sortedBy { (it.country ?: "").lowercase() }

                    setState { copy(isLoading = false, countries = list, errorMessage = null) }

                    val loc = initialFilter?.location
                    if (loc != null) {
                        val found = list.firstOrNull { it.country_id == loc.country.country_id }
                        if (found != null) setState { copy(selectedCountry = found) }
                    }
                }
            }
        )
    }

    private fun loadEventTypes() {
        val req = TLRPC.TL_event_getEventTypes().apply {
            offset = 0
            limit = 100
        }

        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TLRPC.TL_error? ->
                AndroidUtilities.runOnUIThread {
                    if (error != null) {
                        FileLog.e("getEventTypes error: ${error.text} code=${error.code}")
                        setState { copy(errorMessage = error.text ?: "Error") }
                        return@runOnUIThread
                    }

                    val list = (response as? TLRPC.TL_event_eventTypes)?.data.orEmpty()
                    setState { copy(eventTypes = list, errorMessage = null) }
                }
            }
        )
    }
}
