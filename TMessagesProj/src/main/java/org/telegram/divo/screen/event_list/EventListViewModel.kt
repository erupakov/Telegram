package org.telegram.divo.screen.event_list

import org.telegram.divo.base.BaseViewModel
import org.telegram.divo.base.ViewAction
import org.telegram.divo.base.ViewIntent
import org.telegram.divo.base.ViewState
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.RequestDelegate
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.tgnet.TLRPC.TL_error
import kotlin.collections.emptyList

class EventListViewModel :
    BaseViewModel<EventListViewModel.EventListViewState, EventListViewModel.EventListIntent, EventListViewModel.EventListAction>() {

    data class EventListViewState(
        val events: List<TLRPC.TL_event_short> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
    ) : ViewState

    enum class EventCtaType {
        Apply,
        MyEvent,
    }

    sealed class EventListIntent : ViewIntent {
        data object OnSearchClicked : EventListIntent()
        data object OnAddEventClicked : EventListIntent()
        data class OnEventCardClicked(val eventId: Long) : EventListIntent()
        data class OnEventCtaClicked(val eventId: Long) : EventListIntent()
    }

    sealed class EventListAction : ViewAction {
        data object NavigateToSearch : EventListAction()
        data object NavigateToCreateEvent : EventListAction()
        data class NavigateToEventDetails(val eventId: Long) : EventListAction()
    }

    override fun createInitialState(): EventListViewState = EventListViewState(
        events = emptyList(),
    )

    private var currentAccount = UserConfig.selectedAccount
    //TL_event_createEvent
    //TL_event_getCities
    // TL_event_getCountries

    init {
        getEventList()
    }


    fun getEventList(){
        val req = TLRPC.TL_event_getEvents()
        req.offset = 0
        req.limit = 20
        req.filter = null
        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TL_error? ->
                response
                error
                if(response is TLRPC.TL_event_events){
                    setState {
                        copy(
                            events = response.events
                        )
                    }
                }else{
                    response
                }
                error
            },
        )
    }

    fun getEvents(eventId:Long){
        val req = TLRPC.TL_event_getEvent()
        req.event_id = eventId
        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TL_error? ->
                response
                error
                if(response is TLRPC.TL_event_events){
                    response.events
                }else{
                    response
                }
                error

            },
        )
    }


//    TLRPC.TL_event_country
//                            public static class TL_event_country extends TLObject {
//        public static int constructor = 0x6073bcbd;
//
//        public int country_id;
//        public String country;
    //  public ArrayList<TL_event_country> countries = new ArrayList<>();






    fun getData() {
//        val filter = TLRPC.TL_event_filter()
//        filter.search_query = ""
//        filter.location = TLRPC.TL_event_location().apply {
//
//        }
//
//        val req = TLRPC.TL_event_getEvents()
//        req.filter = filter
//        req.offset = 0
//        req.limit = 100
//        req.user_id = 0

//
//        ConnectionsManager.getInstance(currentAccount).sendRequest(
//            req,
//            RequestDelegate { response: TLObject?, error: TL_error? ->
//                response
//                error
//                if(response is TLRPC.TL_event_events){
//                    response.events
//                }else{
//                    response
//                }
//                error
//
//            },
//        )
    }

    fun getCountry(){

//        val filter = TLRPC.TL_event_filter()
//
//        ConnectionsManager.getInstance(currentAccount).sendRequest(
//            req,
//            RequestDelegate { response: TLObject?, error: TL_error? ->
//                response
//                error
//                if(response is TLRPC.TL_event_events){
//                    response.events
//                }else{
//                    response
//                }
//                error
//
//            },
//        )
    }


    override fun handleIntent(intent: EventListIntent) {
        when (intent) {
            EventListIntent.OnAddEventClicked -> sendAction(EventListAction.NavigateToCreateEvent)
            is EventListIntent.OnEventCardClicked -> sendAction(
                EventListAction.NavigateToEventDetails(intent.eventId)
            )
            is EventListIntent.OnEventCtaClicked -> sendAction(
                EventListAction.NavigateToEventDetails(intent.eventId)
            )
            EventListIntent.OnSearchClicked -> sendAction(EventListAction.NavigateToSearch)
        }
    }

}

