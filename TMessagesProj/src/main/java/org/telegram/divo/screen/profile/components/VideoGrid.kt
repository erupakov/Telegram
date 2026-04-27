@file:kotlin.OptIn(FlowPreview::class)

package org.telegram.divo.screen.profile.components


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
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
import androidx.compose.foundation.lazy.grid.LazyGridState
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.entity.Publication
import org.telegram.divo.entity.PublicationFile

@UnstableApi
@OptIn(FlowPreview::class)
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
    topPadding: Dp = 0.dp,
    onLoadMore: () -> Unit,
    onVideoClicked: (String) -> Unit,
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
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            }
        }
    }

    val playingMap = remember { mutableStateMapOf<Int, Int>() }
    val slotReady = remember { mutableStateMapOf<Int, Boolean>() }

    DisposableEffect(Unit) {
        onDispose {
            playerPool.forEach {
                it.stop()
                it.clearMediaItems()
                it.release()
            }
        }
    }

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
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(isActive) {
        if (!isActive) playerPool.forEach { it.playWhenReady = false }
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
            currentIsActive to gridState.layoutInfo.visibleItemsInfo
        }
            .distinctUntilChanged()
            .debounce(200)
            .collect { (active, visibleItems) ->
                if (!active) return@collect

                val visible = visibleItems.filter { it.index < currentItems.size }
                if (visible.isEmpty()) return@collect

                val viewportCenter = gridState.layoutInfo.viewportSize.height / 2f

                val stickinessBonus = viewportCenter * 0.6f

                val sorted = visible.sortedBy { item ->
                    val distanceToCenter = kotlin.math.abs(
                        (item.offset.y + item.size.height / 2f) - viewportCenter
                    )

                    val isAlreadyPlaying = playingMap.containsKey(item.index)

                    if (isAlreadyPlaying) {
                        distanceToCenter - stickinessBonus
                    } else {
                        distanceToCenter
                    }
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
                    playerPool[slot].pause()
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
                        ?.files?.firstOrNull()?.fullUrl
                        ?: return@forEach

                    val currentUrl =
                        player.currentMediaItem?.localConfiguration?.uri?.toString()

                    if (currentUrl != url) {
                        slotReady[slot] = false
                        player.stop()
                        player.clearMediaItems()
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
            val last = gridState.layoutInfo.visibleItemsInfo
                .lastOrNull()?.index ?: return@derivedStateOf false
            last >= currentItems.size - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && currentHasMore && !currentLoading) onLoadMore()
    }

    Box(modifier = modifier.fillMaxSize().clipToBounds()) {
        val bottomPadding = if (isOwnProfile) 72.dp else 16.dp
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = topPadding,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + bottomPadding,
            ),
        ) {
            itemsIndexed(
                items = videoItems,
                key = { _, item -> "video${item.id}" },
                contentType = { _, _ -> "video" },
            ) { index, item ->
                val file = item.files.firstOrNull()

                ThumbnailCell(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clickableWithoutRipple {
                            file?.fullUrl?.let(onVideoClicked)
                        },
                    file = file,
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
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        LottieProgressIndicator(Modifier.size(24.dp))
                    }
                }
            }
        }


        playerPool.forEachIndexed { slot, player ->
            key(slot) {
                PlayerOverlaySlot(
                    slot = slot,
                    player = player,
                    playingMap = playingMap,
                    slotReady = slotReady,
                    gridState = gridState,
                )
            }
        }
    }
}

@Composable
private fun ThumbnailCell(
    modifier: Modifier = Modifier,
    file: PublicationFile? = null,
) {
    Box(modifier = modifier.clip(RectangleShape)) {
        if (file?.thumbnailBitmap != null) {
            Image(
                bitmap = file.thumbnailBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
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

@UnstableApi
@Composable
private fun PlayerOverlaySlot(
    slot: Int,
    player: ExoPlayer,
    playingMap: SnapshotStateMap<Int, Int>,
    slotReady: SnapshotStateMap<Int, Boolean>,
    gridState: LazyGridState,
) {
    val targetIndex by remember {
        derivedStateOf {
            playingMap.entries.firstOrNull { it.value == slot }?.key
        }
    }

    var videoAspect by remember { mutableFloatStateOf(1f) }

    DisposableEffect(player) {
        player.videoSize.let { s ->
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
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            videoAspect = 1f
        }
    }

    val isReady by remember {
        derivedStateOf { slotReady[slot] == true }
    }

    val videoAlpha by animateFloatAsState(
        targetValue = if (isReady && targetIndex != null) 1f else 0f,
        animationSpec = if (isReady) tween(durationMillis = 200) else snap(),
        label = "overlayAlpha_$slot",
    )

    val cropScaleX = if (videoAspect > 1f) videoAspect else 1f
    val cropScaleY = if (videoAspect < 1f) 1f / videoAspect else 1f

    Box(
        modifier = Modifier
            .layout { measurable, _ ->
                val idx = targetIndex
                val layoutInfo = gridState.layoutInfo
                val info = if (idx != null) {
                    layoutInfo.visibleItemsInfo.firstOrNull { it.index == idx }
                } else null

                if (info != null) {
                    val placeable = measurable.measure(
                        Constraints.fixed(info.size.width, info.size.height)
                    )

                    val screenY = info.offset.y + layoutInfo.beforeContentPadding
                    layout(info.size.width, info.size.height) {
                        if (isReady) {
                            placeable.place(info.offset.x, screenY)
                        } else {
                            placeable.place(info.offset.x + 10000, screenY)
                        }
                    }
                } else {
                    val placeable = measurable.measure(Constraints.fixed(1, 1))
                    layout(0, 0) { placeable.place(-10000, -10000) }
                }
            }
            .clip(RectangleShape),
    ) {
        PlayerSurface(
            player = player,
            surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = videoAlpha
                    scaleX = cropScaleX
                    scaleY = cropScaleY
                },
        )
    }
}
