package org.telegram.divo.screen.event_list

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.telegram.divo.base.BaseViewModel
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.RequestDelegate
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.tgnet.TLRPC.TL_auth_requestFirebaseSms
import org.telegram.tgnet.TLRPC.TL_boolTrue
import org.telegram.tgnet.TLRPC.TL_error

class EventListViewModel :
    BaseViewModel<EventListViewState, EventListIntent, EventListAction>() {

    override fun createInitialState(): EventListViewState = EventListViewState(
        events = seedEvents,
    )

    private var currentAccount = UserConfig.selectedAccount

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

    private val seedEvents: List<EventListItem> = listOf(
        EventListItem(
            id = "fashion-model-event-1",
            title = "Fashion Model Event",
            bannerUrl = "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fdivo_event_1.png?alt=media",
            hostHandle = "@nyfw",
            hostAvatarUrl = "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fdivo_avatar_nyfw.png?alt=media",
            dateLocationText = "May 27 · 5:00 PM · 🇺🇸 New York",
            durationText = "4d : 4h : 0m",
            ctaType = EventCtaType.MyEvent,
        ),
        EventListItem(
            id = "fashion-model-event-2",
            title = "Fashion Model Event",
            bannerUrl = "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fdivo_event_2.png?alt=media",
            hostHandle = "@nyfw",
            hostAvatarUrl = "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fdivo_avatar_nyfw.png?alt=media",
            dateLocationText = "May 27 · 5:00 PM · 🇺🇸 New York",
            durationText = "4d : 4h : 0m",
            ctaType = EventCtaType.Apply,
        ),
        EventListItem(
            id = "fashion-model-event-3",
            title = "Fashion Model Event",
            bannerUrl = "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fdivo_event_3.png?alt=media",
            hostHandle = "@nyfw",
            hostAvatarUrl = "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fdivo_avatar_nyfw.png?alt=media",
            dateLocationText = "May 27 · 5:00 PM · 🇺🇸 New York",
            durationText = "4d : 4h : 0m",
            ctaType = EventCtaType.Apply,
        ),
        EventListItem(
            id = "fashion-model-event-4",
            title = "Fashion Model Event",
            bannerUrl = "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fdivo_event_4.png?alt=media",
            hostHandle = "@nyfw",
            hostAvatarUrl = "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fdivo_avatar_nyfw.png?alt=media",
            dateLocationText = "May 27 · 5:00 PM · 🇺🇸 New York",
            durationText = "4d : 4h : 0m",
            ctaType = EventCtaType.Apply,
        ),
        EventListItem(
            id = "fashion-model-event-5",
            title = "Fashion Model Event",
            bannerUrl = "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fdivo_event_1.png?alt=media",
            hostHandle = "@nyfw",
            hostAvatarUrl = "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fdivo_avatar_nyfw.png?alt=media",
            dateLocationText = "May 27 · 5:00 PM · 🇺🇸 New York",
            durationText = "4d : 4h : 0m",
            ctaType = EventCtaType.Apply,
        ),
        EventListItem(
            id = "fashion-model-event-6",
            title = "Fashion Model Event",
            bannerUrl = "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fdivo_event_2.png?alt=media",
            hostHandle = "@nyfw",
            hostAvatarUrl = "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fdivo_avatar_nyfw.png?alt=media",
            dateLocationText = "May 27 · 5:00 PM · 🇺🇸 New York",
            durationText = "4d : 4h : 0m",
            ctaType = EventCtaType.Apply,
        ),
    )
}

