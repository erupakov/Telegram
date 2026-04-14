package org.telegram.divo.screen.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.DivoTextField
import org.telegram.divo.components.items.DivoBottomSheet
import org.telegram.divo.screen.add_model.LocalCountry
import org.telegram.divo.screen.search.LocalCity
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityPickerSheet(
    list: List<LocalCity>,
    allCountries: List<LocalCountry> = emptyList(),      // Все страны для поиска названий
    selectedCountries: List<LocalCountry> = emptyList(), // Выбранные страны в фильтре
    selectedCity: LocalCity? = null, // Заменили List на одиночный элемент (или null)
    onDismiss: () -> Unit,
    onPick: (LocalCity?) -> Unit      // Возвращаем один город
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var searchQuery by remember { mutableStateOf("") }
    val countryNameMap = remember(allCountries) {
        allCountries.associate { it.shortName.uppercase() to it.name }
    }

    // Убрали сложную сортировку и sortSnapshot. Просто фильтруем по тексту.
    val filteredList = remember(searchQuery, list, selectedCountries) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val query = searchQuery.trim().lowercase()
            // Достаем коды выбранных стран
            val allowedCountryCodes = selectedCountries.map { it.shortName.uppercase() }.toSet()

            list.asSequence() // sequence для оптимизации работы с большим списком
                .filter { city ->
                    // 1. Поиск по названию
                    val matchesQuery = city.name.lowercase().contains(query) ||
                            city.asciiName.lowercase().contains(query)

                    // 2. Поиск по стране (если список пуст — ищем везде)
                    val matchesCountry = allowedCountryCodes.isEmpty() ||
                            city.countryCode.uppercase() in allowedCountryCodes

                    matchesQuery && matchesCountry
                }
                .take(50) // Не даем рендерить больше 50 штук за раз
                .toList()
        }
    }

    DivoBottomSheet(
        sheetState = sheetState,
        title = stringResource(R.string.CityLabel),
        isSaveMode = false,
        contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
        onDismiss = onDismiss,
        iconClose = R.drawable.ic_divo_back,
        onReset = {
            scope.launch {
                sheetState.hide()
                onPick(null)
            }
        }
    ) {
        Spacer(Modifier.height(20.dp))

        DivoTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            height = 40.dp,
            cornerRadius = 99.dp,
            leadingIcon = R.drawable.ic_divo_search,
            trailingIcon = if (searchQuery.isNotBlank()) R.drawable.ic_divo_clear else null,
            onTrailingIconClick = { searchQuery = "" },
            backgroundColor = AppTheme.colors.onBackground,
            horizontalContentPadding = 16.dp
        )

        Spacer(Modifier.height(2.dp))

        Box(
            Modifier.fillMaxSize(),
        ) {
            if (filteredList.isNotEmpty()) {
                Column(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppTheme.colors.onBackground),
                ) {
                    Text(
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                        text = stringResource(R.string.SuggestedCities),
                        fontSize = 12.sp,
                        color = AppTheme.colors.textPrimary.copy(0.8f),
                        style = AppTheme.typography.helveticaNeueRegular
                    )
                    Spacer(Modifier.height(12.dp))
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(filteredList) { item ->
                            val isSelected = item == selectedCity
                            val countryName =
                                countryNameMap[item.countryCode.uppercase()] ?: item.countryCode

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clickableWithoutRipple {
                                        scope.launch {
                                            sheetState.hide()
                                            onPick(item)
                                        }
                                    }
                                    .padding(start = 16.dp, end = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.name}, $countryName",
                                    style = AppTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )

                                if (isSelected) {
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = AppTheme.colors.accentOrange
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (searchQuery.isNotBlank()) {
                EmptyPlaceContent(
                    title = stringResource(R.string.CitiesLabel),
                    body = stringResource(R.string.CityLabel)
                )
            }
        }
    }
}
