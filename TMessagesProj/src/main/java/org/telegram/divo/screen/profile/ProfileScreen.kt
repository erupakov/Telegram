package org.telegram.divo.screen.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.common.LaunchedEffectOnce
import org.telegram.divo.common.LockScreenOrientation
import org.telegram.divo.common.rememberGalleryLauncher
import org.telegram.divo.common.utils.uriToFile
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.TelegramPhotoBackground
import org.telegram.divo.components.items.ProfileNameItem
import org.telegram.divo.screen.profile.components.AgencyInfoSection
import org.telegram.divo.screen.profile.components.BiographyAppearanceSection
import org.telegram.divo.screen.profile.components.EngagementStatsBottomSheet
import org.telegram.divo.screen.profile.components.EngagementStatsRow
import org.telegram.divo.screen.profile.components.DivoColumnContent
import org.telegram.divo.screen.profile.components.PortfolioGrid
import org.telegram.divo.screen.profile.components.SocialLinksSection
import org.telegram.divo.screen.profile.components.StatsType
import org.telegram.divo.screen.profile.components.TabContainer
import org.telegram.divo.screen.profile.components.ToolBar
import org.telegram.divo.screen.profile.components.VideoGrid
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.common.SnackbarEvent
import org.telegram.divo.entity.RoleType
import org.telegram.divo.entity.SocialNetworkType
import org.telegram.divo.screen.profile.components.AgencyDescriptionSection
import org.telegram.divo.screen.profile.components.AgencyModels
import org.telegram.divo.screen.profile.components.EventsColumn
import org.telegram.messenger.R

@Composable
fun ProfileScreen(
    userId: Int,
    isOwnProfile: Boolean = false,
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.factory(userId, isOwnProfile)
    ),
    onEditClicked: (Boolean) -> Unit = {},
    onEditLinksClicked: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    showWorkHistory: (Boolean) -> Unit = {},
    onGalleryClicked: (String, Boolean) -> Unit = { _, _ -> },
    onProfileClicked: (Int) -> Unit = {},
    onAddModelClicked: () -> Unit,
    onEventClicked: (Int) -> Unit,
) {
    val context = LocalContext.current
    val snackbarState = remember { AppSnackbarHostState() }
    val retryText = stringResource(R.string.RetryLabel)

    LaunchedEffectOnce {
        viewModel.setIntent(ProfileIntent.OnLoad)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileEffect.OpenUrl -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, effect.url.toUri())
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        snackbarState.show(SnackbarEvent.Error(e.message.orEmpty()))
                    }
                }
                is ProfileEffect.ShowError -> {
                    snackbarState.show(
                        SnackbarEvent.ErrorWithRetry(effect.message, retryText) {
                            viewModel.setIntent(
                                ProfileIntent.OnLoad
                            )
                        }
                    )
                }
                else -> {}
            }
        }
    }

    val uiState = viewModel.state.collectAsState().value

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            LottieProgressIndicator(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Center),
            )
        } else {
            ProfileScreenContent(
                uiState = uiState,
                onEditClicked = {
                    onEditClicked(uiState.isModel)
                },
                onEditLinksClicked = {
                    onEditLinksClicked()
                },
                onNavigateBack = onNavigateBack,
                showWorkHistory = { showWorkHistory(isOwnProfile) },
                onProfileClicked = onProfileClicked,
                onEditBackgroundClicked = {
                    viewModel.setIntent(ProfileIntent.OnBackgroundPhotoSelected(context.uriToFile(it)))
                },
                onGalleryClicked = { url, isVideo -> onGalleryClicked(url, isVideo) },
                onSocialLinkClicked = { viewModel.setIntent(ProfileIntent.OpenSocialLink(it)) },
                onLoadMore = { viewModel.setIntent(ProfileIntent.OnLoadMoreEngagementStats(it)) },
                onQueryChanged = { viewModel.setIntent(ProfileIntent.OnSearchQueryChanged(it)) },
                onLoadMoreSearch = { viewModel.setIntent(ProfileIntent.OnLoadMoreSearchResults) },
                onLoadMoreImages = { viewModel.setIntent(ProfileIntent.OnLoadMorePortfolio) },
                onImageSelected = {
                    viewModel.setIntent(ProfileIntent.OnPortfolioPhotoSelected(context.uriToFile(it)))
                },
                onVideoSelected = {
                    viewModel.setIntent(ProfileIntent.OnVideoSelected(context.uriToFile(it)))
                },
                onLoadMoreVideos = { viewModel.setIntent(ProfileIntent.OnLoadMoreVideos) },
                onAddModelClicked = onAddModelClicked,
                onLoadMoreEvents = { viewModel.setIntent(ProfileIntent.OnLoadMoreEvents) },
                onEventClicked = onEventClicked,
            )
        }

        AppSnackbarHost(
            modifier = Modifier.align(Alignment.BottomCenter),
            state = snackbarState,
            bottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 8.dp
        )
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    uiState: ProfileViewState,
    onEditClicked: () -> Unit = {},
    onEditLinksClicked: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    showWorkHistory: () -> Unit = {},
    onGalleryClicked: (String, Boolean) -> Unit = { _, _ -> },
    onProfileClicked: (Int) -> Unit = {},
    onEditBackgroundClicked: (Uri) -> Unit = {},
    onSocialLinkClicked: (SocialNetworkType) -> Unit = {},
    onStatsClicked: (StatsType) -> Unit = {},
    onLoadMore: (StatsType) -> Unit = {},
    onLoadMoreImages: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onLoadMoreSearch: () -> Unit,
    onImageSelected: (Uri) -> Unit,
    onVideoSelected: (Uri) -> Unit,
    onLoadMoreVideos: () -> Unit,
    onAddModelClicked: () -> Unit,
    onLoadMoreEvents: () -> Unit,
    onEventClicked: (Int) -> Unit,
) {
    val pageCount = uiState.pageCount
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val lazyListState = rememberLazyListState()

    val tabBarHeightDp = 48.dp

    val isHeaderCollapsed by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex >= 1
        }
    }

    val tabBarOffsetY by remember {
        derivedStateOf {
            lazyListState.layoutInfo.visibleItemsInfo
                .firstOrNull { it.key == "pager" }
                ?.offset?.toFloat() ?: 0f
        }
    }
    val pagerNestedScrollConnection = remember(isHeaderCollapsed) {
        PagerNestedScrollConnection(lazyListState, isHeaderCollapsed)
    }

    var showStatsSheet by remember { mutableStateOf(false) }
    var selectedStat by remember { mutableStateOf<StatsType?>(null) }

    val openGallery = rememberGalleryLauncher { uri ->
        onEditBackgroundClicked(uri)
    }
    LockScreenOrientation()

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
            onQueryChanged = onQueryChanged,
            onLoadMoreSearch = onLoadMoreSearch,
            onProfileClicked = { onProfileClicked(it) },
            onLoadMore = { onLoadMore(selectedStat ?: StatsType.LIKES) },
            onDismiss = { showStatsSheet = false }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { } }
    ) {
        TelegramPhotoBackground(
            photo = uiState.userInfo.photoUrl,
            modifier = Modifier.fillMaxSize()
        )

        if (uiState.backgroundChanging) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 134.dp)) {
                LottieProgressIndicator(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center),
                    color = Color.White
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            ToolBar(
                uiState = uiState,
                lazyListState = lazyListState,
                isOwnProfile = uiState.isOwnProfile,
                modifier = Modifier.padding(top = 36.dp),
                onEditSocialLinksClicked = onEditLinksClicked,
                onEditProfileClicked = onEditClicked,
                onEditBackgroundClicked = {
                    openGallery()
                },
                onManageWorkExperienceClicked = showWorkHistory,
                onNavigateBack = onNavigateBack,
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
            ) {
                CompositionLocalProvider(
                    LocalOverscrollConfiguration provides null
                ) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        item(key = "header") {
                            ProfileHeaderContent(
                                modifier = Modifier.padding(bottom = 16.dp),
                                uiState = uiState,
                                onEditLinksClicked = onEditLinksClicked,
                                showWorkHistory = showWorkHistory,
                                onStatsClicked = { stat ->
                                    selectedStat = stat
                                    showStatsSheet = true
                                    onStatsClicked(stat)
                                },
                                onSocialLinkClicked = onSocialLinkClicked,
                            )
                        }

                        item(key = "pager") {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillParentMaxHeight()
                                    .padding(top = tabBarHeightDp)
                                    .nestedScroll(pagerNestedScrollConnection)
                                    .background(
                                        if (uiState.isOwnProfile) Color.Transparent
                                        else Color(0xFFF6F6F6)
                                    ),
                            ) { page ->
                                when (page) {
                                    0 -> PortfolioGrid(
                                        portfolioItems = uiState.userGalleryItems,
                                        similarItems = uiState.agencyModels,
                                        isUploading = uiState.mediaUploading,
                                        isOwnProfile = uiState.isOwnProfile,
                                        isLoadingMore = uiState.isLoadingMoreImages,
                                        isFirstLoading = uiState.isLoadingImages,
                                        hasMore = uiState.hasMoreImages,
                                        onLoadMore = onLoadMoreImages,
                                        onPhotoClicked = { onGalleryClicked(it, false) },
                                        onSimilarClicked = onProfileClicked,
                                        onImageSelected = onImageSelected
                                    )
                                    1 -> {
                                        val isPageActive = pagerState.currentPage == 1

                                        VideoGrid(
                                            videoItems = uiState.videoItems,
                                            isOwnProfile = uiState.isOwnProfile,
                                            isLoadingMore = uiState.isLoadingMoreVideos,
                                            isFirstLoading = uiState.isLoadingVideos,
                                            isActive = isPageActive,
                                            hasMore = uiState.hasMoreVideos,
                                            isUploading = uiState.mediaUploading,
                                            onLoadMore = onLoadMoreVideos,
                                            onVideoClicked = { onGalleryClicked(it, true) },
                                            onVideoSelected = onVideoSelected
                                        )
                                    }
                                    2 -> if (uiState.isModel) {
                                        DivoColumnContent("Vogue Inside")
                                    } else {
                                        AgencyModels(
                                            models = uiState.agencyModels,
                                            isOwnProfile = uiState.isOwnProfile,
                                            isLoadingMoreModels = false, //TODO
                                            onAddModelClicked = onAddModelClicked,
                                            onModelClicked = onProfileClicked,
                                            onLoadMoreAgencyModels = {}  //TODO
                                        )
                                    }
                                    3 -> DivoColumnContent("Vogue Inside")
                                    else -> EventsColumn(
                                        events = uiState.events,
                                        isOwnProfile = uiState.isOwnProfile,
                                        isModel = uiState.isModel,
                                        isLoading = uiState.isLoadingEvents,
                                        isLoadingMore = uiState.isLoadingMoreEvents,
                                        onLoadMore = onLoadMoreEvents,
                                        onEventClicked = onEventClicked
                                    )
                                }
                            }
                        }
                    }
                }

                if (uiState.userInfo.role != RoleType.UNKNOWN) {
                    TabContainer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tabBarHeightDp)
                            .graphicsLayer {
                                translationY = tabBarOffsetY
                            }
                            .zIndex(1f),
                        lazyListState = lazyListState,
                        pagerState = pagerState,
                        destinations = uiState.destinationTabs
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeaderContent(
    modifier: Modifier = Modifier,
    uiState: ProfileViewState,
    onEditLinksClicked: () -> Unit,
    showWorkHistory: () -> Unit,
    onStatsClicked: (StatsType) -> Unit,
    onSocialLinkClicked: (SocialNetworkType) -> Unit = {}
) {
    var selectedBioTab by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = modifier
    ) {
        ProfileNameItem(
            modifier = Modifier.padding(top = 140.dp),
            uiState
        )

//        ButtonAddWorkHistory(
//            modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp),
//            onClick = {
//                showWorkHistory()
//            }
//        )

        Spacer(modifier = Modifier.height(20.dp))

        EngagementStatsRow(
            stats = uiState.statistic,
            isOwnProfile = uiState.isOwnProfile,
            onClicked = {},
            onStatsClicked = onStatsClicked
        )

        if (uiState.isModel) {
            BiographyAppearanceSection(
                selectedTab = selectedBioTab,
                onTabSelected = { selectedBioTab = it },
                uiState = uiState
            )
        } else {
            uiState.userInfo.agency?.description?.let {
                AgencyDescriptionSection(
                    text = it
                )
            }
        }

        if (uiState.userInfo.model?.agency != null) {
            AgencyInfoSection(
                title = uiState.userInfo.model.agency.title,
                photoUrl = uiState.userInfo.model.agency.photo?.fullUrl.orEmpty(),
                onClicked = showWorkHistory
            )
        }

        SocialLinksSection(
            instagram = uiState.instagramUser,
            tiktok = uiState.tiktokUser,
            youtube = uiState.youtubeUser,
            website = uiState.website,
            isOwnProfile = uiState.isOwnProfile,
            onEditLinksClicked = onEditLinksClicked,
            onSocialLinkClicked = onSocialLinkClicked
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

private class PagerNestedScrollConnection(
    val lazyListState: LazyListState,
    val isHeaderCollapsed: Boolean
) : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (available.y < 0 && !isHeaderCollapsed) {
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
        if (available.y > 0 && isHeaderCollapsed) {
            val consumed = lazyListState.dispatchRawDelta(-available.y)
            return Offset(0f, -consumed)
        }
        return Offset.Zero
    }
}

//TODO возможно следует удалить
//@Composable
//private fun ProfilePhotoPage(
//    modifier: Modifier = Modifier,
//) {
//}

//TODO возможно следует удалить
//@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
//@Composable
//fun ProfileCollapsingScreen(
//    modifier: Modifier = Modifier,
//    uiState: ProfileViewState,
//    onEditClicked: () -> Unit = {},
//    onEditLinksClicked: () -> Unit = {},
//    onNavigateBack: () -> Unit = {},
//) {
//    val tabsHeight = 52.dp
//    val headerMax = 260.dp
//    val headerMin = 96.dp
//
//    val density = LocalDensity.current
//    val headerMaxPx = with(density) { headerMax.toPx() }
//    val headerMinPx = with(density) { headerMin.toPx() }
//    val collapseRange = headerMaxPx - headerMinPx
//    var collapseOffsetPx by remember { mutableFloatStateOf(0f) } // 0..collapseRange
//
//    // Nested scroll: сначала схлопываем header, потом отдаём скролл списку
//    val nestedScroll = remember {
//        object : NestedScrollConnection {
//            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
//                val dy = available.y
//                if (dy == 0f) return Offset.Zero
//
//                // dy < 0 = скролл вверх (схлопнуть), dy > 0 = вниз (раскрыть)
//                val newOffset = (collapseOffsetPx - dy).coerceIn(0f, collapseRange)
//                val consumed = newOffset - collapseOffsetPx
//                collapseOffsetPx = newOffset
//
//                // мы "потребили" часть скролла на схлопывание/раскрытие header
//                return Offset(0f, -consumed)
//            }
//        }
//    }
//
//    val headerHeightPx = headerMaxPx - collapseOffsetPx
//    val headerHeightDp = with(density) { headerHeightPx.toDp() }
//
//    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
//    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
//
//    //val name = (uiState.userFull.user.first_name ?: "") + " " + (uiState.userFull.user.last_name ?: "")
//
//    // синхронизация tab -> pager
//    LaunchedEffect(selectedTab) { pagerState.animateScrollToPage(selectedTab) }
//    // синхронизация pager -> tab
//    LaunchedEffect(pagerState.currentPage) { selectedTab = pagerState.currentPage }
//
//
////    Column {
////        ToolBar(
////            title = name,
////            titleVisible = true, //titleVisible,
////            modifier = Modifier
////                .background(Color.Transparent),
////            onEditClicked = {
////                onEditClicked()
////            },
////            onNavigateBack = {
////                onNavigateBack()
////            }
////        )
//    Box(
//        Modifier
//            .fillMaxSize()
//            .nestedScroll(nestedScroll)
//    ) {
//
//        Column(Modifier.fillMaxSize()) {
//
//            Box(
//                Modifier
//                    .fillMaxWidth()
//                    .height(headerHeightDp)
//            ) {
////                    ProfileHeaderContent(
////                        uiState,
////                        onEditLinksClicked,
////                        showWorkHistory
////                    )
//
//            }
//
////            TabContainer(
//////                modifier = Modifier
//////                    .fillMaxWidth()
//////                    .height(tabsHeight)
////            )
//
//            // PAGER (занимает оставшееся место)
//            HorizontalPager(
//                state = pagerState,
//                modifier = Modifier.fillMaxSize()
//            ) { page ->
//                // ВАЖНО: каждая страница обычно со своим LazyColumn
//                val listState = rememberLazyListState()
//                LazyColumn(
//                    state = listState,
//                    modifier = Modifier.fillMaxSize(),
//                    contentPadding = PaddingValues(bottom = 24.dp)
//                ) {
//                    items(50) { idx ->
//                        Text("Tab $page item $idx", modifier = Modifier.padding(16.dp))
//                    }
//                }
//            }
//        }
//    }
////    }
//}


//TODO возможно следует удалить
//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun LazyWithPagerInside() {
//    val pagerState = rememberPagerState(pageCount = { 3 })
//
//    LazyColumn(
//        modifier = Modifier.fillMaxSize()
//    ) {
//        item {
//            Box(
//                Modifier
//                    .fillMaxWidth()
//                    .height(260.dp)
//            ) {
//                Text("HEADER", Modifier.align(Alignment.Center))
//            }
//        }
//
//        stickyHeader {
//            Box(
//                Modifier
//                    .fillMaxWidth()
//                    .height(52.dp)
//            ) {
//                Text("TABS", Modifier.align(Alignment.Center))
//            }
//        }
//
//        item {
//            HorizontalPager(
//                state = pagerState,
//                modifier = Modifier.fillParentMaxHeight() // 🔑 ключ
//            ) { page ->
//                LazyColumn(
//                    modifier = Modifier.fillMaxSize()
//                ) {
//                    items(50) {
//                        Text(
//                            "Page $page item $it",
//                            Modifier.padding(16.dp)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
