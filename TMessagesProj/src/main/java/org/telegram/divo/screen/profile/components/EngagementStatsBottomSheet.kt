package org.telegram.divo.screen.profile.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.DivoTextField
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.shimmer
import org.telegram.divo.entity.EngagementUser
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngagementStatsBottomSheet(
    stats: StatsType,
    items: List<EngagementUser>,
    isLoadingMoreFeed: Boolean,
    isLoadingSearch: Boolean,
    searchQuery: String,
    searchResults: List<EngagementUser>,
    isSearchMode: Boolean,
    isLoadingMoreSearch: Boolean,
    onQueryChanged: (String) -> Unit,
    onLoadMoreSearch: () -> Unit,
    onProfileClicked: (Int) -> Unit,
    onLoadMore: () -> Unit,
    onDismiss: () -> Unit
) {
    val statsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val lazyListState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleIndex = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = lazyListState.layoutInfo.totalItemsCount
            lastVisibleIndex >= totalItems - 5 && totalItems > 0
        }
    }

    val shouldLoadMoreSearch by remember {
        derivedStateOf {
            val last = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = lazyListState.layoutInfo.totalItemsCount
            isSearchMode && last >= total - 5 && total > 0
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    LaunchedEffect(shouldLoadMoreSearch) {
        if (shouldLoadMoreSearch) onLoadMoreSearch()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = statsSheetState,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        containerColor = AppTheme.colors.backgroundLight,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .width(56.dp)
                    .height(5.dp)
                    .background(Color(0xA0A0A099), RoundedCornerShape(100.dp))
            )
        }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val configuration = LocalConfiguration.current
            val screenHeight = configuration.screenHeightDp.dp
            val topOffset = screenHeight * 0.05f
            val targetHeight = (maxHeight - topOffset).coerceAtLeast(0.dp)

            if (items.isEmpty()) {
                EmptyContent(stats)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(targetHeight)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = stringResource(stats.value),
                        style = AppTheme.typography.helveticaNeueLtCom,
                        fontSize = 20.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    DivoTextField(
                        value = searchQuery,
                        onValueChange = onQueryChanged,
                        leadingIcon = R.drawable.ic_divo_search,
                        placeholder = stringResource(R.string.SearchModels),
                        backgroundColor = AppTheme.colors.onBackground,
                        cornerRadius = 99.dp,
                        placeholderColor = AppTheme.colors.textPrimary.copy(0.6f),
                        height = 40.dp,
                        textStyle = TextStyle(fontSize = 15.sp),
                        horizontalContentPadding = 16.dp,
                        trailingIcon = if (searchQuery.isNotBlank()) R.drawable.ic_divo_clear else null,
                        onTrailingIconClick = { onQueryChanged("") }
                    )

                    if (isSearchMode && isLoadingSearch) {
                        Spacer(modifier = Modifier.height(20.dp))
                        EngagementStatsLoadingContent()
                    } else if (isSearchMode && searchResults.isEmpty()) {
                        EmptySearchContent()
                    } else {
                        Spacer(modifier = Modifier.height(20.dp))
                        LazyColumn(
                            state = lazyListState,
                            contentPadding = PaddingValues(bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            when {
                                isSearchMode -> {
                                    items(searchResults, key = { it.id }) { item ->
                                        StatsDetailRow(
                                            name = item.fullName,
                                            type = item.roleLabel,
                                            avatarUrl = item.photoUrl,
                                            onClicked = { onProfileClicked(item.id) }
                                        )
                                    }
                                    if (isLoadingMoreSearch) {
                                        item {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().height(72.dp).navigationBarsPadding(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(color = Color.Black)
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    items(items, key = { it.id }) { user ->
                                        StatsDetailRow(
                                            name = user.fullName,
                                            type = user.roleLabel,
                                            avatarUrl = user.photoUrl,
                                            onClicked = { onProfileClicked(user.id) }
                                        )
                                    }
                                    if (isLoadingMoreFeed) {
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
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsDetailRow(
    name: String,
    type: String,
    avatarUrl: String,
    onClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableWithoutRipple { onClicked() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        DivoAsyncImage(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape),
            model = avatarUrl
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = name,
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    modifier = Modifier
                        .size(16.dp)
                        .padding(bottom = 1.dp),
                    painter = painterResource(R.drawable.divo_premium_bage),
                    contentDescription = null,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = type,
                style = AppTheme.typography.helveticaNeueRegular,
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun EngagementStatsLoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize().background(AppTheme.colors.backgroundLight)
    ) {
        repeat(10) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .shimmer(),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shimmer()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(250.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shimmer()
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun EmptySearchContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.onBackground),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(22.dp))
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.backgroundLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(R.drawable.ic_divo_search),
                contentDescription = null,
                tint = AppTheme.colors.textPrimary
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.NoResultsFound).uppercase(),
            style = AppTheme.typography.helveticaNeueLtCom,
            fontSize = 26.sp,
            color = AppTheme.colors.textPrimary
        )
        Spacer(Modifier.height(22.dp))
    }
}

@Composable
private fun EmptyContent(
    stats: StatsType
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val textId = when (stats) {
        StatsType.LIKES -> R.string.NoLikesYet
        StatsType.VIEWS -> R.string.NoProfileViewedYet
        StatsType.SAVES -> R.string.NothingSavedYet
    }

    Column(
        modifier = Modifier.fillMaxWidth().height(screenHeight * 0.5f).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(textId),
            color = AppTheme.colors.textPrimary,
            style = AppTheme.typography.helveticaNeueLtCom,
            fontSize = 26.sp,
            textAlign = TextAlign.Center
        )
    }
}

enum class StatsType(val value: Int) {
    LIKES(R.string.StatLikes),
    VIEWS(R.string.StatViews),
    SAVES(R.string.StatSaves),
}

@Preview
@Composable
private fun EngagementStatsBottomSheetPreview() {
    AppTheme {
        EngagementStatsBottomSheet(
            stats = StatsType.SAVES,
            items = emptyList(),
            isLoadingMoreFeed = false,
            isLoadingSearch = false,
            searchQuery = "",
            searchResults = emptyList(),
            isSearchMode = false,
            isLoadingMoreSearch = false,
            onQueryChanged = {},
            onLoadMoreSearch = {},
            onProfileClicked = {},
            onLoadMore = {},
            onDismiss = {}
        )
    }
}