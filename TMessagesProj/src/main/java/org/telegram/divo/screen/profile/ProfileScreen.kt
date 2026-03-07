package org.telegram.divo.screen.profile

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.exoplayer2.util.Log
import org.telegram.divo.common.LaunchedEffectOnce
import org.telegram.divo.common.rememberGalleryLauncher
import org.telegram.divo.common.uriToFile
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.TelegramPhotoBackground
import org.telegram.divo.components.items.ProfileNameItem
import org.telegram.divo.screen.profile.components.BiographyAppearanceSection
import org.telegram.divo.screen.profile.components.EngagementStatsBottomSheet
import org.telegram.divo.screen.profile.components.EngagementStatsRow
import org.telegram.divo.screen.profile.components.PortfolioGrid
import org.telegram.divo.screen.profile.components.SocialLinksSection
import org.telegram.divo.screen.profile.components.StatsType
import org.telegram.divo.screen.profile.components.TabContainer
import org.telegram.divo.screen.profile.components.ToolBar

@Composable
fun ProfileScreen(
    userId: Int,
    isOwnProfile: Boolean = false,
    viewModel: ProfileViewModel = viewModel(),
    onEditClicked: () -> Unit = {},
    onEditLinksClicked: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    showWorkHistory: () -> Unit = {},
    onPhotoClicked: (String) -> Unit = {},
    onProfileClicked: (Int) -> Unit = {},
) {
    val context = LocalContext.current

    LaunchedEffectOnce {
        viewModel.setIntent(ProfileIntent.OnLoad(userId, isOwnProfile))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileEffect.OpenUrl -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(effect.url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Handle error
                    }
                }
                is ProfileEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    val uiState = viewModel.state.collectAsState().value

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LottieProgressIndicator(
                modifier = Modifier.size(32.dp).align(Alignment.Center),
            )
        }
    } else {
        ProfileScreenContent(
            uiState = uiState,
            onEditClicked = {
                onEditClicked()
            },
            onEditLinksClicked = {
                onEditLinksClicked()
            },
            onNavigateBack = onNavigateBack,
            showWorkHistory = showWorkHistory,
            onProfileClicked = onProfileClicked,
            onEditBackgroundClicked = {
                Log.d("MyTag", "$it")
                viewModel.setIntent(ProfileIntent.OnBackgroundPhotoSelected(context.uriToFile(it)))
            },
            onPhotoClicked = onPhotoClicked,
            onSocialLinkClicked = { url ->  }, //viewModel.openSocialLink(url)
            onStatsClicked = { viewModel.setIntent(ProfileIntent.OnLoadEngagementStats(it)) },
            onLoadMore = { viewModel.setIntent(ProfileIntent.OnLoadMoreEngagementStats(it)) },
            onQueryChanged = { viewModel.setIntent(ProfileIntent.OnSearchQueryChanged(it)) },
            onLoadMoreSearch = { viewModel.setIntent(ProfileIntent.OnLoadMoreSearchResults) },
            onLoadMoreImages = { viewModel.setIntent(ProfileIntent.OnLoadMorePortfolio) },
            onImageSelected = {
                viewModel.setIntent(ProfileIntent.OnPortfolioPhotoSelected(context.uriToFile(it)))
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    uiState: ProfileViewState,
    onEditClicked: () -> Unit = {},
    onEditLinksClicked: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    showWorkHistory: () -> Unit = {},
    onPhotoClicked: (String) -> Unit = {},
    onProfileClicked: (Int) -> Unit = {},
    onEditBackgroundClicked: (Uri) -> Unit = {},
    onSocialLinkClicked: (String) -> Unit = {},
    onStatsClicked: (StatsType) -> Unit = {},
    onLoadMore: (StatsType) -> Unit = {},
    onLoadMoreImages: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onLoadMoreSearch: () -> Unit,
    onImageSelected: (Uri) -> Unit,
) {
    val pageCount = if (uiState.isOwnProfile) 2 else 3
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
            val layoutInfo = lazyListState.layoutInfo
            val pagerItem = layoutInfo.visibleItemsInfo.find { it.key == "pager" }
            pagerItem?.offset?.toFloat()?.coerceAtLeast(0f) ?: 0f
        }
    }

    val pagerNestedScrollConnection = remember {
        PagerNestedScrollConnection(lazyListState, isHeaderCollapsed)
    }

    var showStatsSheet by remember { mutableStateOf(false) }
    var selectedStat by remember { mutableStateOf<StatsType?>(null) }

    val openGallery = rememberGalleryLauncher { uri ->
        onEditBackgroundClicked(uri)
    }

    if (showStatsSheet) {
        EngagementStatsBottomSheet(
            stats = selectedStat,
            feeds = uiState.feedItems,
            isLoadingMoreFeed = uiState.isLoadingMoreFeed,
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
        uiState.userInfo?.photoUrl?.let { url ->
            TelegramPhotoBackground(
                photo = url,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (uiState.backgroundChanging) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 134.dp)) {
                LottieProgressIndicator(
                    modifier = Modifier.size(32.dp).align(Alignment.Center),
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
                onManageWorkExperienceClicked = {},
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
                                        similarItems = uiState.similarModels,
                                        isUploading = uiState.portfolioUploading,
                                        isOwnProfile = uiState.isOwnProfile,
                                        isLoadingMore = uiState.isLoadingMoreImages,
                                        hasMore = uiState.hasMoreImages,
                                        onLoadMore = onLoadMoreImages,
                                        onPhotoClicked = onPhotoClicked,
                                        onSimilarClicked = onProfileClicked,
                                        onImageSelected = onImageSelected
                                    )

                                    else -> EmptyGridPlaceholder()
                                }
                            }
                        }
                    }
                }

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
                    isOwnProfile = uiState.isOwnProfile
                )
            }
        }
    }
}

@Composable
private fun EmptyGridPlaceholder() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) { }
}

@Composable
private fun ProfileHeaderContent(
    modifier: Modifier = Modifier,
    uiState: ProfileViewState,
    onEditLinksClicked: () -> Unit,
    showWorkHistory: () -> Unit,
    onStatsClicked: (StatsType) -> Unit,
    onSocialLinkClicked: (String) -> Unit = {}
) {
    var selectedBioTab by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = modifier
    ) {
        ProfileNameItem(
            modifier = Modifier.padding(top = 150.dp),
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

        BiographyAppearanceSection(
            selectedTab = selectedBioTab,
            onTabSelected = { selectedBioTab = it },
            uiState = uiState
        )

        SocialLinksSection(
            socialLinks = uiState.socialLinks,
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
