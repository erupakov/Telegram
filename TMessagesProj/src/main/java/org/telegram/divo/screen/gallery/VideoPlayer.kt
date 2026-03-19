package org.telegram.divo.screen.gallery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.delay
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.DivoSlider
import org.telegram.divo.components.LottieProgressIndicator

private const val CONTROLS_HIDE_DELAY_MS = 4000L

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    url: String,
    isPlaying: Boolean,
    controlsVisible: Boolean,
    onChangeControlsVisible: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) player.play() else player.pause()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    player.pause()
                }

                Lifecycle.Event.ON_RESUME -> {
                    if (isPlaying) player.play()
                }

                Lifecycle.Event.ON_DESTROY -> {
                    player.release()
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.release()
        }
    }

    LaunchedEffect(url) {
        val current = player.currentMediaItem?.localConfiguration?.uri?.toString()
        if (current != url) {
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.play()
        }
    }

    var isReady by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var duration by remember { mutableLongStateOf(0L) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var videoAspectRatio by remember { mutableFloatStateOf(16f / 9f) }

    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    var autoHideTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(autoHideTick) {
        delay(CONTROLS_HIDE_DELAY_MS)
        onChangeControlsVisible(false)
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() { isReady = true }
            override fun onIsPlayingChanged(playing: Boolean) {
                if (!isDragging) isPlaying = playing
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    if (player.duration > 0L) duration = player.duration
                    isBuffering = false
                }
                if (state == Player.STATE_BUFFERING) {
                    isBuffering = true
                }
            }
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    videoAspectRatio = videoSize.width.toFloat() / videoSize.height.toFloat()
                }
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(isPlaying, isDragging) {
        while (isPlaying && !isDragging) {
            currentPosition = player.currentPosition
            delay(100)
        }
    }

    val videoAlpha by animateFloatAsState(
        targetValue = if (isReady) 1f else 0f,
        animationSpec = tween(200),
        label = "videoAlpha",
    )

    val sliderValue = if (isDragging) {
        dragFraction
    } else {
        if (duration > 0L) (currentPosition.toFloat() / duration).coerceIn(0f, 1f) else 0f
    }

    val animatedSliderValue by animateFloatAsState(
        targetValue = sliderValue,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing), // ← добавить
        label = "sliderProgress",
    )

    Box(
        modifier = modifier
            .clickableWithoutRipple {
                if (controlsVisible) {
                    onChangeControlsVisible(false)
                } else {
                    onChangeControlsVisible(true)
                    autoHideTick++
                }
            }
    ) {
        if (!isReady) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                LottieProgressIndicator(
                    modifier = Modifier.size(34.dp),
                    color = Color.White,
                )
            }
        }

        PlayerSurface(
            player = player,
            surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(videoAspectRatio)
                .alpha(videoAlpha),
        )

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                    .clickableWithoutRipple {
                        if (player.isPlaying) player.pause() else player.play()
                        autoHideTick++
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (isBuffering) {
                    LottieProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = Color.White,
                    )
                } else if (isPlaying) {
                    Canvas(modifier = Modifier.size(28.dp)) {
                        val barWidth = size.width * 0.22f
                        val barHeight = size.height * 0.75f
                        val top = (size.height - barHeight) / 2f
                        val gap = size.width * 0.18f
                        val leftStart = (size.width - barWidth * 2 - gap) / 2f

                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(leftStart, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2),
                        )
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(leftStart + barWidth + gap, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2),
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            VideoControls(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                currentPosition = currentPosition,
                duration = duration,
                sliderValue = animatedSliderValue,
                onSeekStart = { fraction ->
                    isDragging = true
                    dragFraction = fraction
                    player.seekTo((duration * fraction).toLong())
                    autoHideTick++
                },
                onSeekEnd = {
                    player.seekTo((duration * dragFraction).toLong())
                    currentPosition = (duration * dragFraction).toLong()
                    isDragging = false
                },
            )
        }
    }
}

@Composable
private fun VideoControls(
    modifier: Modifier = Modifier,
    currentPosition: Long,
    duration: Long,
    sliderValue: Float,
    onSeekStart: (Float) -> Unit,
    onSeekEnd: () -> Unit,
) {
    if (duration <= 0L) return

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DivoSlider(
            value = sliderValue,
            activeTrackColor = Color.White,
            onValueChange = onSeekStart,
            onValueChangeFinished = onSeekEnd,
            modifier = Modifier.weight(1f),
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = "${formatDuration(currentPosition)} / ${formatDuration(duration)}",
            color = Color.White,
            fontSize = 13.sp,
        )
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0)
        "%d:%02d:%02d".format(hours, minutes, seconds)
    else
        "%d:%02d".format(minutes, seconds)
}