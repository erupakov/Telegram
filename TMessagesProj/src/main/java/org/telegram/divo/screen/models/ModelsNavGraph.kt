package org.telegram.divo.screen.models

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.telegram.divo.common.utils.DivoDeeplinkDispatcher
import org.telegram.divo.screen.face_search.FaceSearchScreen
import org.telegram.divo.screen.face_search_history.FaceSearchHistoryScreen
import org.telegram.divo.screen.gallery.GalleryItem
import org.telegram.divo.screen.gallery.GallerySource
import org.telegram.divo.screen.gallery.GallerySourceHolder
import org.telegram.divo.screen.gallery.GalleryViewerScreen
import org.telegram.divo.screen.profile.ProfileNavGraph
import org.telegram.divo.screen.search.SearchScreen
import org.telegram.divo.screen.similar_profiles.SimilarProfilesScreen

sealed class ModelsRoute(val route: String) {
    data object Models : ModelsRoute("models")
    data object Search : ModelsRoute("feed_search")

    data object Profile : ModelsRoute("profile/{userId}") {
        const val ROUTE = "profile/{userId}"
        fun createRoute(userId: Int) = "profile/$userId"
    }

    data object FaceSearch : ModelsRoute("face_search/{uri}") {
        fun createRoute(uri: String) = "face_search/${Uri.encode(uri)}"
    }

    data object FaceSearchHistory : ModelsRoute("face_search_history")

    data object SimilarProfiles : ModelsRoute("similar_profiles/{uri}?fx={fx}&fy={fy}&filters={filters}") {
        fun createRoute(
            uri: String,
            fx: Float? = null,
            fy: Float? = null,
            filtersJson: String? = null
        ): String {
            val base = "similar_profiles/${Uri.encode(uri)}"
            val query = buildList {
                if (fx != null && fy != null) {
                    add("fx=$fx")
                    add("fy=$fy")
                }
                if (!filtersJson.isNullOrBlank()) {
                    add("filters=${Uri.encode(filtersJson)}")
                }
            }
            return if (query.isEmpty()) base else "$base?${query.joinToString("&")}"
        }
    }

    object GalleryViewer : ModelsRoute("gallery/{sourceType}") {
        const val ROUTE = "gallery/{sourceType}"

        fun createRoute(items: List<GalleryItem>, initialIndex: Int): String {
            GallerySourceHolder.pendingSource = GallerySource.Feed(items.drop(1), initialIndex)
            return "gallery/feed"
        }
    }
}

@Composable
fun ModelsNavGraph(
    onNavControllerReady: (NavController) -> Unit,
    onInnerNavControllerReady: (NavController?) -> Unit,
) {
    val nav = rememberNavController()

    LaunchedEffect(nav) {
        onNavControllerReady(nav)
    }

    LaunchedEffect(DivoDeeplinkDispatcher.pendingProfileId) {
        val pendingId = DivoDeeplinkDispatcher.pendingProfileId
        if (pendingId != null) {
            nav.navigate(ModelsRoute.Profile.createRoute(pendingId))
            DivoDeeplinkDispatcher.consumePendingProfileId()
        }
    }

    NavHost(
        navController = nav,
        startDestination = ModelsRoute.Models.route
    ) {
        composable(ModelsRoute.Models.route) {
            ModelsHomeScreen(
                onClick = { userId ->
                    nav.navigate(ModelsRoute.Profile.createRoute(userId))
                },
                onPhotoClicked = { items, index ->
                    nav.navigate(ModelsRoute.GalleryViewer.createRoute(items, index))
                }
            )
        }

        composable(
            route = ModelsRoute.Profile.ROUTE,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId", -1)
                ?.takeIf { it != -1 } ?: -1

            ProfileNavGraph(
                userId = userId,
                onNavControllerReady = { onInnerNavControllerReady(it) },
                onNavigateBack = { nav.popBackStack() }
            )
        }

        composable(
            route = ModelsRoute.GalleryViewer.ROUTE,
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
                onBack = { nav.popBackStack() },
            )
        }

        composable(
            route = ModelsRoute.Search.route,
        ) {
            SearchScreen(
                onPhotoSelected = { nav.navigate(ModelsRoute.FaceSearch.createRoute(it)) },
                onProfileClicked = { nav.navigate(ModelsRoute.Profile.createRoute(it)) },
                onNavigateToFaceSearchHistory = { nav.navigate(ModelsRoute.FaceSearchHistory.route) },
                onNavigateToSimilarProfiles = { uri, filtersJson ->
                    nav.navigate(ModelsRoute.SimilarProfiles.createRoute(uri, filtersJson = filtersJson))
                },
                onBack = { nav.popBackStack() }
            )
        }

        composable(
            route = ModelsRoute.FaceSearch.route,
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        ) { backStackEntry ->
            val uri = Uri.decode(backStackEntry.arguments?.getString("uri")).orEmpty()
            FaceSearchScreen(
                uri = uri,
                onNavigateSimilarProfiles = { url, fx, fy ->
                    nav.navigate(ModelsRoute.SimilarProfiles.createRoute(url, fx, fy))
                },
                onNavigateToSearch = {  },
                onBack = { nav.popBackStack() }
            )
        }

        composable(
            route = ModelsRoute.SimilarProfiles.route,
            arguments = listOf(
                navArgument("uri") { type = NavType.StringType },
                navArgument("fx") { type = NavType.StringType; nullable = true },
                navArgument("fy") { type = NavType.StringType; nullable = true },
                navArgument("filters") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val uri = Uri.decode(backStackEntry.arguments?.getString("uri")).orEmpty()
            val fx = backStackEntry.arguments?.getString("fx")?.toFloatOrNull()
            val fy = backStackEntry.arguments?.getString("fy")?.toFloatOrNull()
            val filtersJson = backStackEntry.arguments?.getString("filters")?.let { Uri.decode(it) }

            SimilarProfilesScreen(
                url = uri,
                initialFiltersJson = filtersJson,
                fx = fx,
                fy = fy,
                onProfileClicked = { nav.navigate(ModelsRoute.Profile.createRoute(it)) },
                onBack = { nav.popBackStack() }
            )
        }

        composable(
            route = ModelsRoute.FaceSearchHistory.route
        ) {
            FaceSearchHistoryScreen(
                onSimilarityProfileClicked = { uri, filtersJson ->
                    nav.navigate(ModelsRoute.SimilarProfiles.createRoute(uri, filtersJson = filtersJson))
                },
                onBack = { nav.popBackStack() }
            )
        }
    }
}
