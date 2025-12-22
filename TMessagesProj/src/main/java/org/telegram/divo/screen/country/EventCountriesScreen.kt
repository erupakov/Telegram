package org.telegram.divo.screen.country

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.tgnet.TLRPC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCountriesScreen(
    viewModel: EventCountriesViewModel = viewModel(),
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onDone: (TLRPC.TL_event_country) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color.White,
        tonalElevation = 0.dp

        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { viewModel.setIntent(EventCountriesViewModel.Intent.OnQueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true,
                placeholder = { Text("Search country") }
            )

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                state.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.errorMessage!!, color = Color.Red)
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(state.filtered, key = { it.country_id }) { c ->
                        CountryRow(
                            name = c.country ?: "",
                            selected = state.selectedCountryId == c.country_id,
                            onClick = {
                                onDone(c)
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun CountryRow(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(12.dp))
        Text(text = name)
    }
}
