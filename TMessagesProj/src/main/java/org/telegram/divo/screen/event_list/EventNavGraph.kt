package org.telegram.divo.screen.event_list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class EventRoute(val route: String) {
    data object Events : EventRoute("events")

    data object Detail : EventRoute("detail")
}

@Composable
fun EventsNavGraph(
    onNavigateToEventDetails: (Long) -> Unit = {},
    onNavigateToCreateEvent: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavControllerReady: (NavController) -> Unit,
    onInnerNavControllerReady: (NavController?) -> Unit,
) {
    val nav = rememberNavController()

    LaunchedEffect(nav) { onNavControllerReady(nav) }

    NavHost(
        navController = nav,
        startDestination = EventRoute.Events.route
    ) {
        composable(EventRoute.Events.route) {
            EventListScreen(
                onNavigateToEventDetails = onNavigateToEventDetails,
                onNavigateToCreateEvent = onNavigateToCreateEvent,
                onNavigateToSearch = onNavigateToSearch
            )
        }
    }
}
