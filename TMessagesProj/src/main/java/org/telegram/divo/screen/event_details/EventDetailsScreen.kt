package org.telegram.divo.screen.event_details

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.StatusBarIconColorEffect
import org.telegram.divo.screen.event_details.components.AboutCard
import org.telegram.divo.screen.event_details.components.EventDetailsHeader
import org.telegram.divo.screen.event_details.components.OrganizerCard
import org.telegram.divo.screen.event_details.components.ParametersCard
import org.telegram.divo.screen.event_details.components.PreviousEvents
import org.telegram.divo.screen.event_details.components.ThumbnailRow
import org.telegram.divo.screen.gallery.GalleryItem
import org.telegram.divo.style.AppTheme

@Composable
fun EventDetailsScreen(
    eventId: Int,
    isOwnProfile: Boolean,
    viewModel: EventDetailsViewModel = viewModel(
        factory = EventDetailsViewModel.factory(eventId, isOwnProfile)
    ),
    onPhotoClicked: (List<GalleryItem>, Int) -> Unit,
    onParamsClicked: () -> Unit,
    onPrevEventClicked: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState().value
    val context = LocalContext.current

    StatusBarIconColorEffect(false)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { action ->
            when (action) {
                EventDetailsEffect.Back -> onBack()
                is EventDetailsEffect.ShowError -> Toast.makeText(context, action.message, Toast.LENGTH_SHORT).show()
                is EventDetailsEffect.NavigateToGallery -> { onPhotoClicked(action.items, action.id) }
                EventDetailsEffect.NavigateToParams -> { onParamsClicked() }
                is EventDetailsEffect.NavigateToPrevEvent -> { onPrevEventClicked(action.id) }
            }
        }
    }

    if (uiState.isLoading || uiState.isRoleLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.backgroundLight),
            contentAlignment = Alignment.Center
        ) {
            LottieProgressIndicator(modifier = Modifier.size(32.dp))
        }
    } else {
        EventDetailsContent(
            uiState = uiState,
            onIntent = { viewModel.setIntent(it) }
        )
    }
}

@Composable
private fun EventDetailsContent(
    uiState: EventDetailsViewState,
    onIntent: (EventDetailsIntent) -> Unit = {},
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        contentWindowInsets = WindowInsets(top = 0),
        containerColor = AppTheme.colors.backgroundLight
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item(key = "header") {
                EventDetailsHeader(
                    event = uiState.eventDetails,
                    isModel = uiState.isModel,
                    onMenuClicked = {},
                    onBack = { onIntent(EventDetailsIntent.OnBackClicked) }
                )
            }

            item(key = "organizer") {
                Spacer(Modifier.height(20.dp))
                OrganizerCard(
                    avatarUrl = uiState.eventDetails?.creator?.avatar?.fullUrl,
                    name =  uiState.eventDetails?.creator?.fullName.orEmpty(),
                    status = "Online",
                )
            }

            uiState.eventDetails?.description?.let {
                item(key = "about") {
                    Spacer(Modifier.height(10.dp))
                    AboutCard(
                        text = it
                    )
                }
            }

            uiState.eventDetails?.modelAttributes?.let {
                item(key = "paramsTitle") {
                    Spacer(Modifier.height(10.dp))
                    ParametersCard(
                        param = it,
                        onMoreClicked = { onIntent(EventDetailsIntent.OnParamsClick) }
                    )
                }
            }

            uiState.eventDetails?.files?.let { eventFiles ->
                item(key = "gallery") {
                    Spacer(Modifier.height(20.dp))
                    ThumbnailRow(
                        files = eventFiles,
                        onClicked = { items, index -> onIntent(EventDetailsIntent.OnPhotoClick(items, index)) }
                    )
                }
            }

            item(key = "prevTitle") {
                if (uiState.events.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    PreviousEvents(
                        events = uiState.events,
                        onClick = { onIntent(EventDetailsIntent.OnPrevEventClicked(it)) }
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
