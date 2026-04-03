package org.telegram.divo.screen.models.components

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.components.TextTitle
import org.telegram.divo.screen.models.ModelsViewState
import org.telegram.divo.screen.models.Story
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun AnimatedCollapsingTopBar(
    collapseFraction: Float,
    stories: List<Story>,
    onTitleMeasured: (Float) -> Unit,
    onSearch: () -> Unit,
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topBarHeight = 56.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(statusBarHeight + topBarHeight)
            .background(AppTheme.colors.backgroundLight)
            .padding(top = statusBarHeight),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .onGloballyPositioned { coordinates ->
                            onTitleMeasured(coordinates.size.width.toFloat())
                        },
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        modifier = Modifier.offset(y = 4.dp),
                        text = stringResource(R.string.Models).uppercase(),
                        color = AppTheme.colors.textPrimary,
                        style = AppTheme.typography.displayLarge
                    )
                }

                val topBarItemsAlpha = if (collapseFraction >= 1f) 1f else 0f

                Row(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .alpha(topBarItemsAlpha),
                    horizontalArrangement = Arrangement.spacedBy((-12).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayStories = stories.filter { it.id != "0" }.take(3)

                    displayStories.forEachIndexed { index, story ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .zIndex((3 - index).toFloat())
                                .clip(CircleShape)
                                .border(
                                    width = 1.5.dp,
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF990000), Color(0xFF000000)),
                                        tileMode = TileMode.Repeated
                                    ),
                                    shape = CircleShape
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(1.5.dp)
                                    .clip(CircleShape)
                                    .background(AppTheme.colors.backgroundLight)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(story.imageUrl),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            RoundedButton(
                modifier = Modifier,
                resId = R.drawable.ic_divo_search_24,
                iconSize = 24.dp,
                paddingEnd = 0.dp,
                onClick = onSearch
            )
        }
    }
}

@Composable
fun AnimatedLargeStoriesOverlay(
    collapseFraction: Float,
    stories: List<Story> = ModelsViewState.preview.stories,
    titleWidthPx: Float
) {
    if (collapseFraction >= 1f) return

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val startYDp = statusBarHeight + 56.dp + 8.dp

    val currentYDp = startYDp - (68.dp * collapseFraction)
    val titleWidthDp = with(density) { titleWidthPx.toDp() }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = currentYDp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(stories) { index, story ->
            val itemScale = 1f - (collapseFraction * 0.5f)

            val originalCenterXDp = 16.dp + (index * 80.dp) + 32.dp
            val firstTargetCenterDp = 16.dp + titleWidthDp + 12.dp + 16.dp

            val targetCenterXDp = when (index) {
                0 -> 80.dp
                1 -> firstTargetCenterDp
                2 -> firstTargetCenterDp + 20.dp
                3 -> firstTargetCenterDp + 40.dp
                else -> firstTargetCenterDp + 40.dp
            }

            val deltaXDp = targetCenterXDp - originalCenterXDp
            val shiftXDp = deltaXDp * collapseFraction
            val shiftXPx = with(density) { shiftXDp.toPx() }

            val isTarget = index in 1..3
            val circleAlpha = if (isTarget) {
                1f
            } else {
                (1f - (collapseFraction * 2.5f)).coerceIn(0f, 1f)
            }
            val textAlpha = (1f - (collapseFraction * 3f)).coerceIn(0f, 1f)
            val circleCenter = 32.dp
            val columnCenter = 42.dp
            val centerCorrection = columnCenter - circleCenter  // = 10.dp

            Column(
                modifier = Modifier
                    .zIndex((100 - index).toFloat())
                    .graphicsLayer {
                        scaleX = itemScale
                        scaleY = itemScale
                        translationX = shiftXPx
                        translationY = -with(density) { (centerCorrection * (1f - itemScale)).toPx() }
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(64.dp)
                        .graphicsLayer { alpha = circleAlpha }
                        .clip(CircleShape)
                        .clickable {  }
                        .then(
                            if (story.id == "0") Modifier.background(Color.White)
                            else Modifier.border(
                                width = 3.dp, brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF990000), Color(0xFF000000)),
                                    tileMode = TileMode.Repeated
                                ), shape = CircleShape
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Card(modifier = Modifier.size(54.dp).background(Color.White), shape = CircleShape) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
                            if (story.id == "0") {
                                Icon(
                                    painter = rememberVectorPainter(Icons.Default.Add), contentDescription = null,
                                    tint = Color.Black, modifier = Modifier.size(24.dp).background(Color.White)
                                )
                            } else {
                                Image(
                                    modifier = Modifier.size(54.dp).background(Color(0xFFE7E7E8)),
                                    painter = rememberAsyncImagePainter(story.imageUrl), contentDescription = null, contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (story.id == "0") "Add Story" else story.userName,
                    color = if (story.id == "0") Color(0xFFB0B4BA) else Color.Black,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 11.5.sp,
                    modifier = Modifier.graphicsLayer { alpha = textAlpha }
                )
            }
        }
    }
}