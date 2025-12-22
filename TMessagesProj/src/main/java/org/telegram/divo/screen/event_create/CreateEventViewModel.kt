package org.telegram.divo.screen.event_create

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

class CreateEventViewModel(
) : BaseViewModel<CreateEventViewModel.State,
        CreateEventViewModel.Intent,
        CreateEventViewModel.Action>() {

    data class State(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val query: String = "",
        val cities: List<TLRPC.TL_event_city> = emptyList(),
        val selectedCityId: Int? = null,

        val eventTypes:List<TLRPC.TL_event_eventType> = emptyList(),
        val selectedEventType: TLRPC.TL_event_eventType? = null,

        val availableParameters:List<TLRPC.TL_event_availableParameter> = emptyList(),
        val selectedAvailableParameters: HashSet<TLRPC.TL_event_availableParameter> = HashSet(),

        val startDate:String? = null,
        val endDate:String? = null,

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


    init {
        getEventTypes()
    }


    fun getEventTypes(){
        val req = TLRPC.TL_event_getEventTypes()
        req.offset = 0
        req.limit = 100
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

    fun getAvailableParameters(){
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
            is Intent.OnCityClicked ->
                setState { copy(selectedCityId = intent.city.city_id) }

            Intent.OnBackClicked -> sendAction(Action.Back)
            Intent.OnDoneClicked -> {
                createEvent()
            }
        }
    }

    // event.getEventTypes //
    //event.getAvailableParameters#79aac9ef user_id:long = Vector<event.AvailableParameter>;

    private fun createEvent() {
        val req = TLRPC.TL_event_createEvent()

        req.title = "Android event 1"
        req.description = "Android event 1 description"
        req.event_date = "2026-03-16T00:00:00Z"
        req.event_time = "17:11:00"


//          public String title;
//        public String description;
//        public TL_event_eventType event_type;
//        public String event_date;
//        public String event_time;
//        public TL_event_location location;
//        public long cover_photo_id;
//        public ArrayList<String> enabled_parameter_keys = new ArrayList<>();


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
                    response
                } else {
                    response
                }
                error
            },
        )


    }
}
