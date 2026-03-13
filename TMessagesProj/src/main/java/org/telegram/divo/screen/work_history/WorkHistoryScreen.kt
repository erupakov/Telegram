package org.telegram.divo.screen.work_history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.components.TextTitle
import org.telegram.divo.components.UIButton
import org.telegram.tgnet.TLRPC
import org.telegram.messenger.R


@Composable
fun WorkHistoryScreen(
    viewModel: WorkHistoryViewModel = viewModel(),
    isOwnProfile: Boolean,
    onBack: () -> Unit = {},
    onCreateClicked: () -> Unit = {},
) {
    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect {
            when (it) {
                else -> {}
            }
        }
    }
    val state = viewModel.state.collectAsState().value
    WorkHistoryScreenView(
        state = state,
        isOwnProfile = isOwnProfile,
        onIntent = {
            viewModel.setIntent(it)
        },
        onCreateClicked = onCreateClicked,
        onBack = onBack
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun WorkHistoryScreenView(
    state: WorkHistoryViewModel.State = WorkHistoryViewModel.State(),
    onIntent: (WorkHistoryViewModel.Intent) -> Unit = {},
    isOwnProfile: Boolean = false,
    onCreateClicked: () -> Unit = {},
    onBack: () -> Unit = {}

) {
    Scaffold(
        modifier = Modifier.padding(top = 32.dp),
        topBar = {
            TopBar(
                onBack = {
                    onBack()
                },
                onCreate = {
                    onCreateClicked()
                },
                createEnabled = isOwnProfile
            )
        },
    ) { padding ->

        if (state.experiences.isEmpty()){
            WorkHistoryEmpty(
                isOwnProfile = isOwnProfile,
                onCreateClicked = onCreateClicked
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding)
            ) {
                items(
                    items = state.experiences,
                ) { item ->
                    WorkExperienceRow(
                        item = item,
                        onEdit = { onIntent(WorkHistoryViewModel.Intent.OnEditClicked(item.id)) },
                        onDelete = { onIntent(WorkHistoryViewModel.Intent.OnDeleteClicked(item.id)) }
                    )
                }

            }
        }
    }
}


@Preview
@Composable
private fun WorkHistoryEmpty(
    isOwnProfile: Boolean = false,
    onCreateClicked:()-> Unit ={}
) {
    Box(
        modifier = Modifier.fillMaxSize()
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
                text = "There are no work\nexperience yet.",
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                textAlign = TextAlign.Center
            )
            if (isOwnProfile) {
                Text(
                    text = "Click the button below\n" +
                            "to add your work\nexperience",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (isOwnProfile) {
            UIButton(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding( 16.dp),
                text = "Add Work Experience",
                onClick = {
                    onCreateClicked()
                }
            )
        }
    }
}


@Composable
private fun WorkExperienceRow(
    item: TLRPC.TL_profile_workExperience,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val agencyName = item.agency?.name.orEmpty()
    val subtitle = remember(item.start_date, item.end_date, item.is_current) {
        buildWorkSubtitle(
            startDateSeconds = item.start_date,
            endDateSeconds = item.end_date,
            isCurrent = item.is_current
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AgencyAvatar(
            name = agencyName,
            modifier = Modifier.size(52.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = agencyName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
            Text(
                text = subtitle,
                color = Color(0xFF8A8A8A),
                fontSize = 16.sp
            )
        }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More"
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        menuExpanded = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    }
                )
            }
        }
    }

    Divider(color = Color(0xFFEDEDED))
}

private fun buildWorkSubtitle(
    startDateSeconds: Int,
    endDateSeconds: Int,
    isCurrent: Boolean
): String {
    val start = formatMonthYear(startDateSeconds)
    val end = if (isCurrent || endDateSeconds == 0) "Present" else formatMonthYear(endDateSeconds)
    val duration = computeDurationLabel(startDateSeconds, if (isCurrent) null else endDateSeconds)

    return "$start - $end · $duration"
}

private fun formatMonthYear(epochSeconds: Int): String {
    return try {
        val dt = java.time.Instant.ofEpochSecond(epochSeconds.toLong())
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        dt.format(
            java.time.format.DateTimeFormatter.ofPattern(
                "MMM yyyy",
                java.util.Locale.ENGLISH
            )
        )
    } catch (_: Throwable) {
        ""
    }
}

private fun computeDurationLabel(startSeconds: Int, endSecondsOrNull: Int?): String {
    return try {
        val start = java.time.Instant.ofEpochSecond(startSeconds.toLong())
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
            .withDayOfMonth(1)

        val end = (endSecondsOrNull?.let {
            java.time.Instant.ofEpochSecond(it.toLong())
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
        } ?: java.time.LocalDate.now()).withDayOfMonth(1)

        val months = java.time.Period.between(start, end).toTotalMonths().toInt().coerceAtLeast(0)
        val years = months / 12
        val rem = months % 12

        when {
            years > 0 && rem > 0 -> "${years} year${if (years == 1) "" else "s"} ${rem} month${if (rem == 1) "" else "s"}"
            years > 0 -> "${years} year${if (years == 1) "" else "s"}"
            else -> "${rem} month${if (rem == 1) "" else "s"}"
        }
    } catch (_: Throwable) {
        ""
    }
}

@Composable
private fun AgencyAvatar(
    name: String,
    modifier: Modifier = Modifier
) {
    val initials = remember(name) {
        name.trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
            .joinToString("")
            .ifBlank { "A" }
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFFF2F2F2)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6F6F6F),
            fontSize = 18.sp
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
                text = "WORK EXPERIENCE",
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            Text(
                "Back",
                color = Color(0xFFBF825E),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onBack() }
                    .padding(8.dp),
                fontSize = 16.sp)
        },
        actions = {
            if (createEnabled) {
                Text(
                    "Create",
                    color = if (createEnabled) Color(0xFFBF825E) else Color(0xFFB9B9B9),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(createEnabled) { onCreate() }
                        .padding(8.dp),
                    fontSize = 16.sp)
            }
        }
    )
}



