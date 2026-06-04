package org.telegram.divo.screen.profile.components

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.DivoChip
import org.telegram.divo.components.DivoPopupMenu
import org.telegram.divo.components.PopupMenuItem
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.entity.AgencyModel
import org.telegram.divo.entity.AgencySearchModel
import org.telegram.divo.entity.RoleType
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun AgencyModels(
    models: List<AgencyModel>,
    searchModels: List<AgencySearchModel>,
    query: String,
    onQueryChanged: (String) -> Unit,
    isOwnProfile: Boolean,
    topPadding: Dp = 0.dp,
    isLoadingSearchModels: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    searchModelsError: String?,
    isAddingAgencyModel: Boolean,
    selectedModelForAdd: AgencySearchModel?,
    selectedModelInfo: org.telegram.divo.entity.UserInfo?,
    isBottomSheetVisible: Boolean,
    onToggleBottomSheet: (Boolean) -> Unit,
    onLoadMoreAgencyModels: () -> Unit,
    onLoadMoreSearchModels: () -> Unit,
    onModelClicked: (Int) -> Unit,
    onAddModel: (Int, String?) -> Unit,
    onCancelRequest: (Int) -> Unit,
    onSelectModelForAdd: (AgencySearchModel?) -> Unit,
) {
    var modelToCancel by remember { mutableStateOf<AgencyModel?>(null) }
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp

    if (models.isEmpty()) {
        EmptyModels(
            isOwnProfile = isOwnProfile,
            bottomPadding = bottomPadding,
            onClick = { onToggleBottomSheet(true) }
        )
    } else {
        val lazyListState = rememberLazyListState()

        val shouldLoadMore by remember {
            derivedStateOf {
                val lastVisibleIndex = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItems = lazyListState.layoutInfo.totalItemsCount
                lastVisibleIndex >= totalItems - 3 && totalItems > 0
            }
        }

        LaunchedEffect(shouldLoadMore) {
            if (shouldLoadMore) {
                onLoadMoreAgencyModels()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.backgroundLight),
                state = lazyListState,
                contentPadding = PaddingValues(
                    top = topPadding,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding() + 76.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = models,
                    key = { it.id }
                ) {
                    ModelItem(
                        item = it,
                        isOwnProfile = isOwnProfile,
                        onClicked = { id ->
                            val clickedModel = models.find { it.userId == id }
                            if (clickedModel?.status == org.telegram.divo.entity.AgencyModelStatus.PENDING) {
                                modelToCancel = clickedModel
                            } else {
                                onModelClicked(id)
                            }
                        },
                        onDelete = { modelToCancel = it }
                    )
                }
            }
        }
    }

    if (isBottomSheetVisible) {
        AgencyModelsBottomSheet(
            query = query,
            searchModels = searchModels,
            isLoadingSearch = isLoadingSearchModels,
            isLoadingMore = isLoadingMore,
            hasMore = hasMore,
            error = searchModelsError,
            isAddingModel = isAddingAgencyModel,
            selectedModelForAdd = selectedModelForAdd,
            selectedModelInfo = selectedModelInfo,
            onLoadMore = onLoadMoreSearchModels,
            onValueChanged = onQueryChanged,
            onAddClicked = onAddModel,
            onDismiss = { onToggleBottomSheet(false) },
            onSelectModelForAdd = onSelectModelForAdd
        )
    }

    modelToCancel?.let { model ->
        val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val isPending = model.status == org.telegram.divo.entity.AgencyModelStatus.PENDING

        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { modelToCancel = null },
            sheetState = sheetState,
            containerColor = Color.Transparent,
            dragHandle = null
        ) {
            org.telegram.divo.style.DivoLocaleProvider {
                Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppTheme.colors.backgroundLight),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = if (isPending) stringResource(R.string.DivoAgencyModelCancelRequestTitle, model.name) else stringResource(R.string.DivoAgencyModelRemoveRosterTitle, model.name),
                        style = AppTheme.typography.helveticaNeueRegular,
                        fontSize = 15.sp,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(24.dp))
                    androidx.compose.material.Divider(color = Color.LightGray, thickness = 0.5.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickableWithoutRipple {
                                onCancelRequest(model.id)
                                modelToCancel = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 18.dp),
                            text = if (isPending) stringResource(R.string.DivoAgencyModelYesCancel) else stringResource(R.string.DivoAgencyModelYesRemove),
                            style = AppTheme.typography.helveticaNeueRegular,
                            fontSize = 17.sp,
                            color = AppTheme.colors.accentOrange
                        )
                    }
                    androidx.compose.material.Divider(color = Color.LightGray, thickness = 0.5.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickableWithoutRipple { modelToCancel = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 18.dp),
                            text = if (isPending) stringResource(R.string.DivoAgencyModelKeepRequest) else stringResource(R.string.DivoAgencyModelKeepModel),
                            style = AppTheme.typography.helveticaNeueLtCom.copy(fontWeight = FontWeight.Bold),
                            fontSize = 17.sp,
                            color = Color.Black
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            }
        }
    }
}

@Composable
private fun ModelItem(
    item: AgencyModel,
    isOwnProfile: Boolean,
    onClicked: (Int) -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Spacer(Modifier.height(4.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableWithoutRipple { onClicked(item.userId) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DivoAsyncImage(
            modifier = Modifier.size(52.dp).clip(CircleShape),
            model = item.photoUrl,
            errorContent = {
                Image(
                    painter = painterResource(R.drawable.divo_avatar_placeholder),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        modifier = Modifier.size(15.dp).offset(y = (-1).dp),
                        painter = painterResource(R.drawable.divo_premium_bage),
                        contentDescription = null,
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = RoleType.MODEL.name.lowercase(), 
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 14.sp,
                    color = Color.Black.copy(0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.status == org.telegram.divo.entity.AgencyModelStatus.PENDING) {
                    Spacer(modifier = Modifier.width(8.dp))
                    DivoChip(
                        text = stringResource(R.string.DivoAgencyModelPending),
                        textColor = AppTheme.colors.accentOrange,
                        border = 1.dp,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        if (isOwnProfile) {
            Icon(
                modifier = Modifier.clickableWithoutRipple { menuExpanded = true },
                painter = painterResource(R.drawable.ic_divo_menu_24),
                contentDescription = null
            )

            DivoPopupMenu(
                visible = menuExpanded,
                onDismiss = { menuExpanded = false },
                offset = IntOffset(x = 0, y = 0),
                items = listOf(
                    PopupMenuItem(R.string.DeleteEvent, onDelete),
                )
            )
        }
    }
    Spacer(Modifier.height(10.dp))
    Divider(color = Color.LightGray, thickness = 0.5.dp)
}

@Composable
private fun EmptyModels(
    isOwnProfile: Boolean,
    bottomPadding: Dp,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.backgroundLight)
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding + 56.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.textPrimary.copy(0.1f))
            ) {
                Icon(
                    modifier = Modifier.size(24.dp).align(Alignment.Center),
                    painter = painterResource(R.drawable.ic_divo_tab_agency),
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.AgencyModelsEmpty).uppercase(),
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 26.sp,
                lineHeight = 30.sp,
                textAlign = TextAlign.Center,
            )
            if (isOwnProfile) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.AgencyModelsEmptyHint),
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 16.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (isOwnProfile) {
            UIButtonNew(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = bottomPadding)
                    .align(Alignment.BottomCenter),
                text = stringResource(R.string.AddModel),
                onClick = onClick
            )
        }
    }
}