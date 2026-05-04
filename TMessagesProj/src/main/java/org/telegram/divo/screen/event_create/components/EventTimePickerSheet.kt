package org.telegram.divo.screen.event_create.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.telegram.divo.components.items.DivoBottomSheet
import org.telegram.divo.components.items.DivoWheelPicker
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTimePickerSheet(
    initialTime: String = "",
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit,
) {
    val (initHour, initMinute, initPeriod) = remember(initialTime) {
        parseTo12h(initialTime)
    }

    val hoursList   = (1..12).map { it.toString().padStart(2, '0') }
    val minutesList = (0..59).map { it.toString().padStart(2, '0') }
    val periodList  = listOf("AM", "PM")

    var selectedHour   by remember { mutableStateOf(initHour) }
    var selectedMinute by remember { mutableStateOf(initMinute) }
    var selectedPeriod by remember { mutableStateOf(initPeriod) }

    val itemHeight = 40.dp

    DivoBottomSheet(
        title = stringResource(R.string.EventTime),
        iconClose = R.drawable.ic_divo_back,
        contentPadding = PaddingValues(horizontal = 16.dp),
        onDismiss = onDismiss,
        onSave = {
            onTimeSelected("$selectedHour:$selectedMinute $selectedPeriod")
        }
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.onBackground)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(41.dp))
                        .background(Color(0xFFEBEBEB))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    DivoWheelPicker(
                        modifier = Modifier.weight(1f),
                        items = hoursList,
                        initialIndex = hoursList.indexOf(selectedHour).coerceAtLeast(0),
                        itemHeight = itemHeight,
                        isCyclic = true,
                        onItemSelected = { _, item -> selectedHour = item }
                    )

                    DivoWheelPicker(
                        modifier = Modifier.weight(1f),
                        items = minutesList,
                        initialIndex = minutesList.indexOf(selectedMinute).coerceAtLeast(0),
                        itemHeight = itemHeight,
                        isCyclic = true,
                        onItemSelected = { _, item -> selectedMinute = item }
                    )

                    DivoWheelPicker(
                        modifier = Modifier.weight(1f),
                        items = periodList,
                        initialIndex = periodList.indexOf(selectedPeriod).coerceAtLeast(0),
                        itemHeight = itemHeight,
                        isCyclic = false,
                        onItemSelected = { _, item -> selectedPeriod = item }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Converts a time string to (hour: String, minute: String, period: String).
 *
 * Handles:
 *   - 24h server format "HH:mm:ss" → e.g. "22:30:00" → ("10", "30", "PM")
 *   - 12h picker format "h:mm AM"  → e.g. "10:30 AM" → ("10", "30", "AM")
 *   - Empty / invalid              → ("8", "0", "AM")
 */
private fun parseTo12h(raw: String): Triple<String, String, String> {
    val default = Triple("08", "00", "AM")
    if (raw.isBlank()) return default


    return if (raw.contains("AM", ignoreCase = true) || raw.contains("PM", ignoreCase = true)) {
        // Already 12h format: "hh:mm AM"
        val period = if (raw.contains("PM", ignoreCase = true)) "PM" else "AM"
        val timePart = raw.replace("AM", "", ignoreCase = true)
                          .replace("PM", "", ignoreCase = true)
                          .trim()
        val parts = timePart.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: return default
        val m = parts.getOrNull(1)?.toIntOrNull() ?: return default
        Triple(h.toString().padStart(2, '0'), m.toString().padStart(2, '0'), period)
    } else {
        // 24h format: "HH:mm" or "HH:mm:ss"
        val parts = raw.trim().split(":")
        val h24 = parts.getOrNull(0)?.toIntOrNull() ?: return default
        val m   = parts.getOrNull(1)?.toIntOrNull() ?: return default
        val period = if (h24 < 12) "AM" else "PM"
        val h12 = when (h24) {
            0    -> 12
            in 1..12 -> h24
            else -> h24 - 12
        }
        Triple(h12.toString().padStart(2, '0'), m.toString().padStart(2, '0'), period)
    }
}
