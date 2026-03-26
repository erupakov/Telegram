@file:kotlin.OptIn(FlowPreview::class)

package org.telegram.divo.screen.profile.components

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.entity.Publication
import org.telegram.divo.entity.PublicationFile

@OptIn(UnstableApi::class, FlowPreview::class)
@Composable
fun VideoGrid(
    modifier: Modifier = Modifier,
    videoItems: List<Publication>,
    isOwnProfile: Boolean,
    isFirstLoading: Boolean,
    isLoadingMore: Boolean,
    isUploading: Boolean,
    hasMore: Boolean,
    isActive: Boolean = true,
    onLoadMore: () -> Unit,
    onVideoClicked: (String) -> Unit,
    onVideoSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    val gridState = rememberLazyGridState()

    val currentItems by rememberUpdatedState(videoItems)
    val currentHasMore by rememberUpdatedState(hasMore)
    val currentLoading by rememberUpdatedState(isLoadingMore)
    val currentIsActive by rememberUpdatedState(isActive)

    val playerPool = remember {
        List(2) {
            ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 0f
                playWhenReady = false
            }
        }
    }

    val playingMap = remember { mutableStateMapOf<Int, Int>() }
    val slotReady = remember { mutableStateMapOf<Int, Boolean>() }

    var lifecycleResumed by remember { mutableStateOf(true) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> lifecycleResumed = true
                Lifecycle.Event.ON_PAUSE -> {
                    lifecycleResumed = false
                    playerPool.forEach { it.playWhenReady = false }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(Unit) {
        onDispose {
            playerPool.forEach {
                it.stop()
                it.clearMediaItems()
                it.release()
            }
        }
    }

    LaunchedEffect(isActive) {
        if (!isActive) {
            playerPool.forEach { it.playWhenReady = false }
        }
    }

    DisposableEffect(playerPool) {
        val listeners = playerPool.mapIndexed { slot, player ->
            object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    slotReady[slot] = true
                }
                override fun onMediaItemTransition(item: MediaItem?, reason: Int) {
                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT) return
                    slotReady[slot] = false
                }
            }.also { player.addListener(it) }
        }
        onDispose {
            playerPool.forEachIndexed { i, p -> p.removeListener(listeners[i]) }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow {
            Triple(
                currentIsActive,
                lifecycleResumed,
                gridState.layoutInfo.visibleItemsInfo
            )
        }
            .debounce(150)
            .collect { (tabActive, lcResumed, visibleItems) ->
                val shouldPlay = tabActive && lcResumed

                if (!shouldPlay) {
                    playerPool.forEach { it.playWhenReady = false }
                    return@collect
                }

                val visible = visibleItems.filter { it.index < currentItems.size }
                if (visible.isEmpty()) {
                    playingMap.keys.toList().forEach { idx ->
                        val slot = playingMap.remove(idx) ?: return@forEach
                        slotReady.remove(slot)
                        playerPool[slot].playWhenReady = false
                    }
                    return@collect
                }

                val viewportCenter = gridState.layoutInfo.viewportSize.height / 2
                val sorted = visible.sortedBy { item ->
                    kotlin.math.abs(
                        (item.offset.y + item.size.height / 2) - viewportCenter
                    )
                }

                val outerSorted = sorted.filter { it.index % 3 != 1 }
                val first = outerSorted.firstOrNull() ?: sorted.first()
                val firstRow = first.index / 3
                val firstCol = first.index % 3

                val second = outerSorted.firstOrNull { c ->
                    val row = c.index / 3
                    val col = c.index % 3
                    (row == firstRow + 1 || row == firstRow - 1) && col != firstCol
                }
                    ?: outerSorted.firstOrNull {
                        it.index / 3 != firstRow && it.index % 3 != firstCol
                    }
                    ?: outerSorted.firstOrNull { it.index / 3 != firstRow }
                    ?: sorted.drop(1).firstOrNull { it.index / 3 != firstRow }

                val picked = listOfNotNull(first.index, second?.index)
                val pickedSet = picked.toSet()

                val keptSlots = mutableMapOf<Int, Int>()
                picked.forEach { idx ->
                    playingMap[idx]?.let { keptSlots[idx] = it }
                }

                (playingMap.keys - pickedSet).forEach { idx ->
                    val slot = playingMap.remove(idx) ?: return@forEach
                    slotReady.remove(slot)
                    playerPool[slot].playWhenReady = false
                }

                val usedSlots = keptSlots.values.toSet()
                val freeSlots = (0 until playerPool.size)
                    .filter { it !in usedSlots }
                    .toMutableList()

                picked.forEach { index ->
                    val slot = keptSlots[index]
                        ?: if (freeSlots.isNotEmpty()) freeSlots.removeAt(0)
                        else return@forEach

                    val player = playerPool[slot]
                    val url = currentItems.getOrNull(index)
                        ?.files?.firstOrNull()?.fullUrl ?: return@forEach

                    val currentUrl =
                        player.currentMediaItem?.localConfiguration?.uri?.toString()

                    if (currentUrl != url) {
                        slotReady[slot] = false
                        player.setMediaItem(MediaItem.fromUri(url))
                        player.prepare()
                    }

                    player.playWhenReady = true
                    playingMap[index] = slot
                }
            }
    }


    val shouldLoadMore by remember {
        derivedStateOf {
            val last = gridState.layoutInfo.visibleItemsInfo
                .lastOrNull()?.index ?: return@derivedStateOf false
            last >= currentItems.size - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && currentHasMore && !currentLoading) onLoadMore()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = gridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = WindowInsets.navigationBars
                .asPaddingValues()
                .calculateBottomPadding() + 16.dp,
        ),
    ) {
        itemsIndexed(
            items = videoItems,
            key = { _, item -> "video_${item.id}" },
            contentType = { _, _ -> "video" },
        ) { index, item ->

            val slot by remember(index) { derivedStateOf { playingMap[index] } }
            val isReady by remember(index) {
                derivedStateOf { slot?.let { slotReady[it] } == true }
            }

            val file = item.files.firstOrNull()

            VideoThumbnailItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickableWithoutRipple { file?.fullUrl?.let(onVideoClicked) },
                file = file,
                isActive = isActive,
                exoPlayer = slot?.let { playerPool[it] },
                isVideoReady = isReady,
            )
        }

        if (isFirstLoading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                MediaLoadingContent()
            }
        }

        if (isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) { LottieProgressIndicator(Modifier.size(24.dp)) }
            }
        }

        if (isOwnProfile && !isFirstLoading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                PortfolioAddButton(
                    isUploading = isUploading,
                    isVideo = true,
                    onMediaSelected = onVideoSelected,
                )
            }
        }
    }
}

@Composable
fun VideoThumbnailItem(
    modifier: Modifier = Modifier,
    file: PublicationFile? = null,
    isActive: Boolean = true,
    exoPlayer: ExoPlayer? = null,
    isVideoReady: Boolean = false,
) {
    val videoAlpha by animateFloatAsState(
        targetValue = if (isVideoReady) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "videoAlpha",
    )

    var videoAspect by remember { mutableFloatStateOf(1f) }

    DisposableEffect(exoPlayer) {
        if (exoPlayer == null) {
            videoAspect = 1f
            return@DisposableEffect onDispose {}
        }

        exoPlayer.videoSize.let { s ->
            if (s.width > 0 && s.height > 0) {
                videoAspect = s.width.toFloat() / s.height
            }
        }

        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(size: VideoSize) {
                if (size.width > 0 && size.height > 0) {
                    videoAspect = size.width.toFloat() / size.height
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            videoAspect = 1f
        }
    }

    val cropScaleX = if (videoAspect > 1f) videoAspect else 1f
    val cropScaleY = if (videoAspect < 1f) 1f / videoAspect else 1f

    Box(modifier = modifier.clip(RectangleShape)) {
        if (file?.thumbnailBitmap != null) {
            Image(
                bitmap = file.thumbnailBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (exoPlayer != null) {
            PlayerSurface(
                player = exoPlayer,
                surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = if (isActive) videoAlpha else 0f
                        scaleX = cropScaleX
                        scaleY = cropScaleY
                    },
            )
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