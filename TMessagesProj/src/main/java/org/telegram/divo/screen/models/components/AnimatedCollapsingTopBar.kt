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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
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

    val visibleStories = stories.filter { it.id != "0" }.take(3)
    val collapsedItemSize = 32.dp
    val collapsedOverlap = 12.dp

    val collapsedGroupWidth = visibleStories.size * collapsedItemSize -
            (visibleStories.size - 1) * collapsedOverlap
    val collapsedGroupStartX = (screenWidth - collapsedGroupWidth) / 2

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = baseY + lift),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(stories) { index, story ->

            val itemScale = lerp(1f, collapsedItemSize / 64.dp, collapseFraction)

            val expandedCenterX = 16.dp + (index * 80.dp) + 32.dp

            val collapsedStories = stories.filter { it.id != "0" }.take(3)
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
            val shiftXPx = with(density) { shiftXDp.toPx() }



            val circleAlpha = when {
                story.id == "0" -> (1f - collapseFraction * 2f).coerceIn(0f, 1f)
                isInCollapsedGroup -> 1f
                else -> (1f - collapseFraction * 2f).coerceIn(0f, 1f)
            }

            val textAlpha = (1f - collapseFraction * 2.5f).coerceIn(0f, 1f)

            Column(
                modifier = Modifier
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
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .clickable { }
                        .then(
                            if (story.id == "0") Modifier.background(AppTheme.colors.onBackground)
                            else Modifier.border(
                                width = 3.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF990000), Color(0xFF000000))
                                ),
                                shape = CircleShape
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.White),
                        shape = CircleShape
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(AppTheme.colors.onBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            if (story.id == "0") {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = rememberVectorPainter(Icons.Default.Add),
                                    contentDescription = null,
                                    tint = Color.Black,
                                )
                            } else {
                                Image(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(Color(0xFFE7E7E8)),
                                    painter = rememberAsyncImagePainter(story.imageUrl),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = if (story.id == "0") stringResource(R.string.AddStoryLabel) else story.userName,
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (story.id == "0") Color(0xFFB0B4BA) else Color.Black,
                    modifier = Modifier.graphicsLayer { alpha = textAlpha }
                )
            }
        }
    }
}

private fun lerp(start: Dp, end: Dp, fraction: Float): Dp = start + (end - start) * fraction