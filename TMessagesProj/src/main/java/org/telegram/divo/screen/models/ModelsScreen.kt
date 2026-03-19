package org.telegram.divo.screen.models

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import com.google.android.exoplayer2.util.Log
import kotlinx.coroutines.flow.distinctUntilChanged
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.LaunchedEffectOnce
import org.telegram.divo.common.LockScreenOrientation
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.RoleChip
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.components.TextTitle
import org.telegram.divo.components.items.DMButton
import org.telegram.divo.components.rememberIsOnline
import org.telegram.divo.components.shimmer
import org.telegram.divo.entity.FeedItem
import org.telegram.divo.screen.gallery.GalleryItem
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
    onPhotoClicked: (List<GalleryItem>, Int) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val currentRestModels = state.feedItems

    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val bottomInset = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val cardHeight = remember(bottomInset, statusBarHeight) {
        screenHeight - statusBarHeight - bottomInset - 284.dp
    }

    val isOnline = rememberIsOnline()
    if (!isOnline) {
        Log.d("MyTag", "Ожидание сети...")
    } else {
        Log.d("MyTag", "Связь установлена!")
    }

    LockScreenOrientation()

    LaunchedEffectOnce {
        viewModel.setIntent(ModelsViewIntent.LoadInitialData)
    }

    LaunchedEffect(state.error) {
        if (!state.error.isNullOrBlank()) {
            Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(
        listState,
        currentRestModels.size,
        state.feedHasMore,
        state.isLoadingAllUsers,
        state.isLoadingMoreFeed,
        state.selectedTab
    ) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                if (currentRestModels.isNotEmpty() &&
                    lastVisibleIndex >= currentRestModels.size - 3 &&
                    state.feedHasMore &&
                    !state.isLoadingAllUsers &&
                    !state.isLoadingMoreFeed
                ) {
                    viewModel.setIntent(ModelsViewIntent.LoadMoreAllUsers)
                }
            }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppTheme.colors.backgroundNew,
        contentColor = AppTheme.colors.backgroundNew,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.backgroundNew,
                    scrolledContainerColor = AppTheme.colors.backgroundNew,
                ),
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        TextTitle("Models".uppercase())
                    }
                },
                actions = {
                    RoundedButton(
                        modifier = Modifier.padding(end = 8.dp),
                        resId = R.drawable.ic_divo_search_24,
                        iconSize = 24.dp,
                        onClick = onSearch
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .background(AppTheme.colors.backgroundNew),
            contentPadding = PaddingValues(bottom = bottomInset + 66.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { StoriesRow() }
            item {
                TabsRow(
                    tabs = Tab.entries.map { it.displayName.uppercase() },
                    selectedIndex = state.selectedTab.ordinal,
                    onTabSelected = { viewModel.setIntent(ModelsViewIntent.OnTabSelected(Tab.entries[it])) }
                )
            }

            when {
                currentRestModels.isEmpty() && !state.isLoadingAllUsers -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Placeholder(
                                modifier = Modifier.align(Alignment.Center),
                                text = state.emptyText.uppercase(),
                                cardHeight = cardHeight,
                                onShowMockData = { viewModel.setIntent(ModelsViewIntent.OnShowMockDataClicked) }
                            )
                        }
                    }
                }

                currentRestModels.isEmpty() && state.isLoadingAllUsers -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxWidth()
                                    .height(cardHeight)
                                    .padding(top = 18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                LottieProgressIndicator(modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.LoadingModelsList).uppercase(),
                                    style = AppTheme.typography.helveticaNeueLtCom,
                                    fontSize = 12.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                else -> {
                    itemsIndexed(
                        items = currentRestModels,
                        key = { i, d -> d.id }
                    ) { i, feedItem ->
                        ModelPage(
                            feed = feedItem,
                            cardHeight = cardHeight,
                            onClick = onClick,
                            onPhotoClicked = onPhotoClicked
                        )
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
            .background(AppTheme.colors.backgroundNew),
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
                                Modifier.background(Color.White)
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
                            .background(Color.White),
                        shape = CircleShape,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.White),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (story.id == "0") {
                                Icon(
                                    painter = rememberVectorPainter(Icons.Default.Add),
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color.White)
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
        backgroundColor = AppTheme.colors.backgroundNew,
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
    modifier: Modifier = Modifier,
    feed: FeedItem,
    cardHeight: Dp,
    onClick: (Int) -> Unit,
    onPhotoClicked: (List<GalleryItem>, Int) -> Unit,
) {
    val backgroundPhoto = feed.files.first().url
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(26.dp))
            .clickableWithoutRipple { onClick(feed.id) },
        contentAlignment = Alignment.TopEnd
    ) {
        DivoAsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = backgroundPhoto,
            loadingContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                )
            }
        )

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
                        DivoAsyncImage(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape),
                            model = avatarUrl
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
            ThumbsRow(
                feed = feed,
                onPhotoClicked = {
                    val items = feed.files.map { GalleryItem(it.order, it.url, false) }
                    onPhotoClicked(items, it)
                }
            )
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
    onPhotoClicked: (Int) -> Unit
) {
    val context = LocalContext.current
    val images = remember(feed.files) { feed.files.drop(1) }

    LaunchedEffect(images) {
        images.forEach { file ->
            val request = ImageRequest.Builder(context)
                .data(file.url)
                .size(Size(126.dp.value.toInt(), 136.dp.value.toInt()))
                .build()
            context.imageLoader.enqueue(request)
        }
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        itemsIndexed(items = images, key = { _, it -> it.uuid }) { index, thumb ->
            DivoAsyncImage(
                modifier = Modifier
                    .size(width = 126.dp, height = 136.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickableWithoutRipple { onPhotoClicked(index) },
                model = thumb.url,
                placeholderColor = Color.Transparent,
                loadingContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shimmer(
                                shimmerColor = Color.Black.copy(0.2f),
                                highlightColor = Color.White.copy(0.1f)
                            ),
                    )
                }
            )
        }
    }
}


@Composable
private fun Placeholder(
    modifier: Modifier = Modifier,
    cardHeight: Dp,
    text: String = "NOT IMPLEMENTED YET",
    onShowMockData: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .padding(top = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            fontFamily = HelveticaNeue,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF222222)
        )
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
