package org.telegram.divo.screen.edit_my_profile

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.common.SnackbarEvent.*
import org.telegram.divo.common.rememberGalleryLauncher
import org.telegram.divo.common.utils.uriToFile
import org.telegram.divo.components.DivoTabSelector
import org.telegram.divo.components.DivoTextField
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.components.TabConfig
import org.telegram.divo.components.TelegramUserAvatarEditable
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.screen.work_history.WorkHistoryScreen
import org.telegram.divo.screen.your_parameters.YourParametersScreen
import org.telegram.divo.screen.reg_form.components.PlaceField
import org.telegram.divo.screen.add_model.CountryPickerSheet
import org.telegram.divo.screen.search.components.CityPickerSheet
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R


enum class ProfileDestination {
    BIOGRAPHY, APPEARANCE, EXPERIENCE
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditMyProfileScreen(
    isModel: Boolean,
    viewModel: EditMyProfileViewModel = viewModel(
        factory = EditMyProfileViewModel.factory(isModel)
    ),
    initialPage: Int = 0,
    onCreateWorkHistoryClicked: (Int?) -> Unit,
    onCloseScreen: () -> Unit = {},
) {
    val parametersSavedText = stringResource(R.string.ParametersSaved)
    val uiState = viewModel.state.collectAsState().value

    val tabs = remember {
        listOf(
            TabConfig(
                id = ProfileDestination.BIOGRAPHY.name,
                textResId = R.string.TabBiography,
            ),
            TabConfig(
                id = ProfileDestination.APPEARANCE.name,
                textResId = R.string.TabAppearance
            ),
            TabConfig(
                id = ProfileDestination.EXPERIENCE.name,
                textResId = R.string.TabExperience
            )
        )
    }

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { if (uiState.isModel) ProfileDestination.entries.size else 1 }
    )

    val snackbarState = remember { AppSnackbarHostState() }

    LifecycleResumeEffect(Unit) {
        viewModel.getData()
        onPauseOrDispose { }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { action ->
            when (action) {
                Effect.NavigateBack -> onCloseScreen()
                Effect.SaveSuccess -> {
                    val page = tabs.indexOfFirst {
                        it.id == ProfileDestination.APPEARANCE.name
                    }.coerceAtLeast(0)
                    pagerState.scrollToPage(page)

                    snackbarState.show(Success(parametersSavedText))
                }
                is Effect.ShowError -> {
                    snackbarState.show(Error(action.message))
                }
            }
        }
    }

    Scaffold(
        containerColor = AppTheme.colors.backgroundLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val titleId = if (uiState.isModel) R.string.Profile else R.string.MyAgencyProfile

                    Text(
                        modifier = Modifier.padding(top = 3.dp),
                        text = stringResource(titleId).uppercase(),
                        style = AppTheme.typography.appBar
                    )
                },
                navigationIcon = {
                    RoundedButton(
                        modifier = Modifier.padding(start = 16.dp),
                        resId = R.drawable.ic_divo_back,
                        onClick = onCloseScreen
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.backgroundLight
                )
            )
        },
        snackbarHost = {
            AppSnackbarHost(
                state = snackbarState,
                bottomPadding = 74.dp
            )
        },
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(Modifier.height(14.dp))

            if (uiState.isModel) {
                DivoTabSelector(
                    modifier = Modifier.fillMaxWidth(),
                    tabs = tabs,
                    selectedIndex = pagerState.currentPage,
                    onTabSelected = { index ->
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    horizontalPadding = 16.dp,
                )

                Spacer(Modifier.height(24.dp))
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                beyondViewportPageCount = 1,
                userScrollEnabled = true
            ) { page ->
                when (ProfileDestination.valueOf(tabs[page].id)) {
                    ProfileDestination.BIOGRAPHY -> {
                        EditMyProfileScreenView(
                            uiState = uiState,
                            onIntent = { viewModel.setIntent(it) }
                        )
                    }
                    ProfileDestination.APPEARANCE -> {
                        YourParametersScreen(
                            showTitle = false,
                            onSaved = {
                                scope.launch {
                                    val page = tabs.indexOfFirst {
                                        it.id == ProfileDestination.EXPERIENCE.name
                                    }.coerceAtLeast(0)
                                    pagerState.scrollToPage(page)
                                }
                            }
                        )
                    }
                    ProfileDestination.EXPERIENCE -> {
                        if (uiState.userFull != null) {
                            WorkHistoryScreen(
                                userId = uiState.userFull.id,
                                isOwnProfile = true,
                                isFromEditScreen = true,
                                onCreateClicked = onCreateWorkHistoryClicked
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                LottieProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = AppTheme.colors.accentOrange
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMyProfileScreenView(
    uiState: EventListViewState,
    onIntent: (EditMyProfileIntent) -> Unit = {},
) {
    var fName by rememberSaveable { mutableStateOf(uiState.fName) }
    var lName by rememberSaveable { mutableStateOf(uiState.lName) }
    var bio by rememberSaveable { mutableStateOf(uiState.bio) }
    var selectedAvatarUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    
    var showCountrySheet by rememberSaveable { mutableStateOf(false) }
    var showCitySheet by rememberSaveable { mutableStateOf(false) }

    val openGallery = rememberGalleryLauncher { uri ->
        selectedAvatarUri = uri
    }
    val context = LocalContext.current

    LaunchedEffect(uiState.fName, uiState.lName, uiState.bio) {
        fName = uiState.fName
        lName = uiState.lName
        bio = uiState.bio
    }
    
    if (showCountrySheet) {
        CountryPickerSheet(
            list = uiState.allCountries,
            selectedCountries = listOfNotNull(
                uiState.allCountries.find { it.name == uiState.country }
            ),
            onDismiss = { showCountrySheet = false },
            onPick = { selectedList ->
                val country = selectedList.firstOrNull()
                onIntent(
                    EditMyProfileIntent.OnLocationChanged(
                        country = country?.name ?: "",
                        countryCode = country?.shortName ?: "",
                        city = null
                    )
                )
                showCountrySheet = false
            }
        )
    }

    if (showCitySheet) {
        CityPickerSheet(
            list = uiState.allCities,
            selectedCity = uiState.city,
            allCountries = uiState.allCountries,
            selectedCountries = listOfNotNull(
                uiState.allCountries.find { it.name == uiState.country }
            ),
            onDismiss = { showCitySheet = false },
            onPick = { selectedCity ->
                val matchedCountry = uiState.allCountries.find {
                    it.shortName.equals(selectedCity?.countryCode, ignoreCase = true)
                }
                onIntent(
                    EditMyProfileIntent.OnLocationChanged(
                        city = selectedCity,
                        country = if (uiState.country.isEmpty() && matchedCountry != null)
                            matchedCountry.name
                        else
                            uiState.country,
                        countryCode = if (uiState.countryCode.isEmpty() && matchedCountry != null)
                            matchedCountry.shortName
                        else
                            uiState.countryCode
                    )
                )
                showCitySheet = false
            }
        )
    }

    if (uiState.isLoading) {
        EditMyProfileLoadingContent(uiState.isModel)
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

            key(uiState.avatarUrl, selectedAvatarUri) {
                TelegramUserAvatarEditable(
                    avatarUrl = uiState.avatarUrl,
                    localUri = selectedAvatarUri,
                    background = AppTheme.colors.onBackground,
                    showBorder = false,
                    size = 94.dp,
                    smallIconResId = R.drawable.ic_divo_camera_rounded,
                    onEditClick = { openGallery() }
                )
            }

            DivoTextField(
                modifier = Modifier.padding(top = 32.dp),
                value = fName,
                onValueChange = { fName = it },
                placeholder = stringResource(R.string.EditProfileFirstName),
                trailingIcon = if (fName.isNotBlank()) R.drawable.ic_divo_clear else null,
                onTrailingIconClick = { fName = "" },
                backgroundColor = AppTheme.colors.onBackground,
                cornerRadius = 41.dp,
                placeholderColor = AppTheme.colors.textPrimary.copy(0.6f),
                textStyle = AppTheme.typography.bodyLarge.copy(
                    color = AppTheme.colors.textPrimary
                ),
                horizontalContentPadding = 16.dp,
                verticalContentPadding = 16.dp
            )

            DivoTextField(
                modifier = Modifier.padding(top = 16.dp),
                value = lName,
                onValueChange = { lName = it },
                placeholder = stringResource(R.string.EditProfileLastName),
                trailingIcon = if (lName.isNotBlank()) R.drawable.ic_divo_clear else null,
                onTrailingIconClick = { lName = "" },
                backgroundColor = AppTheme.colors.onBackground,
                cornerRadius = 41.dp,
                placeholderColor = AppTheme.colors.textPrimary.copy(0.6f),
                textStyle = AppTheme.typography.bodyLarge.copy(
                    color = AppTheme.colors.textPrimary
                ),
                horizontalContentPadding = 16.dp,
                verticalContentPadding = 16.dp
            )

            Text(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 6.dp),
                text = stringResource(R.string.Biography),
                color = AppTheme.colors.textPrimary.copy(0.6f),
                style = AppTheme.typography.bodyMedium
            )
            DivoTextField(
                value = bio,
                onValueChange = { bio = it },
                placeholder = stringResource(R.string.BiographyPlaceholder),
                backgroundColor = AppTheme.colors.onBackground,
                cornerRadius = 16.dp,
                height = 114.dp,
                placeholderColor = AppTheme.colors.textPrimary.copy(0.6f),
                minLines = 5,
                maxLines = 5,
                textStyle = AppTheme.typography.bodyLarge.copy(
                    color = AppTheme.colors.textPrimary
                ),
                horizontalContentPadding = 16.dp
            )
            
            Spacer(modifier = Modifier.size(16.dp))
            
            PlaceField(
                text = uiState.country,
                label = stringResource(R.string.CountryLabel),
                onClick = { showCountrySheet = true }
            )
            
            Spacer(modifier = Modifier.size(16.dp))
            
            PlaceField(
                text = uiState.city?.let { it.matchedName ?: it.name }.orEmpty(),
                label = stringResource(R.string.CityLabel),
                onClick = { showCitySheet = true }
            )

            Spacer(modifier = Modifier.size(100.dp))
            } // End of scrollable Column

            // Fixed bottom area
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isSaved) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(AppTheme.colors.accentOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        LottieProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    }
                } else {
                    val isCityRequiredButMissing = uiState.country.isNotEmpty() && uiState.city == null
                    
                    UIButtonNew(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.SaveEditProfileScreen),
                        enabled = !isCityRequiredButMissing,
                        onClick = {
                            onIntent(
                                EditMyProfileIntent.OnSaveClicked(
                                    fName = fName,
                                    lName = lName,
                                    bio = bio,
                                    file = selectedAvatarUri?.let { context.uriToFile(it) }
                                )
                            )
                        })
                }
            }
        }
    }
}




