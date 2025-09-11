package org.telegram.divo.screen.reg_host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.telegram.divo.Screen
import org.telegram.divo.screen.onboarding.OnboardingHost
import org.telegram.divo.screen.reg_agency.ApplyAgenciesBrandsScreen
import org.telegram.divo.screen.reg_new_talent.ApplyNewTalentScreen
import org.telegram.divo.screen.reg_professional_model.ApplyProfessionalModelScreen
import org.telegram.divo.screen.reg_select_role.Role
import org.telegram.divo.screen.reg_select_role.RoleOption
import org.telegram.divo.screen.reg_select_role.RoleSelectionScreen
import org.telegram.messenger.R

import androidx.compose.ui.platform.ComposeView

object DivoHost {
    @JvmStatic
    fun mountInto(view: ComposeView) {
        view.setContent { DivoNavGraph() }
    }
}

@Composable
fun DivoNavGraph(
) {
    val nav = rememberNavController()
    val startDestination: Screen = Screen.RoleSelection

    NavHost(
        navController = nav,
        startDestination = startDestination.route
    ) {

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
                    //onFinishedFlow() // or nav.popBackStack(Screen.RoleSelection.route, false)
                }
            )
        }

        composable(Screen.ApplyProfessionalModel.route) {
            ApplyProfessionalModelScreen(
                onBack = { nav.popBackStack() },
                onSave = { fullName, gender, country, agency, age ->
                   // onFinishedFlow()
                }
            )
        }

        composable(Screen.ApplyAgenciesBrands.route) {
            ApplyAgenciesBrandsScreen(
                onBack = { nav.popBackStack() },
                onSave = { agencyName, country, website ->
                    //onFinishedFlow()
                }
            )
        }
    }
}
