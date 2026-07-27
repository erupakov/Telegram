package org.telegram.divo.screen.models.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import org.telegram.divo.screen.models.ModelsViewState
import org.telegram.divo.screen.models.Story
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.AndroidUtilities.lerp
import org.telegram.messenger.R
import androidx.compose.ui.viewinterop.AndroidView
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.messenger.AndroidUtilities
import org.telegram.ui.Components.BackupImageView
import org.telegram.ui.Components.AvatarDrawable
import org.telegram.ui.LaunchActivity
import org.telegram.ui.Stories.recorder.StoryRecorder

@Composable
fun AnimatedLargeStoriesOverlay(
    collapseFraction: Float,
    stories: List<Story> = ModelsViewState.preview.stories,
    hazeState: HazeState
) {
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    val expandedHeight = 104.dp
    val collapsedHeight = 56.dp
    val currentHeight = lerp(expandedHeight, collapsedHeight, collapseFraction)

    val lift = lerp(0.dp, (-10).dp, collapseFraction)
    val overlayHeight = statusBarHeight + currentHeight + 16.dp + 60.dp

    val startYPx = with(density) { (overlayHeight * 0.15f).toPx() }
    val endYPx = with(density) { overlayHeight.toPx() }

    val listState = rememberLazyListState()
    val itemWidthWithSpacingPx = with(density) { 80.dp.toPx() }
    val scrollOffsetPx by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex * itemWidthWithSpacingPx + listState.firstVisibleItemScrollOffset
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(overlayHeight)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = AppTheme.colors.backgroundLight,
                    blurRadius = 40.dp,
                    tints = listOf(
                        HazeTint(AppTheme.colors.backgroundLight.copy(alpha = 0.75f))
                    )
                )
            ) {
                progressive = HazeProgressive.verticalGradient(
                    startY = startYPx,
                    startIntensity = 1f,
                    endY = endYPx,
                    endIntensity = 0f,
                    easing = FastOutSlowInEasing,
                )
            }
    )

    val baseY = statusBarHeight + 8.dp

    val account = UserConfig.selectedAccount

    val hasOtherStories = stories.any { !it.isSelf }

    val visibleStories = if (stories.size == 1 && stories.first().isSelf) {
        stories.take(1)
    } else {
        stories.filter { !it.isSelf }.take(3)
    }
    val collapsedItemSize = 32.dp
    val collapsedOverlap = 12.dp

    val collapsedGroupWidth = if (visibleStories.isEmpty()) 0.dp else visibleStories.size * collapsedItemSize -
            (visibleStories.size - 1) * collapsedOverlap
    val collapsedGroupStartX = (screenWidth - collapsedGroupWidth) / 2

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = baseY + lift),
        state = listState,
        userScrollEnabled = collapseFraction == 0f,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(stories) { index, story ->

            val itemScale = lerp(1f, collapsedItemSize / 64.dp, collapseFraction)

            val expandedCenterX = 16.dp + (index * 80.dp) + 32.dp

            val collapsedStories = visibleStories
            val collapsedIndex = collapsedStories.indexOfFirst { it.id == story.id }
            val isInCollapsedGroup = collapsedIndex != -1

            val collapsedCenterX = if (isInCollapsedGroup) {
                collapsedGroupStartX +
                        collapsedIndex * (collapsedItemSize - collapsedOverlap) +
                        collapsedItemSize / 2
            } else {
                expandedCenterX
            }
            val shiftXDp = if (isInCollapsedGroup) {
                (collapsedCenterX - expandedCenterX) * collapseFraction
            } else {
                0.dp
            }
            val shiftXPx = with(density) { shiftXDp.toPx() } + if (isInCollapsedGroup) scrollOffsetPx * collapseFraction else 0f



            val circleAlpha = when {
                isInCollapsedGroup -> 1f
                else -> (1f - collapseFraction * 2f).coerceIn(0f, 1f)
            }

            val textAlpha = (1f - collapseFraction * 2.5f).coerceIn(0f, 1f)

            Column(
                modifier = Modifier
                    .width(64.dp)
                    .zIndex((100 - index).toFloat())
                    .graphicsLayer {
                        scaleX = itemScale
                        scaleY = itemScale
                        translationX = shiftXPx
                        alpha = circleAlpha
                        transformOrigin = TransformOrigin(0.5f, 0f)
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .clickable { 
                                val fragment = LaunchActivity.getLastFragment() ?: return@clickable
                                if (story.isSelf && !story.hasStories) {
                                    StoryRecorder.getInstance(fragment.parentActivity, account).open(null)
                                } else {
                                    val peerIds = arrayListOf(story.dialogId)
                                    fragment.getOrCreateStoryViewer().open(
                                        fragment.context, null, peerIds, 0, null, null, null, false
                                    )
                                }
                            }
                            .then(
                                if (story.isLoading) Modifier // No border while loading, spinner handles it
                                else if (!story.hasStories) Modifier
                                else Modifier.drawBehind {
                                    val totalCount = story.totalCount.coerceAtLeast(1)
                                    val unreadCount = story.unreadCount
                                    val readCount = (totalCount - unreadCount).coerceAtLeast(0)
                                    
                                    val strokeWidth = 3.dp.toPx()
                                    val gapAngle = if (totalCount > 1) 10f else 0f
                                    val sweepAngle = (360f - (gapAngle * totalCount)) / totalCount
                                    
                                    val unreadBrush = Brush.horizontalGradient(colors = listOf(Color(0xFF990000), Color(0xFF000000)))
                                    val readBrush = Brush.horizontalGradient(colors = listOf(Color.LightGray, Color.Gray))
                                    
                                    var startAngle = -90f
                                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                                    
                                    for (i in 0 until readCount) {
                                        drawArc(
                                            brush = readBrush,
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle,
                                            useCenter = false,
                                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                            size = arcSize,
                                            topLeft = topLeft
                                        )
                                        startAngle += sweepAngle + gapAngle
                                    }
                                    
                                    for (i in 0 until unreadCount) {
                                        drawArc(
                                            brush = unreadBrush,
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle,
                                            useCenter = false,
                                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                            size = arcSize,
                                            topLeft = topLeft
                                        )
                                        startAngle += sweepAngle + gapAngle
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val avatarSize = if (story.hasStories || story.isLoading) 54.dp else 60.dp
                        val avatarRadius = if (story.hasStories || story.isLoading) 27f else 30f

                        Box(
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    BackupImageView(ctx).apply {
                                        setRoundRadius(AndroidUtilities.dp(avatarRadius))
                                    }
                                },
                                update = { view ->
                                    val avatarDrawable = AvatarDrawable()
                                    if (story.dialogId > 0) {
                                        val user = MessagesController.getInstance(account).getUser(story.dialogId)
                                        avatarDrawable.setInfo(account, user)
                                        view.setForUserOrChat(user, avatarDrawable)
                                    } else {
                                        val chat = MessagesController.getInstance(account).getChat(-story.dialogId)
                                        avatarDrawable.setInfo(account, chat)
                                        view.setForUserOrChat(chat, avatarDrawable)
                                    }
                                },
                                modifier = Modifier.size(avatarSize)
                            )
                        }

                        if (story.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.fillMaxSize(),
                                color = Color(0xFF990000),
                                strokeWidth = 3.dp
                            )
                        }
                    }
                    if (story.isSelf) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 0.dp, y = 0.dp)
                                .size(20.dp)
                                .background(Color.White, CircleShape)
                                .clickable {
                                    val fragment = LaunchActivity.getLastFragment() ?: return@clickable
                                    StoryRecorder.getInstance(fragment.parentActivity, account).open(null)
                                }
                                .padding(2.dp)
                                .background(AppTheme.colors.onBackground, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                modifier = Modifier.size(12.dp),
                                painter = rememberVectorPainter(Icons.Default.Add),
                                contentDescription = null,
                                tint = Color.Black
                            )
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                val text = if (story.isSelf) {
                    org.telegram.messenger.LocaleController.getString(if (story.hasStories) R.string.MyStory else R.string.AddStoryLabel)
                } else {
                    story.userName
                }

                val isGray = story.isSelf && !story.hasStories && hasOtherStories
                Text(
                    text = text,
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isGray) Color(0xFFB0B4BA) else Color.Black,
                    modifier = Modifier.graphicsLayer { alpha = textAlpha }
                )
            }
        }
    }
}

private fun lerp(start: Dp, end: Dp, fraction: Float): Dp = start + (end - start) * fraction