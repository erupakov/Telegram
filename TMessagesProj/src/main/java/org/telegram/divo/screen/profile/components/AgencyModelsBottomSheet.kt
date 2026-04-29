package org.telegram.divo.screen.profile.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.DivoChip
import org.telegram.divo.components.DivoTextField
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.items.DivoBottomSheet
import org.telegram.divo.entity.RoleType
import org.telegram.divo.entity.SearchedProfile
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyModelsBottomSheet(
    query: String,
    searchModels: List<SearchedProfile>,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onValueChanged: (String) -> Unit,
    onClicked: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
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
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                ) {
                    items(
                        items = searchModels,
                        key = { it.id }
                    ) {
                        ModelItem(
                            item = it,
                            onClicked = onClicked
                        )
                    }

                    if (isLoadingMore) {
                        item {
                            Spacer(Modifier.height(4.dp))
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
}

@Composable
private fun ModelItem(
    item: SearchedProfile,
    onClicked: (Int) -> Unit,
) {
    Spacer(Modifier.height(4.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableWithoutRipple { onClicked(item.id) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DivoAsyncImage(
            modifier = Modifier.size(52.dp).clip(CircleShape),
            model = item.photo,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material.Text(
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
    }
    Spacer(Modifier.height(10.dp))
    Divider(color = Color.LightGray, thickness = 0.5.dp)
}