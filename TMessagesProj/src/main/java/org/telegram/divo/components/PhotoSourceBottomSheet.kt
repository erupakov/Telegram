package org.telegram.divo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.entity.SearchedProfile
import org.telegram.divo.screen.search.utils.SearchPreferences
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

enum class SearchImageAction { CAMERA, GALLERY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSourceBottomSheet(
    value: String,
    searchResults: List<SearchedProfile>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    sheetState: SheetState,
    onValueChanged: (String) -> Unit,
    onActionSelected: (SearchImageAction) -> Unit,
    onDismiss: () -> Unit,
    onClicked: (SearchedProfile) -> Unit,
    onLoadMore: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { SearchPreferences(context) }

    var showDisclaimer by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<SearchImageAction?>(null) }

    var isSearchEnabled by remember { mutableStateOf(false) }

    fun handleAction(action: SearchImageAction) {
        if (prefs.isFaceSearchFirstUse) {
            pendingAction = action
            showDisclaimer = true
        } else {
            onActionSelected(action)
        }
    }

    if (showDisclaimer) {
        FaceSearchDisclaimerDialog(
            onConfirm = {
                prefs.isFaceSearchFirstUse = false
                showDisclaimer = false
                pendingAction?.let { onActionSelected(it) }
                pendingAction = null
            },
            onDismiss = {
                isSearchEnabled = false
                showDisclaimer = false
                pendingAction = null
            }
        )
    }

    ModalBottomSheet(
        modifier = Modifier.statusBarsPadding(),
        onDismissRequest = {
            isSearchEnabled = false
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = AppTheme.colors.backgroundLight,
        dragHandle = null
    ) {
        Box {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .padding(top = 8.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.backgroundDark.copy(0.3f))
                )

                ChoosePhotoSection(
                    modifier = Modifier
                        .then(if (isSearchEnabled) Modifier.weight(1f) else Modifier.padding(bottom = 88.dp)),
                    value = value,
                    searchResults = searchResults,
                    isSearchEnabled = isSearchEnabled,
                    isLoading = isLoading,
                    isLoadingMore = isLoadingMore,
                    hasMore = hasMore,
                    onValueChanged = onValueChanged,
                    onTakePhoto = { handleAction(SearchImageAction.CAMERA) },
                    onChooseFromLibrary = { handleAction(SearchImageAction.GALLERY) },
                    onUseDivoPhoto = { isSearchEnabled = true },
                    onClicked = onClicked,
                    onLoadMore = onLoadMore
                )
            }

            UIButtonNew(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(14.dp)),
                text = stringResource(R.string.ButtonCancel),
                background = AppTheme.colors.onBackground,
                shape = RoundedCornerShape(14.dp),
                textStyle = AppTheme.typography.manropeRegular.copy(
                    color = AppTheme.colors.accentOrange,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                paddingTop = 0.dp,
                onClick = onDismiss
            )
        }
    }
}

@Composable
private fun ChoosePhotoSection(
    modifier: Modifier = Modifier,
    value: String,
    searchResults: List<SearchedProfile>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    isSearchEnabled: Boolean,
    onValueChanged: (String) -> Unit,
    onClicked: (SearchedProfile) -> Unit,
    onLoadMore: () -> Unit,
    onTakePhoto: () -> Unit,
    onChooseFromLibrary: () -> Unit,
    onUseDivoPhoto: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.FindSimilarProfiles),
            style = AppTheme.typography.manropeRegular,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = stringResource(R.string.UploadPhoto),
            style = AppTheme.typography.manropeRegular,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.textPrimary.copy(0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))

        Divider(color = Color.LightGray, thickness = 0.5.dp)
        Button(
            modifier = Modifier.clickableWithoutRipple { onTakePhoto() },
            text = stringResource(R.string.TakePhoto)
        )
        Divider(color = Color.LightGray, thickness = 0.5.dp)
        Button(
            modifier = Modifier.clickableWithoutRipple { onChooseFromLibrary() },
            text = stringResource(R.string.ChooseFromLibrary)
        )
        Divider(color = Color.LightGray, thickness = 0.5.dp)
        Button(
            modifier = Modifier.clickableWithoutRipple { onUseDivoPhoto() },
            text = stringResource(R.string.UseDivoPhoto)
        )

        if (isSearchEnabled) {
            SearchContent(
                value = value,
                searchResults = searchResults,
                isLoading = isLoading,
                isLoadingMore = isLoadingMore,
                hasMore = hasMore,
                onValueChanged = onValueChanged,
                onClicked = onClicked,
                onLoadMore = onLoadMore
            )
        } else {
            Divider(color = Color.LightGray, thickness = 0.5.dp)
        }
    }
}

@Composable
private fun SearchContent(
    value: String,
    searchResults: List<SearchedProfile>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onValueChanged: (String) -> Unit,
    onClicked: (SearchedProfile) -> Unit,
    onLoadMore: () -> Unit,
) {
    Column {
        DivoTextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 16.dp),
            value = value,
            onValueChange = { onValueChanged(it) },
            cornerRadius = 99.dp,
            leadingIcon = R.drawable.ic_divo_search_24,
            trailingIcon = if (value.isBlank()) null else R.drawable.ic_divo_clear,
            onTrailingIconClick = { onValueChanged("") },
            backgroundColor = AppTheme.colors.onBackground,
            horizontalContentPadding = 12.dp,
            textStyle = TextStyle(fontSize = 14.sp),
            placeholder = stringResource(R.string.SearchByName),
        )
        Spacer(Modifier.height(16.dp))
        SuggestionContent(
            searchResults = searchResults,
            value = value,
            isLoading = isLoading,
            isLoadingMore = isLoadingMore,
            hasMore = hasMore,
            onClicked = onClicked,
            onLoadMore = onLoadMore
        )
    }
}

@Composable
private fun SuggestionContent(
    searchResults: List<SearchedProfile>,
    value: String,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onClicked: (SearchedProfile) -> Unit,
    onLoadMore: () -> Unit,
) {
    var showEmpty by remember { mutableStateOf(false) }

    LaunchedEffect(value, searchResults, isLoading) {
        if (value.isNotBlank() && searchResults.isEmpty() && !isLoading) {
            delay(500)
            showEmpty = true
        } else {
            showEmpty = false
        }
    }

    when {
        isLoading && searchResults.isEmpty() -> {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                repeat(3) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, end = 48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .shimmer())
                        Spacer(Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(21.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .shimmer()
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }

        showEmpty -> {
            EmptyContent()
        }

        searchResults.isNotEmpty() -> {
            SearchSuggestions(
                items = searchResults,
                isLoadingMore = isLoadingMore,
                hasMore = hasMore,
                onClicked = onClicked,
                onLoadMore = onLoadMore
            )
        }
        else -> {}
    }
}

@OptIn(FlowPreview::class)
@Composable
private fun SearchSuggestions(
    items: List<SearchedProfile>,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onClicked: (SearchedProfile) -> Unit,
    onLoadMore: () -> Unit,
) {
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

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 76.dp)
    ) {
        item { Spacer(Modifier.height(4.dp)) }

        items(
            items = items,
            key = { it.id }
        ) {
            SuggestionItem(
                avatarUrl = it.photo,
                name = it.name,
                onClicked = { onClicked(it) }
            )
        }

        if (!isLoadingMore) item { Spacer(Modifier.height(4.dp)) }

        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LottieProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    avatarUrl: String,
    name: String,
    onClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clickableWithoutRipple { onClicked() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (avatarUrl.isBlank()) {
            PlaceholderAvatar(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.accentOrange),
                name = name,
            )
        } else {
            DivoAsyncImage(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                model = avatarUrl
            )
        }

        Spacer(Modifier.width(10.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = name,
            style = AppTheme.typography.helveticaNeueRegular,
            color = Color.Black,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

    }
}

@Composable
private fun EmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.onBackground),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.backgroundLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(R.drawable.ic_divo_search),
                contentDescription = null,
                tint = AppTheme.colors.textPrimary
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.NoResultsFound).uppercase(),
            style = AppTheme.typography.helveticaNeueLtCom,
            fontSize = 22.sp,
            color = AppTheme.colors.textPrimary
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun FaceSearchDisclaimerDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(270.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(AppTheme.colors.backgroundLight)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                painter = painterResource(R.drawable.ic_divo_face_rec),
                contentDescription = null,
                tint = AppTheme.colors.accentOrange,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.FaceSearchTitle),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.textPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.FaceSearchDescriptionFirst),
                fontSize = 13.sp,
                color = AppTheme.colors.textPrimary.copy(0.6f),
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.FaceSearchDescriptionSecond),
                fontSize = 13.sp,
                color = AppTheme.colors.textPrimary.copy(0.6f),
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(32.dp))
            UIButtonNew(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.FaceSearchConfirm),
                textStyle = AppTheme.typography.helveticaNeueLtCom.copy(
                    fontSize = 16.sp,
                    color = AppTheme.colors.textColor
                ),
                height = 40.dp,
                onClick = onConfirm
            )
        }
    }
}

@Composable
private fun Button(
    modifier: Modifier = Modifier,
    text: String,
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AppTheme.typography.manropeRegular,
            fontSize = 17.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
            color = AppTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

