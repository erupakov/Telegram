package org.telegram.divo.screen.city

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

class EventCitiesViewModel(
    private val countryId: Int
) : BaseViewModel<EventCitiesViewModel.State,
        EventCitiesViewModel.Intent,
        EventCitiesViewModel.Action>() {

    data class State(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val query: String = "",
        val cities: List<TLRPC.TL_event_city> = emptyList(),
        val selectedCityId: Int? = null,
    ) : ViewState {

        val filtered: List<TLRPC.TL_event_city>
            get() {
                val q = query.trim()
                if (q.isEmpty()) return cities
                return cities.filter { it.city?.contains(q, ignoreCase = true) == true }
            }

        val selectedCity: TLRPC.TL_event_city?
            get() = selectedCityId?.let { id ->
                cities.firstOrNull { it.city_id == id }
            }
    }

    sealed interface Intent : ViewIntent {
        data object Load : Intent
        data class OnQueryChanged(val value: String) : Intent
        data class OnCityClicked(val city: TLRPC.TL_event_city) : Intent
        data object OnBackClicked : Intent
        data object OnDoneClicked : Intent
    }

    sealed interface Action : ViewAction {
        data object Back : Action
        data class Done(val city: TLRPC.TL_event_city) : Action
    }

    override fun createInitialState(): State = State()

    private val currentAccount = UserConfig.selectedAccount

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.Load -> loadCities()
            is Intent.OnQueryChanged -> setState { copy(query = intent.value) }
            is Intent.OnCityClicked ->
                setState { copy(selectedCityId = intent.city.city_id) }

            Intent.OnBackClicked -> sendAction(Action.Back)
            Intent.OnDoneClicked -> {
                val city = state.value.selectedCity ?: return
                sendAction(Action.Done(city))
            }
        }
    }

    private fun loadCities() {
        setState { copy(isLoading = true, errorMessage = null) }

        val req = TLRPC.TL_event_getCities().apply {
            country_id = countryId
            offset = 0
            limit = 300
        }

        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TL_error? ->
                AndroidUtilities.runOnUIThread {
                    if (error != null) {
                        setState {
                            copy(
                                isLoading = false,
                                errorMessage = error.text ?: "Error"
                            )
                        }
                        return@runOnUIThread
                    }

                    val list = (response as? TLRPC.TL_event_cities)
                        ?.cities
                        .orEmpty()
                        .sortedBy { (it.city ?: "").lowercase() }

                    setState {
                        copy(
                            isLoading = false,
                            cities = list,
                            errorMessage = if (list.isEmpty()) "Empty list" else null
                        )
                    }
                }
            }
        )
    }
}
