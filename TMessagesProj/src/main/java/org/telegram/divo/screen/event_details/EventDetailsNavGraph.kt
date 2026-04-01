package org.telegram.divo.screen.event_details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.telegram.divo.entity.EventModelAttributes
import org.telegram.divo.screen.event_details.components.EventParametersScreen
import org.telegram.divo.screen.gallery.GalleryItem
import org.telegram.divo.screen.gallery.GallerySource
import org.telegram.divo.screen.gallery.GallerySourceHolder
import org.telegram.divo.screen.gallery.GalleryViewerScreen

object EventParamsHolder {
    var params: EventModelAttributes? = null
}

sealed class EventDetailsRoute(val route: String) {
    data object Detail : EventDetailsRoute("detail/{eventId}") {
        const val ROUTE = "detail/{eventId}"
        fun createRoute(eventId: Int) = "detail/$eventId"
    }
    data object Params : EventDetailsRoute("params")

    object GalleryViewer : EventDetailsRoute("gallery/{sourceType}") {
        const val ROUTE = "gallery/{sourceType}"

        fun createRoute(items: List<GalleryItem>, initialIndex: Int): String {
            GallerySourceHolder.pendingSource = GallerySource.Feed(items, initialIndex)
            return "gallery/feed"
        }
    }
}

@Composable
fun EventDetailsNavGraph(
    eventId: Int,
    isOwnProfile: Boolean = false,
    onNavControllerReady: (NavController) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val nav = rememberNavController()

    LaunchedEffect(nav) { onNavControllerReady(nav) }

    NavHost(
        navController = nav,
        startDestination = EventDetailsRoute.Detail.createRoute(eventId)
    ) {
        composable(
            route = EventDetailsRoute.Detail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val currentEventId = backStackEntry.arguments
                ?.getInt("userId", -1)
                ?.takeIf { it != -1 }
                ?: eventId

            val eventDetailsViewModel: EventDetailsViewModel = viewModel(
                key = "event_detail_$currentEventId",
                factory = EventDetailsViewModel.factory(eventId, isOwnProfile)
            )

            EventDetailsScreen(
                eventId = currentEventId,
                isOwnProfile = isOwnProfile,
                viewModel = eventDetailsViewModel,
                onPhotoClicked = { items, index ->
                    nav.navigate(EventDetailsRoute.GalleryViewer.createRoute(items, index))
                },
                onParamsClicked = {
                    EventParamsHolder.params = eventDetailsViewModel.state.value.eventDetails?.modelAttributes
                    nav.navigate(EventDetailsRoute.Params.route)
                },
                onPrevEventClicked = {
                    nav.navigate(EventDetailsRoute.Detail.createRoute(it))
                },
                onBack = { if (!nav.popBackStack()) onNavigateBack() }
            )
        }
        composable(
            route = EventDetailsRoute.GalleryViewer.ROUTE,
            arguments = listOf(
                navArgument("sourceType") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val sourceType = backStackEntry.arguments?.getString("sourceType") ?: return@composable
            val source = when (sourceType) {
                "feed" -> GallerySourceHolder.pendingSource
                    ?: return@composable
                else -> return@composable
            }

            DisposableEffect(Unit) {
                onDispose {
                    GallerySourceHolder.pendingSource = null
                }
            }

            GalleryViewerScreen(
                source = source,
                onBack = { if (!nav.popBackStack()) onNavigateBack() },
            )
        }
        composable(
            route = EventDetailsRoute.Params.route
        ) {
            DisposableEffect(Unit) {
                onDispose {
                    EventParamsHolder.params = null
                }
            }

            EventParametersScreen(
                params = EventParamsHolder.params,
                onBack = { if (!nav.popBackStack()) onNavigateBack() }
            )
        }
    }
}