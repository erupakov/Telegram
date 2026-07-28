package org.telegram.divo.screen.event_details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.telegram.divo.common.controllers.AppSnackbarHost
import org.telegram.divo.common.controllers.AppSnackbarHostState
import org.telegram.divo.common.controllers.SnackbarEvent
import org.telegram.divo.common.compose.clickableWithoutRipple
import org.telegram.divo.common.utils.DivoShareType
import org.telegram.divo.common.utils.DivoSharingHelper
import org.telegram.divo.common.utils.toEventShortDate
import org.telegram.divo.components.navigation.DivoPopupMenu
import org.telegram.divo.components.media.LottieProgressIndicator
import org.telegram.divo.components.navigation.PopupMenuItem
import org.telegram.divo.components.inputs.RoundedGlassButton
import org.telegram.divo.components.inputs.RoundedGlassContainer
import org.telegram.divo.common.compose.StatusBarIconColorEffect
import org.telegram.divo.components.bottomsheets.DivoWithdrawBottomSheet
import org.telegram.divo.components.navigation.TransparentToolBarBackground
import org.telegram.divo.components.navigation.TransparentToolBarContent
import org.telegram.divo.screen.event_details.components.AboutCard
import org.telegram.divo.screen.event_details.components.CancelEventConfirmationDialog
import org.telegram.divo.screen.event_details.components.CapacityCard
import org.telegram.divo.screen.event_details.components.CloseApplicationsConfirmationDialog
import org.telegram.divo.screen.event_details.components.DeleteEventConfirmationDialog
import org.telegram.divo.screen.event_details.components.EventDetailsHeader
import org.telegram.divo.screen.event_details.components.OrganizerCard
import org.telegram.divo.screen.event_details.components.ParametersCard
import org.telegram.divo.screen.event_details.components.PreviousEvents
import org.telegram.divo.screen.event_details.components.RequirementsCard
import org.telegram.divo.screen.event_details.components.ThumbnailRow
import org.telegram.divo.screen.event_details.components.isEventClosed
import org.telegram.divo.screen.gallery.GalleryItem
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun EventDetailsScreen(
    eventId: Int,
    isOwnProfile: Boolean,
    viewModel: EventDetailsViewModel,
    onPhotoClicked: (List<GalleryItem>, Int) -> Unit,
    onParamsClicked: () -> Unit,
    onEditEvent: (Int) -> Unit,
    onPrevEventClicked: (Int) -> Unit,
    onApplyConfirmation: (Int) -> Unit,
    onEventDeleted: () -> Unit,
    onBack: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState().value
    val context = LocalContext.current
    val snackbarState = remember { AppSnackbarHostState() }
    val retryText = stringResource(R.string.RetryLabel)
    var isSolid by remember { mutableStateOf(false) }
    var withdrawEventId by remember { mutableStateOf<Int?>(null) }

    StatusBarIconColorEffect(isSolid)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { action ->
            when (action) {
                EventDetailsEffect.Back -> onBack()
                EventDetailsEffect.EventDeleted -> onEventDeleted()
                is EventDetailsEffect.ShowError -> {
                    if (action.canRetry) {
                        snackbarState.show(
                            SnackbarEvent.ErrorWithRetry(action.message, retryText) {
                                viewModel.setIntent(EventDetailsIntent.OnLoad)
                            }
                        )
                    } else {
                        snackbarState.show(
                            SnackbarEvent.Error(action.message)
                        )
                    }
                }
                is EventDetailsEffect.NavigateToGallery -> { onPhotoClicked(action.items, action.id) }
                EventDetailsEffect.NavigateToParams -> { onParamsClicked() }
                is EventDetailsEffect.NavigateToEditEvent -> onEditEvent(action.eventId)
                is EventDetailsEffect.NavigateToApplyConfirmation -> onApplyConfirmation(action.eventId)
                is EventDetailsEffect.ShowWithdrawConfirmation -> { withdrawEventId = action.eventId }
                is EventDetailsEffect.NavigateToPrevEvent -> { onPrevEventClicked(action.id) }
                EventDetailsEffect.ApplicationsClosed -> {
                    snackbarState.show(
                        SnackbarEvent.Success("Applications closed successfully")
                    )
                }
                is EventDetailsEffect.ActionChanged -> {
                    snackbarState.show(
                        SnackbarEvent.SuccessWithIcon(
                            action.resDrawableId,
                            context.getString(action.resStringId)
                        )
                    )
                }
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
            onIntent = { viewModel.setIntent(it) },
            onSolidChanged = { isSolid = it }
        )
    }

    if (withdrawEventId != null) {
        DivoWithdrawBottomSheet(
            onKeepApplication = { withdrawEventId = null },
            onWithdraw = {
                val id = withdrawEventId
                withdrawEventId = null
                if (id != null) viewModel.setIntent(EventDetailsIntent.ConfirmWithdraw(id))
            },
            onDismiss = { withdrawEventId = null }
        )
    }
}

@Composable
private fun EventDetailsContent(
    uiState: EventDetailsViewState,
    snackbarHostState: AppSnackbarHostState,
    onIntent: (EventDetailsIntent) -> Unit = {},
    onSolidChanged: (Boolean) -> Unit = {},
) {
    val lazyListState = rememberLazyListState()
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val toolbarHeight = statusBarHeight + 56.dp
    val toolbarHeightPx = with(density) { toolbarHeight.toPx() }
    val fadeRangePx = with(density) { 1.dp.toPx() }
    val engagementsFadeRangePx = with(density) { 20.dp.toPx() }

    val engagementsAlpha by remember {
        derivedStateOf {
            if (lazyListState.firstVisibleItemIndex > 0) {
                0f
            } else {
                val scrollOffset = lazyListState.firstVisibleItemScrollOffset.toFloat()
                (1f - scrollOffset / engagementsFadeRangePx).coerceIn(0f, 1f)
            }
        }
    }

    val transitionProgress by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            if (layoutInfo.visibleItemsInfo.isEmpty()) return@derivedStateOf 0f

            val headerItem = layoutInfo.visibleItemsInfo.find { it.key == "header" }
            if (headerItem == null) {
                1f
            } else {
                val headerBottom = headerItem.offset + headerItem.size
                val distance = headerBottom - toolbarHeightPx
                when {
                    distance <= 0f -> 1f
                    distance >= fadeRangePx -> 0f
                    else -> 1f - (distance / fadeRangePx)
                }
            }
        }
    }

    val isSolid by remember { derivedStateOf { transitionProgress >= 1f } }
    val hazeState = remember { HazeState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCloseAppsDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        DeleteEventConfirmationDialog(
            eventName = uiState.eventDetails?.title.orEmpty(),
            onDismissRequest = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onIntent(EventDetailsIntent.OnDeleteEventConfirmed)
            }
        )
    }

    if (showCloseAppsDialog) {
        CloseApplicationsConfirmationDialog(
            eventName = uiState.eventDetails?.title.orEmpty(),
            onDismissRequest = { showCloseAppsDialog = false },
            onConfirm = {
                showCloseAppsDialog = false
                onIntent(EventDetailsIntent.OnCloseApplicationsConfirmed)
            }
        )
    }

    if (showCancelDialog) {
        CancelEventConfirmationDialog(
            eventName = uiState.eventDetails?.title.orEmpty(),
            onDismissRequest = { showCancelDialog = false },
            onConfirm = {
                showCancelDialog = false
                onIntent(EventDetailsIntent.OnCancelEventConfirmed)
            }
        )
    }

    LaunchedEffect(isSolid) {
        onSolidChanged(isSolid)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TransparentToolBarBackground(
            modifier = Modifier.zIndex(2f),
            transitionProgress = transitionProgress,
            hazeState = hazeState
        )

        TransparentToolBarContent(
            modifier = Modifier.zIndex(3f),
            onNavigateBack = { onIntent(EventDetailsIntent.OnBackClicked) },
            transitionProgress = transitionProgress,
            isSolid = isSolid,
            titleContent = {
                Text(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    text = uiState.eventDetails?.title.orEmpty(),
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 12.sp,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            actionsContent = { _, buttonBgColor, iconColor, buttonBorderColor ->
                if (uiState.isOwnEvent) {
                    RoundedGlassContainer(
                        background = buttonBgColor,
                        borderColor = buttonBorderColor,
                        contentPadding = PaddingValues(start = 10.dp, end = 10.dp),
                        space = 10.dp
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(24.dp)
                                .clickableWithoutRipple {
                                    DivoSharingHelper.share(
                                        context = context,
                                        scope = scope,
                                        type = DivoShareType.EVENT,
                                        id = uiState.eventDetails?.id,
                                        customMessage = "${uiState.eventDetails?.title} - ${uiState.eventDetails?.creator?.roleLabel}",
                                        imageUrl = uiState.eventDetails?.creator?.photo?.fullUrl
                                    )
                                },
                            painter = painterResource(R.drawable.ic_divo_share_model),
                            contentDescription = null,
                            tint = iconColor
                        )
                        Icon(
                            modifier = Modifier
                                .size(24.dp)
                                .clickableWithoutRipple { 
                                    showMenu = true 
                                    onIntent(EventDetailsIntent.OnMenuClicked)
                                },
                            painter = painterResource(R.drawable.ic_ab_other),
                            contentDescription = null,
                            tint = iconColor
                        )
                    }
                } else {
                    RoundedGlassButton(
                        modifier = Modifier.align(Alignment.CenterStart),
                        background = buttonBgColor,
                        resId = R.drawable.ic_divo_share_model,
                        iconTint = iconColor,
                        iconSize = 22.dp,
                        borderColor = buttonBorderColor,
                        onClick = {
                            DivoSharingHelper.share(
                                context = context,
                                scope = scope,
                                type = DivoShareType.EVENT,
                                id = uiState.eventDetails?.id,
                                customMessage = "${uiState.eventDetails?.title} - ${uiState.eventDetails?.creator?.roleLabel}",
                                imageUrl = uiState.eventDetails?.creator?.photo?.fullUrl
                            )
                        }
                    )
                }
            }
        )

        val options = mutableListOf<PopupMenuItem>()
        options.add(PopupMenuItem(R.string.EditEvent, { onIntent(EventDetailsIntent.OnEditEventClick) }, R.drawable.ic_divo_edit_20))
        
        val isClosed = isEventClosed(
            dateFrom = uiState.eventDetails?.date.orEmpty(),
            dateTo = uiState.eventDetails?.dateTo.orEmpty(),
            applicationDeadline = uiState.eventDetails?.applicationDeadline
        )
        
        if (!isClosed) {
            options.add(PopupMenuItem(R.string.CloseApplicationsEvent, { showCloseAppsDialog = true; showMenu = false }, R.drawable.ic_divo_close_20))
        }
        options.add(PopupMenuItem(R.string.CancelEvent, { showCancelDialog = true; showMenu = false }, R.drawable.ic_divo_report))
        options.add(PopupMenuItem(R.string.DeleteEvent, { showDeleteDialog = true; showMenu = false }, R.drawable.ic_divo_cart))

        DivoPopupMenu(
            visible = showMenu,
            onDismiss = { showMenu = false },
            items = options,
            offset = IntOffset(x = -32, y = 0)
        )

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState),
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
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 28.dp)
            ) {
                item(key = "header") {
                    EventDetailsHeader(
                        event = uiState.eventDetails,
                        isModel = uiState.isModel,
                        isOwnEvent = uiState.isOwnEvent,
                        engagementsAlpha = engagementsAlpha,
                        onEditEvent = { onIntent(EventDetailsIntent.OnEditEventClick) },
                        onCtaClicked = { uiState.eventDetails?.id?.let { onIntent(EventDetailsIntent.OnEventCtaClicked(it)) } },
                        onLikeClicked = { onIntent(EventDetailsIntent.OnLikeClicked) },
                        onFavouriteClicked = { onIntent(EventDetailsIntent.OnFavouriteClicked) },
                    )
                }

                if (uiState.eventDetails?.isApplied == true && !uiState.isOwnEvent) {
                    item(key = "applied_banner") {
                        Spacer(Modifier.height(16.dp))
                        AppliedBanner(
                            appliedDateString = uiState.eventDetails.appliedDate ?: uiState.eventDetails.date,
                            onWithdrawClick = { uiState.eventDetails.id.let { onIntent(EventDetailsIntent.OnEventCtaClicked(it)) } }
                        )
                    }
                } else {
                    item(key = "empty_item") {  }
                }

                item(key = "capacity") {
                    Spacer(Modifier.height(20.dp))
                    CapacityCard(
                        appliedCount = uiState.eventDetails?.appliesCount,
                        maxSpotsCount = 100
                    )
                }

                item(key = "organizer") {
                    Spacer(Modifier.height(16.dp))
                    OrganizerCard(
                        avatarModel = uiState.eventDetails?.creator?.avatar?.fullUrl ?: uiState.eventDetails?.creator?.photo?.fullUrl,
                        name =  uiState.eventDetails?.creator?.fullName.orEmpty(),
                        status = "",
                        isVerified = uiState.eventDetails?.creator?.isVerified == true
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

                item(key = "requirements") {
                    Spacer(Modifier.height(10.dp))
                    RequirementsCard("Bring your portfolio")
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

                uiState.eventDetails?.files
                    ?.drop(1)
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { eventFiles ->
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
}

@Composable
private fun AppliedBanner(
    appliedDateString: String?,
    onWithdrawClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                width = 1.dp,
                color = AppTheme.colors.accentOrange,
                shape = RoundedCornerShape(16.dp)
            )
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (appliedDateString.isNullOrEmpty()) {
                    stringResource(R.string.YouApplied)
                } else {
                    stringResource(R.string.YouAppliedOn, appliedDateString.toEventShortDate())
                },
                style = AppTheme.typography.helveticaNeueRegular,
                fontSize = 16.sp,
                color = AppTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                modifier = Modifier.clickableWithoutRipple { onWithdrawClick() },
                text = stringResource(R.string.WithdrawApplicationTextButton),
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 10.sp,
                color = AppTheme.colors.textPrimary
            )
        }
    }
}
