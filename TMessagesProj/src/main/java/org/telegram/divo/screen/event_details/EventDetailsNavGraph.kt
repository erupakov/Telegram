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
    var isNdaRequired: Boolean? = null
}

sealed class EventDetailsRoute(val route: String) {
    data object Detail : EventDetailsRoute("detail/{eventId}") {
        const val ROUTE = "detail/{eventId}"
        fun createRoute(eventId: Int) = "detail/$eventId"
    }
    data object Params : EventDetailsRoute("params")
    data class ApplyConfirmation(val eventId: Int) : EventDetailsRoute("apply_confirmation/$eventId") {
        companion object {
            const val ROUTE = "apply_confirmation/{eventId}"
            fun createRoute(eventId: Int) = "apply_confirmation/$eventId"
        }
    }

    object GalleryViewer : EventDetailsRoute("gallery/{sourceType}") {
        const val ROUTE = "gallery/{sourceType}"

        fun createRoute(items: List<GalleryItem>, initialIndex: Int, userId: Int): String {
            GallerySourceHolder.pendingSource = GallerySource.Feed(items, initialIndex, userId, "event_details")
            return "gallery/feed"
        }
    }
}

@Composable
fun EventDetailsNavGraph(
    eventId: Int,
    isOwnProfile: Boolean = false,
    screenName: String = "EventDetails",
    onNavigateToEditEvent: (Int) -> Unit = {},
    onEventDeleted: () -> Unit = {},
    onNavigateBack: () -> Unit,
    onNavControllerReady: (NavController) -> Unit = {},
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
                ?.getInt("eventId", -1)
                ?.takeIf { it != -1 }
                ?: eventId

            val eventDetailsViewModel: EventDetailsViewModel = viewModel(
                key = "event_detail_$currentEventId",
                factory = EventDetailsViewModel.factory(currentEventId, isOwnProfile, screenName)
            )

            EventDetailsScreen(
                eventId = currentEventId,
                isOwnProfile = isOwnProfile,
                viewModel = eventDetailsViewModel,
                onPhotoClicked = { items, index ->
                    val creatorId = eventDetailsViewModel.state.value.eventDetails?.creator?.id ?: -1
                    nav.navigate(EventDetailsRoute.GalleryViewer.createRoute(items, index, creatorId))
                },
                onParamsClicked = {
                    val eventDetails = eventDetailsViewModel.state.value.eventDetails
                    EventParamsHolder.params = eventDetails?.modelAttributes
                    EventParamsHolder.isNdaRequired = eventDetails?.ndaRequired
                    nav.navigate(EventDetailsRoute.Params.route)
                },
                onEditEvent = onNavigateToEditEvent,
                onPrevEventClicked = {
                    nav.navigate(EventDetailsRoute.Detail.createRoute(it))
                },
                onApplyConfirmation = {
                    nav.navigate(EventDetailsRoute.ApplyConfirmation.createRoute(it))
                },
                onEventDeleted = onEventDeleted,
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
                    EventParamsHolder.isNdaRequired = null
                }
            }

            EventParametersScreen(
                params = EventParamsHolder.params,
                isNdaRequired = EventParamsHolder.isNdaRequired,
                onBack = { if (!nav.popBackStack()) onNavigateBack() }
            )
        }
        composable(
            route = EventDetailsRoute.ApplyConfirmation.ROUTE,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val applyEventId = backStackEntry.arguments?.getInt("eventId") ?: return@composable
            org.telegram.divo.screen.apply_confirmation.ApplyConfirmationScreen(
                eventId = applyEventId,
                onBack = { nav.popBackStack() },
                onSuccessDismiss = { nav.popBackStack() }
            )
        }
    }
}