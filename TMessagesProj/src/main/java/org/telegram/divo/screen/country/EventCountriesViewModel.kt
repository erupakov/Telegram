package org.telegram.divo.screen.country

import org.telegram.divo.base.BaseViewModel
import org.telegram.divo.base.ViewAction
import org.telegram.divo.base.ViewIntent
import org.telegram.divo.base.ViewState
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.RequestDelegate
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.tgnet.TLRPC.TL_error

class EventCountriesViewModel :
    BaseViewModel<EventCountriesViewModel.State, EventCountriesViewModel.Intent, EventCountriesViewModel.Action>() {

    data class State(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val query: String = "",
        val countries: List<TLRPC.TL_event_country> = emptyList(),
        val selectedCountryId: Int? = null,
    ) : ViewState {
        val filtered: List<TLRPC.TL_event_country>
            get() {
                val q = query.trim()
                val src = countries
                if (q.isEmpty()) return src
                return src.filter { it.country?.contains(q, ignoreCase = true) == true }
            }

        val selectedCountry: TLRPC.TL_event_country?
            get() = selectedCountryId?.let { id -> countries.firstOrNull { it.country_id == id } }
    }

    sealed interface Intent : ViewIntent {
        data object Load : Intent
        data class OnQueryChanged(val value: String) : Intent
        data class OnCountryClicked(val country: TLRPC.TL_event_country) : Intent
        data object OnBackClicked : Intent
        data object OnDoneClicked : Intent
    }

    sealed interface Action : ViewAction {
        data object Back : Action
        data class Done(val country: TLRPC.TL_event_country) : Action
    }

    override fun createInitialState(): State = State()

    private val currentAccount = UserConfig.selectedAccount

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.Load -> loadCountries()
            is Intent.OnQueryChanged -> setState { copy(query = intent.value) }
            is Intent.OnCountryClicked -> setState { copy(selectedCountryId = intent.country.country_id) }
            Intent.OnBackClicked -> sendAction(Action.Back)
            Intent.OnDoneClicked -> {
                val selected = state.value.selectedCountry ?: return
                sendAction(Action.Done(selected))
            }
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
            RequestDelegate { response: TLObject?, error: TL_error? ->
                AndroidUtilities.runOnUIThread {
                    if (error != null) {
                        setState { copy(isLoading = false, errorMessage = error.text ?: "Error") }
                        return@runOnUIThread
                    }

                    val list = (response as? TLRPC.TL_event_countries)
                        ?.countries
                        .orEmpty()
                        .sortedBy { (it.country ?: "").lowercase() }

                    setState {
                        copy(
                            isLoading = false,
                            countries = list,
                            errorMessage = if (list.isEmpty()) "Empty list" else null
                        )
                    }
                }
            }
        )
    }
}
