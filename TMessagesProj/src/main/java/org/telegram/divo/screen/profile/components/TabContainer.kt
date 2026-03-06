package org.telegram.divo.screen.profile.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabContainer(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    pagerState: PagerState,
    isOwnProfile: Boolean,
) {
    val coroutineScope = rememberCoroutineScope()

    val destinations = Destination.entries.filter {
        !(isOwnProfile && it == Destination.PLAYLISTS)
    }

    val selectedDestination = pagerState.currentPage
    val flingBehavior = ScrollableDefaults.flingBehavior()

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                val velocityTracker = VelocityTracker()
                detectVerticalDragGestures(
                    onDragEnd = {
                        val velocity = velocityTracker.calculateVelocity()
                        coroutineScope.launch {
                            lazyListState.scroll {
                                with(flingBehavior) {
                                    performFling(-velocity.y)
                                }
                            }
                        }
                        velocityTracker.resetTracking()
                    }
                ) { change, dragAmount ->
                    change.consume()
                    velocityTracker.addPosition(change.uptimeMillis, change.position)
                    coroutineScope.launch {
                        lazyListState.dispatchRawDelta(-dragAmount)
                    }
                }
            }
            .background(AppTheme.colors.blackAlpha12)
    ) {
        PrimaryTabRow(
            selectedTabIndex = selectedDestination,
            containerColor = Color.Transparent,
            indicator = {
                Box(
                    modifier = Modifier.tabIndicatorOffset(selectedDestination)
                ) {
                    Spacer(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(44.dp)
                            .height(2.dp)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 4.dp,
                                    bottomStart = 0.dp,
                                    bottomEnd = 0.dp
                                )
                            )
                    )
                }
            },
            divider = {},
        ) {
            destinations.forEachIndexed { index, destination ->
                Tab(
                    modifier = Modifier.width(100.dp),
                    selected = selectedDestination == index,
                    icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(destination.iconResId),
                            modifier = Modifier.size(24.dp),
                            contentDescription = destination.contentDescription
                        )
                    },
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    selectedContentColor = Color.White
                )
            }
        }
    }
}

enum class Destination(
    val route: String,
    @DrawableRes
    val iconResId: Int,
    val contentDescription: String,
) {
    SONGS("songs", R.drawable.divo_profile_tab_1, "Songs"),
    ALBUM("album", R.drawable.divo_profile_tab_2, "Album"),
    PLAYLISTS("playlist", R.drawable.divo_profile_tab_3, "Playlist")
}

@Preview
@Composable
private fun TabContainerPreview() {
    AppTheme {
        TabContainer(
            isOwnProfile = false,
            lazyListState = rememberLazyListState(),
            pagerState = rememberPagerState(0) { 3 }
        )
    }
}