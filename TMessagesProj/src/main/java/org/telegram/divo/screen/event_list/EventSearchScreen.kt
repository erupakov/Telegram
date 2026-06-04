package org.telegram.divo.screen.event_list

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.screen.event_list.components.EventFilterBottomSheet
import org.telegram.divo.screen.event_list.components.EventItemView
import org.telegram.divo.screen.event_list.components.EventSearchRow
import org.telegram.divo.screen.similar_profiles.components.ActiveFiltersChip
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EventSearchScreen(
    state: EventListViewState,
    snackbarState: AppSnackbarHostState,
    onEventClick: (Int) -> Unit,
    onCtaClick: (Int) -> Unit,
    onCloseSearch: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSearchConfirmed: () -> Unit,
    onApplyFilters: (EventSearchFilters) -> Unit,
    onResetFilters: () -> Unit,
    onLoadMore: () -> Unit,
) {
    val bottomInset = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val searchTopBarHeight = 72.dp
    val contentTopPadding = statusBarHeight + searchTopBarHeight
    
    val listState = rememberLazyGridState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleIndex >= totalItems - 4 && totalItems > 0
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    androidx.activity.compose.BackHandler {
        onCloseSearch()
    }

    var showFilterSheet by remember { mutableStateOf(false) }

    if (showFilterSheet) {
        EventFilterBottomSheet(
            currentFilters = state.searchFilters,
            uiState = state,
            onApply = onApplyFilters,
            onReset = onResetFilters,
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppTheme.colors.backgroundLight,
        snackbarHost = {
            AppSnackbarHost(
                state = snackbarState,
                bottomPadding = bottomInset + 74.dp
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LottieProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                }

                state.events.isEmpty() && !state.isLoading && state.searchFilters.hasAnyFilterOrQuery -> {
                    EventsSearchEmptyState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = contentTopPadding + 60.dp),
                        hasFilters = state.searchFilters.hasActiveFilters,
                        onClearFilters = onResetFilters
                    )
                }

                state.events.isEmpty() && !state.searchFilters.hasAnyFilterOrQuery -> {
                    // Empty search results, but no filters or query applied yet.
                }

                else -> {
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = contentTopPadding, bottom = bottomInset + 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (state.searchFilters.hasAnyFilterOrQuery) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                SearchResultRow(
                                    eventsCount = state.events.size,
                                    query = state.searchFilters.query,
                                    hasActiveFilters = state.searchFilters.hasActiveFilters,
                                    activeFiltersCount = state.searchFilters.activeFiltersCount,
                                    onReset = onResetFilters
                                )
                            }
                        }

                        itemsIndexed(
                            items = state.events,
                            key = { _, event -> event.id },
                        ) { _, event ->
                            EventItemView(
                                modifier = Modifier
                                    .aspectRatio(0.72f)
                                    .fillMaxWidth(),
                                event = event,
                                isModel = state.isModel,
                                onCardClick = { onEventClick(event.id) },
                                onCtaClicked = { onCtaClick(event.id) },
                            )
                        }

                        if (state.isLoadingMore) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    LottieProgressIndicator(modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Top Bar Search Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.backgroundLight)
                    .padding(top = statusBarHeight)
            ) {
                EventSearchRow(
                    value = state.searchFilters.query,
                    onFilterClicked = { showFilterSheet = true },
                    onValueChanged = onSearchQueryChanged,
                    onSearchConfirmed = onSearchConfirmed,
                    onBack = onCloseSearch
                )
            }
        }
    }
}

@Composable
fun EventsSearchEmptyState(
    modifier: Modifier = Modifier,
    hasFilters: Boolean,
    onClearFilters: () -> Unit,
) {
    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.onBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_calendar_item),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = AppTheme.colors.textPrimary
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.EventSearchNoResults).uppercase(),
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
                lineHeight = 32.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.EventSearchNoResultsSubtitle),
                style = AppTheme.typography.helveticaNeueRegular,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )

            if (hasFilters) {
                Spacer(Modifier.height(32.dp))
                UIButtonNew(
                    text = stringResource(R.string.EventSearchClearFilters),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onClearFilters
                )
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    eventsCount: Int,
    query: String,
    hasActiveFilters: Boolean,
    activeFiltersCount: Int,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val text = if (query.isBlank()) {
            "$eventsCount ${stringResource(R.string.EventSearchEventsFound)}"
        } else {
            "$eventsCount ${stringResource(R.string.EventSearchEventsFound)}"
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
