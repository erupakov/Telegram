package org.telegram.divo.screen.models

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import org.telegram.divo.common.LockScreenOrientation
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.RoleChip
import org.telegram.divo.components.TextTitle
import org.telegram.divo.components.items.DMButton
import org.telegram.divo.entity.FeedItem
import org.telegram.divo.style.AppTheme
import org.telegram.divo.style.DivoFont.HelveticaNeue
import org.telegram.messenger.FileLoader
import org.telegram.messenger.ImageLocation
import org.telegram.messenger.R
import org.telegram.tgnet.TLRPC
import org.telegram.ui.Components.BackupImageView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelsHomeScreen(
    viewModel: ModelsViewModel = androidx.lifecycle.viewmodel.compose.viewModel<ModelsViewModel>(),
    onSearch: () -> Unit = {},
    onClick: (Int) -> Unit = {},
    onPhotoClicked: (String) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val currentModels = if (state.selectedTab == Tab.ALL_USERS) state.allUserModels else state.models
    val currentRestModels = if (state.selectedTab == Tab.ALL_USERS) state.feedItems else listOf()
    val pagerState = rememberPagerState(pageCount = { currentRestModels.size })

    LockScreenOrientation()
    LaunchedEffect(Unit) {
        viewModel.setIntent(ModelsViewIntent.LoadInitialData)
    }

    if (state.selectedTab == Tab.ALL_USERS) {
        LaunchedEffect(pagerState.currentPage, currentRestModels.size) {
            if (currentRestModels.isNotEmpty()
                && pagerState.currentPage >= currentRestModels.size - 3
                && state.feedHasMore
                && !state.isLoadingAllUsers
                && !state.isLoadingMoreFeed
            ) {
                viewModel.setIntent(ModelsViewIntent.LoadMoreAllUsers)
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .padding(top = 32.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    Color.White
                ),
                title = {
                    TextTitle("Models".uppercase())
                },
                actions = {
                    IconButton(onClick = {}) {
                        androidx.compose.material3.Icon(
                            modifier = Modifier,
                            painter = painterResource(R.drawable.ic_ab_search),
                            contentDescription = "",
                            tint = AppTheme.colors.buttonColor
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {

                Spacer(modifier = Modifier.height(8.dp))

                StoriesRow()

                TabsRow(
                    tabs = Tab.entries.map { it.displayName.uppercase() },
                    selectedIndex = state.selectedTab.ordinal,
                    onTabSelected = {
                        viewModel.setIntent(ModelsViewIntent.OnTabSelected(Tab.entries.get(it)))
                    },
                )

                when {
                    state.selectedTab != Tab.ALL_USERS && !state.showMockData -> {
                        NotImplementedPlaceholder(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            onShowMockData = {
                                viewModel.setIntent(ModelsViewIntent.OnShowMockDataClicked)
                            }
                        )
                    }
                    currentRestModels.isEmpty() && state.isLoadingAllUsers -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AppTheme.colors.accentColor)
                        }
                    }
                    else -> {
                        VerticalPager(
                            state = pagerState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            reverseLayout = false,
                            userScrollEnabled = true,
                            beyondViewportPageCount = 1,
                            flingBehavior = PagerDefaults.flingBehavior(state = pagerState)
                        ) { page ->
                            ModelPage(
                                feed = currentRestModels[page],
                                onClick = { onClick(it) },
                                onPhotoClicked = onPhotoClicked
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun StoriesRow(
    stories: List<Story> = ModelsViewState.preview.stories,
    viewModel: ModelsViewModel? = null
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(stories) { story ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (story.id == "0") {
                                viewModel?.setIntent(ModelsViewIntent.OnAddStoryClick)
                            } else {
                                viewModel?.setIntent(ModelsViewIntent.OnStoryClick(story.id))
                            }
                        }
                        .then(
                            if (story.id == "0") {
                                Modifier.background(Color(0xFFE7E7E8))
                            } else {
                                Modifier.border(
                                    width = 3.dp, brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF990000),
                                            Color(0xFF000000),
                                        ),
                                        tileMode = TileMode.Repeated
                                    ), shape = CircleShape
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color(0xFFE7E7E8)),
                        shape = CircleShape,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color(0xFFE7E7E8)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (story.id == "0") {
                                Icon(
                                    painter = rememberVectorPainter(Icons.Default.Add),
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color(0xFFE7E7E8))
                                )
                            } else {
                                Image(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(Color(0xFFE7E7E8)),
                                    painter = rememberAsyncImagePainter(story.imageUrl),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    if (story.id == "0") "Add Story" else story.userName,
                    color = if (story.id == "0") Color(0xFFB0B4BA) else Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.5.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun TabsRow(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF000000),
    inactiveColor: Color = Color(0xFFBEBEBE)
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val style = AppTheme.typography.textButtonSmall.copy(
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp,
    )

    val tabWidths = remember(tabs) {
        tabs.map {
            with(density) {
                textMeasurer.measure(it, style).size.width.toDp()
            }
        }
    }

    TabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier.fillMaxWidth(),
        backgroundColor = Color.White,
        contentColor = activeColor,
        indicator = { tabPositions ->
            val targetIndicatorWidth = tabWidths[selectedIndex]
            val animatedIndicatorWidth by animateDpAsState(
                targetValue = targetIndicatorWidth,
                label = "indicatorWidth"
            )

            val currentTabPosition = tabPositions[selectedIndex]
            val targetIndicatorOffset =
                currentTabPosition.left + (currentTabPosition.width - targetIndicatorWidth) / 2
            val animatedIndicatorOffset by animateDpAsState(
                targetValue = targetIndicatorOffset,
                label = "indicatorOffset"
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.BottomStart)
                    .offset(x = animatedIndicatorOffset)
                    .width(animatedIndicatorWidth)
                    .height(2.dp)
                    .background(color = activeColor)
            )
        },
        divider = {
            TabRowDefaults.Divider(
                thickness = 1.dp,
                color = Color(0x11000000)
            )
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        fontFamily = HelveticaNeue,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                },
                selectedContentColor = activeColor,
                unselectedContentColor = inactiveColor
            )
        }
    }
}

@Composable
private fun ModelPage(
    feed: FeedItem,
    onClick: (Int) -> Unit,
    onPhotoClicked: (String) -> Unit,
) {
    val backgroundPhoto = feed.files.first().url
    Box(
        modifier = Modifier.fillMaxSize().clickableWithoutRipple { onClick(feed.id) },
        contentAlignment = Alignment.TopEnd
    ) {
        if (backgroundPhoto.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(backgroundPhoto),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.backgroundDark)
            )
        }

        Column(
            Modifier
                .align(Alignment.TopEnd)
                .padding(end = 12.dp, top = 24.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
//            model.emotions.forEach { emotion ->
//                ReactionPill(
//                    emotion = emotion,
//                    onClick = {
//                        viewModel.setIntent(ModelsViewIntent.OnEmotionClick(model.id, emotion))
//                    }
//                )
//            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f), // bottom strong
                            Color.Transparent                 // top invisible
                        ),
                        startY = Float.POSITIVE_INFINITY, // start bottom
                        endY = 0f                          // end top
                    )
                )
                .padding(vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White.copy(alpha = .9f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val avatarUrl = feed.previewImage.url
                    if (avatarUrl.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(avatarUrl),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        val initials = feed.user.fullName.split(" ")
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .take(2)
                            .joinToString("")
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.accentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = feed.user.fullName.uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.ExtraBold),
                        maxLines = 2
                    )
                    Spacer(Modifier.height(6.dp))
                    RoleChip(feed.user.roleLabel)
                }
                Spacer(Modifier.width(10.dp))
            }

            Row(
                Modifier
                    .padding(top = 24.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DMButton(onClick = {
                    //viewModel.setIntent(ModelsViewIntent.OnSendDmClick(feed.id))
                })

                Spacer(Modifier.weight(1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = .15f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_divo_share_model),
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_divo_save_model),
                        contentDescription = "Bookmark",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                //viewModel.setIntent(ModelsViewIntent.OnBookmarkClick(model.id))
                            }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            ThumbsRow(feed, onPhotoClicked)
        }
    }
}

@Composable
private fun ReactionPill(
    emotion: Emotions,
    onClick: () -> Unit = {}
) {
    val backgroundColor = if (emotion.selected) {
        Color(0xFFFFFFFF)
    } else {
        Color(0x3DFFFFFF)
    }

    Card(
        modifier = Modifier.size(height = 30.dp, width = 50.dp),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
        ),
        onClick = onClick,
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val textColor = if (emotion.selected) {
                Color(0xFF222222)
            } else {
                Color(0xFFFFFFFF)
            }

            Text(emotion.emoji, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(Modifier.weight(1f))
            Text("${emotion.count}", color = textColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ThumbsRow(
    feed: FeedItem,
    onPhotoClicked: (String) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        val images = feed.files.drop(1)

        items(
            items = images,
            key = { it.uuid }
        ) { thumb ->
            Image(
                modifier = Modifier
                    .size(width = 126.dp, height = 136.dp)
                    .clickableWithoutRipple { onPhotoClicked(thumb.url) },
                painter = rememberAsyncImagePainter(thumb.url),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        }
    }
}


@Composable
private fun NotImplementedPlaceholder(
    modifier: Modifier = Modifier,
    onShowMockData: () -> Unit
) {
    Column(
        modifier = modifier
            .background(Color.White)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "NOT IMPLEMENTED YET",
            fontFamily = HelveticaNeue,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF222222)
        )
        Spacer(Modifier.height(24.dp))
        Card(
            onClick = onShowMockData,
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.accentColor)
        ) {
            Text(
                text = "SEE TEST UI WITH MOCK DATA",
                fontFamily = HelveticaNeue,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
            )
        }
    }
}

@Composable
private fun TelegramPhotoCover(
    photo: TLRPC.Photo,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            BackupImageView(context).apply {
            }
        },
        update = { view ->
            val fullSize = FileLoader.getClosestPhotoSizeWithSize(photo.sizes, 640)
                ?: return@AndroidView
            if (photo.dc_id != 0) {
                fullSize.location.dc_id = photo.dc_id
                fullSize.location.file_reference = photo.file_reference
            }
            val fullLoc = ImageLocation.getForPhoto(fullSize, photo) ?: return@AndroidView
            var thumbSize: TLRPC.PhotoSize? = FileLoader.getClosestPhotoSizeWithSize(photo.sizes, 50)
            for (i in 0 until photo.sizes.size) {
                val ps = photo.sizes[i]
                if (ps is TLRPC.TL_photoStrippedSize) {
                    thumbSize = ps
                    break
                }
            }
            val thumbLoc = thumbSize?.let { ImageLocation.getForPhoto(it, photo) }
            val thumbFilter = if (thumbSize is TLRPC.TL_photoStrippedSize) "b" else null
            view.setImage(fullLoc, "640_640", thumbLoc, thumbFilter, null, 0, 1, photo)
        }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF121922)
@Composable
private fun PreviewModelsHome() {
    ModelsHomeScreen()
}
