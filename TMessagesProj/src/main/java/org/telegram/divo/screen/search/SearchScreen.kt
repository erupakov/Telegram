package org.telegram.divo.screen.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.common.SnackbarEvent.Error
import org.telegram.divo.common.rememberCameraCapture
import org.telegram.divo.common.rememberGalleryLauncher
import org.telegram.divo.components.PhotoSourceBottomSheet
import org.telegram.divo.components.ProfilesSearchGrid
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.components.SearchImageAction
import org.telegram.divo.screen.search.components.SearchFilterBottomSheet
import org.telegram.divo.screen.search.components.SearchRow
import org.telegram.divo.screen.search.components.SearchSuggestionContent
import org.telegram.divo.screen.similar_profiles.components.ActiveFiltersChip
import org.telegram.divo.screen.similar_profiles.components.SimilarFilterBottomSheet
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

enum class SearchScreenType {
    SEARCH,
    FR
}

@Composable
fun SearchScreen(
    searchType: SearchScreenType,
    viewModel: SearchViewModel = viewModel(),
    onBack: () -> Unit,
    onProfileClicked: (Int) -> Unit,
    onSimilarProfilesClicked: (String) -> Unit,
    onNewSearch: () -> Unit,
    onPhotoSelected: (String) -> Unit = {},
) {
    val state = viewModel.state.collectAsState().value
    val snackbarState = remember { AppSnackbarHostState() }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect {
            when (it) {
                Effect.NavigateBack -> onBack()
                is Effect.ShowError -> snackbarState.show(Error(it.message))
                is Effect.NavigateToFaceSearch -> onPhotoSelected(it.uri)
                is Effect.NavigateToProfile -> {
                    if (searchType == SearchScreenType.SEARCH) {
                        onProfileClicked(it.user.id)
                    } else {
                        onSimilarProfilesClicked(it.user.photo)
                    }
                }
                Effect.NavigateToSearchSimilarity -> onNewSearch()
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        SearchContent(
            state = state,
            onIntent = { viewModel.setIntent(it) }
        )

        AppSnackbarHost(
            modifier = Modifier.align(Alignment.BottomCenter),
            state = snackbarState,
            bottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 56.dp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchContent(
    state: State,
    onIntent: (Intent) -> Unit,
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var showFiltersBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val camera = rememberCameraCapture { uri ->
        onIntent(Intent.OnPhotoSelected(uri))
    }

    val openGallery = rememberGalleryLauncher { uri ->
        onIntent(Intent.OnPhotoSelected(uri))
    }

    if (showFiltersBottomSheet) {
        SearchFilterBottomSheet(
            uiState = state,
            onApply = { countries, city, role, draftGender, hairLength, hairColor, eyeColor, skinColor, blockParams ->
                onIntent(Intent.OnApplyFilters(countries, city, role, draftGender, hairLength, hairColor, eyeColor, skinColor, blockParams))
            },
            onReset = { onIntent(Intent.OnResetFilters) },
            onDismiss = { showFiltersBottomSheet = false },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.backgroundLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        camera.rationaleDialog()

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            SearchRow(
                value = state.query,
                onSearchFaceClicked = { showBottomSheet = true },
                onFilterClicked = { showFiltersBottomSheet = true },
                onValueChanged = { onIntent(Intent.OnQueryChanged(it)) },
                onSearchConfirmed = { onIntent(Intent.OnSearchConfirmed) },
                onBack = { onIntent(Intent.OnBackClicked) }
            )

            Spacer(Modifier.height(16.dp))

            if (state.isSearchConfirmed) {
                ProfilesSearchGrid(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    profiles = state.searchResults,
                    isLoading = state.isLoading,
                    isLoadingMore = state.isLoadingMore,
                    hasMore = state.hasMore,
                    onMarkClicked = {  }, //TODO
                    onLikeClicked = {  }, //TODO
                    onLoadMore = { onIntent(Intent.OnLoadMore) },
                    onProfileClicked = { onIntent(Intent.OnItemClicked(it)) }
                ) {
                    SearchResultRow(
                        query = state.query,
                        totalProfiles = state.totalProfiles,
                        hasActiveFilters = state.hasActiveFilters,
                        activeFiltersCount = state.activeFiltersCount,
                        onReset = { onIntent(Intent.OnResetFilters) }
                    )
                }
            } else {
                val isEmpty = state.hasSearched && state.searchResults.isEmpty() && !state.isLoading
                if (isEmpty) {
                    EmptyContent()
                } else {
                    SearchSuggestionContent(
                        searchResults = state.searchResults,
                        isLoading = state.isLoading,
                        isLoadingMore = state.isLoadingMore,
                        hasMore = state.hasMore,
                        onClicked = { onIntent(Intent.OnItemClicked(it)) },
                        onLoadMore = { onIntent(Intent.OnLoadMore) }
                    )
                }
            }
        }

        RoundedButton(
            modifier = Modifier
                .padding(bottom = 32.dp, end = 16.dp)
                .size(52.dp)
                .align(Alignment.BottomEnd),
            resId = R.drawable.ic_divo_ai,
            iconSize = 26.dp,
            paddingEnd = 0.dp,
            background = AppTheme.colors.onBackground,
            iconTint = AppTheme.colors.accentOrange,
            onClick = { },
        )

        if (showBottomSheet) {
            PhotoSourceBottomSheet(
                sheetState = sheetState,
                onDismiss = { showBottomSheet = false },
                onActionSelected = { action ->
                    showBottomSheet = false
                    when (action) {
                        SearchImageAction.CAMERA -> camera.launch()
                        SearchImageAction.GALLERY -> openGallery()
                        SearchImageAction.DIVO_PHOTO -> onIntent(Intent.OnDivoProfilesClicked)
                    }
                }
            )
        }
    }
}

@Composable
private fun EmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.onBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(R.drawable.ic_divo_search),
                contentDescription = null,
                tint = AppTheme.colors.textPrimary
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.NoResultsFound).uppercase(),
            style = AppTheme.typography.helveticaNeueLtCom,
            fontSize = 26.sp,
            color = AppTheme.colors.textPrimary
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.TryAdjustingYourFilters),
            style = AppTheme.typography.helveticaNeueRegular,
            fontSize = 16.sp,
            color = AppTheme.colors.textPrimary
        )
    }
}

@Composable
private fun SearchResultRow(
    query: String,
    totalProfiles: Int,
    hasActiveFilters: Boolean,
    activeFiltersCount: Int,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val text = if (query.isBlank()) {
            "$totalProfiles ${pluralStringResource(R.plurals.ResultFor, totalProfiles)}"
        } else {
            "$totalProfiles ${pluralStringResource(R.plurals.ResultFor, totalProfiles)} ${stringResource(R.string.ForLabel)} \"${query}\""
        }

        Text(
            text = text,
            style = AppTheme.typography.helveticaNeueLtCom,
            fontSize = 16.sp,
            color = AppTheme.colors.textPrimary
        )

        if (hasActiveFilters) {
            ActiveFiltersChip(
                activeFiltersCount = activeFiltersCount,
                onReset = onReset
            )
        }
    }
}

