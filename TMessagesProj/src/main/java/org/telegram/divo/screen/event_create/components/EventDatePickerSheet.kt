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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import org.telegram.divo.dal.network.DivoLanguageManager
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R
import java.time.Month
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDatePickerSheet(
    initialDate: String = "",
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit,
) {
    val now = Calendar.getInstance()
    val todayYear  = now.get(Calendar.YEAR)
    val todayMonth = now.get(Calendar.MONTH)
    val todayDay   = now.get(Calendar.DAY_OF_MONTH)

    val allMonths = (1..12).map { Month.of(it).getDisplayName(TextStyle.FULL, DivoLanguageManager.getSystemLocale()) }
    val yearsList = (todayYear..todayYear + 10).map { it.toString() }

    val parts = if (initialDate.isNotBlank()) initialDate.split(" ") else emptyList()

    var selectedMonthIndex by remember {
        val parsed = parts.getOrNull(1)?.let { name ->
            allMonths.indexOfFirst { it.equals(name, ignoreCase = true) }
        }?.takeIf { it >= 0 } ?: todayMonth
        mutableIntStateOf(parsed)
    }

    var selectedYearIndex by remember {
        val y = parts.getOrNull(2)?.toIntOrNull() ?: todayYear
        mutableIntStateOf(yearsList.indexOf(y.toString()).coerceAtLeast(0))
    }

    val daysInMonth by remember(selectedMonthIndex, selectedYearIndex) {
        derivedStateOf {
            Calendar.getInstance().apply {
                set(Calendar.YEAR, yearsList[selectedYearIndex].toInt())
                set(Calendar.MONTH, selectedMonthIndex)
            }.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
    }

    val daysList by remember(daysInMonth) {
        derivedStateOf { (1..daysInMonth).map { it.toString() } }
    }

    var selectedDayIndex by remember {
        val d = parts.getOrNull(0)?.toIntOrNull()?.minus(1) ?: (todayDay - 1)
        mutableIntStateOf(d.coerceIn(0, 30))
    }

    LaunchedEffect(daysInMonth) {
        if (selectedDayIndex >= daysInMonth) {
            selectedDayIndex = daysInMonth - 1
        }
    }

    val isDateValid by remember(selectedYearIndex, selectedMonthIndex, selectedDayIndex, daysList) {
        derivedStateOf {
            val selYear  = yearsList[selectedYearIndex].toInt()
            val selMonth = selectedMonthIndex
            val selDay   = daysList.getOrNull(selectedDayIndex)?.toIntOrNull() ?: 1
            val selected = Calendar.getInstance().apply {
                set(Calendar.YEAR, selYear)
                set(Calendar.MONTH, selMonth)
                set(Calendar.DAY_OF_MONTH, selDay)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            !selected.before(today)
        }
    }

    val itemHeight = 40.dp

    DivoBottomSheet(
        title = stringResource(R.string.EventDate),
        iconClose = R.drawable.ic_divo_back,
        isApplyEnable = isDateValid,
        contentPadding = PaddingValues(horizontal = 16.dp),
        onDismiss = onDismiss,
        onSave = {
            val day   = selectedDayIndex + 1
            val month = allMonths[selectedMonthIndex]
            val year  = yearsList[selectedYearIndex]
            onDateSelected("$day $month $year")
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
                        modifier = Modifier.weight(2f),
                        items = allMonths,
                        initialIndex = selectedMonthIndex,
                        itemHeight = itemHeight,
                        isCyclic = true,
                        onItemSelected = { index, _ -> selectedMonthIndex = index }
                    )

                    DivoWheelPicker(
                        modifier = Modifier.weight(1f),
                        items = daysList,
                        initialIndex = selectedDayIndex.coerceIn(0, daysList.size - 1),
                        itemHeight = itemHeight,
                        isCyclic = false,
                        onItemSelected = { index, _ -> selectedDayIndex = index }
                    )

                    DivoWheelPicker(
                        modifier = Modifier.weight(1f),
                        items = yearsList,
                        initialIndex = selectedYearIndex,
                        itemHeight = itemHeight,
                        isCyclic = false,
                        onItemSelected = { index, _ -> selectedYearIndex = index }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
