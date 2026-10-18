package org.telegram.divo.components.bottomsheets

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import org.telegram.divo.common.compose.clickableWithoutRipple
import org.telegram.divo.components.inputs.DivoTextField
import org.telegram.divo.entity.LocalCountry
import org.telegram.divo.screen.search.LocalCity
import org.telegram.divo.screen.search.components.EmptyPlaceContent
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityPickerSheet(
    list: List<LocalCity>,
    allCountries: List<LocalCountry> = emptyList(),      // Все страны для поиска названий
    selectedCountries: List<LocalCountry> = emptyList(), // Выбранные страны в фильтре
    selectedCity: LocalCity? = null,
    isApplyEnable: Boolean = true,
    onDismiss: () -> Unit,
    onPick: (LocalCity?) -> Unit      // Возвращаем один город
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var searchQuery by remember { mutableStateOf(selectedCity?.name ?: "") }

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
                            city.asciiName.lowercase().contains(query) ||
                            city.alternateNames.lowercase().contains(query)

                    // 2. Поиск по стране (если список пуст — ищем везде)
                    val matchesCountry = allowedCountryCodes.isEmpty() ||
                            city.countryCode.uppercase() in allowedCountryCodes

                    matchesQuery && matchesCountry
                }
                // Biggest first, so of several same-named cities the best-known one leads
                .sortedByDescending { it.population }
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
        isApplyEnable = isApplyEnable,
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
                    // Several cities share a name (Newark in NJ, DE, OH, CA): tell them apart by region
                    val repeatedNames = remember(filteredList) {
                        filteredList.groupingBy { "${it.countryCode}|${it.name}" }.eachCount()
                            .filterValues { it > 1 }.keys
                    }
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(filteredList) { item ->
                            val isSelected = item.id == selectedCity?.id
                            val flag = LocaleController.getLanguageFlag(item.countryCode) ?: ""

                            val displayName = remember(item, searchQuery) {
                                val query = searchQuery.trim().lowercase()
                                if (query.isNotBlank()) {
                                    if (item.name.lowercase().contains(query)) {
                                        item.name
                                    } else if (item.asciiName.lowercase().contains(query)) {
                                        item.asciiName
                                    } else if (item.alternateNames.lowercase().contains(query)) {
                                        val alternates = item.alternateNames.split(",").map { it.trim() }
                                        val cyrillicMatches = alternates.filter { 
                                            it.matches(Regex("^[А-Яа-яЁё\\s-]+$")) && it.lowercase().contains(query) 
                                        }
                                        
                                        if (cyrillicMatches.isNotEmpty()) {
                                            bestCyrillicName(cyrillicMatches, query, item.asciiName)
                                        } else {
                                            alternates.find { it.lowercase().contains(query) } ?: item.name
                                        }
                                    } else {
                                        item.name
                                    }
                                } else {
                                    item.name
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clickableWithoutRipple {
                                        scope.launch {
                                            sheetState.hide()
                                            onPick(item.copy(matchedName = displayName))
                                        }
                                    }
                                    .padding(start = 16.dp, end = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Region codes are readable letters in the US ("NJ") and a few other countries, numbers elsewhere
                                val region = item.adminCode
                                    .takeIf { "${item.countryCode}|${item.name}" in repeatedNames && it.any(Char::isLetter) }
                                Text(
                                    text = "$flag  $displayName${region?.let { ", $it" }.orEmpty()}".trimStart(),
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

private val CYRILLIC_TO_LATIN = mapOf(
    'а' to "a", 'б' to "b", 'в' to "v", 'г' to "g", 'д' to "d", 'е' to "e", 'ё' to "e", 'ж' to "zh",
    'з' to "z", 'и' to "i", 'й' to "y", 'к' to "k", 'л' to "l", 'м' to "m", 'н' to "n", 'о' to "o",
    'п' to "p", 'р' to "r", 'с' to "s", 'т' to "t", 'у' to "u", 'ф' to "f", 'х' to "kh", 'ц' to "ts",
    'ч' to "ch", 'ш' to "sh", 'щ' to "shch", 'ъ' to "", 'ы' to "y", 'ь' to "", 'э' to "e", 'ю' to "yu",
    'я' to "ya"
)

private fun transliterate(text: String): String =
    text.lowercase().map { CYRILLIC_TO_LATIN[it] ?: it.toString() }.joinToString("")

private fun levenshtein(a: String, b: String): Int {
    var prev = IntArray(b.length + 1) { it }
    for (i in 1..a.length) {
        val cur = IntArray(b.length + 1)
        cur[0] = i
        for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            cur[j] = minOf(prev[j] + 1, cur[j - 1] + 1, prev[j - 1] + cost)
        }
        prev = cur
    }
    return prev[b.length]
}

/**
 * GeoNames alternate names carry no language tags and include colloquial and foreign forms
 * ("Челяба" for Chelyabinsk, Tatar "Казан" for Kazan). Of the Cyrillic names matching the query,
 * pick the one whose transliteration is closest to the official ASCII name, preferring names that
 * start with the query. Ties: hyphens over spaces ("Санкт-Петербург"), a final soft sign
 * ("Казань"), no pre-reform hard sign ("Новосибирьскъ").
 */
private fun bestCyrillicName(candidates: List<String>, query: String, asciiName: String): String {
    val official = asciiName.lowercase().replace(Regex("[^a-z]"), "")
    return candidates.minByOrNull { name ->
        val lower = name.lowercase()
        var score = levenshtein(transliterate(name).replace(Regex("[^a-z]"), ""), official).toDouble()
        if (!lower.startsWith(query)) score += 1000
        if (' ' in name) score += 0.5
        if ('ъ' in lower) score += 2
        if (lower.endsWith('ь')) score -= 0.5
        score
    } ?: candidates.first()
}
