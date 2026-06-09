package org.telegram.divo.screen.apply_confirmation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coremedia.iso.boxes.Box
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.SnackbarEvent
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.utils.toAge
import org.telegram.divo.common.utils.toEventDisplayDate
import org.telegram.divo.components.*
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.entity.EventDetails
import org.telegram.divo.entity.UserInfo
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun ApplyConfirmationScreen(
    eventId: Int,
    viewModel: ApplyConfirmationViewModel = viewModel(
        factory = ApplyConfirmationViewModel.factory(eventId)
    ),
    onBack: () -> Unit,
    onSuccessDismiss: () -> Unit
) {
    val uiState by viewModel.state.collectAsState()
    val snackbarState = remember { AppSnackbarHostState() }

    //StatusBarIconColorEffect(useDarkIcons = false)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { action ->
            when (action) {
                ApplyConfirmationEffect.Back -> onBack()
                ApplyConfirmationEffect.FinishWithSuccess -> onSuccessDismiss()
                is ApplyConfirmationEffect.ShowError -> {
                    snackbarState.show(SnackbarEvent.Error(action.message))
                }
            }
        }
    }

    if (uiState.isSuccess) {
        ApplySuccessView(onDismiss = { viewModel.setIntent(ApplyConfirmationIntent.OnSuccessDismiss) })
    } else {
        val lazyListState = rememberLazyListState()
        val density = LocalDensity.current
        val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val toolbarHeight = statusBarHeight + 56.dp
        val toolbarHeightPx = with(density) { toolbarHeight.toPx() }
        val fadeRangePx = with(density) { 40.dp.toPx() }

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

        Box(modifier = Modifier.fillMaxSize().background(AppTheme.colors.backgroundLight)) {
            TransparentToolBarBackground(
                modifier = Modifier.zIndex(2f),
                transitionProgress = transitionProgress,
                hazeState = hazeState
            )

            TransparentToolBarContent(
                modifier = Modifier.zIndex(3f),
                onNavigateBack = { viewModel.setIntent(ApplyConfirmationIntent.OnBackClick) },
                transitionProgress = transitionProgress,
                isSolid = isSolid,
                alwaysShowTitle = true,
                titleContent = {
                    Text(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        text = stringResource(R.string.SubmitApplicationTitle).uppercase(),
                        style = AppTheme.typography.helveticaNeueLtCom,
                        fontSize = 20.sp,
                        lineHeight = 20.sp,
                        color = androidx.compose.ui.graphics.lerp(Color.White, AppTheme.colors.textPrimary, transitionProgress),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )

            Box(modifier = Modifier.fillMaxSize().hazeSource(hazeState)) {
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LottieProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                } else {
                    uiState.eventDetails?.let { event ->
                        val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                        val imageHeight = 260.dp + topPadding

                        TelegramPhotoBackground(
                            photo = event.files.firstOrNull()?.fullUrl,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(imageHeight)
                                .align(Alignment.TopCenter),
                            isBlurSupported = false
                        )

                        // 2. Scrollable Content
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 76.dp)
                        ) {
                            item(key = "header") {
                                Spacer(modifier = Modifier.height(imageHeight - 32.dp))
                            }

                        item {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                // Content Container
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 32.dp)
                                        .background(
                                            AppTheme.colors.backgroundLight,
                                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                                        )
                                        .padding(bottom = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Spacer(modifier = Modifier.height(60.dp)) // Space for the logo

                                    Text(
                                        text = event.title.orEmpty(),
                                        style = AppTheme.typography.displayLarge,
                                        color = AppTheme.colors.textPrimary,
                                        maxLines = 3,
                                        textAlign = TextAlign.Center,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (!event.type.isNullOrEmpty()) {
                                            DivoChip(
                                                modifier = Modifier.height(24.dp).weight(1f, fill = false),
                                                text = event.type,
                                                background = AppTheme.colors.accentOrange,
                                                textColor = Color.White,
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                        var subtitle = event.date?.toEventDisplayDate(
                                            countryCode = event.address?.countryCode,
                                            city = event.address?.cityName
                                        ).orEmpty()
                                        if (!event.cost.isNullOrEmpty() && (event.cost.toDoubleOrNull() ?: 0.0) > 0.0) {
                                            val formattedCost = event.cost.toDoubleOrNull()?.toInt()?.toString() ?: event.cost
                                            subtitle += " · $$formattedCost"
                                        }
                                        Text(
                                            text = subtitle,
                                            style = AppTheme.typography.helveticaNeueRegular,
                                            fontSize = 14.sp,
                                            color = AppTheme.colors.textPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(32.dp))
                                    Text(
                                        text = stringResource(R.string.ApplyConfirmationShareInfo),
                                        style = AppTheme.typography.helveticaNeueRegular,
                                        fontSize = 16.sp,
                                        color = AppTheme.colors.textPrimary.copy(0.8f),
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                        textAlign = TextAlign.Start
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    UserInfoCard(userInfo = uiState.userInfo)

                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(
                                        text = stringResource(R.string.ApplyConfirmationParametersTitle),
                                        style = AppTheme.typography.helveticaNeueRegular,
                                        fontSize = 16.sp,
                                        color = AppTheme.colors.textPrimary.copy(0.8f),
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                        textAlign = TextAlign.Start
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    ParametersCard(event = event, userInfo = uiState.userInfo)
                                }

                                // Overlapping Logo
                                DivoAsyncImage(
                                    model = event.creator?.photo?.fullUrl,
                                    modifier = Modifier
                                        .size(68.dp)
                                        .align(Alignment.TopCenter)
                                        .clip(CircleShape)
                                        .background(AppTheme.colors.backgroundLight, CircleShape), // White border if desired, or just clip
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    // 3. Fixed Bottom Bar
                    if (!uiState.isLoading && !uiState.isSuccess) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            AppTheme.colors.backgroundLight.copy(alpha = 0.8f),
                                            AppTheme.colors.backgroundLight
                                        )
                                    )
                                )
                        ) {
                            ApplyBottomBar(
                                isSubmitting = uiState.isSubmitting,
                                onCancel = { viewModel.setIntent(ApplyConfirmationIntent.OnBackClick) },
                                onSubmit = { viewModel.setIntent(ApplyConfirmationIntent.OnSubmitClick) }
                            )
                        }
                    }
                }
            }
            }
            
            // Snackbar
            AppSnackbarHost(
                state = snackbarState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
            )
        }
    }
}


@Composable
private fun UserInfoCard(userInfo: UserInfo?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DivoAsyncImage(
                model = userInfo?.avatarUrl,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = userInfo?.fullName.orEmpty(),
                        style = AppTheme.typography.helveticaNeueRegular,
                        fontSize = 16.sp,
                        color = AppTheme.colors.textPrimary
                    )
                    if (userInfo?.isVerified == true) {
                        Spacer(Modifier.width(4.dp))
                        Image(
                            painter = painterResource(R.drawable.ic_divo_verified),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DivoChip(
                        modifier = Modifier.height(20.dp),
                        text = userInfo?.roleLabel.orEmpty(),
                        textColor = Color.White,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.FollowersOnlineText, userInfo?.statistic?.followersCount ?: 0),
                        style = AppTheme.typography.helveticaNeueRegular,
                        fontSize = 14.sp,
                        color = AppTheme.colors.textPrimary.copy(0.6f)
                    )
                }
            }
        }
        Text(
            text = stringResource(R.string.PortfolioLinkTitle),
            style = AppTheme.typography.helveticaNeueLtCom,
            fontSize = 10.sp,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp, end = 16.dp)
                .clickableWithoutRipple { /* TODO */ }
        )
    }
}

private data class ParamRowData(val label: String, val value: String, val isMatch: Boolean, val mismatchText: String? = null)

@Composable
private fun formatRange(from: Int?, to: Int?, unit: String? = null): String {
    return if (unit != null) {
        if (from != null && to != null) stringResource(R.string.ParamValueRange, from, to, unit)
        else if (from != null) stringResource(R.string.ParamValueFrom, from, unit)
        else if (to != null) stringResource(R.string.ParamValueTo, to, unit)
        else ""
    } else {
        if (from != null && to != null) stringResource(R.string.ParamValueRangeNoUnit, from, to)
        else if (from != null) stringResource(R.string.ParamValueFromNoUnit, from)
        else if (to != null) stringResource(R.string.ParamValueToNoUnit, to)
        else ""
    }
}

@Composable
private fun ParametersCard(event: EventDetails, userInfo: UserInfo?) {
    val attrs = event.modelAttributes ?: return
    val appearance = userInfo?.model?.appearance
    val isImperial = org.telegram.divo.common.MeasuringUnits.isImperial()

    val rows = mutableListOf<ParamRowData>()

    if (attrs.roles.isNotEmpty()) {
        val userRole = userInfo?.roleLabel
        val reqRoleStr = attrs.roles.joinToString(", ") { it.replaceFirstChar { char -> char.uppercase() } }
        val isMatch = userRole != null && attrs.roles.any { it.equals(userRole, ignoreCase = true) }
        val mismatchText = if (isMatch) null else stringResource(R.string.MismatchRole)
        rows.add(ParamRowData(stringResource(R.string.ParamRole), reqRoleStr, isMatch, mismatchText))
    }

    if (attrs.genders.isNotEmpty()) {
        val userGender = userInfo?.gender?.title
        val reqGenderStr = attrs.genders.joinToString(", ") { it.replaceFirstChar { char -> char.uppercase() } }
        val isMatch = userGender != null && attrs.genders.any { it.equals(userGender, ignoreCase = true) }
        val mismatchText = if (isMatch) null else stringResource(R.string.MismatchGender)
        rows.add(ParamRowData(stringResource(R.string.ParamGender), reqGenderStr, isMatch, mismatchText))
    }

    if (attrs.ageFrom != null || attrs.ageTo != null) {
        val userAge = userInfo?.birthday?.toAge()
        val reqAgeStr = formatRange(attrs.ageFrom, attrs.ageTo, "y.o")
        val isMatch = userAge != null && 
            (attrs.ageFrom == null || userAge >= attrs.ageFrom) && 
            (attrs.ageTo == null || userAge <= attrs.ageTo)
        val mismatchText = if (isMatch) null else stringResource(R.string.MismatchAge)
        rows.add(ParamRowData(stringResource(R.string.ParamAge), reqAgeStr, isMatch, mismatchText))
    }

    if (attrs.heightFrom != null || attrs.heightTo != null) {
        val userHeight = appearance?.height
        val displayFrom = attrs.heightFrom?.let { if (isImperial) org.telegram.divo.common.MeasuringUnits.toDisplayValue(ParametersType.HEIGHT, it) else it }
        val displayTo = attrs.heightTo?.let { if (isImperial) org.telegram.divo.common.MeasuringUnits.toDisplayValue(ParametersType.HEIGHT, it) else it }
        val reqHeightStr = formatRange(displayFrom, displayTo, if (isImperial) "in" else "cm")
        val isMatch = userHeight != null && 
            (attrs.heightFrom == null || userHeight >= attrs.heightFrom) && 
            (attrs.heightTo == null || userHeight <= attrs.heightTo)
        val mismatchText = if (isMatch) null else stringResource(R.string.MismatchHeight)
        rows.add(ParamRowData(stringResource(R.string.ParamHeight), reqHeightStr, isMatch, mismatchText))
    }

    if (attrs.weightFrom != null || attrs.weightTo != null) {
        val userWeight = appearance?.weight
        val displayFrom = attrs.weightFrom?.let { if (isImperial) org.telegram.divo.common.MeasuringUnits.toDisplayValue(ParametersType.WEIGHT, it) else it }
        val displayTo = attrs.weightTo?.let { if (isImperial) org.telegram.divo.common.MeasuringUnits.toDisplayValue(ParametersType.WEIGHT, it) else it }
        val reqWeightStr = formatRange(displayFrom, displayTo, if (isImperial) "lb" else "kg")
        val isMatch = userWeight != null && 
            (attrs.weightFrom == null || userWeight >= attrs.weightFrom) && 
            (attrs.weightTo == null || userWeight <= attrs.weightTo)
        val mismatchText = if (isMatch) null else stringResource(R.string.MismatchWeight)
        rows.add(ParamRowData(stringResource(R.string.ParamWeight), reqWeightStr, isMatch, mismatchText))
    }

    if (attrs.breastSizeFrom != null || attrs.breastSizeTo != null) {
        val userBreastSize = appearance?.breastSize?.toFloatOrNull()
        val displayFrom = attrs.breastSizeFrom?.let { if (isImperial) org.telegram.divo.common.MeasuringUnits.toDisplayValue(ParametersType.BREAST_SIZE, it) else it }
        val displayTo = attrs.breastSizeTo?.let { if (isImperial) org.telegram.divo.common.MeasuringUnits.toDisplayValue(ParametersType.BREAST_SIZE, it) else it }
        val reqBreastSizeStr = formatRange(displayFrom, displayTo, if (isImperial) "in" else "cm")
        val isMatch = userBreastSize != null && 
            (attrs.breastSizeFrom == null || userBreastSize >= attrs.breastSizeFrom) && 
            (attrs.breastSizeTo == null || userBreastSize <= attrs.breastSizeTo)
        val mismatchText = if (isMatch) null else stringResource(R.string.MismatchBreastSize)
        rows.add(ParamRowData(stringResource(R.string.ParamBreastSize), reqBreastSizeStr, isMatch, mismatchText))
    }

    if (attrs.waistFrom != null || attrs.waistTo != null) {
        val userWaist = appearance?.waist
        val displayFrom = attrs.waistFrom?.let { if (isImperial) org.telegram.divo.common.MeasuringUnits.toDisplayValue(ParametersType.WAIST, it) else it }
        val displayTo = attrs.waistTo?.let { if (isImperial) org.telegram.divo.common.MeasuringUnits.toDisplayValue(ParametersType.WAIST, it) else it }
        val reqWaistStr = formatRange(displayFrom, displayTo, if (isImperial) "in" else "cm")
        val isMatch = userWaist != null && 
            (attrs.waistFrom == null || userWaist >= attrs.waistFrom) && 
            (attrs.waistTo == null || userWaist <= attrs.waistTo)
        val mismatchText = if (isMatch) null else stringResource(R.string.MismatchWaist)
        rows.add(ParamRowData(stringResource(R.string.ParamWaist), reqWaistStr, isMatch, mismatchText))
    }

    if (attrs.hipsFrom != null || attrs.hipsTo != null) {
        val userHips = appearance?.hips
        val displayFrom = attrs.hipsFrom?.let { if (isImperial) org.telegram.divo.common.MeasuringUnits.toDisplayValue(ParametersType.HIPS, it) else it }
        val displayTo = attrs.hipsTo?.let { if (isImperial) org.telegram.divo.common.MeasuringUnits.toDisplayValue(ParametersType.HIPS, it) else it }
        val reqHipsStr = formatRange(displayFrom, displayTo, if (isImperial) "in" else "cm")
        val isMatch = userHips != null && 
            (attrs.hipsFrom == null || userHips >= attrs.hipsFrom) && 
            (attrs.hipsTo == null || userHips <= attrs.hipsTo)
        val mismatchText = if (isMatch) null else stringResource(R.string.MismatchHips)
        rows.add(ParamRowData(stringResource(R.string.ParamHips), reqHipsStr, isMatch, mismatchText))
    }

    if (attrs.shoesSizeFrom != null || attrs.shoesSizeTo != null) {
        val userShoesSize = appearance?.shoesSize
        val displayFrom = attrs.shoesSizeFrom?.let { if (isImperial) org.telegram.divo.common.MeasuringUnits.toDisplayValue(ParametersType.SHOE_SIZE, it) else it }
        val displayTo = attrs.shoesSizeTo?.let { if (isImperial) org.telegram.divo.common.MeasuringUnits.toDisplayValue(ParametersType.SHOE_SIZE, it) else it }
        val reqShoesSizeStr = formatRange(displayFrom, displayTo, if (isImperial) "US" else "EU")
        val isMatch = userShoesSize != null && 
            (attrs.shoesSizeFrom == null || userShoesSize >= attrs.shoesSizeFrom) && 
            (attrs.shoesSizeTo == null || userShoesSize <= attrs.shoesSizeTo)
        val mismatchText = if (isMatch) null else stringResource(R.string.MismatchShoesSize)
        rows.add(ParamRowData(stringResource(R.string.ParamShoesSize), reqShoesSizeStr, isMatch, mismatchText))
    }

    if (attrs.hairColors.isNotEmpty()) {
        val userHair = appearance?.hairColor?.title
        val reqHairStr = attrs.hairColors.joinToString(", ") { it.replaceFirstChar { char -> char.uppercase() } }
        val isMatch = userHair != null && attrs.hairColors.any { it.equals(userHair, ignoreCase = true) }
        val mismatchText = if (isMatch) null else stringResource(R.string.MismatchHairColor)
        rows.add(ParamRowData(stringResource(R.string.ParamHairColor), reqHairStr, isMatch, mismatchText))
    }

    if (attrs.hairLengths.isNotEmpty()) {
        val userHairLength = appearance?.hairLength?.title
        val reqHairLengthStr = attrs.hairLengths.joinToString(", ") { it.replaceFirstChar { char -> char.uppercase() } }
        val isMatch = userHairLength != null && attrs.hairLengths.any { it.equals(userHairLength, ignoreCase = true) }
        val mismatchText = if (isMatch) null else stringResource(R.string.MismatchHairLength)
        rows.add(ParamRowData(stringResource(R.string.ParamHairLength), reqHairLengthStr, isMatch, mismatchText))
    }

    if (attrs.eyeColors.isNotEmpty()) {
        val userEyeColor = appearance?.eyeColor?.title
        val reqEyeColorStr = attrs.eyeColors.joinToString(", ") { it.replaceFirstChar { char -> char.uppercase() } }
        val isMatch = userEyeColor != null && attrs.eyeColors.any { it.equals(userEyeColor, ignoreCase = true) }
        val mismatchText = if (isMatch) null else stringResource(R.string.MismatchEyeColor)
        rows.add(ParamRowData(stringResource(R.string.ParamEyeColor), reqEyeColorStr, isMatch, mismatchText))
    }

    if (attrs.skinColors.isNotEmpty()) {
        val userSkinColor = appearance?.skinColor?.title
        val reqSkinColorStr = attrs.skinColors.joinToString(", ") { it.replaceFirstChar { char -> char.uppercase() } }
        val isMatch = userSkinColor != null && attrs.skinColors.any { it.equals(userSkinColor, ignoreCase = true) }
        val mismatchText = if (isMatch) null else stringResource(R.string.MismatchSkinColor)
        rows.add(ParamRowData(stringResource(R.string.ParamSkinColor), reqSkinColorStr, isMatch, mismatchText))
    }

    if (rows.isEmpty()) return

    val hasMismatch = rows.any { !it.isMatch }
    val mismatchMessages = rows.mapNotNull { it.mismatchText }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(vertical = 8.dp)
    ) {
            rows.forEachIndexed { index, row ->
                ParamRow(row.label, row.value, row.isMatch)
                if (index < rows.size - 1) {
                    HorizontalDivider(color = AppTheme.colors.textPrimary.copy(alpha = 0.2f), thickness = 0.5.dp)
                }
            }
            
            if (hasMismatch) {
                val warningText = if (mismatchMessages.size == 1) {
                    "${mismatchMessages.first()} ${stringResource(R.string.ApplyAnywaySuffix)}"
                } else {
                    stringResource(R.string.MismatchMultipleApplyAnyway)
                }

                Text(
                    text = warningText,
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 12.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                )
            }
        }
}

@Composable
private fun ParamRow(label: String, value: String, isMatch: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(0.3f),
            text = label,
            style = AppTheme.typography.helveticaNeueRegular,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = AppTheme.colors.textPrimary.copy(0.6f)
        )
        Row(
            modifier = Modifier.weight(0.7f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                modifier = Modifier.weight(1f, fill = false),
                text = value,
                textAlign = TextAlign.End,
                style = AppTheme.typography.helveticaNeueRegular,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                color = AppTheme.colors.textPrimary.copy(0.6f)
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                painter = painterResource(if (isMatch) R.drawable.divo_check_ic else R.drawable.ic_divo_report), // using report as alert mock
                contentDescription = null,
                tint = if (isMatch) Color(0xFF4CAF50) else Color.Red,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun ApplyBottomBar(
    isSubmitting: Boolean,
    onCancel: () -> Unit,
    onSubmit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        UIButtonNew(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.Cancel),
            background = AppTheme.colors.buttonSecondary,
            onClick = onCancel,
            enabled = !isSubmitting
        )
        UIButtonNew(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.SubmitButton),
            background = AppTheme.colors.accentOrange,
            onClick = onSubmit,
            isLoading = isSubmitting,
            enabled = !isSubmitting
        )
    }
}

@Composable
private fun ApplySuccessView(
    onDismiss: () -> Unit
) {
    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.backgroundLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = topPadding + 16.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundedButton(
                resId = R.drawable.ic_divo_close_20,
                iconSize = 24.dp,
                onClick = onDismiss
            )
            Spacer(Modifier.weight(1f))
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(R.string.SubmitApplicationTitle),
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 20.sp,
                color = AppTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.weight(1f))
            Spacer(modifier = Modifier.size(36.dp)) 
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.divo_check_ic),
                    contentDescription = null,
                    tint = AppTheme.colors.textPrimary,
                    modifier = Modifier.size(24.dp).offset(x = 1.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.ApplySuccessTitle),
                style = AppTheme.typography.displayLarge,
                fontSize = 26.sp,
                color = AppTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.ApplySuccessDescription),
                style = AppTheme.typography.helveticaNeueRegular,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}
