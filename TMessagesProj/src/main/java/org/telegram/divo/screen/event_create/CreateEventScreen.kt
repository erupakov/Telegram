package org.telegram.divo.screen.event_create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel


@Preview
@Composable
fun CreateEventScreen(
        viewModel:CreateEventViewModel = viewModel(),
        onBack: () -> Unit = {},
        onPickCover: () -> Unit = {},
        onPickGallery: () -> Unit = {},
        onPickDate: () -> Unit = {},
        onPickTime: () -> Unit = {},
) {
    var ui by remember { mutableStateOf(CreateEventUiState()) }
    var showParamSheet by remember { mutableStateOf(false) }

    Scaffold(
            modifier = Modifier.padding(top = 32.dp),
            topBar = {
                TopBar(
                        onBack = onBack,
                        onCreate = {
                            viewModel.setIntent(CreateEventViewModel.Intent.OnDoneClicked)
                        },
                        enabled = true//.isValid()
                )
            }
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
            CoverPicker(
                    hasImage = ui.hasCover,
                    onPick = onPickCover,
                    onClear = { ui = ui.copy(hasCover = false) }
            )

            // --- Event info ---
            SectionHeader("Event info")
            OutlinedTextField(
                    value = ui.name,
                    onValueChange = { ui = ui.copy(name = it) },
                    label = { Text("Name event") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words
                    )
            )
            OutlinedTextField(
                    value = ui.about,
                    onValueChange = { ui = ui.copy(about = it) },
                    label = { Text("About event") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
            )

            // Event type (dropdown)
            DropDownField(
                    label = "Event type",
                    value = ui.eventType.ifBlank { "Choose event type" },
                    options = listOf("Casting", "Conference", "Show", "Private"),
                    onSelected = { ui = ui.copy(eventType = it) }
            )

            // Date & Time
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                        value = ui.date.ifBlank { "27 Jun 2025" },
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Event Date") },
                        trailingIcon = {
                            TextButton(onClick = onPickDate) { Text("Pick") }
                        },
                        modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                        value = ui.time.ifBlank { "00:00" },
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Event Time") },
                        trailingIcon = {
                            TextButton(onClick = onPickTime) { Text("Pick") }
                        },
                        modifier = Modifier.weight(1f)
                )
            }

            // Venue / Country
            DropDownField(
                    label = "Venue of the event",
                    value = ui.country.ifBlank { "Choose a country" },
                    options = sampleCountries,
                    onSelected = { ui = ui.copy(country = it) }
            )

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
                                containerColor = Color(0xFFBF825E),
                                contentColor = Color.White
                        )
                ) {
                    Text("+ Add parameters")
                }
                AnimatedVisibility(visible = ui.parameters.isNotEmpty()) {
                    AssistChip(
                            onClick = { showParamSheet = true },
                            label = { Text("${ui.parameters.size} selected") }
                    )
                }
            }

            // Gallery
            SectionHeader("Event Gallery")
            GalleryPickerBox(
                    items = ui.galleryCount,
                    onUpload = onPickGallery
            )

            // Create Button
            Button(
                    onClick = {

                    },
                    enabled = ui.isValid(),
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFBF825E),
                            contentColor = Color.White
                    )
            ) { Text("Create Event") }
            Spacer(Modifier.height(6.dp))
        }
    }

    if (showParamSheet) {
        ParametersSheet(
                initiallyChecked = ui.parameters.toSet(),
                onCancel = { showParamSheet = false },
                onSave = { selected ->
                    ui = ui.copy(parameters = selected.toList())
                    showParamSheet = false
                }
        )
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// UI PARTS
// ────────────────────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(
        onBack: () -> Unit,
        onCreate: () -> Unit,
        enabled: Boolean
) {
    Surface(tonalElevation = 0.dp, shadowElevation = 0.dp) {
        Row(
                Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .padding(horizontal = 12.dp)
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                    "Back",
                    color = Color(0xFFBF825E),
                    modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onBack() }
                            .padding(8.dp),
                    fontSize = 16.sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                    "CREATE EVENT",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(3f),
                    textAlign = TextAlign.Center
            )
            Spacer(Modifier.weight(1f))
            Text(
                    "Create",
                    color = if (enabled) Color(0xFFBF825E) else Color(0xFFB9B9B9),
                    modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled) { onCreate() }
                            .padding(8.dp),
                    fontSize = 16.sp
            )
        }
    }
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
        hasImage: Boolean,
        onPick: () -> Unit,
        onClear: () -> Unit
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

            Card




        }
    }
}

@Composable
private fun GalleryPickerBox(
        items: Int,
        onUpload: () -> Unit
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

@Composable
private fun DropDownField(
        label: String,
        value: String,
        options: List<String>,
        onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { expanded = true },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE4E4E4)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF9F9F9))
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(value, color = if (value.startsWith("Choose")) Color.Gray else Color.Black)
                Spacer(Modifier.weight(1f))
                Text("▼", color = Color.Gray)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(
                        text = { Text(it) },
                        onClick = { onSelected(it); expanded = false }
                )
            }
        }
    }
}

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
                "All Members", "Gender", "Age", "Height", "Waist",
                "Hips", "Shoe size", "Hair length", "Hair color"
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
                ListItem(
                        headlineContent = { Text(key) },
                        leadingContent = {
                            Checkbox(
                                    checked = value,
                                    onCheckedChange = {
                                        if (isAllToggle) {
                                            checked = if (!value) allParams.toSet() else emptySet()
                                        } else {
                                            checked = checked.toMutableSet()
                                                    .apply { if (value) remove(key) else add(key) }
                                        }
                                    }
                            )
                        }
                )
                Divider()
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

data class CreateEventUiState(
        val hasCover: Boolean = false,
        val name: String = "",
        val about: String = "",
        val eventType: String = "",
        val date: String = "",
        val time: String = "",
        val country: String = "",
        val parameters: List<String> = emptyList(),
        val galleryCount: Int = 0,
) {
    fun isValid(): Boolean =
            name.isNotBlank() && eventType.isNotBlank() && country.isNotBlank()
}

private val sampleCountries = listOf(
        "United States", "United Kingdom", "Italy", "France", "Germany",
        "Spain", "United Arab Emirates", "Armenia", "Ukraine", "Japan"
)

@Stable
fun SolidDashed(color: Color, intervals: FloatArray): androidx.compose.ui.graphics.SolidColor {
    return androidx.compose.ui.graphics.SolidColor(color).also { _ ->
    }
}
