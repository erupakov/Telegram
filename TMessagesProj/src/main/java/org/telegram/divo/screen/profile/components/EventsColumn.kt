package org.telegram.divo.screen.profile.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.entity.Event
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun EventsColumn(
    events: List<Event>,
    isOwnProfile: Boolean,
    isModel: Boolean,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    topPadding: Dp = 0.dp,
    onLoadMore: () -> Unit,
    onEventClicked: (Int) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleIndex = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = lazyListState.layoutInfo.totalItemsCount
            lastVisibleIndex >= totalItems - 5 && totalItems > 0
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppTheme.colors.backgroundLight),
        state = lazyListState,
        contentPadding = PaddingValues(
            top = topPadding,
            bottom = WindowInsets.navigationBars
                .asPaddingValues()
                .calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = events,
            key = { it.id }
        ) {
            EventItem(
                item = it,
                isOwnProfile = isOwnProfile,
                isModel = isModel,
                onEventClicked = onEventClicked
            )
        }
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(54.dp).navigationBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    LottieProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
private fun EventItem(
    item: Event,
    isOwnProfile: Boolean,
    isModel: Boolean,
    onEventClicked: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f).clickableWithoutRipple { onEventClicked(item.id) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DivoAsyncImage(
                modifier = Modifier.size(60.dp).clip(CircleShape),
                model = item.creator?.avatar?.fullUrl,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    modifier = Modifier,
                    text = item.title.orEmpty(),
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "June 26 · 5:00 PM · \uD83C\uDDFA\uD83C\uDDF8 New York",
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 14.sp,
                    color = Color.Black.copy(0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (isOwnProfile) {
            UIButtonNew(
                modifier = Modifier
                    .height(32.dp),
                text = stringResource(R.string.ButtonEdit),
                textStyle = AppTheme.typography.textButton.copy(
                    fontSize = 14.sp
                ),
                onClick = {}
            )
        }

        if (isModel && !isOwnProfile) {
            UIButtonNew(
                modifier = Modifier
                    .height(32.dp),
                text = stringResource(R.string.ButtonApply),
                textStyle = AppTheme.typography.textButton.copy(
                    fontSize = 14.sp
                ),
                onClick = {}
            )
        }
    }
}