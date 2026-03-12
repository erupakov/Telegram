package org.telegram.divo.screen.models

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.telegram.divo.screen.gallery.GalleryItem
import org.telegram.divo.screen.gallery.GallerySource
import org.telegram.divo.screen.gallery.GalleryViewerScreen
import org.telegram.divo.screen.profile.ProfileNavGraph

object GallerySourceHolder {
    var pendingSource: GallerySource? = null
}

sealed class ModelsRoute(val route: String) {
    data object Models : ModelsRoute("models")

    data object Profile : ModelsRoute("profile/{userId}") {
        const val ROUTE = "profile/{userId}"
        fun createRoute(userId: Int) = "profile/$userId"
    }

    object GalleryViewer : ModelsRoute("gallery/{sourceType}/{userId}/{initialIndex}") {
        const val ROUTE = "gallery/{sourceType}/{userId}/{initialIndex}"

        fun createRoute(items: List<GalleryItem>, initialIndex: Int): String {
            GallerySourceHolder.pendingSource = GallerySource.Feed(items.drop(1), initialIndex)
            return "gallery/feed/0/$initialIndex"
        }
    }
}

@Composable
fun ModelsNavGraph(
    onNavControllerReady: (NavController) -> Unit,
    onInnerNavControllerReady: (NavController?) -> Unit,
) {
    val nav = rememberNavController()

    LaunchedEffect(nav) { onNavControllerReady(nav) }

    NavHost(
        navController = nav,
        startDestination = ModelsRoute.Models.route
    ) {
        composable(ModelsRoute.Models.route) {
            ModelsHomeScreen(
                onSearch = {},
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
                ?.takeIf { it != -1 } ?: return@composable

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
                navArgument("userId") { type = NavType.IntType },
                navArgument("initialIndex") { type = NavType.IntType },
            )
        ) { backStackEntry ->
            val sourceType = backStackEntry.arguments?.getString("sourceType") ?: return@composable
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val initialIndex = backStackEntry.arguments?.getInt("initialIndex") ?: 0

            val source = when (sourceType) {
                "portfolio" -> GallerySource.Portfolio(userId, initialIndex)
                "video" -> GallerySource.Video(userId, initialIndex)
                "feed" -> GallerySourceHolder.pendingSource
                    ?: return@composable
                else -> return@composable
            }

            GalleryViewerScreen(
                source = source,
                onBack = { nav.popBackStack() },
            )
        }
    }
}
