package org.telegram.divo.screen.event_create

import org.telegram.divo.base.BaseViewModel
import org.telegram.divo.base.ViewEffect
import org.telegram.divo.base.ViewIntent
import org.telegram.divo.base.ViewState
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.RequestDelegate
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.tgnet.TLRPC.TL_error

class CreateEventViewModel(
) : BaseViewModel<CreateEventViewModel.State,
        CreateEventViewModel.Intent,
        CreateEventViewModel.Effect>() {

    data class State(
        val isLoading: Boolean = false,

        val title: String = "",
        val description: String = "",

        val errorMessage: String? = null,
        val query: String = "",

        val eventTypes: List<TLRPC.TL_event_eventType> = emptyList(),
        val selectedEventType: TLRPC.TL_event_eventType? = null,

        val availableParameters: List<TLRPC.TL_event_availableParameter> = emptyList(),
        val selectedAvailableParameters: HashSet<TLRPC.TL_event_availableParameter> = HashSet(),
        val startDate: String? = null,
        val endDate: String? = null,

        val countries: List<TLRPC.TL_event_country> = emptyList(),
        val selectedCountry:TLRPC.TL_event_country? = null,
        val cities: List<TLRPC.TL_event_city> = emptyList(),
        val selectedCity: TLRPC.TL_event_city? = null

        ) : ViewState {

        val filtered: List<TLRPC.TL_event_city>
            get() {
                val q = query.trim()
                if (q.isEmpty()) return cities
                return cities.filter { it.city?.contains(q, ignoreCase = true) == true }
            }
    }

    sealed interface Intent : ViewIntent {
        data object Load : Intent
        data class OnQueryChanged(val value: String) : Intent

        data class OnCountrySelected(val item: TLRPC.TL_event_country) : Intent
        data class OnCitySelected(val item: TLRPC.TL_event_city) : Intent

        data object OnBackClicked : Intent
        data class OnCreateClicked(
            val title: String,
            val description: String,
            val date: String,
            val time: String,
        ) : Intent
    }

    sealed interface Effect : ViewEffect {
        data object NavigateBack : Effect
    }

    init {
        loadCountries()
    }

    override fun createInitialState(): State = State()

    private fun loadCountries() {
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

    private fun loadCities(countryId:Int) {
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
                        )
                    }
                }
            }
        )
    }

    fun getEventTypes() {
        val req = TLRPC.TL_event_getEventTypes()
        req.offset = 0
        req.limit = 3
        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TL_error? ->
                if (response is TLRPC.TL_event_eventTypes) {
                    response.data
                } else {
                    response
                }
                error
            },
        )
    }

    fun getAvailableParameters() {
        val req = TLRPC.TL_event_getAvailableParameters()
        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TL_error? ->
                response
                error
                if (response is TLRPC.TL_event_availableParameters) {
                    response.data

                } else {
                    response
                }
                error
            },
        )

    }

    private val currentAccount = UserConfig.selectedAccount

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.Load -> {}
            is Intent.OnQueryChanged -> setState { copy(query = intent.value) }

            is Intent.OnCountrySelected -> {
                loadCities(intent.item.country_id)
                setState {
                    copy(
                        selectedCountry = intent.item
                    )
                }
            }
            is Intent.OnCitySelected -> {
                setState {
                    copy(
                        selectedCity = intent.item
                    )
                }
            }

            Intent.OnBackClicked -> sendEffect(Effect.NavigateBack)
            is Intent.OnCreateClicked -> {
                createEvent(
                    title = intent.title,
                    description = intent.description,
                    date = intent.date,
                    time = intent.time
                )
            }
        }
    }

    // event.getEventTypes //
    //event.getAvailableParameters#79aac9ef user_id:long = Vector<event.AvailableParameter>;

    private fun createEvent(
        title: String,
        description: String,
        date: String,
        time: String
    ) {
        val req = TLRPC.TL_event_createEvent()

        req.title = title
        req.description = description
        req.event_date = date
        req.event_time = time
//        val location = TLRPC.TL_event_location()
//        location.city
//        location.country

//        public String title;
//        public String description;
//        public TL_event_eventType event_type;
//        public String event_date;
//        public String event_time;
//        public TL_event_location location;
//        public long cover_photo_id;
//        public ArrayList<String> enabled_parameter_keys = new ArrayList<>();
//
        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TL_error? ->
                response
                error
                if (response is TLRPC.TL_event_event) {
                    sendEffect(Effect.NavigateBack)
                } else {
                    response
                }
                error
            },
        )


    }
}
