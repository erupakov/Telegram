package org.telegram.divo.screen.profile

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.components.PortfolioUploadPreview
import org.telegram.divo.components.TelegramPhotoBackground
import org.telegram.divo.components.items.ProfileNameItem
import org.telegram.divo.entity.UserGalleryItem
import org.telegram.divo.screen.profile.components.BiographyAppearanceSection
import org.telegram.divo.screen.profile.components.EngagementStatsRow
import org.telegram.divo.screen.profile.components.SocialLinksSection
import org.telegram.divo.screen.profile.components.TabContainer
import org.telegram.divo.screen.profile.components.ToolBar
import org.telegram.divo.style.AppTheme

@Composable
fun ProfileScreen(
    userId: Int,
    isOwnProfile: Boolean = false,
    viewModel: ProfileViewModel = viewModel(),
    onEditClicked: () -> Unit = {},
    onEditLinksClicked: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    showWorkHistory: () -> Unit = {},
    onAddPortfolioClicked: () -> Unit = {},
    onEditBackgroundClicked: () -> Unit = {}
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.setIntent(ProfileIntent.OnLoad(userId, isOwnProfile))
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
            CircularProgressIndicator()
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
            onAddPortfolioClicked = onAddPortfolioClicked,
            onEditBackgroundClicked = onEditBackgroundClicked,
            onSocialLinkClicked = { url ->  } //viewModel.openSocialLink(url)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    uiState: ProfileViewState,
    onEditClicked: () -> Unit = {},
    onEditLinksClicked: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    showWorkHistory: () -> Unit = {},
    onAddPortfolioClicked: () -> Unit = {},
    onEditBackgroundClicked: () -> Unit = {},
    onSocialLinkClicked: (String) -> Unit = {},
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val tabHeightDp = 48.dp
    val density = LocalDensity.current

    var headerHeightPx by remember { mutableStateOf(0f) }
    var headerOffsetPx by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val previous = headerOffsetPx
                headerOffsetPx = (headerOffsetPx + delta).coerceIn(-headerHeightPx, 0f)
                val consumed = headerOffsetPx - previous
                return Offset(0f, consumed)
            }
        }
    }

    val tabOffsetY = (headerHeightPx + headerOffsetPx).coerceAtLeast(0f)
    val titleVisible = headerOffsetPx < -40f

    uiState.userInfo?.photoUrl?.let { url ->
        TelegramPhotoBackground(
            photo = url,
            modifier = Modifier.fillMaxSize()
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 36.dp)
    ) {
        ToolBar(
            uiState = uiState,
            titleVisible = titleVisible,
            modifier = Modifier.background(Color.Transparent).zIndex(2f),
            onEditClicked = onEditClicked,
            onNavigateBack = onNavigateBack,
            onEditBackgroundClicked = onEditBackgroundClicked
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RectangleShape)
                .nestedScroll(nestedScrollConnection)
        ) {
            ProfileHeaderContent(
                modifier = Modifier
                    .onGloballyPositioned {
                        headerHeightPx = it.size.height.toFloat()
                    }
                    .offset { IntOffset(0, headerOffsetPx.toInt()) }
                    .padding(bottom = 16.dp)
                    .zIndex(0f),
                uiState = uiState,
                onEditLinksClicked = onEditLinksClicked,
                showWorkHistory = showWorkHistory,
                onSocialLinkClicked = onSocialLinkClicked
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = tabHeightDp)
                    .offset {
                        IntOffset(0, tabOffsetY.toInt())
                    }
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) { page ->
                    when (page) {
                        0 -> PortfolioGrid(
                            portfolioItems = uiState.userGalleryItems,
                            isUploading = uiState.portfolioUploading,
                            uploadLocalPath = "uiState.portfolioUploadLocalPath",
                            onAddClicked = onAddPortfolioClicked,
                            dialogId = 0
                        )
                        else -> LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {}
                    }
                }
            }

            TabContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, tabOffsetY.toInt()) }
                    .zIndex(1f)
            )
        }
    }
}

@Composable
private fun ProfileHeaderContent(
    modifier: Modifier = Modifier,
    uiState: ProfileViewState,
    onEditLinksClicked: () -> Unit,
    showWorkHistory: () -> Unit,
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
            onClicked = {}
        )

        // Biography / Appearance Tabs
        BiographyAppearanceSection(
            selectedTab = selectedBioTab,
            onTabSelected = { selectedBioTab = it },
            uiState = uiState
        )

        // Social Links Section
        SocialLinksSection(
            socialLinks = uiState.socialLinks,
            onEditLinksClicked = onEditLinksClicked,
            onSocialLinkClicked = onSocialLinkClicked
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun PortfolioGrid(
    portfolioItems: List<UserGalleryItem>,
    isUploading: Boolean,
    uploadLocalPath: String?,
    onAddClicked: () -> Unit,
    dialogId: Long,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
    ) {
//        item {
//            PortfolioAddButton(
//                isUploading = isUploading,
//                uploadLocalPath = uploadLocalPath,
//                onClick = onAddClicked
//            )
//        }

        items(portfolioItems) { item ->
            DivoAsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                url = item.previewUrl
            )
        }
    }
}

@Composable
private fun PortfolioAddButton(
    isUploading: Boolean,
    uploadLocalPath: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(AppTheme.colors.blackAlpha12)
            .clickable(enabled = !isUploading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when {
            isUploading && uploadLocalPath != null -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    PortfolioUploadPreview(
                        filePath = uploadLocalPath,
                        modifier = Modifier.fillMaxSize(),
                        cornerRadiusDp = 8
                    )
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center),
                        color = AppTheme.colors.accentColor,
                        strokeWidth = 3.dp
                    )
                }
            }
            isUploading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = AppTheme.colors.accentColor,
                    strokeWidth = 3.dp
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add photo",
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ProfilePhotoPage(
    modifier: Modifier = Modifier,
) {
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileCollapsingScreen(
    modifier: Modifier = Modifier,
    uiState: ProfileViewState,
    onEditClicked: () -> Unit = {},
    onEditLinksClicked: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    val tabsHeight = 52.dp
    val headerMax = 260.dp
    val headerMin = 96.dp

    val density = LocalDensity.current
    val headerMaxPx = with(density) { headerMax.toPx() }
    val headerMinPx = with(density) { headerMin.toPx() }
    val collapseRange = headerMaxPx - headerMinPx
    var collapseOffsetPx by remember { mutableFloatStateOf(0f) } // 0..collapseRange

    // Nested scroll: сначала схлопываем header, потом отдаём скролл списку
    val nestedScroll = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                if (dy == 0f) return Offset.Zero

                // dy < 0 = скролл вверх (схлопнуть), dy > 0 = вниз (раскрыть)
                val newOffset = (collapseOffsetPx - dy).coerceIn(0f, collapseRange)
                val consumed = newOffset - collapseOffsetPx
                collapseOffsetPx = newOffset

                // мы "потребили" часть скролла на схлопывание/раскрытие header
                return Offset(0f, -consumed)
            }
        }
    }

    val headerHeightPx = headerMaxPx - collapseOffsetPx
    val headerHeightDp = with(density) { headerHeightPx.toDp() }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

    //val name = (uiState.userFull.user.first_name ?: "") + " " + (uiState.userFull.user.last_name ?: "")

    // синхронизация tab -> pager
    LaunchedEffect(selectedTab) { pagerState.animateScrollToPage(selectedTab) }
    // синхронизация pager -> tab
    LaunchedEffect(pagerState.currentPage) { selectedTab = pagerState.currentPage }


//    Column {
//        ToolBar(
//            title = name,
//            titleVisible = true, //titleVisible,
//            modifier = Modifier
//                .background(Color.Transparent),
//            onEditClicked = {
//                onEditClicked()
//            },
//            onNavigateBack = {
//                onNavigateBack()
//            }
//        )
    Box(
        Modifier
            .fillMaxSize()
            .nestedScroll(nestedScroll)
    ) {

        Column(Modifier.fillMaxSize()) {

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(headerHeightDp)
            ) {
//                    ProfileHeaderContent(
//                        uiState,
//                        onEditLinksClicked,
//                        showWorkHistory
//                    )

            }

            TabContainer(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(tabsHeight)
            )

            // PAGER (занимает оставшееся место)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                // ВАЖНО: каждая страница обычно со своим LazyColumn
                val listState = rememberLazyListState()
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(50) { idx ->
                        Text("Tab $page item $idx", modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
//    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyWithPagerInside() {
    val pagerState = rememberPagerState(pageCount = { 3 })

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Text("HEADER", Modifier.align(Alignment.Center))
            }
        }

        stickyHeader {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("TABS", Modifier.align(Alignment.Center))
            }
        }

        item {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillParentMaxHeight() // 🔑 ключ
            ) { page ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(50) {
                        Text(
                            "Page $page item $it",
                            Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
