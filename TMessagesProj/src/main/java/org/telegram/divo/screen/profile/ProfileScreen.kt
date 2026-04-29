package org.telegram.divo.screen.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.flow.drop
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.common.SnackbarEvent.Error
import org.telegram.divo.common.SnackbarEvent.ErrorWithRetry
import org.telegram.divo.common.rememberGalleryLauncher
import org.telegram.divo.common.utils.uriToFile
import org.telegram.divo.entity.RoleType
import org.telegram.divo.screen.profile.components.AgencyModels
import org.telegram.divo.screen.profile.components.ChannelsContent
import org.telegram.divo.screen.profile.components.EngagementStatsBottomSheet
import org.telegram.divo.screen.profile.components.EventsColumn
import org.telegram.divo.screen.profile.components.PortfolioGrid
import org.telegram.divo.screen.profile.components.ProfileHeadlineContent
import org.telegram.divo.screen.profile.components.ProfileInfoPager
import org.telegram.divo.screen.profile.components.ProfileInfoTabs
import org.telegram.divo.screen.profile.components.SocialLinksSection
import org.telegram.divo.screen.profile.components.StatsType
import org.telegram.divo.screen.profile.components.PortfolioAddButton
import org.telegram.divo.screen.profile.components.ProfileLoadingContent
import org.telegram.divo.screen.profile.components.TabContainer
import org.telegram.divo.screen.profile.components.ToolBarBackground
import org.telegram.divo.screen.profile.components.ToolBarContent
import org.telegram.divo.screen.profile.components.VideoGrid
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: Int,
    isOwnProfile: Boolean = false,
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.factory(userId, isOwnProfile)
    ),
    onEditClicked: (Boolean, Int) -> Unit,
    onEditLinksClicked: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    showWorkHistory: (Boolean) -> Unit = {},
    onGalleryClicked: (Int, Boolean) -> Unit = { _, _ -> },
    onProfileClicked: (Int) -> Unit = {},
    onAddModelClicked: () -> Unit,
    onEventClicked: (Int) -> Unit,
    onEventCreateClicked: () -> Unit,
    onFindSimilarProfiles: (String) -> Unit,
    onNavigateToAppearances: (PhysicalParams) -> Unit,
) {
    val context = LocalContext.current
    val uiState = viewModel.state.collectAsState().value
    val snackbarState = remember { AppSnackbarHostState() }
    android.util.Log.d("VideoGrid", "ллл $userId")
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) isRefreshing = false
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileEffect.OpenUrl -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, effect.url.toUri())
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        snackbarState.show(Error(e.message.orEmpty()))
                    }
                }
                is ProfileEffect.ShowError -> {
                    if (effect.hasRetry) {
                        snackbarState.show(
                            ErrorWithRetry(effect.message, context.getString(R.string.RetryLabel)) {
                                viewModel.setIntent(ProfileIntent.OnLoad)
                            }
                        )
                    } else {
                        snackbarState.show(Error(effect.message))
                    }
                }
                is ProfileEffect.NavigateToEdit -> { onEditClicked(effect.isModel, effect.initialPage) }
                is ProfileEffect.NavigateBack -> onNavigateBack()
                is ProfileEffect.ShowWorkHistory -> showWorkHistory(effect.isOwnProfile)
                is ProfileEffect.NavigateToGallery -> onGalleryClicked(effect.index, effect.isVideo)
                is ProfileEffect.NavigateToProfile -> {
                    onProfileClicked(effect.profileId)
                }
                is ProfileEffect.NavigateToAddModel -> onAddModelClicked()
                is ProfileEffect.NavigateToEvent -> onEventClicked(effect.eventId)
                is ProfileEffect.NavigateToFindSimilarProfiles -> onFindSimilarProfiles(effect.photoUrl)
                is ProfileEffect.NavigateToEditLinks -> onEditLinksClicked()
                ProfileEffect.ShowAppearances -> onNavigateToAppearances(viewModel.state.value.physicalParams)
                ProfileEffect.NavigateToCreateEvent -> onEventCreateClicked()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.setIntent(ProfileIntent.OnLoad)
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            val showSkeleton = uiState.isLoading || (!uiState.hasBackgroundReady || uiState.errorMessage != null)

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.errorMessage == null) {
                    ProfileScreenContent(
                        uiState = uiState,
                        onIntent = { intent ->
                            viewModel.setIntent(intent)
                        }
                    )
                }

                AnimatedVisibility(
                    visible = showSkeleton,
                    enter = fadeIn(),
                    exit = fadeOut(animationSpec = tween(500))
                ) {
                    ProfileLoadingContent(
                        onNavigateBack = onNavigateBack
                    )
                }
            }
        }

        AppSnackbarHost(
            modifier = Modifier.align(Alignment.BottomCenter),
            state = snackbarState,
            bottomPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 8.dp
        )
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreenContent(
    uiState: ProfileViewState,
    onIntent: (ProfileIntent) -> Unit
) {
    val pageCount = uiState.pageCount
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val pagerInfoState = rememberPagerState(pageCount = { 3 })
    val lazyListState = rememberLazyListState()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val statusBarHeight = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    val toolbarHeight = statusBarHeight + 56.dp
    val density = LocalDensity.current
    val toolbarHeightPx = with(density) { toolbarHeight.toPx() }

    val hasTabs = uiState.userInfo.role != RoleType.UNKNOWN

    val spacerPreDp = if (!uiState.isVisibleSocialLinks) 22.dp else 16.dp
    val tabsHeightDp = if (hasTabs) 32.dp else 0.dp
    val spacerPostDp = 10.dp
    val totalTopPaddingDp = spacerPreDp + tabsHeightDp + spacerPostDp

    val spacerPrePx = with(density) { spacerPreDp.toPx() }

    val isTabsPinned by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val pagerItem = layoutInfo.visibleItemsInfo.find { it.key == "pager_section" }
            if (pagerItem != null) {
                pagerItem.offset + spacerPrePx <= (toolbarHeightPx + 1f)
            } else {
                lazyListState.firstVisibleItemIndex >= 3
            }
        }
    }

    val currentHasTabs by rememberUpdatedState(hasTabs)
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        snapshotFlow { isTabsPinned }
            .drop(1)
            .collect {
                if (currentHasTabs) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
    }

    val lowerTabsOffsetPx by remember {
        derivedStateOf {
            lazyListState.layoutInfo.visibleItemsInfo.find { it.key == "pager_section" }
                ?.let { it.offset + spacerPrePx } ?: Int.MAX_VALUE.toFloat()
        }
    }

    val pagerNestedScrollConnection = remember {
        PagerNestedScrollConnection(lazyListState, { isTabsPinned })
    }

    var showStatsSheet by remember { mutableStateOf(false) }
    var selectedStat by remember { mutableStateOf<StatsType?>(null) }
    val hazeState = remember { dev.chrisbanes.haze.HazeState() }

    val context = LocalContext.current

    val (items, isLoadingMore)  = when (selectedStat) {
        StatsType.LIKES -> uiState.likedItems to uiState.isLoadingLiked
        StatsType.VIEWS -> uiState.viewedItems to uiState.isLoadingViewed
        else -> uiState.followedItems to uiState.isLoadingFollowed
    }

    if (showStatsSheet && selectedStat != null && items.isNotEmpty()) {
        EngagementStatsBottomSheet(
            stats = selectedStat,
            items = items,
            isLoadingMoreFeed = isLoadingMore,
            searchQuery = uiState.searchQuery,
            searchResults = uiState.searchResults,
            isSearchMode = uiState.isSearchMode,
            isLoadingStats = uiState.isLoadingStats,
            isLoadingMoreSearch = uiState.isLoadingMoreSearch,
            onQueryChanged = { onIntent(ProfileIntent.OnSearchQueryChanged(it)) },
            onLoadMoreSearch = { onIntent(ProfileIntent.OnLoadMoreSearchResults) },
            onProfileClicked = { onIntent(ProfileIntent.OnProfileClicked(it)) },
            onLoadMore = { onIntent(ProfileIntent.OnLoadMoreEngagementStats(selectedStat ?: StatsType.LIKES)) },
            onDismiss = { showStatsSheet = false }
        )
    }

    val fadeRangePx = with(density) { 30.dp.toPx() }

    val transitionProgress by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val firstVisibleIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0

            if (firstVisibleIndex > 0) {
                1f
            } else {
                val tabsItem = layoutInfo.visibleItemsInfo.find { it.key == "info_tabs" }
                if (tabsItem != null) {
                    val distance = tabsItem.offset - toolbarHeightPx
                    when {
                        distance <= 0f -> 1f
                        distance >= fadeRangePx -> 0f
                        else -> 1f - (distance / fadeRangePx)
                    }
                } else {
                    0f
                }
            }
        }
    }

    val isToolbarSolid by remember {
        derivedStateOf {
            transitionProgress >= 1f
        }
    }

    val isPagerSectionVisible by remember {
        derivedStateOf {
            lazyListState.layoutInfo.visibleItemsInfo.any { it.key == "pager_section" }
        }
    }

    val currentPage = pagerState.currentPage
    val showAddButton = uiState.isOwnProfile && isPagerSectionVisible && when (currentPage) {
        0 -> uiState.userGalleryItems.isNotEmpty()
        1 -> uiState.videoItems.isNotEmpty()
        else -> false
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val viewportHeight = this.maxHeight

        CompositionLocalProvider(
            LocalOverscrollConfiguration provides null
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.backgroundLight)
                    .hazeSource(hazeState),
            ) {
                item(key = "header") {
                    ProfileHeadlineContent(
                        modifier = Modifier.height(screenHeight * 0.5f),
                        uiState = uiState,
                        onEditLinksClicked = { onIntent(ProfileIntent.OnEditLinksClicked) },
                        showWorkHistory = { onIntent(ProfileIntent.OnShowWorkHistory) },
                        onStatsClicked = { stat ->
                            selectedStat = stat
                            showStatsSheet = true
                        },
                        onSocialLinkClicked = { onIntent(ProfileIntent.OpenSocialLink(it)) },
                        onSendDMClicked = { }, //TODO
                        onReady = { onIntent(ProfileIntent.OnBackgroundReady) }
                    )
                }

                if (uiState.isModel) {
                    item(key = "info_tabs") {
                        Spacer(Modifier.height(20.dp))
                        ProfileInfoTabs(
                            pagerInfoState = pagerInfoState,
                            destinationInfoTabs = uiState.destinationInfoTabs,
                        )
                    }
                }

                item(key = "info_pager") {
                    ProfileInfoPager(
                        isModel = uiState.isModel,
                        pagerInfoState = pagerInfoState,
                        bio = uiState.userInfo.model?.description.orEmpty(),
                        physicalParams = uiState.physicalParams,
                        agency = uiState.userInfo.model?.agency,
                        isOwnProfile = uiState.isOwnProfile,
                        onWorkHistoryClicked = { onIntent(ProfileIntent.OnShowWorkHistory) },
                        onAppearanceClicked = { onIntent(ProfileIntent.OnShowAppearances) },
                        onEditClicked = { onIntent(ProfileIntent.OnEditClicked(it)) }
                    )
                }

                if (!uiState.isVisibleSocialLinks) {
                    item(key = "social_links") {
                        SocialLinksSection(
                            instagram = uiState.instagramUser,
                            tiktok = uiState.tiktokUser,
                            youtube = uiState.youtubeUser,
                            website = uiState.website,
                            isOwnProfile = uiState.isOwnProfile,
                            onEditLinksClicked = { onIntent(ProfileIntent.OnEditLinksClicked) },
                            onSocialLinkClicked = { onIntent(ProfileIntent.OpenSocialLink(it)) }
                        )
                    }
                }

                item(key = "pager_section") {
                    Box(
                        modifier = Modifier
                            .height(maxOf(0.dp, viewportHeight - toolbarHeight + spacerPreDp))
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(pagerNestedScrollConnection)
                                .background(AppTheme.colors.backgroundLight),
                        ) { page ->
                            when (page) {
                                0 -> PortfolioGrid(
                                    topPadding = totalTopPaddingDp,
                                    portfolioItems = uiState.userGalleryItems,
                                    similarItems = uiState.similarProfiles,
                                    isUploading = uiState.mediaUploading,
                                    isOwnProfile = uiState.isOwnProfile,
                                    isModel = uiState.isModel,
                                    isLoadingMore = uiState.isLoadingMoreImages,
                                    isFirstLoading = uiState.isLoadingImages,
                                    hasMore = uiState.hasMoreImages,
                                    onLoadMore = { onIntent(ProfileIntent.OnLoadMorePortfolio) },
                                    onPhotoClicked = { onIntent(ProfileIntent.OnGalleryClicked(it, false)) },
                                    onSimilarClicked = { onIntent(ProfileIntent.OnProfileClicked(it)) },
                                    onImageSelected = { onIntent(ProfileIntent.OnPortfolioPhotoSelected(context.uriToFile(it))) }
                                )
                                1 -> {
                                    val isPageActive = pagerState.currentPage == 1

                                    VideoGrid(
                                        topPadding = totalTopPaddingDp,
                                        videoItems = uiState.videoItems,
                                        isOwnProfile = uiState.isOwnProfile,
                                        isLoadingMore = uiState.isLoadingMoreVideos,
                                        isFirstLoading = uiState.isLoadingVideos,
                                        isActive = isPageActive,
                                        hasMore = uiState.hasMoreVideos,
                                        isUploading = uiState.mediaUploading,
                                        onLoadMore = { onIntent(ProfileIntent.OnLoadMoreVideos) },
                                        onVideoClicked = { onIntent(ProfileIntent.OnGalleryClicked(it, true)) },
                                        onVideoSelected = { onIntent(ProfileIntent.OnVideoSelected(context.uriToFile(it))) }
                                    )
                                }
                                2 -> if (uiState.isModel) {
                                    ChannelsContent(title = "Vogue Inside", isOwnProfile = uiState.isOwnProfile, isModel = uiState.isModel, topPadding = totalTopPaddingDp)
                                } else {
                                    AgencyModels(
                                        topPadding = totalTopPaddingDp,
                                        models = uiState.agencyModels,
                                        //query = uiState.searchModelsQuery,
                                        //searchModels = uiState.searchModels,
                                        isOwnProfile = uiState.isOwnProfile,
                                        //isLoadingMore = uiState.isLoadingMoreSearchModels,
                                        //hasMore = uiState.hasMoreSearchModels,
                                        //onQueryChanged = { onIntent(ProfileIntent.OnSearchModelsQueryChanged(it)) },
                                        onAddModelClicked = { onIntent(ProfileIntent.OnAddModelClicked) },
                                        onModelClicked = { onIntent(ProfileIntent.OnProfileClicked(it)) },
                                        onLoadMoreAgencyModels = { onIntent(ProfileIntent.OnLoadMoreAgencyModels) }
                                    )
                                }
                                3 -> ChannelsContent(title = "Vogue Inside", isOwnProfile = uiState.isOwnProfile, isModel = uiState.isModel, topPadding = totalTopPaddingDp)
                                else -> EventsColumn(
                                    topPadding = totalTopPaddingDp,
                                    events = uiState.events,
                                    isOwnProfile = uiState.isOwnProfile,
                                    isModel = uiState.isModel,
                                    isLoading = uiState.isLoadingEvents,
                                    isLoadingMore = uiState.isLoadingMoreEvents,
                                    onLoadMore = { onIntent(ProfileIntent.OnLoadMoreEvents) },
                                    onEventClicked = { onIntent(ProfileIntent.OnEventClicked(it)) },
                                    onEventCreate = { onIntent(ProfileIntent.OnEventCreate) }
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    alpha = if (isTabsPinned && hasTabs) 0f else 1f
                                }
                        ) {
                            Spacer(Modifier.height(spacerPreDp))
                            if (hasTabs) {
                                TabContainer(
                                    pagerState = pagerState,
                                    destinations = uiState.destinationTabs,
                                    tabWidth = 60.dp
                                )
                            }
                            Spacer(Modifier.height(spacerPostDp))
                        }
                    }
                }
            }
        }

        ToolBarBackground(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f),
            transitionProgress = transitionProgress,
            hazeState = hazeState,
            lowerTabsOffsetPx = lowerTabsOffsetPx,
            isTabsPinned = isTabsPinned
        )

        val openGalleryForBg = rememberGalleryLauncher { uri ->
            onIntent(ProfileIntent.OnBackgroundPhotoSelected(context.uriToFile(uri)))
        }

        ToolBarContent(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(3f),
            uiState = uiState,
            transitionProgress = transitionProgress,
            isSolid = isToolbarSolid,
            isOwnProfile = uiState.isOwnProfile,
            onEditSocialLinksClicked = { onIntent(ProfileIntent.OnEditLinksClicked) },
            onEditProfileClicked = { onIntent(ProfileIntent.OnEditClicked(0)) },
            onEditBackgroundClicked = { openGalleryForBg() },
            onManageWorkExperienceClicked = { onIntent(ProfileIntent.OnShowWorkHistory) },
            onNavigateBack = { onIntent(ProfileIntent.OnNavigateBack) },
            onFindSimilarProfiles = { onIntent(ProfileIntent.OnFindSimilarProfiles) }
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = toolbarHeight)
                .zIndex(3f)
                .graphicsLayer {
                    alpha = if (isTabsPinned && hasTabs) 1f else 0f
                }
        ) {
            TabContainer(
                pagerState = pagerState,
                destinations = uiState.destinationTabs,
                tabWidth = 60.dp,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(4f)
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 12.dp
                )
        ) {
            AnimatedVisibility(
                visible = showAddButton,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                PortfolioAddButton(
                    isUploading = uiState.mediaUploading,
                    isVideo = pagerState.currentPage == 1,
                    onMediaSelected = { uri: Uri ->
                        if (pagerState.currentPage == 1) {
                            onIntent(ProfileIntent.OnVideoSelected(context.uriToFile(uri)))
                        } else {
                            onIntent(ProfileIntent.OnPortfolioPhotoSelected(context.uriToFile(uri)))
                        }
                    }
                )
            }
        }
    }
}

private class PagerNestedScrollConnection(
    val lazyListState: LazyListState,
    val isHeaderCollapsed: () -> Boolean
) : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (available.y < 0 && !isHeaderCollapsed()) {
            val consumed = lazyListState.dispatchRawDelta(-available.y)
            return Offset(0f, -consumed)
        }
        return Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        if (available.y > 0 && isHeaderCollapsed()) {
            val consumed = lazyListState.dispatchRawDelta(-available.y)
            return Offset(0f, -consumed)
        }
        return Offset.Zero
    }
}