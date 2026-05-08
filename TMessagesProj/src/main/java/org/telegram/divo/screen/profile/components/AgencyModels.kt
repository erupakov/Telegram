package org.telegram.divo.screen.profile.components

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.DivoChip
import org.telegram.divo.components.DivoPopupMenu
import org.telegram.divo.components.PopupMenuItem
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.entity.AgencyModel
import org.telegram.divo.entity.RoleType
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun AgencyModels(
    models: List<AgencyModel>,
    //searchModels: List<SearchedProfile>,
    //query: String,
    //onQueryChanged: (String) -> Unit,
    isOwnProfile: Boolean,
    topPadding: Dp = 0.dp,
    //isLoadingMore: Boolean,
    //hasMore: Boolean,
    onLoadMoreAgencyModels: () -> Unit,
    onAddModelClicked: () -> Unit,
    onModelClicked: (Int) -> Unit,
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp

    if (models.isEmpty()) {
        EmptyModels(
            isOwnProfile = isOwnProfile,
            bottomPadding = bottomPadding,
            onClick = onAddModelClicked
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
                        onClicked = onModelClicked,
                        onEdit = {}, //TODO
                        onDelete = {} //TODO
                    )
                }
            }

            if (isOwnProfile) {
                UIButtonNew(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding)
                        .align(Alignment.BottomCenter),
                    text = stringResource(R.string.AddModel),
                    onClick = onAddModelClicked
                )
            }
        }
    }

    if (showBottomSheet) {
//        AgencyModelsBottomSheet(
//            query = query,
//            searchModels = searchModels,
//            onValueChanged = onQueryChanged,
//            isLoadingMore = isLoadingMore,
//            hasMore = hasMore,
//            onLoadMore = onLoadMoreAgencyModels,
//            onClicked = {},//TODO
//            onDismiss = { showBottomSheet = false }
//        )
    }
}

@Composable
private fun ModelItem(
    item: AgencyModel,
    isOwnProfile: Boolean,
    onClicked: (Int) -> Unit,
    onEdit: () -> Unit,
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
                Spacer(modifier = Modifier.width(8.dp))
                DivoChip(
                    modifier = Modifier.offset(y = (-2).dp),
                    text = stringResource(R.string.PremiumLabel),
                    resId = R.drawable.ic_divo_premium_11,
                    background = Color.Red,
                    textColor = AppTheme.colors.accentOrange,
                    iconSize = 11.dp,
                    border = 1.dp,
                    contentPadding= PaddingValues(start = 6.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = RoleType.MODEL.name.lowercase(), 
                style = AppTheme.typography.helveticaNeueRegular,
                fontSize = 14.sp,
                color = Color.Black.copy(0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
                offset = IntOffset(x = 0, y = -63),
                items = listOf(
                    PopupMenuItem(R.string.ButtonEdit, onEdit),
                    PopupMenuItem(R.string.ButtonDelete, onDelete),
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