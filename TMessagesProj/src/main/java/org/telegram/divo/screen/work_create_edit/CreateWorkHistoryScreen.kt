package org.telegram.divo.screen.work_create_edit

import org.telegram.messenger.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.tgnet.TLRPC
import java.time.Instant
import java.time.ZoneId


@Composable
fun CreateWorkHistoryScreen(
    viewModel: CreateWorkHistoryViewModel = viewModel(),
    onBack: () -> Unit = {},
    onPickCover: () -> Unit = {},
    onPickGallery: () -> Unit = {},
    onPickDate: () -> Unit = {},
    onPickTime: () -> Unit = {},
) {

    LaunchedEffect(true) {
        //viewModel.getEventTypes()
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect {
            when (it) {
                CreateWorkHistoryViewModel.Effect.NavigateBack -> {
                    onBack()
                }

                else -> {}
            }

        }
    }
    val state = viewModel.state.collectAsState().value
    CreateWorkHistoryScreenView(
        state = state,
        onIntent = {
            viewModel.setIntent(it)
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CreateWorkHistoryScreenView(
    state: CreateWorkHistoryViewModel.State = CreateWorkHistoryViewModel.State(),
    onIntent: (CreateWorkHistoryViewModel.Intent) -> Unit = {},
) {
    var showParamSheet by remember { mutableStateOf(false) }
    var showCountrySheet by remember { mutableStateOf(false) }
    var showCitySheet by remember { mutableStateOf(false) }

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    val datePickerState = rememberDatePickerState()
    // Time picker state
    val timePickerState = rememberTimePickerState(
        initialHour = 0, initialMinute = 0, is24Hour = true
    )

    Scaffold(
        modifier = Modifier.padding(top = 32.dp),
        topBar = {
            TopBar(
                onBack = {
                    onIntent(CreateWorkHistoryViewModel.Intent.OnBackClicked)
                }, onCreate = {
                    onIntent(
                        CreateWorkHistoryViewModel.Intent.OnCreateExperience
                    )
                },
                createEnabled = state.createEnabled
            )
        },


        ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            SectionHeader("Work experience info")

            AgencyAutoCompleteField(
                query = state.query,
                agencies = state.agencies,
                selectedAgency = state.selectedAgency,
                onQueryChange = { onIntent(CreateWorkHistoryViewModel.Intent.OnQueryChanged(it)) },
                onAgencySelected = { agency ->
                    onIntent(CreateWorkHistoryViewModel.Intent.OnAgencySelected(agency))
                }
            )




            // Date & Time
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Start Date") },
                    trailingIcon = {
                        TextButton(onClick = {
                            showStartDatePicker = true
                        }) { Text("Pick") }
                    },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = endDate,
                    onValueChange = {},
                    enabled = false,
                    label = { Text("End Date") },
                    trailingIcon = {
                        TextButton(onClick = {
                            showEndDatePicker = true
                        }) { Text("Pick") }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically,) {

                Checkbox(checked = state.isCurrent, onCheckedChange = { checked ->
                    onIntent(CreateWorkHistoryViewModel.Intent.OnCurrentChanged(isCurrent = checked))
                })
                Text("I am currently working in this role", modifier = Modifier.padding(8.dp).clickable(onClick = {
                    onIntent(CreateWorkHistoryViewModel.Intent.OnCurrentChanged(isCurrent = !state.isCurrent))
                }))
            }


            // Create Button
            Button(
                onClick = {
                    onIntent(
                        CreateWorkHistoryViewModel.Intent.OnCreateExperience
                    )
                },
                enabled = state.createEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFBF825E), contentColor = Color.White
                )
            ) { Text("Create Event") }
            Spacer(Modifier.height(6.dp))
        }
    }

    if (showStartDatePicker) {
        DatePickerDialog(onDismissRequest = { showStartDatePicker = false }, confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val localDate = Instant
                            .ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        startDate = localDate.toString()
                        onIntent(
                            CreateWorkHistoryViewModel.Intent.OnStartDateSelected(startDate, millis)
                        )
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
        }, dismissButton = {
            TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
        }) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(onDismissRequest = { showEndDatePicker = false }, confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val localDate = Instant
                            .ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        endDate = localDate.toString()
                        onIntent(
                            CreateWorkHistoryViewModel.Intent.OnEndDateSelected(startDate, millis)
                        )
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
        }, dismissButton = {
            TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
        }) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyAutoCompleteField(
    query: String,
    agencies: List<TLRPC.TL_profile_agency>,
    selectedAgency: TLRPC.TL_profile_agency?,
    onQueryChange: (String) -> Unit,
    onAgencySelected: (TLRPC.TL_profile_agency) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var queryText by remember { mutableStateOf("") }
    val showSuggestions = query.length >= 2 && agencies.isNotEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded && showSuggestions,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = queryText,
            onValueChange = {
                queryText = it
                onQueryChange(queryText)
                expanded = true
            },
            label = { Text("Agency Name") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded && showSuggestions,
            onDismissRequest = { expanded = false }
        ) {
            agencies.forEach { agency ->
                val title = agency.name ?: "Agency" // depends on your TL schema
                DropdownMenuItem(
                    text = { Text(title) },
                    onClick = {
                        onAgencySelected(agency)
                        expanded = false
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onBack: () -> Unit,
    onCreate: () -> Unit,
    createEnabled: Boolean
) {
    TopAppBar(
        title = {
            Text(
                text = "CREATE EXPERIENCE",
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
            Text(
                "Create",
                color = if (createEnabled) Color(0xFFBF825E) else Color(0xFFB9B9B9),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(createEnabled) { onCreate() }
                    .padding(8.dp),
                fontSize = 16.sp)
        }
    )
}


@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
private fun CoverPicker(
    hasImage: Boolean, onPick: () -> Unit, onClear: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF7F7F7),
        border = BorderStroke(
            1.dp, SolidDashed(color = Color(0xFFE2E2E2), intervals = floatArrayOf(10f, 12f))
        )
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .clickable { if (!hasImage) onPick() },
            contentAlignment = Alignment.Center
        ) {

        }
    }
}

@Composable
private fun GalleryPickerBox(
    items: Int, onUpload: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF7F7F7),
        border = BorderStroke(
            1.dp, SolidDashed(color = Color(0xFFE2E2E2), intervals = floatArrayOf(10f, 12f))
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("+", fontSize = 28.sp, color = Color(0xFF999999))
            Text("Upload photo", color = Color(0xFF999999))
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onUpload, shape = RoundedCornerShape(10.dp)) {
                Text("Add to gallery ($items)")
            }
        }
    }
}

@Preview
@Composable
private fun CountryField(
    label: String = "Country",
    value: String = "",
    placeholder: String = "Choose Country",
    onClick: () -> Unit = {}
) {
    Box {
        OutlinedTextField(
            value = value.ifBlank { placeholder },
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    painterResource(R.drawable.ic_arrow_drop_down),
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledBorderColor = Color(0xFFE4E4E4),
                focusedBorderColor = Color(0xFFBF825E),
                unfocusedBorderColor = Color(0xFFE4E4E4),
            )
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(
                    onClick = {
                        onClick()
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                )
        )
    }
}

@Preview
@Composable
private fun CityField(
    label: String = "City",
    value: String = "",
    placeholder: String = "Choose City",
    onClick: () -> Unit = {}
) {
    Box {
        OutlinedTextField(
            value = value.ifBlank { placeholder },
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    painterResource(R.drawable.ic_arrow_drop_down),
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledBorderColor = Color(0xFFE4E4E4),
                focusedBorderColor = Color(0xFFBF825E),
                unfocusedBorderColor = Color(0xFFE4E4E4),
            )
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(
                    onClick = {
                        onClick()
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                )
        )
    }
}

@Composable
private fun DropDownField(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    placeholder: String = "Choose"
) {
    var expanded by remember { mutableStateOf(false) }

    Column {

        OutlinedTextField(
            value = value.ifBlank { placeholder },
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    painterResource(R.drawable.ic_arrow_drop_down),
                    contentDescription = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledBorderColor = Color(0xFFE4E4E4),
                focusedBorderColor = Color(0xFFBF825E),
                unfocusedBorderColor = Color(0xFFE4E4E4),
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Stable
fun SolidDashed(color: Color, intervals: FloatArray): SolidColor {
    return SolidColor(color).also { _ ->
    }
}
