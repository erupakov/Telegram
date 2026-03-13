package org.telegram.divo.screen.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.telegram.divo.screen.edit_my_profile.EditMyProfileScreen
import org.telegram.divo.screen.gallery.GallerySource
import org.telegram.divo.screen.gallery.GalleryViewerScreen
import org.telegram.divo.screen.profile_social_links.ProfileSocialLinksScreen
import org.telegram.divo.screen.work_create_edit.CreateWorkHistoryScreen
import org.telegram.divo.screen.work_history.WorkHistoryScreen

sealed class ProfileRoute(val route: String) {
    data object Edit : ProfileRoute("profile_edit")
    data object EditLinks : ProfileRoute("profile_edit_links")
    data object WorkHistory : ProfileRoute("profile_work_history")
    data object CreateWorkHistory : ProfileRoute("create_work_history")

    data object Profile : ProfileRoute("profile/{userId}") {
        const val ROUTE = "profile/{userId}"
        fun createRoute(userId: Int) = "profile/$userId"
    }

    object Gallery : ProfileRoute("gallery/{sourceType}/{userId}/{initialIndex}") {
        const val ROUTE = "gallery/{sourceType}/{userId}/{initialIndex}"

        fun portfolio(userId: Int, initialIndex: Int) =
            "gallery/portfolio/$userId/$initialIndex"

        fun video(userId: Int, initialIndex: Int) =
            "gallery/video/$userId/$initialIndex"
    }
}

@Composable
fun ProfileNavGraph(
    userId: Int,
    isOwnProfile: Boolean = false,
    onNavControllerReady: (NavController) -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    val nav = rememberNavController()

    LaunchedEffect(nav) { onNavControllerReady(nav) }

    NavHost(
        navController = nav,
        startDestination = ProfileRoute.Profile.createRoute(userId)
    ) {
        composable(
            route = ProfileRoute.Profile.ROUTE,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val currentUserId = backStackEntry.arguments
                ?.getInt("userId", -1)
                ?.takeIf { it != -1 }
                ?: userId

            val profileViewModel: ProfileViewModel = viewModel(key = "profile_$currentUserId")
            val uiState = profileViewModel.state.collectAsState().value

            ProfileScreen(
                viewModel = profileViewModel,
                userId = currentUserId,
                isOwnProfile = isOwnProfile,
                onEditClicked = { nav.navigate(ProfileRoute.Edit.route) },
                onEditLinksClicked = { nav.navigate(ProfileRoute.EditLinks.route) },
                onNavigateBack = { if (!nav.popBackStack()) onNavigateBack() },
                showWorkHistory = { nav.navigate(ProfileRoute.WorkHistory.route) },
                onGalleryClicked = { url, isVideo ->
                    if (isVideo) {
                        val index = uiState.videoItems
                            .indexOfFirst { it.files.any { f -> f.isVideo && f.fullUrl == url } }
                            .coerceAtLeast(0)
                        nav.navigate(ProfileRoute.Gallery.video(currentUserId, index))
                    } else {
                        val index = uiState.userGalleryItems
                            .indexOfFirst { it.photoUrl == url }
                            .coerceAtLeast(0)
                        nav.navigate(ProfileRoute.Gallery.portfolio(currentUserId, index))
                    }
                },
                onProfileClicked = { anotherUserId ->
                    nav.navigate(ProfileRoute.Profile.createRoute(anotherUserId))
                },
            )
        }

        composable(ProfileRoute.Edit.route) {
            EditMyProfileScreen(
                onCloseScreen = { if (!nav.popBackStack()) onNavigateBack() }
            )
        }

        composable(ProfileRoute.EditLinks.route) {
            ProfileSocialLinksScreen(
                onCloseScreen = { if (!nav.popBackStack()) onNavigateBack() }
            )
        }

        composable(ProfileRoute.WorkHistory.route) {
            WorkHistoryScreen(
                isOwnProfile = isOwnProfile,
                onCreateClicked = {
                    nav.navigate(ProfileRoute.CreateWorkHistory.route)
                },
                onBack = { if (!nav.popBackStack()) onNavigateBack() }
            )
        }

        composable(ProfileRoute.CreateWorkHistory.route) {
            CreateWorkHistoryScreen(
                onBack = { if (!nav.popBackStack()) onNavigateBack() }
            )
        }

        composable(
            route = ProfileRoute.Gallery.ROUTE,
            arguments = listOf(
                navArgument("sourceType") { type = NavType.StringType },
                navArgument("userId") { type = NavType.IntType },
                navArgument("initialIndex") { type = NavType.IntType },
            )
        ) { backStackEntry ->
            val sourceType = backStackEntry.arguments?.getString("sourceType") ?: return@composable
            val sourceUserId = backStackEntry.arguments?.getInt("userId") ?: return@composable
            val initialIndex = backStackEntry.arguments?.getInt("initialIndex") ?: 0

            val source = when (sourceType) {
                "portfolio" -> GallerySource.Portfolio(sourceUserId, initialIndex)
                "video" -> GallerySource.Video(sourceUserId, initialIndex)
                else -> return@composable
            }

            GalleryViewerScreen(
                source = source,
                onBack = { if (!nav.popBackStack()) onNavigateBack() },
            )
        }
    }
}