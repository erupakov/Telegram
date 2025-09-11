package org.telegram.divo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.telegram.divo.screen.onboarding.OnboardingHost
import org.telegram.divo.screen.reg_new_talent.ApplyNewTalentScreen
import org.telegram.divo.screen.reg_professional_model.ApplyProfessionalModelScreen
import org.telegram.divo.screen.reg_agency.ApplyAgenciesBrandsScreen
import org.telegram.divo.screen.reg_select_role.Role
import org.telegram.divo.screen.reg_select_role.RoleOption
import org.telegram.divo.screen.reg_select_role.RoleSelectionScreen
import org.telegram.messenger.R

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object RoleSelection : Screen("role_selection")
    data object ApplyNewTalent : Screen("apply_new_talent")
    data object ApplyProfessionalModel : Screen("apply_professional_model")
    data object ApplyAgenciesBrands : Screen("apply_agencies_brands")
}

class DivoPreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge + dark system bars
     /*   WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
*/
        val start = parseStartScreen(intent.getStringExtra(EXTRA_START_ROUTE))

        setContent {
            DivoPreviewTheme {
                // Solid dark background for the whole flow
                Box(Modifier.fillMaxSize().background(Color(0xFF222222))) {
                    DivoNavGraph(
                        startDestination = start,
                        onFinishedFlow = { finish() }
                    )
                }
            }
        }
    }

    private fun parseStartScreen(route: String?): Screen = when (route) {
        Screen.RoleSelection.route -> Screen.RoleSelection
        Screen.ApplyNewTalent.route -> Screen.ApplyNewTalent
        Screen.ApplyProfessionalModel.route -> Screen.ApplyProfessionalModel
        Screen.ApplyAgenciesBrands.route -> Screen.ApplyAgenciesBrands
        else -> Screen.Onboarding
    }

    companion object {
        private const val EXTRA_START_ROUTE = "extra_start_route"

        /** Convenience launcher. Example: `start(context, Screen.RoleSelection)` */
        fun start(context: Context, startScreen: Screen = Screen.Onboarding) {
            context.startActivity(intent(context, startScreen))
        }

        /** Create an Intent to start at a specific screen in the nav graph. */
        fun intent(context: Context, startScreen: Screen = Screen.Onboarding): Intent =
            Intent(context, DivoPreviewActivity::class.java).putExtra(EXTRA_START_ROUTE, startScreen.route)
    }
}

/* ---------- minimal dark theme wrapper (no dependencies on your app theme) ---------- */
@Composable
private fun DivoPreviewTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = darkColors(
            primary = Color(0xFFC57B53),
            background = Color(0xFF222222),
            surface = Color(0xFF222222),
            onPrimary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}

@Composable
fun DivoNavGraph(
    startDestination: Screen = Screen.Onboarding,
    onFinishedFlow: () -> Unit = {} // call when you want to hand off to non-Compose flow
) {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = startDestination.route
    ) {
        /* Onboarding -> Role selection */
        composable(Screen.Onboarding.route) {
            OnboardingHost(
                navigateNext = {
                    nav.navigate(Screen.RoleSelection.route) {
                        launchSingleTop = true
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        /* Role selection -> branch by role */
        composable(Screen.RoleSelection.route) {
            // You likely already have these drawables
            val options = remember {
                listOf(
                    RoleOption(
                        Role.NEW_TALENT, "New Talent",
                        "I don’t have any / have little experience. I’m new in.",
                        imageRes = R.drawable.divo_role_new_talent
                    ),
                    RoleOption(Role.MODEL, "Model",
                        "I have working experience as a model. I’m professional.",
                        imageRes = R.drawable.divo_role_model),
                    RoleOption(Role.AGENCY_SCOUTS, "Agencies & Scouts",
                        "Looking for / working with models.",
                        imageRes = R.drawable.divo_role_agency)
                )
            }

            RoleSelectionScreen(
                options = options,
                onSelect = { /* you can persist choice here if needed */ },
                onContinue = { role ->
                    when (role) {
                        Role.NEW_TALENT -> nav.navigate(Screen.ApplyNewTalent.route)
                        Role.MODEL -> nav.navigate(Screen.ApplyProfessionalModel.route)
                        Role.AGENCY_SCOUTS -> nav.navigate(Screen.ApplyAgenciesBrands.route)
                    }
                }
            )
        }

        /* Forms */
        composable(Screen.ApplyNewTalent.route) {
            ApplyNewTalentScreen(
                onBack = { nav.popBackStack() },
                onSave = { fullName, gender, country, age ->
                    // TODO persist + submit
                    onFinishedFlow() // or nav.popBackStack(Screen.RoleSelection.route, false)
                }
            )
        }

        composable(Screen.ApplyProfessionalModel.route) {
            ApplyProfessionalModelScreen(
                onBack = { nav.popBackStack() },
                onSave = { fullName, gender, country, agency, age ->
                    // TODO persist + submit
                    onFinishedFlow()
                }
            )
        }

        composable(Screen.ApplyAgenciesBrands.route) {
            ApplyAgenciesBrandsScreen(
                onBack = { nav.popBackStack() },
                onSave = { agencyName, country, website ->
                    // TODO persist + submit
                    onFinishedFlow()
                }
            )
        }
    }
}
