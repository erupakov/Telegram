package org.telegram.divo.screen.event_list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.telegram.divo.common.utils.DivoDeeplinkDispatcher
import org.telegram.divo.screen.event_details.EventDetailsNavGraph

sealed class EventRoute(val route: String) {
    data object Events : EventRoute("events")

    data object Detail : EventRoute("detail/{eventId}") {
        fun createRoute(eventId: Int) = "detail/$eventId"
    }
}

@Composable
fun EventsNavGraph(
    onNavigateToCreateEvent: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
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
        composable(EventRoute.Events.route) {
            EventListScreen(
                onNavigateToEventDetails = { nav.navigate(EventRoute.Detail.createRoute(it)) },
                onNavigateToCreateEvent = onNavigateToCreateEvent,
                onNavigateToSearch = onNavigateToSearch
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
                onNavigateBack = { nav.popBackStack() },
            )
        }
    }
}
