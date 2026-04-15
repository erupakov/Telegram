package org.telegram.divo.screen.face_search_history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.SnackbarEvent
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.dal.db.entity.FaceRecognitionEntity
import org.telegram.divo.screen.face_search_history.Effect.NavigateBack
import org.telegram.divo.screen.face_search_history.Effect.NavigateToSimilarity
import org.telegram.divo.screen.face_search_history.Effect.ShowError
import org.telegram.divo.screen.similar_profiles.SimilarFiltersSerializer
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R
import java.text.SimpleDateFormat

@Composable
fun FaceSearchHistoryScreen(
    viewModel: FaceSearchHistoryViewModel = viewModel(),
    onSimilarityProfileClicked: (String, String?) -> Unit,
    onBack: () -> Unit
) {
    val state = viewModel.state.collectAsState().value
    val snackbarState = remember { AppSnackbarHostState() }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect {
            when (it) {
                NavigateBack -> onBack()
                is NavigateToSimilarity -> onSimilarityProfileClicked(it.url, it.filtersJson)
                is ShowError -> snackbarState.show(SnackbarEvent.Error(it.message))
            }
        }
    }

    FaceSearchHistoryContent(
        uiState = state,
        snackbarHostState = snackbarState,
        onIntent = { viewModel.setIntent(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FaceSearchHistoryContent(
    uiState: State,
    snackbarHostState: AppSnackbarHostState,
    onIntent: (Intent) -> Unit = {},
) {
    Scaffold(
        containerColor = AppTheme.colors.backgroundLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.FaceSearchHistoryTitle).uppercase(),
                        style = AppTheme.typography.appBar,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    RoundedButton(
                        modifier = Modifier.padding(start = 16.dp),
                        resId = R.drawable.ic_divo_back,
                        onClick = { onIntent(Intent.OnBackClicked) }
                    )
                },
                actions = {
                    Text(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickableWithoutRipple { onIntent(Intent.OnClearAllClicked) },
                        text = stringResource(R.string.ClearAll),
                        style = AppTheme.typography.helveticaNeueRegular,
                        color = AppTheme.colors.accentOrange,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.backgroundLight
                )
            )
        },
        snackbarHost = {
            AppSnackbarHost(
                state = snackbarHostState,
                bottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 66.dp
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(AppTheme.colors.onBackground),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            itemsIndexed(
                items = uiState.frSearchHistory,
                key = { _, item -> item.id }
            ) { index, item ->
                if (index == uiState.frSearchHistory.size - 1 && !uiState.isLoadingMore && uiState.hasMore) {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        onIntent(Intent.OnLoadMore)
                    }
                }
                HistoryItem(
                    item = item,
                    onItemClicked = { onIntent(Intent.OnSimilarityProfileClicked(item)) }
                )
            }
        }
    }
}

@Composable
private fun HistoryItem(
    item: FaceRecognitionEntity,
    onItemClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableWithoutRipple { onItemClicked() }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DivoAsyncImage(
                model = item.imageUri,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${item.resultsCount} ${pluralStringResource(R.plurals.ResultFor, item.resultsCount)} ${stringResource(R.string.Found)}",
                    style = AppTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(6.dp))

                val formattedDate = remember(item.createdAt) {
                    SimpleDateFormat(
                        "EEEE, d MMM 'at' HH:mm",
                        java.util.Locale.getDefault()
                    ).format(java.util.Date(item.createdAt))
                }

                Text(
                    text = formattedDate,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textPrimary.copy(0.6f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                val noFiltersText = stringResource(R.string.NoFilters)
                val filtersText = remember(item.filtersJson, noFiltersText) {
                    item.toFiltersSummary(noFiltersText)
                }

                Text(
                    text = filtersText,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textPrimary.copy(0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(R.drawable.ic_divo_arrow_right_20),
            contentDescription = null,
            tint = AppTheme.colors.textPrimary
        )
    }
}

private fun FaceRecognitionEntity.toFiltersSummary(noFiltersText: String): String {
    val payload = SimilarFiltersSerializer.deserialize(filtersJson)
        ?: return noFiltersText

    val parts = mutableListOf<String>()
    if (payload.similarityPercent > 35) {
        parts += "Similarity ${payload.similarityPercent}%"
    }

    val location = payload.countryNames.firstOrNull()
        ?: payload.countryShortNames.firstOrNull()
    if (!location.isNullOrBlank()) {
        parts += location
    }

    if (payload.roleValue.isNotBlank()) {
        parts += payload.roleValue.split(", ").firstOrNull().orEmpty()
    }

    val hasNonEmptyParam = payload.blockParams.any { param ->
        param.type != ParametersType.ROLE && param.value.isNotBlank()
    }
    if (hasNonEmptyParam) {
        parts += "Params"
    }

    return if (parts.isEmpty()) noFiltersText else parts.joinToString(" · ")
}