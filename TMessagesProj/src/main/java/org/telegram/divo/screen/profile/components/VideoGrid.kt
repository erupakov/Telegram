package org.telegram.divo.screen.profile.components

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.entity.Publication

private const val SURFACE_READY_DELAY_MS = 300L

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(UnstableApi::class, FlowPreview::class)
@Composable
fun VideoGrid(
    modifier: Modifier = Modifier,
    videoItems: List<Publication>,
    isOwnProfile: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    isActive: Boolean = true,
    onLoadMore: () -> Unit,
    onVideoClicked: (String) -> Unit,
) {
    val context = LocalContext.current
    val gridState = rememberLazyGridState()

    val currentItems by rememberUpdatedState(videoItems)
    val currentHasMore by rememberUpdatedState(hasMore)
    val currentLoading by rememberUpdatedState(isLoadingMore)

    val scope = rememberCoroutineScope()

    val playerPool = remember {
        List(2) {
            ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 0f
                playWhenReady = false
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            }
        }
    }

    val playingMap = remember { mutableStateMapOf<Int, Int>() }
    val slotReady = remember { mutableStateMapOf<Int, Boolean>() }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP -> {
                    playerPool.forEach { it.pause() }
                    playingMap.clear()
                    slotReady.clear()
                }
                Lifecycle.Event.ON_RESUME -> {}
                Lifecycle.Event.ON_DESTROY -> {
                    playerPool.forEach { it.release() }
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            playerPool.forEach {
                it.pause()
                it.release()
            }
        }
    }

    LaunchedEffect(isActive) {
        if (!isActive) {
            playerPool.forEach {
                it.pause()
                it.clearMediaItems()
            }
            playingMap.clear()
            slotReady.clear()
        }
    }

    DisposableEffect(playerPool) {
        val listeners = playerPool.mapIndexed { slot, player ->
            object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    if (slotReady[slot] == true) return
                    scope.launch {
                        delay(SURFACE_READY_DELAY_MS)
                        slotReady[slot] = true
                    }
                }

                override fun onMediaItemTransition(item: MediaItem?, reason: Int) {
                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT) return
                    slotReady[slot] = false
                }
            }.also { player.addListener(it) }
        }
        onDispose {
            playerPool.forEachIndexed { slot, player ->
                Log.d("VideoGrid", "player.removeListener")
                player.removeListener(listeners[slot])
            }
        }
    }

    // Пауза во время скролла
    LaunchedEffect(Unit) {
        snapshotFlow { gridState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { scrolling ->
                if (scrolling) {
                    playerPool.forEach {
                        it.pause()
                        it.clearMediaItems()
                    }
                    playingMap.clear()
                    slotReady.clear()
                }
            }
    }

    // Autoplay после остановки скролла
    LaunchedEffect(Unit) {
        snapshotFlow { gridState.isScrollInProgress }
            .distinctUntilChanged()
            .filter { !it }
            .debounce(250)
            .collect {
                val visible = gridState.layoutInfo.visibleItemsInfo
                    .filter { it.index < currentItems.size }

                if (visible.isEmpty()) return@collect

                val viewportCenter = gridState.layoutInfo.viewportSize.height / 2

                val sorted = visible.sortedBy { item ->
                    kotlin.math.abs((item.offset.y + item.size.height / 2) - viewportCenter)
                }

                val outerSorted = sorted.filter { it.index % 3 != 1 }

                val first = outerSorted.firstOrNull() ?: sorted.first()
                val firstRow = first.index / 3
                val firstCol = first.index % 3

                val second = outerSorted.firstOrNull { candidate ->
                    val row = candidate.index / 3
                    val col = candidate.index % 3
                    (row == firstRow + 1 || row == firstRow - 1) && col != firstCol
                }
                    ?: outerSorted.firstOrNull { it.index / 3 != firstRow && it.index % 3 != firstCol }
                    ?: outerSorted.firstOrNull { it.index / 3 != firstRow }
                    ?: sorted.drop(1).firstOrNull { it.index / 3 != firstRow }

                val picked = listOfNotNull(first.index, second?.index)
                val pickedSet = picked.toSet()

                (playingMap.keys - pickedSet).forEach { idx ->
                    val slot = playingMap.remove(idx) ?: return@forEach
                    slotReady.remove(slot)
                    playerPool[slot].run { pause(); clearMediaItems() }
                }

                picked.forEachIndexed { slot, index ->
                    val player = playerPool[slot]
                    val url = currentItems.getOrNull(index)
                        ?.files?.firstOrNull()?.fullUrl ?: return@forEachIndexed

                    val currentUrl =
                        player.currentMediaItem?.localConfiguration?.uri?.toString()

                    if (currentUrl != url) {
                        slotReady[slot] = false
                        player.setMediaItem(MediaItem.fromUri(url))
                        player.prepare()
                    }

                    player.play()
                    playingMap[index] = slot
                }
            }
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val info = gridState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            last >= currentItems.size - 6
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && currentHasMore && !currentLoading) {
            onLoadMore()
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = gridState,
        modifier = modifier.fillMaxSize(),
    ) {
        itemsIndexed(
            items = videoItems,
            key = { _, item -> "video_${item.id}" },
            contentType = { _, _ -> "video" },
        ) { index, item ->

            val fileUrl = remember(item.id) {
                item.files.firstOrNull()?.fullUrl
            }

            val slot by remember(index) { derivedStateOf { playingMap[index] } }
            val isReady by remember(index) {
                derivedStateOf { slot?.let { slotReady[it] } == true }
            }
            val displayMetrics = context.resources.displayMetrics
            val cellSizePx = remember {
                (displayMetrics.widthPixels / 3)
            }

            val thumbRequest = remember(item.id) {
                fileUrl?.let { url ->
                    ImageRequest.Builder(context)
                        .data(url)
                        .decoderFactory { src, opts, _ -> VideoFrameDecoder(src.source, opts) }
                        .memoryCacheKey("thumb_$url")
                        .diskCacheKey("thumb_$url")
                        .videoFrameMillis(0L)
                        .size(cellSizePx, cellSizePx)
                        .crossfade(false)
                        .allowHardware(true)
                        .build()
                }
            }

            VideoThumbnailItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickableWithoutRipple { fileUrl?.let(onVideoClicked) },
                thumbRequest = thumbRequest,
                isActive = isActive,
                exoPlayer = slot?.let { playerPool[it] },
                isVideoReady = isReady,
            )
        }

        if (isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    LottieProgressIndicator(Modifier.size(24.dp))
                }
            }
        }

        if (isOwnProfile) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                PortfolioAddButton(
                    isUploading = false,
                    isVideo = true,
                    onMediaSelected = {}
                )
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoThumbnailItem(
    modifier: Modifier = Modifier,
    thumbRequest: ImageRequest? = null,
    isActive: Boolean = true,
    exoPlayer: ExoPlayer? = null,
    isVideoReady: Boolean = false,
) {
    val videoAlpha by animateFloatAsState(
        targetValue = if (isVideoReady) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "videoAlpha",
    )

    Box(modifier = modifier.clip(RectangleShape)) {
        if (thumbRequest != null) {
            DivoAsyncImage(
                model = thumbRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (exoPlayer != null && isActive) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                PlayerSurface(
                    player = exoPlayer,
                    surfaceType = SURFACE_TYPE_SURFACE_VIEW,
                    modifier = Modifier.fillMaxSize().graphicsLayer { alpha = videoAlpha },
                )
            }
        }

        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(16.dp),
        )
    }
}