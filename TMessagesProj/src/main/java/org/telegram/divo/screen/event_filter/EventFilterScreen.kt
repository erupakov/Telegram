package org.telegram.divo.screen.event_filter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.telegram.tgnet.TLRPC
import java.time.Instant
import java.time.ZoneId

@Composable
fun EventFilterScreen(
    viewModel: EventFilterViewModel = viewModel(),
    onBack: () -> Unit = {},
    onApply: () -> Unit = {},
    onBacK: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.effect.collect { eff ->
            when (eff) {
                EventFilterViewModel.Effect.NavigateBack -> onBack()
                EventFilterViewModel.Effect.Apply -> {}
            }
        }
    }

    val state = viewModel.state.collectAsState().value
    EventFilterScreenView(state = state, onIntent = viewModel::setIntent, onApply = onApply, onBack = onBacK)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFilterScreenView(
    state: EventFilterViewModel.State,
    onIntent: (EventFilterViewModel.Intent) -> Unit,
    onApply: () -> Unit,
    onBack: () -> Unit
) {
    var showCountrySheet by remember { mutableStateOf(false) }
    var showTypeSheet by remember { mutableStateOf(false) }
    var showMemberSheet by remember { mutableStateOf(false) }

    var showDateFromPicker by remember { mutableStateOf(false) }
    var showDateToPicker by remember { mutableStateOf(false) }

    val dateFromPickerState = rememberDatePickerState()
    val dateToPickerState = rememberDatePickerState()

    var search by rememberSaveable { mutableStateOf(state.searchQuery) }
    LaunchedEffect(state.searchQuery) {
        if (search != state.searchQuery) search = state.searchQuery
    }

    Scaffold(
        modifier = Modifier.padding(top = 32.dp),
        topBar = {
            FilterTopBar(
                onBack = { onBack() },
                onClear = { onIntent(EventFilterViewModel.Intent.OnClearClicked) }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                    onIntent(EventFilterViewModel.Intent.OnSearchChanged(it))
                },
                placeholder = { Text("Search by event name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))
            Text("Filter by:", color = Color(0xFF8A8A8A), fontSize = 13.sp)

            Text("Location", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            FilterDropdownField(
                value = state.selectedCountry?.country ?: "",
                placeholder = "Choose a country",
                onClick = { showCountrySheet = true }
            )

            Text("Event Type", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            FilterDropdownField(
                value = state.selectedEventType?.title ?: "",
                placeholder = "All Types",
                onClick = { showTypeSheet = true }
            )

            Text("Member Type", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            FilterDropdownField(
                value = state.memberSummary(),
                placeholder = "All Members",
                onClick = { showMemberSheet = true }
            )

            Text("Date Range", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)

            DateRow(
                label = "From",
                value = state.dateFrom ?: "Today",
                onClick = { showDateFromPicker = true }
            )
            DateRow(
                label = "To",
                value = state.dateTo ?: "",
                onClick = { showDateToPicker = true }
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    onIntent(EventFilterViewModel.Intent.OnApplyClicked)
                    onApply()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFBF825E),
                    contentColor = Color.White
                )
            ) {
                Text("Apply filter", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    // --- Sheets ---
    if (showCountrySheet) {
        CountryPickerSheet(
            list = state.countries,
            selected = state.selectedCountry,
            onDismiss = { showCountrySheet = false },
            onPick = {
                onIntent(EventFilterViewModel.Intent.OnCountrySelected(it))
                showCountrySheet = false
            }
        )
    }

    if (showTypeSheet) {
        EventTypePickerSheet(
            list = state.eventTypes,
            selected = state.selectedEventType,
            onDismiss = { showTypeSheet = false },
            onPick = {
                onIntent(EventFilterViewModel.Intent.OnEventTypeSelected(it))
                showTypeSheet = false
            },
            onPickAll = {
                onIntent(EventFilterViewModel.Intent.OnEventTypeSelected(null))
                showTypeSheet = false
            }
        )
    }

    if (showMemberSheet) {
        MemberTypeMultiSelectSheet(
            state = state,
            onDismiss = { showMemberSheet = false },
            onToggleAll = { onIntent(EventFilterViewModel.Intent.ToggleAllMembers(it)) },
            onToggleModel = { onIntent(EventFilterViewModel.Intent.ToggleMemberModel(it)) },
            onToggleNewTalents = { onIntent(EventFilterViewModel.Intent.ToggleMemberNewTalents(it)) },
            onToggleBooker = { onIntent(EventFilterViewModel.Intent.ToggleMemberBooker(it)) },
            onToggleScout = { onIntent(EventFilterViewModel.Intent.ToggleMemberScout(it)) },
        )
    }

    // --- Date pickers ---
    if (showDateFromPicker) {
        DatePickerDialog(
            onDismissRequest = { showDateFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = dateFromPickerState.selectedDateMillis
                    if (millis != null) {
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onIntent(EventFilterViewModel.Intent.OnDateFromChanged(localDate.toString()))
                    }
                    showDateFromPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDateFromPicker = false
                }) { Text("Cancel") }
            }
        ) { DatePicker(state = dateFromPickerState) }
    }

    if (showDateToPicker) {
        DatePickerDialog(
            onDismissRequest = { showDateToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = dateToPickerState.selectedDateMillis
                    if (millis != null) {
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onIntent(EventFilterViewModel.Intent.OnDateToChanged(localDate.toString()))
                    }
                    showDateToPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDateToPicker = false
                }) { Text("Cancel") }
            }
        ) { DatePicker(state = dateToPickerState) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterTopBar(
    onBack: () -> Unit,
    onClear: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = "FILTER",
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
                fontSize = 16.sp
            )
        },
        actions = {
            Text(
                "Clear",
                color = Color(0xFFBF825E),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClear() }
                    .padding(8.dp),
                fontSize = 16.sp
            )
        }
    )
}

@Composable
private fun FilterDropdownField(
    value: String,
    placeholder: String,
    onClick: () -> Unit
) {
    Box {
        OutlinedTextField(
            value = if (value.isBlank()) placeholder else value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFBF825E),
                unfocusedBorderColor = Color(0xFFE4E4E4),
            )
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(
                    onClick = onClick,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        )
    }
}

@Composable
private fun DateRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 18.sp)
        Spacer(Modifier.weight(1f))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
    HorizontalDivider(color = Color(0xFFEDEDED))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryPickerSheet(
    list: List<TLRPC.TL_event_country>,
    selected: TLRPC.TL_event_country?,
    onDismiss: () -> Unit,
    onPick: (TLRPC.TL_event_country) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        modifier = Modifier.padding(top = 32.dp),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        tonalElevation = 16.dp
    ) {
        Column(Modifier.fillMaxSize()) {
            Text(
                "Choose a country",
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.SemiBold
            )
            LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(list) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { sheetState.hide(); onPick(item) }
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = item.country_id == selected?.country_id,
                            onClick = null
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(item.country ?: "")
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventTypePickerSheet(
    list: List<TLRPC.TL_event_eventType>,
    selected: TLRPC.TL_event_eventType?,
    onDismiss: () -> Unit,
    onPick: (TLRPC.TL_event_eventType) -> Unit,
    onPickAll: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        modifier = Modifier.padding(top = 32.dp),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        tonalElevation = 16.dp
    ) {
        Column(Modifier.fillMaxSize()) {
            Text("Event type", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.SemiBold)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { scope.launch { sheetState.hide(); onPickAll() } }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selected == null, onClick = null)
                Spacer(Modifier.width(10.dp))
                Text("All Types")
            }
            HorizontalDivider()

            LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(list) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { scope.launch { sheetState.hide(); onPick(item) } }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = item.type_id == selected?.type_id, onClick = null)
                        Spacer(Modifier.width(10.dp))
                        Text(item.title ?: "")
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberTypeMultiSelectSheet(
    state: EventFilterViewModel.State,
    onDismiss: () -> Unit,
    onToggleAll: (Boolean) -> Unit,
    onToggleModel: (Boolean) -> Unit,
    onToggleNewTalents: (Boolean) -> Unit,
    onToggleBooker: (Boolean) -> Unit,
    onToggleScout: (Boolean) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        modifier = Modifier.padding(top = 32.dp),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        tonalElevation = 16.dp
    ) {
        Column(Modifier.fillMaxSize()) {
            Text(
                "Member type",
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.SemiBold
            )

            MemberCheckRow(
                title = "All Members",
                checked = state.memberAll,
                onCheckedChange = onToggleAll
            )
            HorizontalDivider()

            MemberCheckRow(
                title = "Model",
                checked = state.memberModel,
                onCheckedChange = onToggleModel
            )
            HorizontalDivider()

            MemberCheckRow(
                title = "New Talents",
                checked = state.memberNewTalents,
                onCheckedChange = onToggleNewTalents
            )
            HorizontalDivider()

            MemberCheckRow(
                title = "Booker",
                checked = state.memberBooker,
                onCheckedChange = onToggleBooker
            )
            HorizontalDivider()

            MemberCheckRow(
                title = "Scout",
                checked = state.memberScout,
                onCheckedChange = onToggleScout
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MemberCheckRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(Modifier.width(10.dp))
        Text(title, fontSize = 16.sp)
    }
}
