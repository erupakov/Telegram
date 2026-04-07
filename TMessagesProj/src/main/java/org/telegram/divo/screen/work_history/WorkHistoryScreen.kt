package org.telegram.divo.screen.work_history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.SnackbarEvent.Error
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.DivoPopupMenu
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.PlaceholderAvatar
import org.telegram.divo.components.PopupMenuItem
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.components.TextTitle
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.entity.WorkExperience
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun WorkHistoryScreen(
    viewModel: WorkHistoryViewModel = viewModel(),
    isOwnProfile: Boolean,
    isFromEditScreen: Boolean = false,
    onBack: () -> Unit = {},
    onCreateClicked: (Int?) -> Unit = {},
) {
    val state = viewModel.state.collectAsState().value
    val snackbarState = remember { AppSnackbarHostState() }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect {
            when (it) {
                Effect.NavigateBack -> onBack()
                is Effect.ShowError -> snackbarState.show(Error(it.message))
                is Effect.NavigateToCreate -> onCreateClicked(it.id)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        WorkHistoryScreenView(
            state = state,
            isOwnProfile = isOwnProfile,
            isFromEditScreen = isFromEditScreen,
            onIntent = {
                viewModel.setIntent(it)
            },
        )

        AppSnackbarHost(
            modifier = Modifier.align(Alignment.BottomCenter),
            state = snackbarState,
            bottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 56.dp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkHistoryScreenView(
    state: State = State(),
    onIntent: (Intent) -> Unit = {},
    isFromEditScreen: Boolean,
    isOwnProfile: Boolean = false,
) {
    Box(
        modifier = Modifier.fillMaxSize().background(AppTheme.colors.backgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isFromEditScreen && state.experiences.isNotEmpty()) 86.dp else 0.dp)
        ) {
            if (!isFromEditScreen) {
                TopBar(
                    onBack = {
                        onIntent(Intent.OnBackClicked)
                    },
                    onCreate = {
                        onIntent(Intent.OnCreateClicked)
                    },
                    createEnabled = isOwnProfile
                )
                Spacer(Modifier.height(8.dp))
            }

            if (state.experiences.isEmpty()) {
                WorkHistoryEmpty(
                    modifier = Modifier,
                    isOwnProfile = isOwnProfile,
                    isFromEditScreen = isFromEditScreen,
                    onCreateClicked = { onIntent(Intent.OnCreateClicked) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppTheme.colors.onBackground),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = state.experiences,
                        key = { it.id }
                    ) { item ->
                        WorkExperienceRow(
                            item = item,
                            isDeleting = state.deletingId == item.id,
                            onEdit = { onIntent(Intent.OnEditClicked(item.id)) },
                            onDelete = { onIntent(Intent.OnDeleteClicked(item.id)) }
                        )
                    }

                }
            }
        }
        if (isFromEditScreen && state.experiences.isNotEmpty()) {
            UIButtonNew(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                text = stringResource(R.string.AddWorkExperience),
                onClick = {
                    onIntent(Intent.OnCreateClicked)
                }
            )
        }
    }
}

@Composable
private fun WorkHistoryEmpty(
    modifier: Modifier = Modifier,
    isOwnProfile: Boolean = false,
    isFromEditScreen: Boolean,
    onCreateClicked:()-> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painterResource(R.drawable.divo_work_history_empty),
                contentDescription = null,
                modifier = Modifier.size(68.dp)
            )
            TextTitle(
                text = stringResource(R.string.WorkHistoryEmptyTitle),
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                textAlign = TextAlign.Center
            )
            if (isOwnProfile) {
                Text(
                    text = stringResource(R.string.WorkHistoryEmptySubtitle),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (isOwnProfile) {
            UIButtonNew(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .then(if (isFromEditScreen) Modifier else Modifier.navigationBarsPadding())
                    .padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                text = stringResource(R.string.AddWorkExperience),
                onClick = {
                    onCreateClicked()
                }
            )
        }
    }
}

@Composable
private fun WorkExperienceRow(
    item: WorkExperience,
    isDeleting: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val agencyName = item.agencyName.orEmpty()
    val subtitle = buildWorkSubtitle(
        startDate = item.startDate,
        endDate = item.endDate,
        isCurrent = item.isCurrent
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
            AgencyAvatar(
                agencyName = agencyName,
                avatarUrl = "",
                modifier = Modifier.size(60.dp)
            )

            if (isDeleting) LottieProgressIndicator(Modifier.size(24.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = agencyName,
                style = AppTheme.typography.helveticaNeueRegular,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                color = Color.Black.copy(0.6f),
                style = AppTheme.typography.helveticaNeueRegular,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = Color.Black.copy(0.6f)
                )
            }

            DivoPopupMenu(
                visible = menuExpanded,
                onDismiss = { menuExpanded = false },
                offset = IntOffset(x = -34, y = -63),
                items = listOf(
                    PopupMenuItem(R.string.ButtonEdit, onEdit),
                    PopupMenuItem(R.string.ButtonDelete, onDelete),
                )
            )
        }
    }
}

@Composable
private fun buildWorkSubtitle(
    startDate: String,
    endDate: String?,
    isCurrent: Boolean
): String {
    val start = formatMonthYear(startDate)
    val end = if (isCurrent || endDate == null) stringResource(R.string.PresentLabel) else formatMonthYear(endDate)
    val duration = computeDurationLabel(startDate, if (isCurrent) null else endDate)
    return "$start - $end · $duration"
}

@Composable
private fun computeDurationLabel(startDate: String, endDate: String?): String {
    val months = remember(startDate, endDate) {
        runCatching {
            val start = LocalDate.parse(startDate).withDayOfMonth(1)
            val end = (endDate?.let { LocalDate.parse(it) } ?: LocalDate.now()).withDayOfMonth(1)
            Period.between(start, end).toTotalMonths().toInt().coerceAtLeast(0)
        }.getOrDefault(0)
    }

    val years = months / 12
    val rem = months % 12

    return when {
        years > 0 && rem > 0 -> {
            val y = pluralStringResource(R.plurals.work_years, years, years)
            val m = pluralStringResource(R.plurals.work_months, rem, rem)
            "$y $m"
        }
        years > 0 -> pluralStringResource(R.plurals.work_years, years, years)
        else -> pluralStringResource(R.plurals.work_months, months, months)
    }
}

private fun formatMonthYear(date: String): String = try {
    LocalDate.parse(date).format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH))
} catch (_: Throwable) { "" }

@Composable
private fun AgencyAvatar(
    agencyName: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier
) {
    if (avatarUrl.isNullOrBlank()) {
        PlaceholderAvatar(
            modifier = modifier
                .border(1.dp, AppTheme.colors.backgroundLight, shape = CircleShape),
            name = agencyName,
            initialsColor = AppTheme.colors.textPrimary
        )
    } else {
        DivoAsyncImage(
            modifier = modifier
                .clip(CircleShape)
                .border(1.dp, AppTheme.colors.backgroundLight, shape = CircleShape),
            model = avatarUrl
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onBack: () -> Unit,
    onCreate: () -> Unit,
    createEnabled: Boolean
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.WorkExperience),
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            RoundedButton(
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 7.dp),
                onClick = onBack
            )
        },
        actions = {
            if (createEnabled) {
                Icon(
                    modifier = Modifier
                        .padding(end = 14.dp, bottom = 7.dp)
                        .size(17.dp)
                        .clickableWithoutRipple { onCreate() },
                    painter = painterResource(R.drawable.ic_divo_plus),
                    contentDescription = null,
                    tint = AppTheme.colors.textPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.colors.backgroundLight
        )
    )
}



