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
    maxDate: String = "",
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit,
) {
    val now = Calendar.getInstance()
    val todayYear  = now.get(Calendar.YEAR)
    val todayMonth = now.get(Calendar.MONTH)
    val todayDay   = now.get(Calendar.DAY_OF_MONTH)

    val allMonths = (1..12).map { Month.of(it).getDisplayName(TextStyle.FULL, DivoLanguageManager.getSystemLocale()) }
    val maxCalendar = remember(maxDate) {
        if (maxDate.isNotBlank()) {
            try {
                val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", Locale.US)
                val date = sdf.parse(maxDate)
                if (date != null) {
                    val cal = Calendar.getInstance()
                    cal.time = date
                    return@remember cal
                }
            } catch (e: Exception) {}
        }
        null
    }

    val maxYear = maxCalendar?.get(Calendar.YEAR) ?: (todayYear + 10)
    val yearsList = (todayYear..maxYear).map { it.toString() }

    val parsedCalendar = remember(initialDate) {
        val cal = Calendar.getInstance()
        if (initialDate.isNotBlank()) {
            try {
                val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", Locale.US)
                val date = sdf.parse(initialDate)
                if (date != null) cal.time = date
            } catch (e: Exception) {}
        }
        cal
    }

    var selectedYearIndex by remember {
        val y = parsedCalendar.get(Calendar.YEAR)
        mutableIntStateOf(yearsList.indexOf(y.toString()).coerceAtLeast(0))
    }

    val minMonthForSelectedYear by remember(selectedYearIndex) {
        derivedStateOf {
            val selYear = yearsList.getOrNull(selectedYearIndex)?.toIntOrNull() ?: todayYear
            if (selYear == todayYear) todayMonth else 0
        }
    }

    val monthsList by remember(selectedYearIndex, maxCalendar, minMonthForSelectedYear) {
        derivedStateOf {
            val selYear = yearsList.getOrNull(selectedYearIndex)?.toIntOrNull() ?: todayYear
            val maxMonth = if (maxCalendar != null && selYear == maxCalendar.get(Calendar.YEAR)) {
                maxCalendar.get(Calendar.MONTH)
            } else {
                11
            }
            if (minMonthForSelectedYear <= maxMonth) {
                (minMonthForSelectedYear..maxMonth).map { Month.of(it + 1).getDisplayName(TextStyle.FULL, DivoLanguageManager.getSystemLocale()) }
            } else {
                listOf(Month.of(minMonthForSelectedYear + 1).getDisplayName(TextStyle.FULL, DivoLanguageManager.getSystemLocale()))
            }
        }
    }

    var selectedMonthIndex by remember {
        val m = parsedCalendar.get(Calendar.MONTH)
        val selYear = parsedCalendar.get(Calendar.YEAR)
        val minM = if (selYear == todayYear) todayMonth else 0
        mutableIntStateOf((m - minM).coerceAtLeast(0))
    }

    LaunchedEffect(monthsList) {
        if (selectedMonthIndex >= monthsList.size) {
            selectedMonthIndex = (monthsList.size - 1).coerceAtLeast(0)
        }
    }

    val actualSelectedMonth by remember(minMonthForSelectedYear, selectedMonthIndex) {
        derivedStateOf { minMonthForSelectedYear + selectedMonthIndex }
    }

    val minDayForSelected by remember(selectedYearIndex, actualSelectedMonth) {
        derivedStateOf {
            val selYear = yearsList.getOrNull(selectedYearIndex)?.toIntOrNull() ?: todayYear
            if (selYear == todayYear && actualSelectedMonth == todayMonth) todayDay else 1
        }
    }

    val daysList by remember(actualSelectedMonth, selectedYearIndex, maxCalendar, minDayForSelected) {
        derivedStateOf {
            val selYear = yearsList.getOrNull(selectedYearIndex)?.toIntOrNull() ?: todayYear
            
            val actualMax = Calendar.getInstance().apply {
                set(Calendar.YEAR, selYear)
                set(Calendar.MONTH, actualSelectedMonth)
            }.getActualMaximum(Calendar.DAY_OF_MONTH)

            val maxDay = if (maxCalendar != null && selYear == maxCalendar.get(Calendar.YEAR) && actualSelectedMonth == maxCalendar.get(Calendar.MONTH)) {
                minOf(actualMax, maxCalendar.get(Calendar.DAY_OF_MONTH))
            } else {
                actualMax
            }

            if (minDayForSelected <= maxDay) {
                (minDayForSelected..maxDay).map { it.toString() }
            } else {
                listOf(minDayForSelected.toString())
            }
        }
    }

    var selectedDayIndex by remember {
        val d = parsedCalendar.get(Calendar.DAY_OF_MONTH)
        val selYear = parsedCalendar.get(Calendar.YEAR)
        val selMonth = parsedCalendar.get(Calendar.MONTH)
        val minD = if (selYear == todayYear && selMonth == todayMonth) todayDay else 1
        mutableIntStateOf((d - minD).coerceAtLeast(0))
    }

    LaunchedEffect(daysList) {
        if (selectedDayIndex >= daysList.size) {
            selectedDayIndex = (daysList.size - 1).coerceAtLeast(0)
        }
    }

    val itemHeight = 40.dp

    DivoBottomSheet(
        title = stringResource(R.string.EventDate),
        iconClose = R.drawable.ic_divo_back,
        isApplyEnable = daysList.isNotEmpty() && monthsList.isNotEmpty(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        onDismiss = onDismiss,
        onSave = {
            val day = (minDayForSelected + selectedDayIndex).toString().padStart(2, '0')
            val month = (actualSelectedMonth + 1).toString().padStart(2, '0')
            val year = yearsList.getOrNull(selectedYearIndex) ?: todayYear.toString()
            onDateSelected("$day.$month.$year")
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
                        items = monthsList,
                        initialIndex = selectedMonthIndex.coerceIn(0, maxOf(0, monthsList.size - 1)),
                        itemHeight = itemHeight,
                        isCyclic = false,
                        onItemSelected = { index, _ -> selectedMonthIndex = index }
                    )

                    DivoWheelPicker(
                        modifier = Modifier.weight(1f),
                        items = daysList,
                        initialIndex = selectedDayIndex.coerceIn(0, maxOf(0, daysList.size - 1)),
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
