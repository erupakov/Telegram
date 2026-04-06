package org.telegram.divo.screen.event_details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.common.SnackbarEvent
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
import org.telegram.messenger.R

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
    val snackbarState = remember { AppSnackbarHostState() }
    val retryText = stringResource(R.string.RetryLabel)

    StatusBarIconColorEffect(false)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { action ->
            when (action) {
                EventDetailsEffect.Back -> onBack()
                is EventDetailsEffect.ShowError -> {
                    snackbarState.show(
                        SnackbarEvent.ErrorWithRetry(action.message, retryText) {
                            viewModel.setIntent(EventDetailsIntent.OnLoad)
                        }
                    )
                }
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
            snackbarHostState = snackbarState,
            onIntent = { viewModel.setIntent(it) }
        )
    }
}

@Composable
private fun EventDetailsContent(
    uiState: EventDetailsViewState,
    snackbarHostState: AppSnackbarHostState,
    onIntent: (EventDetailsIntent) -> Unit = {},
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        contentWindowInsets = WindowInsets(top = 0),
        containerColor = AppTheme.colors.backgroundLight,
        snackbarHost = {
            AppSnackbarHost(
                state = snackbarHostState,
                bottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 8.dp
            )
        }
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
                    isOwnProfile = uiState.isOwnProfile,
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

            uiState.eventDetails?.previousEventsFromSameOrigin?.let {
                if (it.isNotEmpty()) {
                    item(key = "prevTitle") {
                        Spacer(Modifier.height(20.dp))
                        PreviousEvents(
                            events = it,
                            onClick = { id -> onIntent(EventDetailsIntent.OnPrevEventClicked(id)) }
                        )
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}
