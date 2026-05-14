package org.telegram.divo.screen.reg_select_role

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.telegram.divo.screen.reg_form.RegFormsScreen
import org.telegram.divo.screen.reg_select_role.components.QuizResultScreen
import org.telegram.divo.screen.reg_select_role.components.QuizScreen

sealed class Screen(val route: String) {
    data object RoleSelection : Screen("role_selection")
    data object Quiz : Screen("quiz/{intent}") {
        fun createRoute(intent: UserIntent) = "quiz/${intent.name}"
    }
    data object QuizResult : Screen("quiz_result/{subRole}") {
        fun createRoute(subRole: SubRole) = "quiz_result/${subRole.name}"
    }
    data object RegForms : Screen("reg_forms")
}

@Composable
fun RoleNavGraph(
    onNavControllerReady: (NavController) -> Unit,
    onBackToPhone: () -> Unit,
    onFinishedFlow: () -> Unit = {}
) {
    val nav = rememberNavController()
    val viewModel: RoleSelectionViewModel = viewModel()

    LaunchedEffect(nav) {
        onNavControllerReady(nav)
    }

    val startDestination: Screen = Screen.RoleSelection

    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = nav,
        startDestination = startDestination.route
    ) {

        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                viewModel = viewModel,
                onContinue = { intent ->
                    when (intent) {
                        UserIntent.FAN -> nav.navigate(Screen.QuizResult.createRoute(SubRole.FAN))
                        UserIntent.GET_HIRED, UserIntent.LOOKING_FOR_TALENT -> nav.navigate(Screen.Quiz.createRoute(intent))
                    }
                },
                onBack = onBackToPhone
            )
        }

        composable(
            route = Screen.Quiz.route,
            arguments = listOf(
                navArgument("intent") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val intent = backStackEntry.arguments
                ?.getString("intent")
                ?.let { UserIntent.valueOf(it) }
                ?: return@composable

            QuizScreen(
                viewModel = viewModel,
                intent = intent,
                onComplete = { subRole ->
                    nav.navigate(Screen.QuizResult.createRoute(subRole))
                },
                onBack = { nav.popBackStack() }
            )
        }

        composable(
            route = Screen.QuizResult.route,
            arguments = listOf(
                navArgument("subRole") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val subRole = backStackEntry.arguments
                ?.getString("subRole")
                ?.let { SubRole.valueOf(it) }
                ?: return@composable

            QuizResultScreen(
                viewModel = viewModel,
                subRole = subRole,
                onContinue = { nav.navigate(Screen.RegForms.route) },
                onBack = { nav.popBackStack() },
            )
        }

        composable(
            route = Screen.RegForms.route
        ) {
            RegFormsScreen()
        }
    }
}
