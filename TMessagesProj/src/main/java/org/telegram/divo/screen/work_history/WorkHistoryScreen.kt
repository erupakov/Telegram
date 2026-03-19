package org.telegram.divo.screen.work_history

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.BackButton
import org.telegram.divo.components.DivoPopupMenu
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.PlaceholderAvatar
import org.telegram.divo.components.PopupMenuItem
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
    onBack: () -> Unit = {},
    onCreateClicked: (Int?) -> Unit = {},
) {
    val context = LocalContext.current
    val state = viewModel.state.collectAsState().value

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect {
            when (it) {
                Effect.NavigateBack -> onBack()
                is Effect.ShowError -> Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                is Effect.NavigateToCreate -> onCreateClicked(it.id)
            }
        }
    }

    WorkHistoryScreenView(
        state = state,
        isOwnProfile = isOwnProfile,
        onIntent = {
            viewModel.setIntent(it)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkHistoryScreenView(
    state: State = State(),
    onIntent: (Intent) -> Unit = {},
    isOwnProfile: Boolean = false,
) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            TopBar(
                onBack = {
                    onIntent(Intent.OnBackClicked)
                },
                onCreate = {
                    onIntent(Intent.OnCreateClicked)
                },
                createEnabled = isOwnProfile
            )
        },
        containerColor = AppTheme.colors.backgroundNew
    ) { padding ->

        if (state.experiences.isEmpty()){
            WorkHistoryEmpty(
                modifier = Modifier.padding(padding),
                isOwnProfile = isOwnProfile,
                onCreateClicked = { onIntent(Intent.OnCreateClicked) }
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = state.experiences,
                    key = { it.id }
                ) { item ->
                    WorkExperienceRow(
                        item = item,
                        onEdit = { onIntent(Intent.OnEditClicked(item.id)) },
                        onDelete = { onIntent(Intent.OnDeleteClicked(item.id)) }
                    )
                }

            }
        }
    }
}

@Composable
private fun WorkHistoryEmpty(
    modifier: Modifier = Modifier,
    isOwnProfile: Boolean = false,
    onCreateClicked:()-> Unit
) {
    Box(
        modifier = modifier.fillMaxSize().background(AppTheme.colors.backgroundNew)
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
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding( 16.dp),
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
        AgencyAvatar(
            agencyName = agencyName,
            avatarUrl = "",
            modifier = Modifier.size(60.dp)
        )

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
    val end = if (isCurrent || endDate == null) "Present" else formatMonthYear(endDate)
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
            modifier = modifier,
            name = agencyName,
        )
    } else {
        DivoAsyncImage(
            modifier = modifier
                .clip(CircleShape),
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
            BackButton(
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 7.dp),
                color = AppTheme.colors.buttonColor,
                onBackClicked = onBack
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
                    tint = AppTheme.colors.buttonColor
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}



