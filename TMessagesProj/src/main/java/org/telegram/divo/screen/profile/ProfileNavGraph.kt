package org.telegram.divo.screen.profile

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.telegram.divo.screen.photo.PhotoViewerScreen

sealed class ProfileRoute(val route: String) {
    data object Edit : ProfileRoute("edit")
    data object EditLinks : ProfileRoute("edit_links")
    data object WorkHistory : ProfileRoute("work_history")
    data object Profile : ProfileRoute("profile")

    data object OtherProfile : ProfileRoute("profile/{userId}") {
        const val ROUTE = "profile/{userId}"
        fun createRoute(userId: Int) = "profile/$userId"
    }

    object PhotoViewer : ProfileRoute("photo/{url}") {
        const val ROUTE = "photo/{url}"
        fun createRoute(url: String) = "photo/${Uri.encode(url)}"
    }
}

@Composable
fun ProfileNavGraph(
    initialUserId: Int,
    initialIsOwnProfile: Boolean,
    onEditBackgroundClicked: () -> Unit,
    onEditClicked: () -> Unit,
    onEditLinksClicked: () -> Unit,
    showWorkHistory: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavControllerReady: (NavController) -> Unit,
) {
    val nav = rememberNavController()

    LaunchedEffect(nav) {
        onNavControllerReady(nav)
    }

    NavHost(
        navController = nav,
        startDestination = ProfileRoute.Profile.route
    ) {
        composable(ProfileRoute.Profile.route) {
            ProfileScreen(
                userId = initialUserId,
                isOwnProfile = initialIsOwnProfile,
                onEditClicked = onEditClicked,
                onEditLinksClicked = onEditLinksClicked,
                showWorkHistory = showWorkHistory,
                onPhotoClicked = { photoUrl ->
                    nav.navigate(ProfileRoute.PhotoViewer.createRoute(photoUrl))
                },
                onProfileClicked = { anotherUserId ->
                    nav.navigate(ProfileRoute.OtherProfile.createRoute(anotherUserId))
                },
                onEditBackgroundClicked = onEditBackgroundClicked,
                onNavigateBack = { if (!nav.popBackStack()) onNavigateBack() }
            )
        }

        composable(
            route = ProfileRoute.OtherProfile.ROUTE,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1

            ProfileScreen(
                userId = userId,
                isOwnProfile = false,
                onEditClicked = onEditClicked,
                onEditLinksClicked = onEditLinksClicked,
                showWorkHistory = showWorkHistory,
                onPhotoClicked = { photoUrl ->
                    nav.navigate(ProfileRoute.PhotoViewer.createRoute(photoUrl))
                },
                onProfileClicked = { anotherUserId ->
                    //nav.navigate(ProfileRoute.OtherProfile.createRoute(anotherUserId))
                },
                onEditBackgroundClicked = onEditBackgroundClicked,
                onNavigateBack = { if (!nav.popBackStack()) onNavigateBack() }
            )
        }

        composable(
            route = ProfileRoute.PhotoViewer.ROUTE,
            arguments = listOf(navArgument("url") { type = NavType.StringType })
        ) { backStackEntry ->
            PhotoViewerScreen(
                url = Uri.decode(backStackEntry.arguments?.getString("url") ?: ""),
                onBack = { nav.popBackStack() }
            )
        }
    }
}
