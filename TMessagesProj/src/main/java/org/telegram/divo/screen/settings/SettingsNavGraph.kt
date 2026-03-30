package org.telegram.divo.screen.settings

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.telegram.divo.screen.profile.ProfileNavGraph

sealed class SettingsRoute(val route: String) {
    data object Settings : SettingsRoute("settings")

    data object Profile : SettingsRoute("profile/{userId}") {
        const val ROUTE = "profile/{userId}"
        fun createRoute(userId: Int) = "profile/$userId"
    }
}

@Composable
fun SettingsNavGraph(
    navigateToFillParameters: () -> Unit,
    navigateToSavedMessages: () -> Unit,
    navigateToStickers: () -> Unit,
    navigateToNotifications: () -> Unit,
    navigateToPrivacy: () -> Unit,
    navigateToDataStorage: () -> Unit,
    navigateToAppearance: () -> Unit,
    navigateToSetUsername: () -> Unit,
    onNavControllerReady: (NavController) -> Unit,
    onInnerNavControllerReady: (NavController?) -> Unit,
) {
    val nav = rememberNavController()

    LaunchedEffect(nav) { onNavControllerReady(nav) }

    NavHost(
        navController = nav,
        startDestination = SettingsRoute.Settings.route
    ) {
        composable(SettingsRoute.Settings.route) {
            SettingsScreen(
                navigateToFillParameters = navigateToFillParameters,
                navigateToProfile = { nav.navigate(SettingsRoute.Profile.createRoute(it)) },
                navigateToSavedMessages = navigateToSavedMessages,
                navigateToStickers = navigateToStickers,
                navigateToNotifications = navigateToNotifications,
                navigateToPrivacy = navigateToPrivacy,
                navigateToDataStorage = navigateToDataStorage,
                navigateToAppearance = navigateToAppearance,
                navigateToSetUsername = navigateToSetUsername
            )
        }

        composable(
            route = SettingsRoute.Profile.ROUTE,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1

            ProfileNavGraph(
                userId = userId,
                isOwnProfile = true,
                onNavControllerReady = { onInnerNavControllerReady(it) },
                onNavigateBack = { nav.popBackStack() }
            )
        }
    }
}