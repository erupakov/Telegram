package org.telegram.divo.screen.profile

import android.net.Uri
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
import org.telegram.divo.screen.add_model.AddModelScreen
import org.telegram.divo.screen.edit_my_profile.EditMyProfileScreen
import org.telegram.divo.screen.event_details.EventDetailsNavGraph
import org.telegram.divo.screen.face_search.FaceSearchScreen
import org.telegram.divo.screen.gallery.GallerySource
import org.telegram.divo.screen.gallery.GalleryViewerScreen
import org.telegram.divo.screen.profile_social_links.ProfileSocialLinksScreen
import org.telegram.divo.screen.search_agency.SearchAgencyScreen
import org.telegram.divo.screen.similar_profiles.SimilarProfilesScreen
import org.telegram.divo.screen.work_create_edit.CreateWorkHistoryScreen
import org.telegram.divo.screen.work_history.WorkHistoryScreen
import org.telegram.divo.screen.your_parameters.YourParametersScreen

sealed class ProfileRoute(val route: String) {
    data object YourParameters : ProfileRoute("profile_your_parameters")
    data object EditLinks : ProfileRoute("profile_edit_links")
    data object WorkHistory : ProfileRoute("profile_work_history")
    data object AddModel : ProfileRoute("profile_add_model")
    data object CreateWorkHistory : ProfileRoute("create_work_history?id={id}") {
        fun create(id: Int? = null) = if (id != null) "create_work_history?id=$id" else "create_work_history?id=-1"
    }

    data object Profile : ProfileRoute("profile/{userId}") {
        fun createRoute(userId: Int) = "profile/$userId"
    }

    data object Event : ProfileRoute("event/{eventId}") {
        fun createRoute(eventId: Int) = "event/$eventId"
    }

    data object Edit : ProfileRoute("profile_edit/{isModel}") {
        fun createRoute(isModel: Boolean) = "profile_edit/$isModel"
    }

    data object Search : ProfileRoute("search_agency")

    object Gallery : ProfileRoute("gallery/{sourceType}/{userId}/{initialIndex}") {
        const val ROUTE = "gallery/{sourceType}/{userId}/{initialIndex}"

        fun portfolio(userId: Int, initialIndex: Int) =
            "gallery/portfolio/$userId/$initialIndex"

        fun video(userId: Int, initialIndex: Int) =
            "gallery/video/$userId/$initialIndex"
    }

    data object SimilarProfiles : ProfileRoute("similar_profiles/{uri}?fx={fx}&fy={fy}&filters={filters}") {
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

    data object FaceSearch : ProfileRoute("face_search/{uri}") {
        fun createRoute(uri: String) = "face_search/${Uri.encode(uri)}"
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
            route = ProfileRoute.Profile.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val currentUserId = backStackEntry.arguments
                ?.getInt("userId", -1)
                ?.takeIf { it != -1 }
                ?: userId

            val profileViewModel: ProfileViewModel = viewModel(
                key = "profile_$currentUserId",
                factory = ProfileViewModel.factory(currentUserId, isOwnProfile)
            )
            val uiState = profileViewModel.state.collectAsState().value

            ProfileScreen(
                viewModel = profileViewModel,
                userId = currentUserId,
                isOwnProfile = isOwnProfile,
                onEditClicked = { nav.navigate(ProfileRoute.Edit.createRoute(it)) },
                onEditLinksClicked = { nav.navigate(ProfileRoute.EditLinks.route) },
                onNavigateBack = { if (!nav.popBackStack()) onNavigateBack() },
                showWorkHistory = { nav.navigate(ProfileRoute.WorkHistory.route) },
                onGalleryClicked = { url, isVideo ->
                    if (isVideo) {
                        val index = uiState.videoItems
                            .indexOfFirst { it.files.any { f -> f.fullUrl == url } }
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
                onAddModelClicked = {
                    nav.navigate(ProfileRoute.AddModel.route)
                },
                onEventClicked = {
                    nav.navigate(ProfileRoute.Event.createRoute(it))
                },
                onFindSimilarProfiles = {
                    nav.navigate(ProfileRoute.FaceSearch.createRoute(it))
                }
            )
        }

        composable(ProfileRoute.Edit.route) {
            val isModel = it.arguments?.getString("isModel")?.toBoolean() ?: false

            EditMyProfileScreen(
                isModel = isModel,
                onCreateWorkHistoryClicked = { nav.navigate(ProfileRoute.CreateWorkHistory.create(it)) },
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
                onCreateClicked = { id ->
                    nav.navigate(ProfileRoute.CreateWorkHistory.create(id))
                },
                onBack = { if (!nav.popBackStack()) onNavigateBack() }
            )
        }

        composable(
            route = ProfileRoute.CreateWorkHistory.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            val editId = backStackEntry.arguments?.getInt("id")?.takeIf { it != -1 }

            CreateWorkHistoryScreen(
                editId = editId,
                onNavigateToSearch = { nav.navigate(ProfileRoute.Search.route) },
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
                isOwnProfile = isOwnProfile,
                onBack = { if (!nav.popBackStack()) onNavigateBack() },
            )
        }

        composable(
            route = ProfileRoute.Search.route,
        ) {
            SearchAgencyScreen(
                onBack = { if (!nav.popBackStack()) onNavigateBack() }
            )
        }

        composable(ProfileRoute.AddModel.route) {
            AddModelScreen(
                onNavigateToYourParameters = {
                    nav.navigate(ProfileRoute.YourParameters.route)
                },
                onCloseScreen = { if (!nav.popBackStack()) onNavigateBack() }
            )
        }
        composable(ProfileRoute.YourParameters.route) {
            YourParametersScreen(
                showTitle = false,
                isFromAgency = true,
                onSaved = {
                    nav.popBackStack(
                        ProfileRoute.AddModel.route,
                        inclusive = true
                    )
                },
                onBack = { if (!nav.popBackStack()) onNavigateBack() }
            )
        }
        composable(
            route = ProfileRoute.Event.route,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId")?.takeIf { it != -1 } ?: -1

            EventDetailsNavGraph(
                eventId = eventId,
                isOwnProfile = isOwnProfile,
                onNavigateBack = { if (!nav.popBackStack()) onNavigateBack() }
            )
        }
        composable(
            route = ProfileRoute.SimilarProfiles.route,
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
                onProfileClicked = { nav.navigate(ProfileRoute.Profile.createRoute(it)) },
                onBack = { nav.popBackStack() }
            )
        }
        composable(
            route = ProfileRoute.FaceSearch.route,
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        ) { backStackEntry ->
            val uri = Uri.decode(backStackEntry.arguments?.getString("uri")).orEmpty()
            FaceSearchScreen(
                uri = uri,
                onNavigateSimilarProfiles = { url, fx, fy ->
                    nav.navigate(ProfileRoute.SimilarProfiles.createRoute(url, fx, fy)) {
                        popUpTo(ProfileRoute.FaceSearch.route) { inclusive = true }
                    }
                },
                onNavigateToSearch = {  },
                onBack = { nav.popBackStack() }
            )
        }
    }
}