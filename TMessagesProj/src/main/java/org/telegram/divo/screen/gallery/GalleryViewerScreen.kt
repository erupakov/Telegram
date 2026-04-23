package org.telegram.divo.screen.gallery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.LaunchedEffectOnce
import org.telegram.divo.common.SnackbarEvent.Error
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.DivoPopupMenu
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.PopupMenuItem
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.style.AppTheme
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

    val uiState by viewModel.state.collectAsState()
    val snackbarState = remember { AppSnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                GalleryEffect.Deleted -> onBack()
                is GalleryEffect.ShowError -> snackbarState.show(Error(effect.message))
            }
        }
    }

    if (uiState.source == null || uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            LottieProgressIndicator(modifier = Modifier.size(34.dp), color = Color.White)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GalleryPagerContent(
            uiState = uiState,
            isOwnProfile = isOwnProfile,
            onLoadMore = { viewModel.setIntent(GalleryIntent.OnLoadMore) },
            onBack = onBack,
            onDelete = { viewModel.setIntent(GalleryIntent.OnDelete(it)) }
        )

        AppSnackbarHost(
            modifier = Modifier.align(Alignment.BottomCenter),
            state = snackbarState,
            bottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 8.dp
        )
    }
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
    val thumbnailListState = rememberLazyListState(initialFirstVisibleItemIndex = uiState.initialIndex)

    var controlsVisible by remember { mutableStateOf(true) }
    var isZoomed by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // 1. Когда мы открываем панель (controlsVisible = true), сразу перескакиваем к текущей миниатюре
    LaunchedEffect(controlsVisible) {
        if (controlsVisible && uiState.items.isNotEmpty()) {
            val index = pagerState.currentPage.coerceIn(0, uiState.items.lastIndex)
            thumbnailListState.scrollToItem(index)   // ← было scrollToItem, тут ок, просто убедись
        }
    }

    // 2. Когда мы свайпаем большое фото, плавно двигаем миниатюры (работает ТОЛЬКО если панель открыта)
    LaunchedEffect(pagerState.currentPage) {
        if (controlsVisible && uiState.items.isNotEmpty()) {
            val index = pagerState.currentPage.coerceIn(0, uiState.items.lastIndex)
            thumbnailListState.scrollToItem(index)   // ← было animateScrollToItem
        }
    }

    // 3. Подгрузка новых элементов
    LaunchedEffect(pagerState.currentPage, uiState.items.size) {
        if (pagerState.currentPage >= uiState.items.size - 3 && uiState.hasMore) {
            onLoadMore()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .navigationBarsPadding()
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !isZoomed, // Блокируем свайпы страниц, если картинка увеличена
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
                        .background(Color.Black)
                        .clickableWithoutRipple { controlsVisible = !controlsVisible },
                    contentAlignment = Alignment.Center
                ) {
                    LottieProgressIndicator(modifier = Modifier.size(34.dp), color = Color.White)
                }
            } else {
                GalleryPage(
                    item = uiState.items[page],
                    isActive = pagerState.currentPage == page,
                    controlsVisible = controlsVisible,
                    onChangeControlsVisible = { controlsVisible = it },
                    onZoomChanged = { isZoomed = it }
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

        val isPhotoSource = uiState.source is GallerySource.Portfolio || uiState.source is GallerySource.Feed
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
            .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
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
                    placeholderColor = AppTheme.colors.onBackground.copy(0.5f)
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
    onZoomChanged: (Boolean) -> Unit
) {
    if (item.isVideo) {
        VideoPlayer(
            modifier = Modifier.fillMaxSize(),
            url = item.url,
            isPlaying = isActive,
            controlsVisible = controlsVisible,
            onChangeControlsVisible = onChangeControlsVisible
        )
    } else {
        ZoomableBox(
            modifier = Modifier.fillMaxSize(),
            isActive = isActive,
            onTap = { onChangeControlsVisible(!controlsVisible) },
            onZoomChanged = onZoomChanged
        ) {
            DivoAsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = item.url,
                contentScale = ContentScale.Fit,
                loadingContent = {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        LottieProgressIndicator(modifier = Modifier.size(34.dp), color = Color.White)
                    }
                }
            )
        }
    }
}

@Composable
fun ZoomableBox(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    onTap: () -> Unit,
    onZoomChanged: (Boolean) -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnZoomChanged by rememberUpdatedState(onZoomChanged)

    LaunchedEffect(isActive) {
        if (!isActive && scale.value > 1f) {
            scale.snapTo(1f)
            offsetX.snapTo(0f)
            offsetY.snapTo(0f)
            currentOnZoomChanged(false)
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                val tapSlop = viewConfiguration.touchSlop
                val doubleTapTimeout = viewConfiguration.doubleTapTimeoutMillis

                var lastTapTime = 0L

                awaitPointerEventScope {
                    while (true) {
                        val firstDown = awaitFirstDown(requireUnconsumed = false)
                        val downTime = System.currentTimeMillis()
                        val downPosition = firstDown.position

                        coroutineScope.launch {
                            scale.stop()
                            offsetX.stop()
                            offsetY.stop()
                        }

                        var totalPan = Offset.Zero
                        var wasPinching = false

                        do {
                            val event = awaitPointerEvent()
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()
                            val isPinching = event.changes.size > 1

                            if (isPinching) wasPinching = true
                            totalPan += pan

                            if (scale.value > 1f || isPinching) {
                                val newScale = (scale.value * zoom).coerceIn(1f, 5f)
                                coroutineScope.launch {
                                    scale.snapTo(newScale)
                                    if (newScale > 1f) {
                                        val maxX = (size.width * (newScale - 1)) / 2f
                                        val maxY = (size.height * (newScale - 1)) / 2f
                                        offsetX.snapTo((offsetX.value + pan.x).coerceIn(-maxX, maxX))
                                        offsetY.snapTo((offsetY.value + pan.y).coerceIn(-maxY, maxY))
                                    } else {
                                        offsetX.snapTo(0f)
                                        offsetY.snapTo(0f)
                                    }
                                }
                                currentOnZoomChanged(newScale > 1.01f)
                                event.changes.forEach { it.consume() }
                            }
                        } while (event.changes.any { it.pressed })

                        if (scale.value > 1f) {
                            val maxX = (size.width * (scale.value - 1)) / 2f
                            val maxY = (size.height * (scale.value - 1)) / 2f
                            if (abs(offsetX.value) > maxX || abs(offsetY.value) > maxY) {
                                coroutineScope.launch {
                                    launch { offsetX.animateTo(offsetX.value.coerceIn(-maxX, maxX)) }
                                    launch { offsetY.animateTo(offsetY.value.coerceIn(-maxY, maxY)) }
                                }
                            }
                        }

                        val movedDistance = totalPan.getDistance()
                        val isTap = movedDistance < tapSlop && !wasPinching

                        if (isTap) {
                            val now = System.currentTimeMillis()
                            val timeSinceLastTap = now - lastTapTime

                            if (timeSinceLastTap < doubleTapTimeout && lastTapTime != 0L) {
                                lastTapTime = 0L
                                if (scale.value > 1.01f) {
                                    coroutineScope.launch {
                                        launch { scale.animateTo(1f) }
                                        launch { offsetX.animateTo(0f) }
                                        launch { offsetY.animateTo(0f) }
                                    }
                                    currentOnZoomChanged(false)
                                } else {
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val tapOffset = downPosition
                                    val targetOffset = (center - tapOffset) * 2f
                                    val maxX = (size.width * 2f) / 2f
                                    val maxY = (size.height * 2f) / 2f
                                    coroutineScope.launch {
                                        launch { scale.animateTo(3f) }
                                        launch { offsetX.animateTo(targetOffset.x.coerceIn(-maxX, maxX)) }
                                        launch { offsetY.animateTo(targetOffset.y.coerceIn(-maxY, maxY)) }
                                    }
                                    currentOnZoomChanged(true)
                                }
                            } else {
                                lastTapTime = now
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(doubleTapTimeout)
                                    if (lastTapTime == now) {
                                        currentOnTap()
                                    }
                                }
                            }
                        }
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    translationX = offsetX.value
                    translationY = offsetY.value
                },
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}