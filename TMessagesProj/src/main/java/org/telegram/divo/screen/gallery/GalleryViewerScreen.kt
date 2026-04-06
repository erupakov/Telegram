package org.telegram.divo.screen.gallery

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.LaunchedEffectOnce
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.DivoPopupMenu
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.PopupMenuItem
import org.telegram.divo.components.RoundedButton
import org.telegram.messenger.R
import kotlin.math.abs

@Composable
fun GalleryViewerScreen(
    viewModel: GalleryViewerViewModel = viewModel(),
    isOwnProfile: Boolean = false,
    source: GallerySource,
    onBack: () -> Unit,
) {
    LaunchedEffectOnce {
        viewModel.setIntent(GalleryIntent.OnLoad(source))
    }
    val context = LocalContext.current
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                GalleryEffect.Deleted -> {
                    onBack()
                    Toast.makeText(context, "deleted", Toast.LENGTH_SHORT).show()
                }
                is GalleryEffect.ShowError -> Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (uiState.source == null || uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            LottieProgressIndicator(
                modifier = Modifier.size(34.dp),
                color = Color.White
            )
        }
        return
    }

    GalleryPagerContent(
        uiState = uiState,
        isOwnProfile = isOwnProfile,
        onLoadMore = { viewModel.setIntent(GalleryIntent.OnLoadMore) },
        onBack = onBack,
        onDelete = { viewModel.setIntent(GalleryIntent.OnDelete(it)) }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GalleryPagerContent(
    uiState: GalleryViewerState,
    isOwnProfile: Boolean,
    onLoadMore: () -> Unit,
    onBack: () -> Unit,
    onDelete: (Int) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = uiState.initialIndex,
        pageCount = { if (uiState.hasMore) uiState.items.size + 1 else uiState.items.size }
    )
    val thumbnailListState = rememberLazyListState()
    var controlsVisible by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    var isSyncingFromPager by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        if (!thumbnailListState.isScrollInProgress) {
            val index = pagerState.currentPage
                .coerceAtMost((uiState.items.size - 1).coerceAtLeast(0))
            isSyncingFromPager = true
            try {
                thumbnailListState.animateScrollToItem(index = index, scrollOffset = 0)
            } finally {
                isSyncingFromPager = false
            }
        }
    }

    LaunchedEffect(controlsVisible, pagerState.currentPage) {
        if (controlsVisible) {
            val index = pagerState.currentPage
                .coerceAtMost((uiState.items.size - 1).coerceAtLeast(0))
            thumbnailListState.scrollToItem(index)
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow {
            val info = thumbnailListState.layoutInfo
            if (info.visibleItemsInfo.isEmpty()) -1
            else {
                val viewportCenter =
                    (info.viewportStartOffset + info.viewportEndOffset) / 2
                info.visibleItemsInfo.minByOrNull {
                    abs((it.offset + it.size / 2) - viewportCenter)
                }?.index ?: -1
            }
        }.collect { centerIndex ->
            if (!isSyncingFromPager
                && thumbnailListState.isScrollInProgress
                && centerIndex in 0 until uiState.items.size
                && centerIndex != pagerState.currentPage
            ) {
                pagerState.scrollToPage(centerIndex)
            }
        }
    }

    LaunchedEffect(pagerState.currentPage, uiState.items.size) {
        if (pagerState.currentPage >= uiState.items.size - 3 && uiState.hasMore) {
            onLoadMore()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures { controlsVisible = !controlsVisible }
            }
            .navigationBarsPadding()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 2,
            key = { page ->
                if (page < uiState.items.size) "${uiState.items[page].url}$page"
                else "loading_page_$page"
            }
        ) { page ->
            if (page >= uiState.items.size) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    LottieProgressIndicator(
                        modifier = Modifier.size(34.dp),
                        color = Color.White
                    )
                }
            } else {
                GalleryPage(
                    item = uiState.items[page],
                    isActive = pagerState.currentPage == page,
                    controlsVisible = controlsVisible,
                    onChangeControlsVisible = { controlsVisible = it }
                )
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RoundedButton(
                    modifier = Modifier.padding(start = 16.dp),
                    onClick = onBack
                )

                if (isOwnProfile) {
                    IconButton(onClick = { showDropdownMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }

        DivoPopupMenu(
            visible = showDropdownMenu,
            onDismiss = { showDropdownMenu = false },
            offset = IntOffset(x = -32, y = 100),
            items = listOf(
                PopupMenuItem(
                    titleRes = R.string.ButtonDelete,
                    onClick = {
                        val currentItem = uiState.items.getOrNull(pagerState.currentPage)
                        currentItem?.let { onDelete(it.id) }
                    }
                ),
            )
        )

        val isPhotoSource =
            uiState.source is GallerySource.Portfolio || uiState.source is GallerySource.Feed
        if (isPhotoSource && uiState.items.size > 1) {
            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            ) {
                ThumbnailStrip(
                    items = uiState.items,
                    currentPage = pagerState.currentPage,
                    listState = thumbnailListState,
                    onThumbnailClick = { index ->
                        scope.launch { pagerState.animateScrollToPage(index) }
                    }
                )
            }
        }
    }
}

@Composable
private fun ThumbnailStrip(
    items: List<GalleryItem>,
    currentPage: Int,
    listState: LazyListState,
    onThumbnailClick: (Int) -> Unit,
) {
    val itemSize = 64.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val horizontalPadding = (screenWidth - itemSize) / 2

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                )
            )
            .padding(vertical = 12.dp)
    ) {
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(
                items = items,
                key = { index, item -> "thumb_${item.url}$index" }
            ) { index, item ->
                val isSelected = index == currentPage

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.9f,
                    animationSpec = tween(150),
                    label = "thumbScale"
                )

                DivoAsyncImage(
                    modifier = Modifier
                        .size(itemSize)
                        .graphicsLayer { scaleX = scale; scaleY = scale }
                        .clip(RoundedCornerShape(4.dp))
                        .clickableWithoutRipple { onThumbnailClick(index) },
                    model = item.url,
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}

@Composable
private fun GalleryPage(
    item: GalleryItem,
    isActive: Boolean,
    controlsVisible: Boolean,
    onChangeControlsVisible: (Boolean) -> Unit,
) {

    Box(modifier = Modifier.fillMaxSize()) {
        if (item.isVideo) {
            VideoPlayer(
                modifier = Modifier.fillMaxSize(),
                url = item.url,
                isPlaying = isActive,
                controlsVisible = controlsVisible,
                onChangeControlsVisible = onChangeControlsVisible
            )
        } else {
            DivoAsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = item.url,
                contentScale = ContentScale.Fit,
                loadingContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        LottieProgressIndicator(
                            modifier = Modifier.size(34.dp),
                            color = Color.White
                        )
                    }
                }
            )
        }
    }
}
