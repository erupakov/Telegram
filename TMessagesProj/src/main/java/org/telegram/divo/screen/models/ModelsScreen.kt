package org.telegram.divo.screen.models

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.common.SnackbarEvent.Error
import org.telegram.divo.common.SnackbarEvent.ErrorWithRetry
import org.telegram.divo.common.SnackbarEvent.SuccessWithIcon
import org.telegram.divo.components.DivoTabSelector
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.TabConfig
import org.telegram.divo.screen.gallery.GalleryItem
import org.telegram.divo.screen.models.components.AnimatedLargeStoriesOverlay
import org.telegram.divo.screen.models.components.ModelPage
import org.telegram.divo.screen.models.components.ModelPageSkeleton
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R
import kotlin.math.roundToInt

private val HeaderExpandedHeight = 114.dp
private val HeaderCollapsedHeight = 50.dp
private val HeaderSpacerAdditional = 34.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ModelsHomeScreen(
    viewModel: ModelsViewModel = viewModel(
        viewModelStoreOwner = LocalContext.current.findActivity() as ViewModelStoreOwner
    ),
    onClick: (Int) -> Unit = {},
    onPhotoClicked: (List<GalleryItem>, Int) -> Unit = { _, _ -> },
) {
    val state by viewModel.state.collectAsState()
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { Tab.entries.size })

    val modelsListState = rememberLazyListState()
    val newTalentsListState = rememberLazyListState()
    val agenciesListState = rememberLazyListState()

    val listStates = remember(modelsListState, newTalentsListState, agenciesListState) {
        mapOf(
            Tab.MODELS to modelsListState,
            Tab.NEW_TALENTS to newTalentsListState,
            Tab.AGENCIES to agenciesListState
        )
    }

    val hazeState = remember { HazeState() }
    val snackbarState = remember { AppSnackbarHostState() }

    val maxScrollOffsetPx = remember {
        with(density) { (HeaderExpandedHeight - HeaderCollapsedHeight).toPx() }
    }

    val headerScrollOffset by remember {
        derivedStateOf {
            val page = pagerState.currentPage
            val offsetFraction = pagerState.currentPageOffsetFraction

            fun getScrollForPage(index: Int): Float {
                val list = listStates[Tab.entries.getOrNull(index)] ?: return 0f
                return if (list.firstVisibleItemIndex > 0) maxScrollOffsetPx
                else list.firstVisibleItemScrollOffset.toFloat().coerceAtMost(maxScrollOffsetPx)
            }

            val currentScroll = getScrollForPage(page)
            if (offsetFraction == 0f) return@derivedStateOf currentScroll

            val targetPage = if (offsetFraction > 0) page + 1 else page - 1
            val targetScroll = getScrollForPage(targetPage)

            currentScroll + (targetScroll - currentScroll) * kotlin.math.abs(offsetFraction)
        }
    }

    val haptic = LocalHapticFeedback.current
    var isInitialized by remember { mutableStateOf(false) }

    val isAtBoundary by remember {
        derivedStateOf { headerScrollOffset <= 0.5f || headerScrollOffset >= maxScrollOffsetPx - 0.5f }
    }

    LaunchedEffect(isAtBoundary) {
        if (!isInitialized) {
            isInitialized = true
            return@LaunchedEffect
        }

        if (isAtBoundary) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val collapseFraction = headerScrollOffset / maxScrollOffsetPx
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

    val nestedScrollConnection =
        rememberHeaderSnapNestedScroll(listStates, pagerState, maxScrollOffsetPx)

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarState.show(ErrorWithRetry(it, context.getString(R.string.RetryLabel)) {
                viewModel.setIntent(ModelsViewIntent.LoadInitialData)
            })
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            launch {
                when (effect) {
                    is ModelsViewEffect.ShowError -> {
                        snackbarState.show(Error(effect.message))
                    }
                    is ModelsViewEffect.ActionChanged -> {
                        snackbarState.show(
                            SuccessWithIcon(
                                effect.resDrawableId,
                                context.getString(effect.resStringId)
                            )
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val currentTab = Tab.entries[pagerState.currentPage]
        val activeList = listStates[currentTab] ?: return@LaunchedEffect

        snapshotFlow {
            if (activeList.firstVisibleItemIndex > 0) maxScrollOffsetPx.toInt()
            else activeList.firstVisibleItemScrollOffset
        }.collect { offset ->
            listStates.forEach { (tab, listState) ->
                if (tab != currentTab && listState.firstVisibleItemIndex == 0) {
                    val targetOffset = offset.coerceAtMost(maxScrollOffsetPx.toInt())
                    listState.scrollToItem(0, targetOffset)
                }
            }
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        state = pullToRefreshState,
        isRefreshing = state.isRefreshing,
        onRefresh = { viewModel.setIntent(ModelsViewIntent.Refresh) },
        modifier = Modifier.fillMaxSize(),
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = state.isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = AppTheme.colors.backgroundLight,
                color = AppTheme.colors.textPrimary
            )
        }
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.backgroundLight)
                .nestedScroll(nestedScrollConnection)
        ) {
            val cardHeight = remember(this.maxHeight, statusBarHeight, bottomInset) {
                this.maxHeight - statusBarHeight - bottomInset - 272.dp
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().hazeSource(hazeState),
                beyondViewportPageCount = 1,
                key = { id -> id }
            ) { page ->
                val tab = Tab.entries[page]
                ModelsList(
                    tab = tab,
                    state = state,
                    listState = listStates[tab]!!,
                    statusBarHeight = statusBarHeight,
                    cardHeight = cardHeight,
                    bottomInset = bottomInset,
                    onLoadMore = { viewModel.setIntent(ModelsViewIntent.LoadMore(tab)) },
                    onLikeClick = { id, liked ->
                        viewModel.setIntent(
                            ModelsViewIntent.OnLikeClick(tab, id, liked)
                        )
                    },
                    onClick = onClick,
                    onPhotoClicked = onPhotoClicked,
                    onBookmarkClick = { viewModel.setIntent(ModelsViewIntent.OnBookmarkClick(it)) }
                )
            }

            AnimatedLargeStoriesOverlay(
                collapseFraction = collapseFraction,
                stories = ModelsViewState.preview.stories,
                hazeState = hazeState
            )

            val currentHeaderHeight =
                lerp(HeaderExpandedHeight, HeaderCollapsedHeight, collapseFraction)
            val tabsTopOffsetPx = with(density) { (statusBarHeight + currentHeaderHeight).toPx() }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, tabsTopOffsetPx.roundToInt()) }
                    .zIndex(2f)
            ) {
                DivoTabSelector(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    tabs = remember { Tab.entries.map { TabConfig(it.name, it.displayResId) } },
                    selectedIndex = pagerState.currentPage,
                    onTabSelected = { index ->
                        scope.launch {
                            val targetTab = Tab.entries[index]
                            val targetListState = listStates[targetTab]

                            if (targetListState?.firstVisibleItemIndex == 0) {
                                val currentPx = headerScrollOffset.roundToInt()
                                targetListState.scrollToItem(0, currentPx)
                            }

                            pagerState.animateScrollToPage(index)
                        }
                    },
                    horizontalPadding = 16.dp,
                )
            }

            AppSnackbarHost(
                modifier = Modifier.align(Alignment.BottomCenter).zIndex(3f),
                state = snackbarState,
                bottomPadding = bottomInset + 74.dp
            )
        }
    }
}

@Composable
private fun ModelsList(
    tab: Tab,
    state: ModelsViewState,
    listState: LazyListState,
    statusBarHeight: androidx.compose.ui.unit.Dp,
    cardHeight: androidx.compose.ui.unit.Dp,
    bottomInset: androidx.compose.ui.unit.Dp,
    onLoadMore: () -> Unit,
    onLikeClick: (Int, Boolean) -> Unit,
    onBookmarkClick: (Int) -> Unit,
    onClick: (Int) -> Unit,
    onPhotoClicked: (List<GalleryItem>, Int) -> Unit
) {
    val pageFeedItems = state.tabFeeds[tab] ?: emptyList()
    val isLoading = state.tabLoadingStates[tab] ?: false
    val isLoadingMore = state.tabLoadingMoreStates[tab] ?: false

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val total = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 1 && lastVisible >= total - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !isLoadingMore && (state.tabHasMore[tab] != false)) {
            onLoadMore()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(bottom = bottomInset + 70.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(statusBarHeight + HeaderExpandedHeight + HeaderSpacerAdditional))
        }

        if (state.isRefreshing) {
            item {
                ModelPageSkeleton(
                    cardHeight = cardHeight,
                )
            }
        } else if (pageFeedItems.isEmpty() && isLoading) {
            item {
                LoadingPlaceholder(cardHeight)
            }
        } else {
            items(
                count = pageFeedItems.size,
                key = { i -> "${tab.name}_${pageFeedItems[i].id}" }
            ) { i ->
                ModelPage(
                    feed = pageFeedItems[i],
                    cardHeight = cardHeight,
                    onClick = onClick,
                    onPhotoClicked = onPhotoClicked,
                    onLikeClick = { id, liked -> onLikeClick(id, liked) },
                    onBookmarkClick = onBookmarkClick
                )
            }

            if (isLoadingMore) {
                item {
                    Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                        LottieProgressIndicator(Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberHeaderSnapNestedScroll(
    listStates: Map<Tab, LazyListState>,
    pagerState: PagerState,
    maxScrollOffsetPx: Float
): NestedScrollConnection = remember(pagerState.currentPage) {
    object : NestedScrollConnection {
        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            val activeList = listStates[Tab.entries[pagerState.currentPage]] ?: return super.onPostFling(consumed, available)
            if (activeList.firstVisibleItemIndex == 0) {
                val offset = activeList.firstVisibleItemScrollOffset.toFloat()
                if (offset > 0f && offset < maxScrollOffsetPx) {
                    val target = if (offset > maxScrollOffsetPx / 2f) maxScrollOffsetPx else 0f
                    activeList.animateScrollToItem(0, target.roundToInt())
                }
            }
            return super.onPostFling(consumed, available)
        }
    }
}


@Composable
private fun LoadingPlaceholder(cardHeight: androidx.compose.ui.unit.Dp) {
    Box(Modifier.fillMaxWidth().height(cardHeight), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LottieProgressIndicator(Modifier.size(32.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.LoadingModelsList).uppercase(),
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 12.sp,
                color = Color.Black
            )
        }
    }
}

private fun android.content.Context.findActivity(): android.app.Activity? {
    var context = this
    while (context is android.content.ContextWrapper) {
        if (context is android.app.Activity) return context
        context = context.baseContext
    }
    return null
}

@Preview(showBackground = true, backgroundColor = 0xFF121922)
@Composable
private fun PreviewModelsHome() {
    ModelsHomeScreen()
}
