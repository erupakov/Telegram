package org.telegram.divo.screen.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.utils.toAge
import org.telegram.divo.common.utils.toCountryFlagEmoji
import org.telegram.divo.components.DivoChip
import org.telegram.divo.components.DivoTextField
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.items.DivoBottomSheet
import org.telegram.divo.components.shimmer
import org.telegram.divo.entity.AgencySearchModel
import org.telegram.divo.entity.AgencySearchModelStatus
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyModelsBottomSheet(
    query: String,
    searchModels: List<AgencySearchModel>,
    isLoadingSearch: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    error: String?,
    isAddingModel: Boolean,
    selectedModelForAdd: AgencySearchModel?,
    selectedModelInfo: org.telegram.divo.entity.UserInfo?,
    onLoadMore: () -> Unit,
    onValueChanged: (String) -> Unit,
    onAddClicked: (Int, String?) -> Unit,
    onDismiss: () -> Unit,
    onSelectModelForAdd: (AgencySearchModel?) -> Unit,
) {
    var modelWithConflict by remember { mutableStateOf<AgencySearchModel?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible =
                layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
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

    DivoBottomSheet(
        sheetState = sheetState,
        title = stringResource(R.string.AddModelToYourRoster),
        onDismiss = onDismiss,
        isSaveMode = false,
        isApplyEnable = false,
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier,
        ) {
            Spacer(Modifier.height(20.dp))
            DivoTextField(
                modifier = Modifier.height(40.dp),
                value = query,
                onValueChange = onValueChanged,
                cornerRadius = 99.dp,
                leadingIcon = R.drawable.ic_divo_search_24,
                trailingIcon = if (query.isNotBlank()) R.drawable.ic_divo_clear else null,
                onTrailingIconClick = { onValueChanged("") },
                backgroundColor = AppTheme.colors.onBackground,
                horizontalContentPadding = 12.dp,
                textStyle = TextStyle(fontSize = 14.sp),
                placeholder = stringResource(R.string.SearchByName),
            )

            Spacer(Modifier.height(14.dp))

            if (query.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.StartTypingToSearchForModel).uppercase(),
                        fontSize = 26.sp,
                        style = AppTheme.typography.helveticaNeueLtCom,
                        color = AppTheme.colors.textPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp
                    )
                }
            } else if (isLoadingSearch && searchModels.isEmpty()) {
                SearchModelsLoadingContent()
            } else if (searchModels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    org.telegram.divo.screen.search.components.EmptyPlaceContent(
                        title = stringResource(R.string.Models),
                        body = stringResource(R.string.Model)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppTheme.colors.onBackground),
                    state = listState,
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 8.dp
                    )
                ) {
                    items(
                        items = searchModels,
                        key = { it.id }
                    ) {
                        ModelItem(
                            item = it,
                            onClicked = {
                                when (it.status) {
                                    is AgencySearchModelStatus.Available -> {
                                        onSelectModelForAdd(it)
                                    }

                                    is AgencySearchModelStatus.RepresentedByOther -> {
                                        modelWithConflict = it
                                    }

                                    else -> {}
                                }
                            }
                        )
                    }

                    if (error != null) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                org.telegram.divo.components.UIButtonNew(
                                    text = stringResource(R.string.RetryLabel),
                                    height = 40.dp,
                                    onClick = onLoadMore
                                )
                            }
                        }
                    } else if (isLoadingMore) {
                        item {
                            Spacer(Modifier.height(8.dp))
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
        }
    }

    if (selectedModelForAdd != null) {
        ConfirmModelAdditionBottomSheet(
            model = selectedModelForAdd,
            selectedModelInfo = selectedModelInfo,
            isAddingModel = isAddingModel,
            onDismiss = { onSelectModelForAdd(null) },
            onAddClicked = onAddClicked
        )
    }

    modelWithConflict?.let { conflictModel ->
        ModelConflictDialog(
            conflictModel = conflictModel,
            onDismiss = { modelWithConflict = null }
        )
    }
}

@Composable
private fun ModelConflictDialog(
    conflictModel: AgencySearchModel,
    onDismiss: () -> Unit
) {
    val agencyName =
        (conflictModel.status as? AgencySearchModelStatus.RepresentedByOther)?.agencyName
            ?: stringResource(R.string.DivoAgencyModelAnotherAgency)
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(28.dp),
            color = AppTheme.colors.backgroundLight,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.DivoAgencyModelRepresentedBy, agencyName),
                    style = AppTheme.typography.helveticaNeueLtCom.copy(fontWeight = FontWeight.Bold),
                    fontSize = 20.sp,
                    color = Color.Black,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.DivoAgencyModelRepresentedByDesc),
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 15.sp,
                    color = Color.Black.copy(0.5f),
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(28.dp))
                org.telegram.divo.components.UIButtonNew(
                    text = stringResource(R.string.Cancel),
                    modifier = Modifier.fillMaxWidth(),
                    background = Color(0xFF333333),
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
private fun ModelItem(
    item: AgencySearchModel,
    onClicked: () -> Unit,
) {
    Spacer(Modifier.height(10.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableWithoutRipple { onClicked() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DivoAsyncImage(
            modifier = Modifier.size(52.dp).clip(CircleShape),
            model = item.photoUrl,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f, fill = false),
                    text = item.name,
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.isPremium) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Image(
                        modifier = Modifier.size(16.dp).offset(y = (-1).dp),
                        painter = painterResource(R.drawable.divo_premium_bage),
                        contentDescription = null
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (item.status) {
                    is AgencySearchModelStatus.AlreadyAdded -> {
                        Text(
                            text = stringResource(R.string.DivoAgencyModelAlreadyAdded),
                            style = AppTheme.typography.helveticaNeueRegular,
                            fontSize = 14.sp,
                            color = Color.Black.copy(0.6f)
                        )
                    }
                    is AgencySearchModelStatus.RepresentedByOther -> {
                        Icon(
                            painter = painterResource(R.drawable.ic_divo_report),
                            contentDescription = null,
                            tint = AppTheme.colors.accentOrange,
                            modifier = Modifier.size(16.dp).offset(y = (-1).dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.DivoAgencyModelRepresentedByOther),
                            style = AppTheme.typography.helveticaNeueRegular,
                            fontSize = 14.sp,
                            color = AppTheme.colors.accentOrange,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    else -> {
                        if (item.username != null) {
                            Text(
                                text = item.username,
                                style = AppTheme.typography.helveticaNeueRegular,
                                fontSize = 14.sp,
                                color = Color.Black.copy(0.6f),
                            )
                            Text(
                                text = " • ",
                                style = AppTheme.typography.helveticaNeueRegular,
                                fontSize = 14.sp,
                                color = Color.Black.copy(0.6f),
                            )
                        }
                        Text(
                            text = item.role,
                            style = AppTheme.typography.helveticaNeueRegular,
                            fontSize = 14.sp,
                            color = Color.Black.copy(0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(10.dp))
    Divider(color = Color.LightGray, thickness = 0.5.dp)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmModelAdditionBottomSheet(
    model: AgencySearchModel,
    selectedModelInfo: org.telegram.divo.entity.UserInfo?,
    isAddingModel: Boolean,
    onDismiss: () -> Unit,
    onAddClicked: (Int, String?) -> Unit
) {
    var personalNote by remember { mutableStateOf("") }
    val confirmSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val systemBars = WindowInsets.systemBars.asPaddingValues()
    val topPadding = systemBars.calculateTopPadding()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = confirmSheetState,
        dragHandle = null,
        containerColor = Color(0xFFF5F5F5),
        modifier = Modifier.padding(top = topPadding + 16.dp),
        contentWindowInsets = { WindowInsets(0.dp) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.backgroundLight)
        ) {
            val screenHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp
            val imageHeight = screenHeight * 0.4f
            if (selectedModelInfo == null) {
                ConfirmModelAdditionLoadingContent(imageHeight)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Image
                    Box(modifier = Modifier.fillMaxWidth().height(imageHeight)) {
                        DivoAsyncImage(
                            model = model.photoUrl,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar overlapping the image
                        DivoAsyncImage(
                            model = model.photoUrl,
                            modifier = Modifier
                                .size(68.dp)
                                .offset(y = (-34).dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Name and Premium
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.offset(y = (-15).dp)
                        ) {
                            Text(
                                text = model.name,
                                style = AppTheme.typography.helveticaNeueLtCom.copy(fontWeight = FontWeight.Bold),
                                fontSize = 32.sp,
                                color = Color.Black
                            )
                            if (model.isPremium) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Image(
                                    modifier = Modifier.size(26.dp).offset(y = (-3).dp),
                                    painter = painterResource(R.drawable.divo_premium_bage),
                                    contentDescription = null,
                                )
                            }
                        }

                        // Role, age, location
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.offset(y = (-8).dp)
                        ) {
                            DivoChip(
                                text = model.role,
                                background = Color(0xFF2653CE),
                                textColor = Color.White,
                                border = 0.dp,
                                contentPadding = PaddingValues(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            val age = model.birthday?.toAge()
                            val ageStr = age?.let { stringResource(R.string.DivoAgencyModelYearsOld, it) }
                            val locationStr = buildString {
                                if (ageStr != null) append("$ageStr · ")
                                val country = model.city?.countryName ?: ""
                                val flag = model.city?.countryCode?.toCountryFlagEmoji() ?: ""
                                if (flag.isNotBlank()) append("$flag ")
                                append(country)
                            }
                            Text(
                                text = locationStr,
                                style = AppTheme.typography.helveticaNeueRegular,
                                fontSize = 14.sp,
                                color = AppTheme.colors.textPrimary.copy(0.8f)
                            )
                        }

                        val appearance = selectedModelInfo.model?.appearance
                        if (appearance != null) {
                            val h = appearance.height?.toInt()
                            val b = appearance.breastSize
                            val w = appearance.waist?.toInt()
                            val hips = appearance.hips?.toInt()

                            val parts = mutableListOf<String>()
                            if (h != null && h > 0) parts.add(stringResource(R.string.DivoAgencyModelHeight, h))
                            if (!b.isNullOrBlank()) parts.add(stringResource(R.string.DivoAgencyModelBreast, b))
                            if (w != null && w > 0) parts.add(stringResource(R.string.DivoAgencyModelWaist, w))
                            if (hips != null && hips > 0) parts.add(stringResource(R.string.DivoAgencyModelHips, hips))

                            if (parts.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(AppTheme.colors.onBackground)
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = parts.joinToString(" · "),
                                        style = AppTheme.typography.helveticaNeueRegular,
                                        color = AppTheme.colors.textPrimary.copy(0.8f),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = stringResource(R.string.DivoAgencyModelAddNoteDescription),
                            style = AppTheme.typography.helveticaNeueRegular,
                            fontSize = 16.sp,
                            lineHeight = 20.sp,
                            color = Color.Black.copy(0.6f),
                            modifier = Modifier.fillMaxWidth()
                        )
//                        Spacer(modifier = Modifier.height(16.dp))
//                        Text(
//                            text = stringResource(R.string.DivoAgencyModelAddNoteTitle),
//                            style = AppTheme.typography.helveticaNeueRegular,
//                            fontSize = 14.sp,
//                            color = Color.Black.copy(0.6f),
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                        Spacer(modifier = Modifier.height(6.dp))
//                        DivoTextField(
//                            value = personalNote,
//                            onValueChange = { personalNote = it },
//                            placeholder = stringResource(R.string.DivoAgencyModelAddNoteHint),
//                            modifier = Modifier.fillMaxWidth(),
//                            height = 92.dp,
//                            cornerRadius = 16.dp,
//                            minLines = 3,
//                            maxLines = 3,
//                            backgroundColor = AppTheme.colors.onBackground
//                        )

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(
                            bottom = WindowInsets.navigationBars.asPaddingValues()
                                .calculateBottomPadding() + 8.dp
                        )
                        .padding(horizontal = 16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        org.telegram.divo.components.UIButtonNew(
                            text = stringResource(R.string.Cancel),
                            modifier = Modifier.weight(1f),
                            background = Color(0xFF333333),
                            onClick = onDismiss
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        org.telegram.divo.components.UIButtonNew(
                            text = stringResource(R.string.Confirm),
                            modifier = Modifier.weight(1f),
                            isLoading = isAddingModel,
                            onClick = {
                                onAddClicked(
                                    model.userId,
                                    personalNote.takeIf { p -> p.isNotBlank() }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchModelsLoadingContent() {
    Column(modifier = Modifier.fillMaxSize()) {
        repeat(10) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .shimmer()
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmer()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmer()
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Divider(color = Color.LightGray, thickness = 0.5.dp)
        }
    }
}

@Composable
private fun ConfirmModelAdditionLoadingContent(imageHeight: androidx.compose.ui.unit.Dp) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().height(imageHeight).shimmer())
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(68.dp).offset(y = (-34).dp)
                    .clip(CircleShape).background(AppTheme.colors.onBackground).shimmer()
            )
            Box(
                modifier = Modifier.width(180.dp).height(32.dp)
                    .clip(RoundedCornerShape(8.dp)).offset(y = (-15).dp).shimmer()
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.width(120.dp).height(24.dp)
                    .clip(RoundedCornerShape(8.dp)).shimmer()
            )
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier.width(240.dp).height(34.dp)
                    .clip(RoundedCornerShape(16.dp)).shimmer()
            )
            Spacer(Modifier.height(32.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(92.dp)
                    .clip(RoundedCornerShape(16.dp)).shimmer()
            )
        }
    }
}
