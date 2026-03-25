package org.telegram.divo.screen.add_model

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.font.FontWeight
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
        containerColor = AppTheme.colors.backgroundNew,
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
                    containerColor = AppTheme.colors.backgroundNew
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
                    .padding(horizontal = 16.dp)
                    .align(Alignment.BottomCenter),
                text = stringResource(R.string.NextStep),
                onClick = { onIntent(Intent.OnNextClicked) },
            )
        }

        if (showCountrySheet) {
            CountryPickerSheet(
                list = uiState.countries,
                onDismiss = { showCountrySheet = false },
                onPick = {
                    onIntent(Intent.OnCountrySelected(it))
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
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
private fun CountryPickerSheet(
    list: List<LocalCountry>,
    onDismiss: () -> Unit,
    onPick: (LocalCountry) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        modifier = Modifier.statusBarsPadding(),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        tonalElevation = 16.dp
    ) {
        Column(Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.ChooseCountry),
                modifier = Modifier.padding(16.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(list) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { sheetState.hide(); onPick(item) }
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.flag != null) {
                            Text(text = item.flag, fontSize = 20.sp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(item.name)
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}