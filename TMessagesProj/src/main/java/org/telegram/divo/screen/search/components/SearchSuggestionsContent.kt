package org.telegram.divo.screen.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.PlaceholderAvatar
import org.telegram.divo.entity.SearchedProfile
import org.telegram.divo.screen.search_agency.component.SearchSuggestionsLoadingContent
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun SearchSuggestionContent(
    searchResults: List<SearchedProfile>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onClicked: (SearchedProfile) -> Unit,
    onLoadMore: () -> Unit,
) {
    val insets = WindowInsets.systemBars.asPaddingValues()
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val navBarBottom = insets.calculateBottomPadding()

    val height = LocalConfiguration.current.screenHeightDp.dp -
            insets.calculateTopPadding() -
            navBarBottom -
            180.dp -
            maxOf(imeBottom - navBarBottom, 0.dp)

    if (isLoading) {
        SearchSuggestionsLoadingContent()
    } else if (searchResults.isNotEmpty()) {
        SearchSuggestions(
            items = searchResults,
            height = height,
            isLoadingMore = isLoadingMore,
            hasMore = hasMore,
            onClicked = onClicked,
            onLoadMore = onLoadMore
        )
    }
}

@OptIn(FlowPreview::class)
@Composable
private fun SearchSuggestions(
    items: List<SearchedProfile>,
    height: Dp,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onClicked: (SearchedProfile) -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            val total = layoutInfo.totalItemsCount

            lastVisible >= total - 1 && hasMore && !isLoadingMore
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { shouldLoadMore }
            .distinctUntilChanged()
            .filter { it }
            .collect { onLoadMore() }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (items.size < 10) Modifier else Modifier.height(height)
            )
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppTheme.colors.onBackground),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Spacer(Modifier.height(4.dp)) }

        items(
            items = items,
            key = { it.id }
        ) {
            SuggestionItem(
                avatarUrl = it.photo,
                name = it.name,
                onClicked = { onClicked(it) }
            )
        }

        if (!isLoadingMore) item { Spacer(Modifier.height(4.dp)) }

        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LottieProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    avatarUrl: String,
    name: String,
    onClicked: () -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp).clickableWithoutRipple { onClicked() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (avatarUrl.isBlank()) {
            PlaceholderAvatar(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.accentOrange),
                name = name,
            )
        } else {
            DivoAsyncImage(
                modifier = Modifier.size(48.dp).clip(CircleShape),
                model = avatarUrl
            )
        }

        Spacer(Modifier.width(10.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = name,
            style = AppTheme.typography.helveticaNeueRegular,
            color = Color.Black,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            painter = painterResource(R.drawable.ic_divo_arrow_insert),
            contentDescription = null
        )
    }
}