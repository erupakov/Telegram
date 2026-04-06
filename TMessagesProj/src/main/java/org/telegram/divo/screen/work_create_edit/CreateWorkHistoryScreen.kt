package org.telegram.divo.screen.work_create_edit

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.common.SnackbarEvent.Error
import org.telegram.divo.common.SnackbarEvent.Success
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.rememberGalleryLauncher
import org.telegram.divo.common.utils.toFormattedDate
import org.telegram.divo.components.BackButton
import org.telegram.divo.components.DivoTextField
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R
import java.time.Instant
import java.time.ZoneId

@Composable
fun CreateWorkHistoryScreen(
    editId: Int? = null,
    viewModel: CreateWorkHistoryViewModel = viewModel(
        factory = CreateWorkHistoryViewModel.factory(editId)
    ),
    onBack: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onPickCover: () -> Unit = {},
    onPickGallery: () -> Unit = {},
    onPickDate: () -> Unit = {},
    onPickTime: () -> Unit = {},
) {
    val snackbarState = remember { AppSnackbarHostState() }
    val parametersSavedText = stringResource(R.string.WorkExperienceSaved)

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect {
            when (it) {
                Effect.NavigateBack -> {
                    onBack()
                }
                Effect.NavigateToSearch -> {
                    onNavigateToSearch()
                }
                is Effect.ShowSuccess -> {
                    snackbarState.show(Success(parametersSavedText))
                    onBack()
                }
                is Effect.ShowError -> {
                    snackbarState.show(Error(it.message))
                }
            }
        }
    }
    val state = viewModel.state.collectAsState().value

    CreateWorkHistoryScreenView(
        state = state,
        snackbarState = snackbarState,
        onIntent = {
            viewModel.setIntent(it)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkHistoryScreenView(
    state: State,
    snackbarState: AppSnackbarHostState,
    onIntent: (Intent) -> Unit,
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val startDatePickerState = key(state.startDateMil) {
        rememberDatePickerState(
            initialSelectedDateMillis = state.startDateMil
        )
    }
    val endDatePickerState = key(state.startDateMil) {
        rememberDatePickerState(
            initialSelectedDateMillis = state.endDateMil,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val startMil = state.startDateMil
                    return utcTimeMillis >= startMil
                }
            }
        )
    }

    var selectedAvatarUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val openGallery = rememberGalleryLauncher { uri ->
        selectedAvatarUri = uri
    }

    Scaffold(
        topBar = {
            TopBar(
                title = if (state.isEditMode) stringResource(R.string.EditExperience) else stringResource(R.string.CreateExperience),
                actionText = if (state.isEditMode) stringResource(R.string.ButtonSave) else stringResource(R.string.ButtonCreate),
                onBack = {
                    onIntent(Intent.OnBackClicked)
                }, onCreate = {
                    onIntent(Intent.OnCreateExperience)
                },
                createEnabled = state.createEnabled
            )
        },
        snackbarHost = {
            AppSnackbarHost(
                state = snackbarState,
                bottomPadding = 56.dp
            )
        },
        containerColor = Color.White
    ) { padding ->
        Divider(modifier = Modifier.padding(padding))

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
//                TelegramUserAvatarEditable(
//                    avatarUrl = state.avatarUrl,
//                    localUri = selectedAvatarUri,
//                    background = Color.White,
//                    borderColor = AppTheme.colors.buttonColor,
//                    isVisibleSmallIcon = state.isEditMode,
//                    usePlaceholder = true,
//                    placeholderSymbols = state.agencyName,
//                    smallIconResId = R.drawable.ic_divo_work_edit_avatar,
//                    onEditClick = { openGallery() }
//                )

                Spacer(modifier = Modifier.height(32.dp + 100.dp))

                Title(text = stringResource(R.string.WorkExperienceInfo))

                Spacer(modifier = Modifier.height(20.dp))

                Title(
                    text = stringResource(R.string.AgencyName),
                    textColor = Color(0xFF3C3C43)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.06f))
                        .clickableWithoutRipple { onIntent(Intent.OnSearchSelected) },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                        text = state.agencyName.ifBlank { stringResource(R.string.EnterAgencyName) },
                        color = Color(0x993C3C43),
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Date & Time
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DateField(
                        modifier = Modifier.weight(1f),
                        date = startDatePickerState.selectedDateMillis?.toFormattedDate().orEmpty(),
                        title = stringResource(R.string.StartDate),
                        onClick = { showStartDatePicker = true }
                    )
                    DateField(
                        modifier = Modifier.weight(1f),
                        date = endDatePickerState.selectedDateMillis?.toFormattedDate().orEmpty(),
                        title = stringResource(R.string.EndDate),
                        onClick = { if (!state.isCurrent) showEndDatePicker = true },
                        enabled = !state.isCurrent
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Checkbox(
                        modifier = Modifier,
                        checked = state.isCurrent,
                        colors = CheckboxDefaults.colors(
                            checkedColor = AppTheme.colors.accentOrange
                        ),
                        onCheckedChange = { checked ->
                            onIntent(Intent.OnCurrentChanged(isCurrent = checked))
                        }
                    )
                    Text(
                        text = stringResource(R.string.CurrentlyWorkingRole),
                        style = AppTheme.typography.manropeRegular,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable(
                                onClick = { onIntent(Intent.OnCurrentChanged(isCurrent = !state.isCurrent)) }
                            )
                    )
                }
            }

            UIButtonNew(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                    .align(Alignment.BottomCenter),
                text = if (state.isEditMode) stringResource(R.string.SaveChanges) else stringResource(R.string.CreateNewWorkExperience),
                enabled = state.createEnabled,
                onClick = {
                    onIntent(Intent.OnCreateExperience)
                }
            )
        }
    }

    if (showStartDatePicker) {
        DivoDatePickerDialog(
            state = startDatePickerState,
            onDismiss = { showStartDatePicker = false },
            onConfirm = { millis ->
                val date = Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .toString()
                onIntent(Intent.OnStartDateSelected(date, millis))
            }
        )
    }

    if (showEndDatePicker) {
        DivoDatePickerDialog(
            state = endDatePickerState,
            onDismiss = { showEndDatePicker = false },
            onConfirm = { millis ->
                val date = Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .toString()
                onIntent(Intent.OnEndDateSelected(date, millis))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DivoDatePickerDialog(
    state: DatePickerState,
    onDismiss: () -> Unit,
    onConfirm: (millis: Long) -> Unit,
) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = AppTheme.colors.accentOrange,
            onPrimary = Color.White,
            primaryContainer = AppTheme.colors.accentOrange,
            onPrimaryContainer = Color.White,
            surface = AppTheme.colors.backgroundLight,
            onSurface = Color(0xFF3C3C43),
        )
    ) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onConfirm(it) }
                    onDismiss()
                }) { Text(stringResource(R.string.ButtonOk)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.ButtonCancel)) }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun DateField(
    modifier: Modifier = Modifier,
    date: String,
    title: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Column(modifier) {
        Title(text = title, textColor = Color(0xFF3C3C43))
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            DivoTextField(
                value = date,
                horizontalContentPadding = 16.dp,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = if (enabled) Color(0x993C3C43) else Color(0x403C3C43)
                ),
                readOnly = true,
                trailingIcon = Icons.Default.DateRange
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickableWithoutRipple(enabled = enabled) { onClick() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    title: String,
    actionText: String,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    createEnabled: Boolean
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            BackButton(
                modifier = Modifier
                    .padding(start = 8.dp, bottom = 7.dp),
                color = AppTheme.colors.accentOrange,
                onBackClicked = onBack
            )
        },
        actions = {
            Text(
                text = actionText,
                color = if (createEnabled) Color(0xFFBF825E) else Color(0xFFB9B9B9),
                modifier = Modifier
                    .clickableWithoutRipple { if (createEnabled) onCreate() }
                    .padding(end = 8.dp, bottom = 7.dp),
                fontSize = 17.sp,
                style = AppTheme.typography.manropeRegular
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

@Composable
private fun Title(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color = Color.Black,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Text(
        modifier = modifier.fillMaxWidth(),
        text = text,
        fontWeight = fontWeight,
        style = AppTheme.typography.helveticaNeueRegular,
        color = textColor,
        fontSize = 16.sp,
        textAlign = TextAlign.Start
    )
}