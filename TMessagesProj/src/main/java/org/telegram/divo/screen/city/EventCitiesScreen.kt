package org.telegram.divo.screen.city

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
fun EventCitiesScreen(
    viewModel: EventCitiesViewModel = viewModel(),
    onBack: () -> Unit,
    onDone: (TLRPC.TL_event_city) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setIntent(EventCitiesViewModel.Intent.Load)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { action ->
            when (action) {
                EventCitiesViewModel.Effect.Back -> onBack()
                is EventCitiesViewModel.Effect.Done -> onDone(action.city)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select city") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.setIntent(EventCitiesViewModel.Intent.OnBackClicked)
                    }) {
                        Text("←")
                    }
                },
                actions = {
                    TextButton(
                        enabled = state.selectedCityId != null,
                        onClick = {
                            viewModel.setIntent(EventCitiesViewModel.Intent.OnDoneClicked)
                        }
                    ) {
                        Text("Done")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = {
                    viewModel.setIntent(
                        EventCitiesViewModel.Intent.OnQueryChanged(it)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true,
                placeholder = { Text("Search city") }
            )

            when {
                state.isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                state.errorMessage != null -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.errorMessage!!, color = Color.Red)
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(state.filtered, key = { it.city_id }) { city ->
                        CityRow(
                            name = city.city ?: "",
                            selected = state.selectedCityId == city.city_id,
                            onClick = {
                                viewModel.setIntent(
                                    EventCitiesViewModel.Intent.OnCityClicked(city)
                                )
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
private fun CityRow(
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
