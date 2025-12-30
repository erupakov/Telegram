package org.telegram.divo.screen.event_create

import org.telegram.messenger.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.telegram.tgnet.TLRPC
import java.time.Instant
import java.time.ZoneId


@Composable
fun CreateEventScreen(
    viewModel: CreateEventViewModel = viewModel(),
    onBack: () -> Unit = {},
    onPickCover: () -> Unit = {},
    onPickGallery: () -> Unit = {},
    onPickDate: () -> Unit = {},
    onPickTime: () -> Unit = {},
) {

    LaunchedEffect(true) {
        viewModel.getEventTypes()
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect {
            when (it) {
                CreateEventViewModel.Effect.NavigateBack -> {
                    // onBack()
                }

                else -> {}
            }

        }
    }
    val state = viewModel.state.collectAsState().value
    CreateEventScreenView(
        state = state,
        onIntent = {
            viewModel.setIntent(it)
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CreateEventScreenView(
    state: CreateEventViewModel.State = CreateEventViewModel.State(),
    onIntent: (CreateEventViewModel.Intent) -> Unit = {},
) {
    var showParamSheet by remember { mutableStateOf(false) }
    var showCountrySheet by remember { mutableStateOf(false) }
    var showCitySheet by remember { mutableStateOf(false) }

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var dateForView by remember { mutableStateOf("") }
    var timeForView by remember { mutableStateOf("") }

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
                    onIntent(CreateEventViewModel.Intent.OnBackClicked)
                }, onCreate = {
                    onIntent(
                        CreateEventViewModel.Intent.OnCreateClicked(
                            title = title,
                            description = description,
                            date = dateForView,
                            time = timeForView
                        )
                    )
                },
                createEnabled = true//.isValid()
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
            // Cover
//            CoverPicker(
//                hasImage = ui.hasCover,
//                onPick = onPickCover,
//                onClear = { ui = ui.copy(hasCover = false) }
//            )

            // --- Event info ---
            SectionHeader("Event info")

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                },
                label = { Text("Name event") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                )
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("About event") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            // Event type (dropdown)
//            DropDownField(
//                label = "Event type",
//                value = "Choose event type",
//                options = listOf("Casting", "Conference", "Show", "Private"),
//                onSelected = {
//
//                })

            // Date & Time
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = dateForView,
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Event Date") },
                    trailingIcon = {
                        TextButton(onClick = {
                            showDatePicker = true
                        }) { Text("Pick") }
                    },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = timeForView,
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Event Time") },
                    trailingIcon = {
                        TextButton(onClick = {
                            showTimePicker = true
                        }) { Text("Pick") }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            CountryField(
                value = state.selectedCountry?.country ?: "",
                onClick = {
                    showCountrySheet = true
                }
            )

            AnimatedVisibility(state.selectedCountry != null) {
                CityField(
                    value = state.selectedCity?.city ?: "",
                    onClick = {
                        showCitySheet = true
                    }
                )
            }

            // Venue / Country
//            DropDownField(
//                label = "Venue of the event",
//                value = ui.country.ifBlank { "Choose a country" },
//                options = sampleCountries,
//                onSelected = { ui = ui.copy(country = it) }
//            )

            // Parameters
            SectionHeader("Parameters for Applying")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ElevatedButton(
                    onClick = { showParamSheet = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color(0xFFBF825E), contentColor = Color.White
                    )
                ) {
                    Text("+ Add parameters")
                }
//                AnimatedVisibility(visible = ui.parameters.isNotEmpty()) {
//                    AssistChip(
//                        onClick = { showParamSheet = true },
//                        label = { Text("${ui.parameters.size} selected") }
//                    )
//                }
            }

            // Gallery
            SectionHeader("Event Gallery")
            GalleryPickerBox(
                items = 0, onUpload = {

                }
            )
            // Create Button
            Button(
                onClick = {
                    onIntent(
                        CreateEventViewModel.Intent.OnCreateClicked(
                            title = title,
                            description = description,
                            date = dateForView,
                            time = timeForView
                        )
                    )
                },
                //enabled = ui.isValid(),
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

    if (showDatePicker) {
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val localDate = Instant
                            .ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        dateForView = localDate.toString()

                        //ui = ui.copy(date = localDate.format(dateFormatter))
                    }
                    showDatePicker = false
                }) { Text("OK") }
        }, dismissButton = {
            TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
        }) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(onDismissRequest = { showTimePicker = false }, confirmButton = {
            TextButton(
                onClick = {
                    val hh = timePickerState.hour
                    val mm = timePickerState.minute
                    //ui = ui.copy(time = "%02d:%02d".format(hh, mm))
                    showTimePicker = false
                    // ✅ HH:MM:SS (секунды = 00)
                    timeForView = "%02d:%02d:00".format(hh, mm)
                    showTimePicker = false

                }) { Text("OK") }
        }, dismissButton = {
            TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
        }, title = { Text("Pick time") }, text = {
            TimePicker(state = timePickerState)
        })
    }

    if (showCountrySheet) {
        CountryModalSheet(
            countryList = state.countries,
            selectedCountry = state.selectedCountry,
            onDismiss = {
                showCountrySheet = false
            },
            onDone = {
                onIntent(CreateEventViewModel.Intent.OnCountrySelected(it))
                showCountrySheet = false
            }
        )
    }
    if (showCitySheet) {
        CityModalSheet(
            countryList = state.cities,
            selectedCountry = state.selectedCity,
            onDismiss = {
                showCountrySheet = false
            },
            onDone = {
                onIntent(CreateEventViewModel.Intent.OnCitySelected(it))
                showCountrySheet = false
            }
        )
    }


    if (showParamSheet) {
//        ParametersSheet(
//            initiallyChecked = ui.parameters.toSet(),
//            onCancel = { showParamSheet = false },
//            onSave = { selected ->
//                ui = ui.copy(parameters = selected.toList())
//                showParamSheet = false
//            }
//        )
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// UI PARTS
// ────────────────────────────────────────────────────────────────────────────────

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
                text = "CREATE EVENT",
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

//@Composable
//private fun DropDownField(
//    label: String, value: String, options: List<String>, onSelected: (String) -> Unit
//) {
//    var expanded by remember { mutableStateOf(false) }
//    Column {
//        Text(
//            label, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp)
//        )
//
//        OutlinedButton(
//            modifier = Modifier.fillMaxWidth(),
//            onClick = { expanded = true },
//            shape = RoundedCornerShape(12.dp),
//            border = BorderStroke(1.dp, Color(0xFFE4E4E4)),
//            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF9F9F9))
//        ) {
//            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//                Text(value, color = if (value.startsWith("Choose")) Color.Gray else Color.Black)
//                Spacer(Modifier.weight(1f))
//                Text("▼", color = Color.Gray)
//            }
//        }
//        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
//            options.forEach {
//                DropdownMenuItem(
//                    text = { Text(it) },
//                    onClick = { onSelected(it); expanded = false })
//            }
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParametersSheet(
    initiallyChecked: Set<String>,
    onCancel: () -> Unit,
    onSave: (Set<String>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val allParams = remember {
        listOf(
            "All Members",
            "Gender",
            "Age",
            "Height",
            "Waist",
            "Hips",
            "Shoe size",
            "Hair length",
            "Hair color"
        )
    }
    var checked by remember { mutableStateOf(initiallyChecked) }

    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        // Header
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            Text(
                "PARAMETERS",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = { onSave(checked) }) { Text("Save") }
        }

        Text(
            "Choose Parameters for Applying",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            fontSize = 13.sp,
            color = Color(0xFF6F6F6F)
        )

        Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            allParams.forEach { key ->
                val isAllToggle = key == "All Members"
                val value = key in checked
                ListItem(headlineContent = { Text(key) }, leadingContent = {
                    Checkbox(
                        checked = value, onCheckedChange = {
                            if (isAllToggle) {
                                checked = if (!value) allParams.toSet() else emptySet()
                            } else {
                                checked = checked.toMutableSet()
                                    .apply { if (value) remove(key) else add(key) }
                            }
                        })
                })
                Divider()
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryModalSheet(
    countryList: List<TLRPC.TL_event_country>,
    selectedCountry: TLRPC.TL_event_country? = null,
    onDismiss: () -> Unit,
    onDone: (TLRPC.TL_event_country) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        modifier = Modifier.padding(top = 32.dp),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color.White,
        tonalElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(countryList) { item ->
                    CountryItem(
                        name = item.country ?: "",
                        selected = item.country_id == selectedCountry?.country_id,
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                onDone(item)
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityModalSheet(
    countryList: List<TLRPC.TL_event_city>,
    selectedCountry: TLRPC.TL_event_city? = null,
    onDismiss: () -> Unit,
    onDone: (TLRPC.TL_event_city) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        modifier = Modifier.padding(top = 32.dp),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color.White,
        tonalElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(countryList) { item ->
                    CountryItem(
                        name = item.city ?: "",
                        selected = item.city_id == selectedCountry?.city_id,
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                onDone(item)
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun CountryItem(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(12.dp))
        Text(text = name)
    }
}


@Stable
fun SolidDashed(color: Color, intervals: FloatArray): androidx.compose.ui.graphics.SolidColor {
    return androidx.compose.ui.graphics.SolidColor(color).also { _ ->
    }
}
