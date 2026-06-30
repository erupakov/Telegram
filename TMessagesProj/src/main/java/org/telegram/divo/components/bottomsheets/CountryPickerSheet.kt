package org.telegram.divo.components.bottomsheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.telegram.divo.common.compose.clickableWithoutRipple
import org.telegram.divo.components.inputs.DivoTextField
import org.telegram.divo.entity.LocalCountry
import org.telegram.divo.screen.search.components.EmptyPlaceContent
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPickerSheet(
    list: List<LocalCountry>,
    selectedCountries: List<LocalCountry> = emptyList(),
    isMultiSelection: Boolean = false,
    onDismiss: () -> Unit,
    onPick: (List<LocalCountry>) -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentSelection = remember {
        mutableStateListOf<LocalCountry>().apply { addAll(selectedCountries) }
    }

    var searchQuery by remember { mutableStateOf("") }

    var sortSnapshot by remember { mutableStateOf(selectedCountries.toList()) }
    val filteredList = remember(searchQuery, list, sortSnapshot) {
        val searchResult = if (searchQuery.isBlank()) {
            list
        } else {
            list.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }

        searchResult.sortedWith(
            compareByDescending<LocalCountry> { sortSnapshot.contains(it) }
                .thenBy { it.name }
        )
    }

    DivoBottomSheet(
        sheetState = sheetState,
        title = stringResource(R.string.CountryLabel),
        isSaveMode = isMultiSelection,
        contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
        onDismiss = onDismiss,
        iconClose = R.drawable.ic_divo_back,
        onSave = { onPick(currentSelection) },
        onReset = {
            scope.launch {
                sheetState.hide()
                onPick(emptyList())
            }
        }
    ) {
        Spacer(Modifier.height(20.dp))
        DivoTextField(
            value = searchQuery,
            onValueChange = { newText ->
                if (searchQuery.isNotEmpty() && newText.isEmpty()) {
                    sortSnapshot = currentSelection.toList()
                }
                searchQuery = newText
            },
            height = 40.dp,
            cornerRadius = 99.dp,
            leadingIcon = R.drawable.ic_divo_search,
            trailingIcon = if (searchQuery.isNotBlank()) R.drawable.ic_divo_clear else null,
            onTrailingIconClick = {
                sortSnapshot = currentSelection.toList()
                searchQuery = ""
            },
            backgroundColor = AppTheme.colors.onBackground,
            horizontalContentPadding = 16.dp
        )
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxSize()) {
            if (filteredList.isEmpty() && searchQuery.isNotBlank()) {
                EmptyPlaceContent(
                    title = stringResource(R.string.CountriesLabel),
                    body = stringResource(R.string.CountryLabel)
                )
            } else {
                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppTheme.colors.onBackground),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    itemsIndexed(filteredList, key = { i, c -> c.code }) { index, item ->
                        val isSelected = currentSelection.contains(item)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clickableWithoutRipple {
                                    if (isMultiSelection) {
                                        if (isSelected) currentSelection.remove(item)
                                        else currentSelection.add(item)
                                    } else {
                                        scope.launch {
                                            sheetState.hide()
                                            onPick(listOf(item))
                                        }
                                    }
                                }
                                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            if (item.flag != null) {
                                Text(text = item.flag, fontSize = 14.sp)
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                text = item.name,
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

                        if (index != filteredList.size -1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }
        }
    }
}