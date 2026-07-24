package org.telegram.divo.screen.event_list

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.common.SnackbarEvent
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.screen.event_list.components.EventItemView
import org.telegram.divo.screen.event_list.components.EventListTopBar
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

object EventIntentData {
    var eventId: Int = 0
}

@Composable
fun EventListScreen(
    viewModel: EventListViewModel,
    onNavigateToEventDetails: (Int) -> Unit,
    onNavigateToCreateEvent: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToApplyConfirmation: (Int) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val snackbarState = remember { AppSnackbarHostState() }
    val retryText = stringResource(R.string.RetryLabel)
    var withdrawEventId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { action ->
            when (action) {
                EventListEffect.NavigateToCreateEvent -> onNavigateToCreateEvent()
                EventListEffect.NavigateToSearch -> onNavigateToSearch()
                is EventListEffect.NavigateToEventDetails -> onNavigateToEventDetails(
                    action.eventId
                )
                is EventListEffect.NavigateToApplyConfirmation -> onNavigateToApplyConfirmation(
                    action.eventId
                )
                is EventListEffect.ShowWithdrawConfirmation -> { withdrawEventId = action.eventId }
                is EventListEffect.ShowError -> {
                    snackbarState.show(
                        SnackbarEvent.ErrorWithRetry(action.message, retryText) {
                            viewModel.setIntent(EventListIntent.OnLoad)
                        }
                    )
                }
            }
        }
    }
    EventListContent(
        state = state,
        snackbarState = snackbarState,
        onEventClick = {
            EventIntentData.eventId = it
            onNavigateToEventDetails(it)
        },
        onCtaClick = { 
            viewModel.handleIntent(EventListIntent.OnEventCtaClicked(it))
        },
        onSearchClick = {
            viewModel.handleIntent(EventListIntent.OnSearchClicked)
        },
        onAddEventClick = {
            onNavigateToCreateEvent()
        },
        onLoadMore = {
            viewModel.handleIntent(EventListIntent.OnLoadMore)
        },
        onTabSelected = {
            viewModel.handleIntent(EventListIntent.OnTabSelected(it))
        },
    )

    if (withdrawEventId != null) {
        org.telegram.divo.components.DivoWithdrawBottomSheet(
            onKeepApplication = { withdrawEventId = null },
            onWithdraw = {
                val id = withdrawEventId
                withdrawEventId = null
                if (id != null) viewModel.handleIntent(EventListIntent.ConfirmWithdraw(id))
            },
            onDismiss = { withdrawEventId = null }
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun EventListContent(
    state: EventListViewState,
    snackbarState: AppSnackbarHostState,
    onEventClick: (Int) -> Unit,
    onCtaClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onAddEventClick: () -> Unit,
    onLoadMore: () -> Unit,
    onTabSelected: (Int) -> Unit,
) {
    val bottomInset = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
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

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val normalTopBarHeight = if (state.isAgency) 56.dp + 44.dp else 56.dp
    val contentTopPadding = statusBarHeight + normalTopBarHeight

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = AppTheme.colors.backgroundLight,
        snackbarHost = {
            AppSnackbarHost(
                state = snackbarState,
                bottomPadding = 74.dp
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                (state.isLoading || state.isRoleLoading) && !state.isSearchMode -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LottieProgressIndicator(
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                state.events.isEmpty() && !state.isLoading && !state.isRoleLoading -> {
                    EventsEmptyState(
                        modifier = Modifier
                            .fillMaxSize(),
                        isAgency = state.isAgency,
                        bottomInset = bottomInset,
                        onCreateEventClick = onAddEventClick
                    )
                }

                else -> {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxSize(),
                        state = listState,
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = contentTopPadding + 4.dp, bottom = bottomInset + 72.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        itemsIndexed(
                            items = state.events,
                            key = { _, event -> event.id },
                        ) { index, event ->
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
                                    LottieProgressIndicator(
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        } else if (!state.hasMore && state.events.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_divo_check_circle),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = AppTheme.colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.EventSeenAll),
                                        style = AppTheme.typography.helveticaNeueRegular,
                                        fontSize = 16.sp,
                                        color = AppTheme.colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Top bar
            EventListTopBar(
                    isModel = state.isModel,
                    isAgency = state.isAgency,
                    selectedTabIndex = state.selectedTab,
                    onTabSelected = onTabSelected,
                    onSearchClick = onSearchClick,
                    onAddEventClick = onAddEventClick,
                )

        }
    }
}

@Composable
private fun EventsEmptyState(
    modifier: Modifier = Modifier,
    isAgency: Boolean,
    bottomInset: Dp,
    onCreateEventClick: () -> Unit
) {
    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
                text = stringResource(R.string.EventEmptyTitle).uppercase(),
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
                lineHeight = 32.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = if (isAgency) {
                    stringResource(R.string.EventEmptySubtitleAgency)
                } else {
                    stringResource(R.string.EventEmptySubtitle)
                },
                style = AppTheme.typography.helveticaNeueRegular,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
        }

        if (isAgency) {
            UIButtonNew(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = bottomInset + 76.dp),
                text = stringResource(R.string.EventCreateButton),
                onClick = onCreateEventClick
            )
        }
    }
}


fun android.content.Context.findActivity(): android.app.Activity? {
    var context = this
    while (context is android.content.ContextWrapper) {
        if (context is android.app.Activity) return context
        context = context.baseContext
    }
    return null
}