package org.telegram.divo.screen.your_parameters.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R.string
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParameterBottomSheet(
    sheetState: SheetState,
    paramType: ParametersType?,
    options: List<String>? = null,
    initialValue: String = "",
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit
) {
    val isDatePicker = paramType == ParametersType.AGE

    val dateParts = if (isDatePicker) initialValue.split("-") else emptyList()
    var selectedYear by remember { mutableStateOf(dateParts.getOrNull(0)?.takeIf { it.length == 4 } ?: "2000") }
    var selectedMonth by remember { mutableStateOf(dateParts.getOrNull(1) ?: "01") }
    var selectedDay by remember { mutableStateOf(dateParts.getOrNull(2) ?: "01") }

    val daysInMonth = remember(selectedYear, selectedMonth) {
        val year = selectedYear.toIntOrNull() ?: 2000
        val month = selectedMonth.toIntOrNull() ?: 1
        when (month) {
            2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
    }

    LaunchedEffect(daysInMonth) {
        val currentDay = selectedDay.toIntOrNull() ?: 1
        if (currentDay > daysInMonth) {
            selectedDay = daysInMonth.toString().padStart(2, '0')
        }
    }

    val integerParts = remember(paramType) {
        val range = when (paramType) {
            ParametersType.HEIGHT -> 120..220
            ParametersType.WAIST -> 40..130
            ParametersType.HIPS -> 60..150
            ParametersType.SHOE_SIZE -> 30..50
            else -> 0..250
        }
        range.map { it.toString() }
    }
    val decimalParts = remember { (0..9).map { it.toString() } }

    val initialOptionIndex = remember(options, initialValue) {
        options?.indexOf(initialValue)?.takeIf { it >= 0 } ?: 0
    }
    var selectedSingleOption by remember {
        mutableStateOf(options?.getOrNull(initialOptionIndex) ?: "")
    }

    var selectedIntPart by remember(initialValue, integerParts) {
        val parsed = initialValue.substringBefore(".").takeIf { it.isNotEmpty() }
        mutableStateOf(parsed.takeIf { integerParts.contains(it) } ?: integerParts[integerParts.size / 2])
    }
    var selectedDecPart by remember(initialValue) {
        mutableStateOf(if (initialValue.contains(".")) initialValue.substringAfter(".") else "0")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.colors.backgroundLight,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.backgroundLight)
                .padding(bottom = 32.dp, top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF29C74))
                        .clickable {
                            if (isDatePicker) {
                                onSave("$selectedYear-$selectedMonth-$selectedDay")
                            } else if (options.isNullOrEmpty()) {
                                onSave("$selectedIntPart.$selectedDecPart")
                            } else {
                                onSave(selectedSingleOption)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Save", tint = Color.White)
                }
            }
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = paramType?.titleRes?.let { stringResource(it) }.orEmpty(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            val itemHeight = 40.dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(41.dp))
                        .background(Color(0xFFEBEBEB), RoundedCornerShape(8.dp))
                )

                if (isDatePicker) {
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val maxYear = currentYear - 14

                    val daysList = (1..daysInMonth).map { it.toString().padStart(2, '0') }
                    val monthsList = (1..12).map { it.toString().padStart(2, '0') }
                    val yearsList = (1950..maxYear).map { it.toString() }.reversed()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        WheelPicker(
                            items = daysList,
                            initialIndex = daysList.indexOf(selectedDay).coerceAtLeast(0),
                            isCyclic = false,
                            modifier = Modifier.weight(1f),
                            onItemSelected = { _, item -> selectedDay = item }
                        )
                        WheelPicker(
                            items = monthsList,
                            initialIndex = monthsList.indexOf(selectedMonth).coerceAtLeast(0),
                            isCyclic = false,
                            modifier = Modifier.weight(1f),
                            onItemSelected = { _, item -> selectedMonth = item }
                        )
                        WheelPicker(
                            items = yearsList,
                            initialIndex = yearsList.indexOf(selectedYear).coerceAtLeast(0),
                            isCyclic = false,
                            modifier = Modifier.weight(1f),
                            onItemSelected = { _, item -> selectedYear = item }
                        )
                    }
                } else if (!options.isNullOrEmpty()) {
                    WheelPicker(
                        items = options,
                        initialIndex = initialOptionIndex,
                        isCyclic = true,
                        modifier = Modifier.fillMaxWidth(0.8f),
                        onItemSelected = { _, item -> selectedSingleOption = item }
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        WheelPicker(
                            items = integerParts,
                            initialIndex = integerParts.indexOf(selectedIntPart).coerceAtLeast(0),
                            isCyclic = true,
                            modifier = Modifier.width(70.dp),
                            onItemSelected = { _, item -> selectedIntPart = item }
                        )

                        WheelPicker(
                            items = decimalParts,
                            initialIndex = decimalParts.indexOf(selectedDecPart).coerceAtLeast(0),
                            isCyclic = true,
                            modifier = Modifier.width(70.dp),
                            onItemSelected = { _, item -> selectedDecPart = item }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            UIButtonNew(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                text = stringResource(string.DeleteParameter),
                background = AppTheme.colors.textPrimary.copy(0.6f),
                onClick = onDelete
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelPicker(
    modifier: Modifier = Modifier,
    items: List<String>,
    initialIndex: Int = 0,
    itemHeight: Dp = 40.dp,
    visibleItemsCount: Int = 5,
    isCyclic: Boolean = false,
    onItemSelected: (index: Int, item: String) -> Unit
) {
    if (items.isEmpty()) return

    val centerIndex = visibleItemsCount / 2
    val actualItemCount = if (isCyclic) Int.MAX_VALUE else items.size

    val actualStartIndex = if (isCyclic) {
        val middle = Int.MAX_VALUE / 2
        middle - (middle % items.size) + initialIndex
    } else {
        initialIndex
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = actualStartIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(listState, items, isCyclic) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { if (isCyclic) it % items.size else it }
            .distinctUntilChanged()
            .collect { index ->
                val safeIndex = index.coerceIn(0, items.size - 1)
                onItemSelected(safeIndex, items[safeIndex])
            }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * visibleItemsCount),
            contentPadding = PaddingValues(vertical = itemHeight * centerIndex)
        ) {
            items(actualItemCount) { index ->
                val realIndex = if (isCyclic) index % items.size else index

                val isSelected by remember(index) {
                    derivedStateOf { listState.firstVisibleItemIndex == index }
                }

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[realIndex],
                        fontSize = 20.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) Color.Black else Color.Gray,
                        modifier = Modifier.alpha(if (isSelected) 1f else 0.4f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * centerIndex)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF3F3F3),
                            Color(0x00F3F3F3)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * centerIndex)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x00F3F3F3),
                            Color(0xFFF3F3F3)
                        )
                    )
                )
        )
    }
}