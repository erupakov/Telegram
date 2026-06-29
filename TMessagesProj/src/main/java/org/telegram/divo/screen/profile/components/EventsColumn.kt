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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.material.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.utils.toEventDisplayDate
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
    transitionProgress: Float = 1f,
    topPadding: Dp = 0.dp,
    onLoadMore: () -> Unit,
    onEventClicked: (Int) -> Unit,
    onEventCreate: () -> Unit,
    onEventApplied: (Int) -> Unit
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

    if (events.isEmpty()) {
        EmptyEvent(
            isOwnProfile = isOwnProfile,
            transitionProgress = transitionProgress,
            topPadding = topPadding,
            bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp,
            onClick = onEventCreate
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.backgroundLight),
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
                    onEventClicked = onEventClicked,
                    onApplied = onEventApplied
                )
            }
            if (isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .navigationBarsPadding(),
                        contentAlignment = Alignment.Center
                    ) {
                        LottieProgressIndicator(modifier = Modifier.size(32.dp))
                    }
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
    onApplied: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickableWithoutRipple { onEventClicked(item.id) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DivoAsyncImage(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                model = item.files.firstOrNull()?.fullUrl ?: item.creator?.avatar?.fullUrl ?: item.creator?.photo?.fullUrl,
                errorContent = {
                    Image(
                        painter = painterResource(R.drawable.divo_event_placeholder),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
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
                    text = item.date.toEventDisplayDate(item.countryCode, item.city),
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 14.sp,
                    color = Color.Black.copy(0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (!isOwnProfile) {
            if (item.isApplied) {
                Row(
                    modifier = Modifier
                        .clickableWithoutRipple { onApplied(item.id) }
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Icon(
                        painter = painterResource(R.drawable.ic_divo_apply),
                        contentDescription = null,
                        tint = AppTheme.colors.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.ButtonApplied),
                        style = AppTheme.typography.textButton.copy(
                            fontSize = 14.sp,
                            color = AppTheme.colors.textPrimary
                        )
                    )
                }
            } else {
                UIButtonNew(
                    modifier = Modifier
                        .height(32.dp),
                    text = stringResource(R.string.ButtonApply),
                    paddingTop = 0.dp,
                    background = AppTheme.colors.accentOrange,
                    textStyle = AppTheme.typography.textButton.copy(
                        fontSize = 14.sp,
                        color = AppTheme.colors.onBackground
                    ),
                    onClick = { onApplied(item.id) }
                )
            }
        }
    }
}

@Composable
private fun EmptyEvent(
    isOwnProfile: Boolean,
    transitionProgress: Float,
    topPadding: Dp,
    bottomPadding: Dp,
    onClick: () -> Unit = {}
) {
    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.backgroundLight)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        val startOffset = topPadding + 16.dp
        val endOffset = maxHeight / 2 - 100.dp
        val currentOffset = startOffset + (endOffset - startOffset) * transitionProgress

        Column(
            modifier = Modifier
                .offset(y = currentOffset),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.textPrimary.copy(0.1f))
            ) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    painter = painterResource(R.drawable.ic_divo_event),
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.ThereAreNoUpcomingEvents).uppercase(),
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 26.sp,
                lineHeight = 30.sp,
                textAlign = TextAlign.Center,
            )
            if (isOwnProfile) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.CheckBackLaterOrCreateEvents),
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 16.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (isOwnProfile) {
            UIButtonNew(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = bottomPadding)
                    .align(Alignment.BottomCenter),
                text = stringResource(R.string.CreateEvent),
                onClick = onClick
            )
        }
    }
}