package org.telegram.divo.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.telegram.divo.screen.profile.ProfileNavGraph
import org.telegram.divo.screen.your_parameters.YourParametersScreen

sealed class SettingsRoute(val route: String) {
    data object Settings : SettingsRoute("settings")

    data object Parameters : SettingsRoute("parameters")

    data object Profile : SettingsRoute("profile/{userId}") {
        const val ROUTE = "profile/{userId}"
        fun createRoute(userId: Int) = "profile/$userId"
    }
}

@Composable
fun SettingsNavGraph(
    navigateToSavedMessages: () -> Unit,
    navigateToNotifications: () -> Unit,
    navigateToPrivacy: () -> Unit,
    navigateToDataStorage: () -> Unit,
    navigateToAppearance: () -> Unit,
    navigateToSetUsername: () -> Unit,
    navigateToLanguage: () -> Unit,
    navigateToLogout: () -> Unit,
    onNavControllerReady: (NavController) -> Unit,
    onInnerNavControllerReady: (NavController?) -> Unit,
    onNavigateToChat: (tgId: Long, tgHash: Long?, tgUsername: String?) -> Unit = { _, _, _ -> },
    onNavigateToCreateChannel: () -> Unit = {},
) {
    val nav = rememberNavController()

    LaunchedEffect(nav) { onNavControllerReady(nav) }

    NavHost(
        navController = nav,
        startDestination = SettingsRoute.Settings.route
    ) {
        composable(SettingsRoute.Settings.route) {
            SettingsScreen(
                navigateToFillParameters = { nav.navigate(SettingsRoute.Parameters.route) },
                navigateToProfile = { nav.navigate(SettingsRoute.Profile.createRoute(it)) },
                navigateToSavedMessages = navigateToSavedMessages,
                navigateToNotifications = navigateToNotifications,
                navigateToPrivacy = navigateToPrivacy,
                navigateToDataStorage = navigateToDataStorage,
                navigateToAppearance = navigateToAppearance,
                navigateToSetUsername = navigateToSetUsername,
                navigateToLanguage = navigateToLanguage,
                navigateToLogout = navigateToLogout
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
                onNavigateToChat = onNavigateToChat,
                onNavigateToCreateChannel = onNavigateToCreateChannel,
                onNavigateBack = { nav.popBackStack() }
            )
        }

        composable(
            route = SettingsRoute.Parameters.route
        ) {
            YourParametersScreen(
                showTopBar = true,
                showTitle = false,
                onSaved = {
                    nav.popBackStack()
                },
                onBack = {
                    nav.popBackStack()
                }
            )
        }
    }
}