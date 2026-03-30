package org.telegram.divo.screen.event_list

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.screen.event_list.components.EventItemView
import org.telegram.divo.screen.event_list.components.EventListTopBar
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

object EventIntentData{
    var eventId: Long = 0
}


@Composable
fun EventListScreen(
    viewModel: EventListViewModel = viewModel(),
    onNavigateToEventDetails: (Long) -> Unit = {},
    onNavigateToCreateEvent: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true){
       // viewModel.getEventList(null)
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { action ->
            when (action) {
                EventListEffect.NavigateToCreateEvent -> onNavigateToCreateEvent()
                EventListEffect.NavigateToSearch -> onNavigateToSearch()
                is EventListEffect.NavigateToEventDetails -> onNavigateToEventDetails(
                    action.eventId
                )
            }
        }
    }
    EventListContent(
        state = state,
        onEventClick = {
            EventIntentData.eventId = it
            onNavigateToEventDetails(
                it
            )
        },
        onCtaClick = {  },
        onSearchClick = {
            onNavigateToSearch()
        },
        onAddEventClick = {
            onNavigateToCreateEvent()
        },
        onLoadMore = {
            viewModel.handleIntent(EventListIntent.OnLoadMore)
        }
    )
}

@Composable
private fun EventListContent(
    state: EventListViewState,
    onEventClick: (Long) -> Unit,
    onCtaClick: (Long) -> Unit,
    onSearchClick: () -> Unit,
    onAddEventClick: () -> Unit,
    onLoadMore: () -> Unit,
) {
    val bottomInset = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    val listState = rememberLazyGridState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleIndex >= totalItems - 4 && totalItems > 0
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = AppTheme.colors.backgroundLight,
        topBar = {
            EventListTopBar(
                isModel = state.isModel,
                onSearchClick = onSearchClick,
                onAddEventClick = onAddEventClick,
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    contentAlignment = Alignment.Center,
                ) {
                    LottieProgressIndicator(
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            state.events.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.errorMessage ?: "No events yet",
                        color = Color(0xFF000000),
                        fontSize = 16.sp,
                    )
                }
            }

            else -> {
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    state = listState,
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = bottomInset + 66.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(
                        items = state.events,
                        key = { _, event -> event.id },
                    ) { index, event ->
                        EventItemView(
                            modifier = Modifier
                                .aspectRatio(0.8f)
                                .fillMaxWidth(),
                            eventName = event.title.orEmpty(),
                            eventImageUrl = event.creator?.avatar?.fullUrl.orEmpty(),
                            eventOwnerName = "",
                            eventOwnerImage = "",
                            dateLocationText = "",
                            durationText = "",
                            ctaText = "Apply",
                            ctaType = EventCtaType.Apply,
                            onCardClick = { onEventClick(event.id.toLong()) },
                            onCtaClicked = { onCtaClick(event.id.toLong()) },
                        )
                    }

                    if (state.isLoadingMore) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                LottieProgressIndicator(
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun TopBarIconFilterButton(
    @DrawableRes iconRes: Int = R.drawable.outline_filter_alt_24,
    contentDescription: String? = null,
    filterCount:Int? = null,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                tint = Color(0xFFBF7A54),
            )
        }
        if(filterCount!= null && filterCount > 1){
            Card(modifier = Modifier.size(12.dp), shape = CircleShape){
                Text("9+")
            }
        }
    }
}