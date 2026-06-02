package org.telegram.divo.screen.event_list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.telegram.divo.common.utils.DivoDeeplinkDispatcher
import org.telegram.divo.screen.event_create.CreateEventScreen
import org.telegram.divo.screen.event_create.CreateEventViewModel
import org.telegram.divo.screen.event_create.components.EventPreviewScreen
import org.telegram.divo.screen.event_details.EventDetailsNavGraph

sealed class EventRoute(val route: String) {
    data object Events : EventRoute("events")
    data object Search : EventRoute("search")
    data object CreateEvent : EventRoute("create_event?eventId={eventId}") {
        const val BASE_ROUTE = "create_event"
        fun createRoute(eventId: Int? = null): String =
            if (eventId != null) "$BASE_ROUTE?eventId=$eventId" else BASE_ROUTE
    }
    data object EventPreview : EventRoute("event_preview")

    data object Detail : EventRoute("detail/{eventId}") {
        fun createRoute(eventId: Int) = "detail/$eventId"
    }
    data object ApplyConfirmation : EventRoute("apply_confirmation/{eventId}") {
        fun createRoute(eventId: Int) = "apply_confirmation/$eventId"
    }
}

@Composable
fun EventsNavGraph(
    onNavControllerReady: (NavController) -> Unit,
    onInnerNavControllerReady: (NavController?) -> Unit,
) {
    val nav = rememberNavController()

    LaunchedEffect(nav) {
        onNavControllerReady(nav)
    }

    LaunchedEffect(DivoDeeplinkDispatcher.pendingEventId) {
        val pendingId = DivoDeeplinkDispatcher.pendingEventId
        if (pendingId != null) {
            nav.navigate(EventRoute.Detail.createRoute(pendingId))
            DivoDeeplinkDispatcher.consumePendingEventId()
        }
    }

    NavHost(
        navController = nav,
        startDestination = EventRoute.Events.route
    ) {
        composable(EventRoute.Events.route) { entry ->
            // Observe refresh signal from CreateEvent / EditEvent
            val needsRefresh = entry.savedStateHandle.get<Boolean>("needsRefresh") == true
            if (needsRefresh) {
                entry.savedStateHandle.remove<Boolean>("needsRefresh")
            }
            val viewModel: EventListViewModel = viewModel(
                viewModelStoreOwner = LocalContext.current.findActivity() as ViewModelStoreOwner
            )

            LaunchedEffect(needsRefresh) {
                if (needsRefresh) {
                    viewModel.loadData()
                }
            }

            EventListScreen(
                viewModel = viewModel,
                onNavigateToEventDetails = { nav.navigate(EventRoute.Detail.createRoute(it)) },
                onNavigateToCreateEvent = { nav.navigate(EventRoute.CreateEvent.createRoute()) },
                onNavigateToSearch = { nav.navigate(EventRoute.Search.route) },
                onNavigateToApplyConfirmation = { nav.navigate(EventRoute.ApplyConfirmation.createRoute(it)) }
            )
        }
        composable(EventRoute.Search.route) {
            val viewModel: EventListViewModel = viewModel(
                viewModelStoreOwner = LocalContext.current.findActivity() as ViewModelStoreOwner
            )
            val state = viewModel.state.collectAsState().value
            val snackbarState = remember { org.telegram.divo.common.AppSnackbarHostState() }
            val context = LocalContext.current
            
            LaunchedEffect(viewModel.effect) {
                viewModel.effect.collect { action ->
                    when (action) {
                        is EventListEffect.NavigateToEventDetails -> {
                            EventIntentData.eventId = action.eventId
                            nav.navigate(EventRoute.Detail.createRoute(action.eventId))
                        }
                        is EventListEffect.NavigateToApplyConfirmation -> {
                            nav.navigate(EventRoute.ApplyConfirmation.createRoute(action.eventId))
                        }
                        is EventListEffect.ShowError -> {
                            snackbarState.show(org.telegram.divo.common.SnackbarEvent.Error(action.message))
                        }
                        else -> {}
                    }
                }
            }
            
            EventSearchScreen(
                state = state,
                snackbarState = snackbarState,
                onEventClick = {
                    EventIntentData.eventId = it
                    nav.navigate(EventRoute.Detail.createRoute(it))
                },
                onCtaClick = {
                    viewModel.handleIntent(EventListIntent.OnEventCtaClicked(it))
                },
                onCloseSearch = {
                    viewModel.handleIntent(EventListIntent.OnCloseSearch)
                    nav.popBackStack()
                },
                onSearchQueryChanged = {
                    viewModel.handleIntent(EventListIntent.OnSearchQueryChanged(it))
                },
                onSearchConfirmed = {
                    viewModel.handleIntent(EventListIntent.OnSearchConfirmed)
                },
                onApplyFilters = {
                    viewModel.handleIntent(EventListIntent.OnApplyFilters(it))
                },
                onResetFilters = {
                    viewModel.handleIntent(EventListIntent.OnResetFilters)
                },
                onLoadMore = {
                    viewModel.handleIntent(EventListIntent.OnLoadMore)
                }
            )
        }
        composable(
            route = EventRoute.CreateEvent.route,
            arguments = listOf(navArgument("eventId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { entry ->
            val editingEventId = entry.arguments?.getInt("eventId")?.takeIf { it > 0 }
            CreateEventScreen(
                editingEventId = editingEventId,
                onBack = { nav.popBackStack() },
                onPreviewClicked = {
                    nav.navigate(EventRoute.EventPreview.route)
                },
                onEventPublished = {
                    nav.getBackStackEntry(EventRoute.Events.route)
                        .savedStateHandle["needsRefresh"] = true
                    nav.popBackStack(EventRoute.Events.route, inclusive = false)
                }
            )
        }
        composable(
            route = EventRoute.EventPreview.route
        ) {
            val createEventEntry = remember(it) {
                nav.getBackStackEntry(EventRoute.CreateEvent.BASE_ROUTE)
            }
            val sharedViewModel: CreateEventViewModel = viewModel(createEventEntry)
            EventPreviewScreen(
                viewModel = sharedViewModel,
                onPublish = {
                    nav.getBackStackEntry(EventRoute.Events.route)
                        .savedStateHandle["needsRefresh"] = true
                    nav.popBackStack(EventRoute.Events.route, inclusive = false)
                },
                onBack = { nav.popBackStack() }
            )
        }
        composable(
            route = EventRoute.Detail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId", -1)
                ?.takeIf { it != -1 } ?: return@composable

            EventDetailsNavGraph(
                eventId = eventId,
                onNavControllerReady = { onInnerNavControllerReady(it) },
                onNavigateToEditEvent = { nav.navigate(EventRoute.CreateEvent.createRoute(it)) },
                onEventDeleted = {
                    nav.getBackStackEntry(EventRoute.Events.route).savedStateHandle["needsRefresh"] = true
                    nav.popBackStack()
                },
                onNavigateBack = { nav.popBackStack() },
            )
        }
        composable(
            route = EventRoute.ApplyConfirmation.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId") ?: return@composable
            org.telegram.divo.screen.apply_confirmation.ApplyConfirmationScreen(
                eventId = eventId,
                onSuccessDismiss = { nav.popBackStack() },
                onBack = { nav.popBackStack() }
            )
        }
    }
}
