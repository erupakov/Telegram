package org.telegram.divo.screen.add_model

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.rememberGalleryLauncher
import org.telegram.divo.components.DivoTextField
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.components.TelegramUserAvatarEditable
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.components.items.DivoBottomSheet
import org.telegram.divo.screen.search.components.EmptyPlaceContent
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun AddModelScreen(
    viewModel: AddModelViewModel = viewModel(),
    onNavigateToYourParameters: () -> Unit,
    onCloseScreen: () -> Unit
) {
    val uiState = viewModel.state.collectAsState().value

    LaunchedEffect(Unit) {
        viewModel.effect.collect { action ->
            when (action) {
                Effect.NavigateBack -> onCloseScreen()
                Effect.NavigateNext -> onNavigateToYourParameters()
            }
        }
    }

    AddModelScreenContent(
        uiState = uiState,
        onIntent = { viewModel.setIntent(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddModelScreenContent(
    uiState: State,
    onIntent: (Intent) -> Unit
) {
    var showCountrySheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = AppTheme.colors.backgroundLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.AddNewModel).uppercase(),
                        style = AppTheme.typography.appBar,
                    )
                },
                navigationIcon = {
                    RoundedButton(
                        modifier = Modifier.padding(start = 16.dp),
                        resId = R.drawable.ic_divo_back,
                        onClick = { onIntent(Intent.OnBack) }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.backgroundLight
                )
            )
        }
    ) { padding ->
        val openGallery = rememberGalleryLauncher { uri ->
            onIntent(Intent.OnAvatarSelected(uri.toString()))
        }
        val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp

        Box(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, bottom = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))
                TelegramUserAvatarEditable(
                    localUri = uiState.avatarUri?.toUri(),
                    background = Color.White,
                    borderColor = AppTheme.colors.accentOrange,
                    isVisibleSmallIcon = false,
                    placeholderIconSize = 24.dp,
                    onEditClick = { openGallery() }
                )
                Spacer(Modifier.height(42.dp))
                Text(
                    text = stringResource(R.string.AddModelDescription),
                    style = AppTheme.typography.helveticaNeueRegular,
                    color = Color(0xFF17181C),
                    lineHeight = 18.sp,
                    fontSize = 14.sp,
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.PersonalData),
                    style = AppTheme.typography.helveticaNeueRegular,
                    color = Color.Black,
                    fontSize = 16.sp,
                )
                Spacer(Modifier.height(16.dp))
                TextField(
                    text = uiState.name,
                    onValueChange = { onIntent(Intent.OnNicknameChanged(it)) },
                )
                Spacer(Modifier.height(16.dp))
                CountryField(
                    country = uiState.country,
                    onClick = { showCountrySheet = true }
                )
                Spacer(Modifier.height(16.dp))
                TextField(
                    text = uiState.link,
                    placeholder = stringResource(R.string.ProfileLink),
                    onValueChange = { onIntent(Intent.OnLinkChanged(it)) },
                )
            }

            UIButtonNew(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                    .align(Alignment.BottomCenter),
                text = stringResource(R.string.NextStep),
                onClick = { onIntent(Intent.OnNextClicked) },
            )
        }

        if (showCountrySheet) {
            CountryPickerSheet(
                list = uiState.countries,
                selectedCountries = uiState.country?.let { listOf(it) } ?: emptyList(),
                onDismiss = { showCountrySheet = false },
                onPick = {
                    onIntent(Intent.OnCountrySelected(it.first()))
                    showCountrySheet = false
                }
            )
        }
    }
}

@Composable
private fun CountryField(
    country: LocalCountry?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(41.dp))
            .background(Color.White)
            .clickableWithoutRipple { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val text = if (country != null) {
            if (country.flag != null) "${country.flag} ${country.name}" else country.name
        } else stringResource(R.string.CountryLabel)
        
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp).offset(y = 1.dp),
            text = text,
            color = if (country != null) Color.Black else Color.Black.copy(0.6f),
            fontSize = 16.sp,
        )
        Icon(
            modifier = Modifier.padding(end = 20.dp).size(12.dp),
            painter = painterResource(R.drawable.ic_divo_arrow_right),
            contentDescription = null,
            tint = Color.Black
        )
    }
}

@Composable
private fun TextField(
    text: String,
    placeholder: String = stringResource(R.string.FullName),
    onValueChange: (String) -> Unit,
) {
    DivoTextField(
        value = text,
        onValueChange = onValueChange,
        cornerRadius = 41.dp,
        horizontalContentPadding = 16.dp,
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = Color.Black
        ),
        backgroundColor = Color.White,
        placeholder = placeholder
    )
}

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
        contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
        onDismiss = onDismiss,
        iconClose = R.drawable.ic_divo_back,
        onSave = { onPick(currentSelection) }
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