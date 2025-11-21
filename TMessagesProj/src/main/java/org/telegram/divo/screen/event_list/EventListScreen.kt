package org.telegram.divo.screen.event_list

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.RectangleShape
import org.telegram.divo.components.TextTitle
import org.telegram.divo.items.EventItemView
import org.telegram.messenger.R

@Composable
fun EventListScreen(
    viewModel: EventListViewModel = viewModel(),
    onNavigateToEventDetails: (String) -> Unit = {},
    onNavigateToCreateEvent: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.getData()
    }

    LaunchedEffect(viewModel.action) {


        viewModel.action.collect { action ->
            when (action) {
                EventListAction.NavigateToCreateEvent -> onNavigateToCreateEvent()
                EventListAction.NavigateToSearch -> onNavigateToSearch()
                is EventListAction.NavigateToEventDetails -> onNavigateToEventDetails(action.eventId)
            }
        }
    }

    EventListContent(
        state = state,
        onEventClick = { viewModel.setIntent(EventListIntent.OnEventCardClicked(it)) },
        onCtaClick = { viewModel.setIntent(EventListIntent.OnEventCtaClicked(it)) },
        onSearchClick = { viewModel.setIntent(EventListIntent.OnSearchClicked) },
        onAddEventClick = { viewModel.setIntent(EventListIntent.OnAddEventClicked) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventListContent(
    state: EventListViewState,
    onEventClick: (String) -> Unit,
    onCtaClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onAddEventClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
            .padding(top = 32.dp),
        containerColor = Color(0xFFFFFFFF),
        topBar = {
            EventListTopBar(
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
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Color(0xFFBF7A54))
                }
            }

            state.events.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
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
                        .padding(innerPadding),
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = state.events,
                        key = { it.id },
                    ) { event ->
                        EventItemView(
                            modifier = Modifier
                                .aspectRatio(0.8f)
                                .fillMaxWidth(),
                            eventName = event.title,
                            eventImageUrl = event.bannerUrl,
                            eventOwnerName = event.hostHandle,
                            eventOwnerImage = event.hostAvatarUrl,
                            dateLocationText = event.dateLocationText,
                            durationText = event.durationText,
                            ctaText = event.ctaLabel,
                            ctaType = event.ctaType,
                            onCardClick = { onEventClick(event.id) },
                            onCtaClicked = { onCtaClick(event.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventListTopBar(
    onSearchClick: () -> Unit,
    onAddEventClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextTitle(
                text = "EVENTS",
                modifier = Modifier.weight(1f),
                color = Color(0xFF000000),
                fontSize = 28.sp,
            )

            TopBarIconButton(
                iconRes = R.drawable.ic_ab_search,
                contentDescription = "Search events",
                onClick = onSearchClick,
            )
            Spacer(modifier = Modifier.width(8.dp))
            TopBarIconButton(
                iconRes = R.drawable.msg_add,
                contentDescription = "Add event",
                onClick = onAddEventClick,
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun TopBarIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.size(40.dp),
        color = Color.White,
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
    }
}

@Preview
@Composable
private fun EventListScreenPreview() {
    val previewEvents = listOf(
        EventListItem(
            id = "preview-1",
            title = "Fashion Model Event",
            bannerUrl = "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fdivo_event_1.png?alt=media",
            hostHandle = "@nyfw",
            hostAvatarUrl = "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fdivo_avatar_nyfw.png?alt=media",
            dateLocationText = "May 27 · 5:00 PM · 🇺🇸 New York",
            durationText = "4d : 4h : 0m",
            ctaType = EventCtaType.MyEvent,
        ),
        EventListItem(
            id = "preview-2",
            title = "Fashion Model Event",
            bannerUrl = "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fdivo_event_2.png?alt=media",
            hostHandle = "@nyfw",
            hostAvatarUrl = "https://firebasestorage.googleapis.com/v0/b/kitcolorspro.appspot.com/o/test_davit%2Fdivo_avatar_nyfw.png?alt=media",
            dateLocationText = "May 27 · 5:00 PM · 🇺🇸 New York",
            durationText = "4d : 4h : 0m",
            ctaType = EventCtaType.Apply,
        ),
    )

    EventListContent(
        state = EventListViewState(events = previewEvents),
        onEventClick = {},
        onCtaClick = {},
        onSearchClick = {},
        onAddEventClick = {},
    )
}