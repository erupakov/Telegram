package org.telegram.divo.screen.search

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.DivoTextField
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.PlaceholderAvatar
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.entity.Agency
import org.telegram.divo.screen.search.components.SearchSuggestionsLoadingContent
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    onBack: () -> Unit
) {
    val state = viewModel.state.collectAsState().value
    val context = LocalContext.current

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect {
            when (it) {
                Effect.NavigateBack -> onBack()
                is Effect.ShowError -> Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    SearchContent(
        state = state,
        onIntent = { viewModel.setIntent(it) }
    )
}

@Composable
fun SearchContent(
    state: State,
    onIntent: (Intent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.backgroundNew)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            SearchRow(
                value = state.query,
                onValueChanged = { onIntent(Intent.OnQueryChanged(it)) },
                onBack = { onIntent(Intent.OnBackClicked) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoadingAgencies) {
                SearchSuggestionsLoadingContent()
            } else if (state.items.isNotEmpty()) {
                SearchSuggestions(
                    items = state.items,
                    isLoadingMore = state.isLoadingMoreAgencies,
                    hasMore = state.hasMoreAgencies,
                    onClicked = { onIntent(Intent.OnSearchSelected(it)) },
                    onLoadMore = { onIntent(Intent.OnLoadMore) }
                )
            }
        }
        UIButtonNew(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            enabled = state.query.isNotBlank() || state.agencyName.isNotBlank(),
            onClick = { onIntent(Intent.OnSearchSelected(state.query)) }
        )
    }
}

@OptIn(FlowPreview::class)
@Composable
private fun SearchSuggestions(
    items: List<Agency>,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onClicked: (String) -> Unit,
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
            .fillMaxHeight()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(vertical = 20.dp),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            items = items,
            key = { it.id }
        ) {
            SuggestionItem(
                avatarUrl = it.photo?.fullUrl.orEmpty(),
                agencyName = it.title,
                onClicked = { onClicked(it.title) }
            )
        }
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
    agencyName: String,
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
                name = agencyName,
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
            text = agencyName,
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

@Composable
private fun SearchRow(
    value: String,
    onValueChanged: (String) -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        DivoTextField(
            modifier = Modifier.weight(1f),
            value = value,
            onValueChange = onValueChanged,
            cornerRadius = 99.dp,
            backgroundColor = Color.White,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 13.dp),
            textStyle = TextStyle(fontSize = 14.sp),
            placeholder = stringResource(R.string.EnterAgencyName)
        )
        Spacer(Modifier.width(10.dp))
        RoundedButton(
            resId = R.drawable.ic_divo_close,
            onClick = onBack
        )
    }
}