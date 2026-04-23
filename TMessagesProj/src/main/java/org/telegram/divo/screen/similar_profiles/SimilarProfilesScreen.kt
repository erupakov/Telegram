package org.telegram.divo.screen.similar_profiles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.common.SnackbarEvent.Error
import org.telegram.divo.components.ProfilesSearchGrid
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.entity.RoleType
import org.telegram.divo.screen.similar_profiles.components.SearchResultSection
import org.telegram.divo.screen.similar_profiles.components.SimilarFilterBottomSheet
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun SimilarProfilesScreen(
    url: String,
    initialFiltersJson: String? = null,
    resultsJson: String? = null,
    viewModel: SimilarProfilesViewModel = viewModel(
        factory = SimilarProfilesViewModel.factory(url, initialFiltersJson, resultsJson)
    ),
    fx: Float? = null,
    fy: Float? = null,
    onProfileClicked: (Int) -> Unit,
    onBack: () -> Unit
) {


    val state = viewModel.state.collectAsState().value
    val snackbarState = remember { AppSnackbarHostState() }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect {
            when (it) {
                Effect.NavigateBack -> onBack()
                is Effect.ShowError -> snackbarState.show(Error(it.message))
                is Effect.NavigateToProfile -> onProfileClicked(it.id)
            }
        }
    }

    SimilarProfilesContent(
        uiState = state,
        snackbarHostState = snackbarState,
        fx = fx,
        fy = fy,
        onIntent = { viewModel.setIntent(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimilarProfilesContent(
    uiState: State,
    fx: Float? = null,
    fy: Float? = null,
    snackbarHostState: AppSnackbarHostState,
    onIntent: (Intent) -> Unit = {},
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val statusBars = WindowInsets.statusBars
    val currentTopPx = statusBars.getTop(density)

    var maxTopPaddingPx by remember { mutableIntStateOf(0) }
    if (currentTopPx > maxTopPaddingPx) {
        maxTopPaddingPx = currentTopPx
    }
    val topPaddingDp = with(density) { maxTopPaddingPx.toDp() }

    Scaffold(
        containerColor = AppTheme.colors.backgroundLight,
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0.dp),
                modifier = Modifier.padding(top = topPaddingDp),
                title = {
                    Text(
                        text = stringResource(R.string.SimilarProfilesTitle).uppercase(),
                        style = AppTheme.typography.appBar,
                    )
                },
                navigationIcon = {
                    RoundedButton(
                        modifier = Modifier.padding(start = 16.dp),
                        resId = R.drawable.ic_divo_back,
                        onClick = { onIntent(Intent.OnBackClicked) }
                    )
                },
                actions = {
                    RoundedButton(
                        modifier = Modifier.padding(end = 16.dp),
                        resId = R.drawable.ic_divo_filter,
                        iconSize = 24.dp,
                        onClick = { showBottomSheet = true }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.backgroundLight
                )
            )
        },
        snackbarHost = {
            AppSnackbarHost(
                state = snackbarHostState,
                bottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 66.dp
            )
        }
    ) { paddingValues ->
        if (showBottomSheet) {
            SimilarFilterBottomSheet(
                uiState = uiState,
                onApply = { countries, percent, role, blockParams ->
                    onIntent(Intent.OnApplyFilters(countries, percent, role, blockParams))
                },
                onReset = { onIntent(Intent.OnParamsReset) },
                onDismiss = { showBottomSheet = false },
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 16.dp)
        ) {
            val newProfiles = uiState.profiles.map {
                it.copy(
                    roleLabel = when (it.roleLabel) {
                        RoleType.NEW_FACE.value -> stringResource(R.string.NewTalent)
                        RoleType.AGENCY.value -> stringResource(R.string.Agency)
                        RoleType.MODEL.value -> stringResource(R.string.Model)
                        else -> it.roleLabel
                    }
                )
            }

            ProfilesSearchGrid(
                profiles = newProfiles,
                isLoading = false, //TODO
                isLoadingMore = false, //TODO
                hasMore = false, //TODO
                onMarkClicked = { onIntent(Intent.OnMarkChanged(it)) },
                onLikeClicked = { onIntent(Intent.OnLikeChanged(it)) },
                onProfileClicked = { onIntent(Intent.OnProfileClicked(it.id)) },
                onLoadMore = {} //TODO
            ) {
                SearchResultSection(
                    imageUrl = uiState.imageUrl,
                    topMatchesCount = uiState.topMatches,
                    totalProfilesCount = uiState.profiles.size,
                    similarityPercent = HIGH_PERCENT,
                    hasActiveFilters = uiState.hasActiveFilters,
                    activeFiltersCount = uiState.activeFiltersCount,
                    fx = fx,
                    fy = fy,
                    onReset = { onIntent(Intent.OnParamsReset) }
                )
            }
        }
    }
}