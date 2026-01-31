package org.telegram.divo.screen.profile

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.components.TelegramPhoto
import org.telegram.divo.components.TelegramUserAvatar
import org.telegram.divo.items.ButtonAddWorkHistory
import org.telegram.divo.items.PofileNameItem
import org.telegram.divo.items.ProfileBioItem
import org.telegram.divo.items.ProfileSocialItem
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ImageLocation
import org.telegram.messenger.R
import org.telegram.tgnet.TLRPC
import org.telegram.ui.Components.AvatarDrawable
import org.telegram.ui.Components.BackupImageView

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onEditClicked: () -> Unit = {},
    onEditLinksClicked: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    showWorkHistory: () -> Unit = {}
) {
    LaunchedEffect(true) {
        viewModel.getData()
    }

    val uiState = viewModel.state.collectAsState().value

//    LazyWithPagerInside()

//    ProfileCollapsingScreen(
//        uiState = uiState,
//        onEditClicked = {
//            onEditClicked()
//        },
//        onEditLinksClicked = {
//            onEditLinksClicked()
//        },
//        onNavigateBack = onNavigateBack
//    )

    ProfileScreenParts(
        uiState = uiState,
        onEditClicked = {
            onEditClicked()
        },
        onEditLinksClicked = {
            onEditLinksClicked()
        },
        onNavigateBack = onNavigateBack,
        showWorkHistory = showWorkHistory
    )
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


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileCollapsingScreen(
    modifier: Modifier = Modifier,
    uiState: ProfileViewModel.ProfileViewState,
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

    val name = (uiState.userFull.user.first_name ?: "") + " " + (uiState.userFull.user.last_name ?: "")

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
fun ProfileScreenParts(
    modifier: Modifier = Modifier,
    uiState: ProfileViewModel.ProfileViewState,
    onEditClicked: () -> Unit = {},
    onEditLinksClicked: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    showWorkHistory:() -> Unit = {},
) {
    val listState = rememberLazyListState()
    val pagerState = rememberPagerState(pageCount = { 3 })

    val titleVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 40
        }
    }

    OnePhotoTopSharpBottomBlur(
        modifier = Modifier.padding(bottom = 200.dp),
        painter = painterResource(R.drawable.divo_profile_background_test)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 36.dp),
    ) {

        val name =
            (uiState.userFull.user.first_name ?: "") + " " + (uiState.userFull.user.last_name ?: "")

        ToolBar(
            title = name,
            titleVisible = titleVisible,
            modifier = Modifier
                .background(Color.Transparent),
            onEditClicked = {
                onEditClicked()
            },
            onNavigateBack = {
                onNavigateBack()
            }
        )
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize(),
        ) {
            item {
                ProfileHeaderContent(
                    uiState,
                    onEditLinksClicked,
                    showWorkHistory
                )
            }
            stickyHeader {
                TabContainer()
            }

            item {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillParentMaxHeight().fillMaxWidth().background(color = Color.White) // 🔑 ключ
                ) { page ->
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3), // 🔑 3 в ряд
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.userPhotos) { photo ->
                            TelegramPhoto(
                                photo = photo,
                                dialogId = uiState.userFull.id,
                                modifier = Modifier,
                                sizeDp = 120
                            )
                        }
                    }
//                    LazyColumn(
//                        modifier = Modifier.fillMaxSize()
//                    ) {
//                        items(uiState.userPhotos) { item ->
//                            TelegramPhoto(
//                                photo = item,
//                                dialogId = uiState.userFull.id,
//                                modifier = Modifier.fillMaxSize(),
//                                sizeDp = 300
//                            )
//                        }
//                    }
                }
            }
        }
    }
}

@Composable
fun OnePhotoTopSharpBottomBlur(
    painter: Painter,
    modifier: Modifier = Modifier,
    blurRadius: Dp = 100.dp
) {
    Box(modifier) {

        // Слой 1: та же картинка — резкая
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Слой 2: та же картинка — blurred, но видна только снизу через градиент-маску
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(blurRadius)
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startY = size.height * 0.45f, // где начинается blur
                            endY = size.height
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
        )
    }
}


@Composable
private fun ToolBar(
    modifier: Modifier = Modifier,
    title: String,
    titleVisible: Boolean = false,
    onEditClicked: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                onNavigateBack()
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Row(modifier = Modifier.weight(1f)) {
                AnimatedVisibility(
                    titleVisible
                ) {
                    Text(title)
                }
            }
            IconButton(onClick = { onEditClicked() }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
            }
            IconButton(onClick = { /* settings/edit */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Settings", tint = Color.White)
            }
        }
    }
}


@Composable
private fun ProfileHeaderContent(
    uiState: ProfileViewModel.ProfileViewState,
    onEditLinksClicked: () -> Unit,
    showWorkHistory: ()-> Unit
) {
    Column {
        PofileNameItem(
            modifier = Modifier.padding(top = 150.dp),
            firstName = uiState.userFull.user.first_name ?: "",
            lastName = uiState.userFull.user.last_name ?: "",
            roleLabel = "Model",
            uiState
        )
//        Row(modifier = Modifier.padding(top = 16.dp)) {
//            DMButton(
//                onClick = {
//
//                }
//            )
//        }

        ButtonAddWorkHistory(
            modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp),
            onClick = {
                showWorkHistory()
            }
        )

        ProfileBioItem(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            bio = uiState.userFull.about?:""
        )

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("My Links", color = Color.White)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = {
                    onEditLinksClicked()
                }) {
                    Text("Edit Links".uppercase(), color = Color.White)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                ProfileSocialItem(
                    modifier = Modifier.weight(1f),
                    iconResId = R.drawable.icon_divo_instagram
                )

                ProfileSocialItem(
                    modifier = Modifier.weight(1f),
                    iconResId = R.drawable.icon_divo_tiktok

                )

                ProfileSocialItem(
                    modifier = Modifier.weight(1f),
                    iconResId = R.drawable.icon_divo_youtube

                )

                ProfileSocialItem(
                    modifier = Modifier.weight(1f),
                    iconResId = R.drawable.icon_divo_web
                )

            }
        }



        Spacer(modifier = Modifier.height(8.dp))
    }
}

enum class Destination(
    val route: String,
    @DrawableRes
    val iconResId: Int,
    val contentDescription: String
) {
    SONGS("songs", R.drawable.divo_profile_tab_1, "Songs"),
    ALBUM("album", R.drawable.divo_profile_tab_2, "Album"),
    PLAYLISTS("playlist", R.drawable.divo_profile_tab_3, "Playlist")
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TabContainer() {

    Box(
        Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.blackAlpha12)
    ) {

        val startDestination = Destination.SONGS
        var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

        PrimaryTabRow(
            selectedTabIndex = selectedDestination, modifier = Modifier,
            containerColor = Color.Transparent,
            indicator = {
                Spacer(
                    Modifier
                        .width(56.dp)
                        .height(4.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(
                                topStart = 5.dp,
                                topEnd = 5.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 0.dp
                            )
                        )
                )
            }
        ) {
            Destination.entries.forEachIndexed { index, destination ->
                Tab(
                    modifier = Modifier.width(100.dp),
                    selected = selectedDestination == index,
                    icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(destination.iconResId),
                            modifier = Modifier.size(24.dp),
                            contentDescription = destination.contentDescription
                        )
                    },
                    onClick = {
                        selectedDestination = index
                    },
                    selectedContentColor = Color.White
                )
            }
        }
    }
}

@Composable
private fun ProfilePhotoPage(
    modifier: Modifier = Modifier,

) {
//    LazyColumn(
//        state = listState,
//        modifier = modifier,
//    ) {
//        items(40) {
//            TelegramUserAvatar(
//                user =uiState.userFull.user,
//                modifier  = Modifier
//                    .size(68.dp)
//                    .clip(CircleShape),
//                68
//            )
//        }
//    }
}
