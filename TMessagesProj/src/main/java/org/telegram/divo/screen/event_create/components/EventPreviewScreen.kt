package org.telegram.divo.screen.event_create.components

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import org.telegram.divo.common.controllers.AppSnackbarHost
import org.telegram.divo.common.controllers.AppSnackbarHostState
import org.telegram.divo.components.media.DivoAsyncImage
import org.telegram.divo.common.controllers.SnackbarEvent
import org.telegram.divo.common.utils.toMonthDayFormat
import org.telegram.divo.common.utils.toShortString
import org.telegram.divo.components.inputs.DivoChip
import org.telegram.divo.components.inputs.RoundedGlassButton
import org.telegram.divo.components.inputs.RoundedGlassContainer
import org.telegram.divo.common.compose.StatusBarIconColorEffect
import org.telegram.divo.components.media.TelegramPhotoBackground
import org.telegram.divo.components.navigation.TransparentToolBarBackground
import org.telegram.divo.components.navigation.TransparentToolBarContent
import org.telegram.divo.components.inputs.UIButton
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.common.DivoSettings
import org.telegram.divo.common.utils.numericFilterRange
import org.telegram.divo.common.utils.resolveNumericBlockParamBounds
import org.telegram.divo.entity.EventModelAttributes
import org.telegram.divo.screen.event_create.CreateEventViewModel
import org.telegram.divo.screen.event_create.Effect
import org.telegram.divo.screen.event_create.Intent
import org.telegram.divo.screen.event_create.State
import org.telegram.divo.screen.event_details.components.AboutCard
import org.telegram.divo.screen.event_details.components.CapacityCard
import org.telegram.divo.screen.event_details.components.OrganizerCard
import org.telegram.divo.screen.event_details.components.ParametersCard
import org.telegram.divo.screen.event_details.components.RequirementsCard
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R

@Composable
fun EventPreviewScreen(
    viewModel: CreateEventViewModel = viewModel(),
    onBack: () -> Unit,
    onPublish: () -> Unit = {},
) {
    val state = viewModel.state.collectAsState().value
    val snackbarState = remember { AppSnackbarHostState() }
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

    StatusBarIconColorEffect(useDarkIcons = isSolid)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { action ->
            when (action) {
                is Effect.ShowError -> {
                    snackbarState.show(
                        SnackbarEvent.Error(action.message)
                    )
                }
                is Effect.EventPublished -> {
                    onPublish()
                }
                else -> {}
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TransparentToolBarBackground(
            modifier = Modifier.zIndex(2f),
            transitionProgress = transitionProgress,
            hazeState = hazeState
        )

        TransparentToolBarContent(
            modifier = Modifier.zIndex(3f),
            onNavigateBack = onBack,
            transitionProgress = transitionProgress,
            isSolid = true,
            titleContent = {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    text = stringResource(R.string.EventPreview).uppercase(),
                    style = AppTheme.typography.helveticaNeueLtCom,
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            },
            actionsContent = { _, buttonBgColor, iconColor, buttonBorderColor ->
                RoundedGlassButton(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .align(Alignment.CenterStart),
                    background = buttonBgColor,
                    resId = R.drawable.ic_divo_share_model,
                    iconSize = 22.dp,
                    iconTint = iconColor,
                    borderColor = buttonBorderColor,
                )
            }
        )

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState),
            containerColor = AppTheme.colors.backgroundLight,
            contentWindowInsets = WindowInsets(top = 0),
            snackbarHost = {
                AppSnackbarHost(
                    state = snackbarState,
                    bottomPadding = 72.dp
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .navigationBarsPadding(),
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 74.dp)
                ) {
                    item(key = "header") {
                        PreviewHeader(
                            state = state,
                            engagementsAlpha = engagementsAlpha,
                            onBack = onBack,
                        )
                    }

                    item(key = "empty_item") {  }

                    if (state.maxParticipants > 0) {
                        item(key = "capacity") {
                            Spacer(Modifier.height(20.dp))
                            CapacityCard(
                                appliedCount = 0,
                                maxSpotsCount = state.maxParticipants
                            )
                        }
                    }

                    item(key = "organizer") {
                        Spacer(Modifier.height(16.dp))
                        OrganizerCard(
                            avatarModel = state.currentUser.avatarUrl.ifBlank { state.currentUser.photoUrl.ifBlank { null } },
                            name = state.currentUser.fullName,
                            status = "",
                            isVerified = true
                        )
                    }

                    if (state.eventDescription.isNotBlank()) {
                        item(key = "about") {
                            Spacer(Modifier.height(10.dp))
                            AboutCard(text = state.eventDescription)
                        }
                    }

                    if (state.eventRequirements.isNotBlank()) {
                        item(key = "requirements") {
                            Spacer(Modifier.height(10.dp))
                            RequirementsCard(text = state.eventRequirements)
                        }
                    }

                    item(key = "paramsTitle") {
                        Spacer(Modifier.height(10.dp))
                        ParametersCard(
                            param = state.toEventModelAttributes(),
                            onMoreClicked = {}
                        )
                    }

                    if (state.galleryUris.drop(1).isNotEmpty()) {
                        item(key = "gallery") {
                            Spacer(Modifier.height(20.dp))
                            PreviewThumbnailRow(uris = state.galleryUris.drop(1))
                        }
                    }
                }

                PreviewBottomBar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    isUploading = state.isUploading,
                    onEdit = {
                        viewModel.setIntent(Intent.OnEditFromPreviewClicked)
                        onBack()
                    },
                    onPublish = { viewModel.setIntent(Intent.OnPublishClicked) },
                )
            }
        }
    }
}

private fun State.toEventModelAttributes(): EventModelAttributes {
    fun parseList(raw: String): List<String> =
        raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    fun parseRange(type: ParametersType): Pair<Int?, Int?> {
        val raw = blockParams.find { it.type == type }?.value.orEmpty()
        val measuringSystem = DivoSettings.measuringSystem
        val bounds = type.numericFilterRange(measuringSystem) ?: return null to null
        return resolveNumericBlockParamBounds(raw, bounds, type, measuringSystem)
    }

    val (ageFrom, ageTo) = parseRange(ParametersType.AGE)
    val (heightFrom, heightTo) = parseRange(ParametersType.HEIGHT)
    val (weightFrom, weightTo) = parseRange(ParametersType.WEIGHT)
    val (breastFrom, breastTo) = parseRange(ParametersType.BREAST_SIZE)
    val (waistFrom, waistTo) = parseRange(ParametersType.WAIST)
    val (hipsFrom, hipsTo) = parseRange(ParametersType.HIPS)
    val (shoesFrom, shoesTo) = parseRange(ParametersType.SHOE_SIZE)

    return EventModelAttributes(
        roles = parseList(role.value),
        ageFrom = ageFrom,
        ageTo = ageTo,
        genders = org.telegram.divo.entity.mapGenderToEnglish(gender.value)?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            ?: listOf("male", "female"),
        heightFrom = heightFrom,
        heightTo = heightTo,
        weightFrom = weightFrom,
        weightTo = weightTo,
        breastSizeFrom = breastFrom,
        breastSizeTo = breastTo,
        waistFrom = waistFrom,
        waistTo = waistTo,
        hipsFrom = hipsFrom,
        hipsTo = hipsTo,
        shoesSizeFrom = shoesFrom,
        shoesSizeTo = shoesTo,
        hairColors = parseList(hairColor.value),
        hairLengths = parseList(hairLength.value),
        eyeColors = parseList(eyeColor.value),
        skinColors = parseList(skinColor.value),
        measuringSystem = DivoSettings.measuringSystem
    )
}

@Composable
private fun PreviewHeader(
    state: State,
    engagementsAlpha: Float = 1f,
    onBack: () -> Unit,
) {
    val rawTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var topPadding by remember { mutableStateOf(rawTopPadding) }
    if (rawTopPadding > topPadding) {
        topPadding = rawTopPadding
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
    ) {
        val bgModel: String = state.galleryUris.firstOrNull()?.toString().orEmpty()

        TelegramPhotoBackground(
            photo = bgModel,
            modifier = Modifier.fillMaxSize()
        )

        // Stats section – matches EventDetailsHeader.StatsSection
        Column(
            modifier = Modifier
                .padding(top = topPadding + 16.dp)
                .graphicsLayer { alpha = engagementsAlpha }
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Space for fixed toolbar buttons
            Spacer(Modifier.height(48.dp))

            EngagementItem(
                resId = R.drawable.ic_divo_favorite,
                count = 0
            )
            Spacer(Modifier.height(10.dp))
            EngagementItem(
                resId = R.drawable.ic_divo_visibility,
                count = 0
            )
            Spacer(Modifier.height(10.dp))
            EngagementItem(
                resId = R.drawable.ic_divo_bookmark_glass,
                count = 0
            )
        }

        // Content section – matches EventDetailsHeader.ContentSection
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                state.selectedEventType?.title?.let { typeTitle ->
                    DivoChip(
                        modifier = Modifier.height(27.dp),
                        text = typeTitle,
                        background = getEventTypeColor(state.selectedEventType.id, typeTitle),
                        textColor = Color.White,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                if (state.isPaid) {
                    DivoChip(
                        modifier = Modifier.height(27.dp),
                        text = state.selectedPaymentType?.title ?: stringResource(R.string.EventPaid),
                        resId = R.drawable.ic_divo_paid,
                        background = Color.White.copy(alpha = 0.3f),
                        textColor = Color.White,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Text(
                text = state.eventName.ifBlank { stringResource(R.string.EventName) },
                style = AppTheme.typography.displayLarge,
                color = AppTheme.colors.onBackground,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))

            val dateLabel = buildString {
                if (state.eventDate.isNotBlank()) {
                    append(state.eventDate.toMonthDayFormat())
                    if (state.eventTime.isNotBlank()) {
                        if (isNotEmpty()) append(" · ")
                        append(state.eventTime)
                    }
                }
                if (state.selectedCity != null) {
                    if (isNotEmpty()) append(" · ")
                    val flag = LocaleController.getLanguageFlag(state.selectedCity.countryCode) ?: ""
                    append(flag)
                }
                if (state.isPaid && state.eventRate.isNotBlank()) {
                    if (isNotEmpty()) append(" · ")
                    append("$ ${state.eventRate}")
                }
            }
            if (dateLabel.isNotBlank()) {
                Text(
                    text = dateLabel,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.onBackground
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.deadlineDate.isNotBlank()) {
                        RoundedGlassContainer(
                            height = 30.dp,
                            borderColor = AppTheme.colors.onBackground.copy(alpha = 0.1f),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            val date = state.deadlineDate.toMonthDayFormat()
                            Text(
                                text = stringResource(R.string.ClosesDate, date),
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.colors.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
                RoundedGlassContainer(
                    height = 36.dp,
                    background = Color.White.copy(alpha = 0.8f),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ApplyNowPreview),
                        style = AppTheme.typography.helveticaNeueLtCom,
                        color = Color.Black,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PreviewThumbnailRow(uris: List<Uri>) {
    LazyRow(modifier = Modifier.fillMaxWidth()) {
        itemsIndexed(uris) { _, uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(134.dp)
                    .height(136.dp)
                    .clip(RoundedCornerShape(0.dp))
            )
        }
    }
}

@Composable
private fun PreviewBottomBar(
    modifier: Modifier = Modifier,
    isUploading: Boolean,
    onEdit: () -> Unit,
    onPublish: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 8.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        // Edit button (secondary style)
        UIButton(
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            text = stringResource(R.string.ButtonEdit),
            enabled = !isUploading,
            background = AppTheme.colors.buttonSecondary,
            leadingIcon = R.drawable.ic_divo_edit_24,
            leadingIconTint = AppTheme.colors.onBackground,
            onClick = onEdit
        )

        UIButton(
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            text = stringResource(R.string.EventPublish),
            enabled = !isUploading,
            isLoading = isUploading,
            onClick = onPublish
        )
    }
}

@Composable
private fun EngagementItem(
    @DrawableRes resId: Int,
    count: Int,
    tint: Color = AppTheme.colors.onBackground
) {
    RoundedGlassContainer(
        modifier = Modifier.width(56.dp),
        height = 30.dp,
        space = 4.dp,
        contentPadding = PaddingValues(horizontal = 6.dp)
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(resId),
            contentDescription = null,
            tint = tint
        )
        Text(
            modifier = Modifier.offset(y = 0.5.dp),
            text = count.toShortString(),
            style = AppTheme.typography.helveticaNeueRegular,
            color = AppTheme.colors.onBackground,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun Background(
    modifier: Modifier = Modifier,
    backgroundUrl: String?,
) {
    val hazeState = remember { HazeState() }
    var componentHeight by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { componentHeight = it.height.toFloat() }
    ) {
        DivoAsyncImage(
            modifier = Modifier
                .hazeSource(state = hazeState),
            model = backgroundUrl,
            loadingContent = {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFBF7A54)))
            },
            errorContent = {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFBF7A54)))
            }
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = Color.Black,
                        blurRadius = 30.dp,
                        tints = listOf(HazeTint(Color.Black.copy(alpha = 0.2f)))
                    )
                ) {
                    progressive = HazeProgressive.verticalGradient(
                        startY = componentHeight * 0.65f,
                        startIntensity = 0f,
                        endY = componentHeight * 0.8f,
                        endIntensity = 1f,
                        easing = LinearEasing
                    )
                }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

private fun getEventTypeColor(typeId: Int?, type: String?): Color {
    if (typeId != null) {
        return when (typeId) {
            1 -> Color(0xFF185FA5)
            281 -> Color(0xFF0F6E56)
            else -> generateColorForId(typeId)
        }
    }
    return Color(0xFF185FA5)
}

private fun generateColorForId(id: Int): Color {
    val colors = listOf(
        Color(0xFF185FA5), Color(0xFF534AB7), Color(0xFF0F6E56),
        Color(0xFF888780), Color(0xFFE8520A), Color(0xFFD81B60),
        Color(0xFF8E24AA), Color(0xFF00897B), Color(0xFFF4511E)
    )
    return colors[id % colors.size]
}
