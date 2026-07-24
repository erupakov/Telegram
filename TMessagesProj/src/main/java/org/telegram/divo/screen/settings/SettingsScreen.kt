package org.telegram.divo.screen.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.common.DivoSettings
import org.telegram.divo.common.SnackbarEvent.ErrorWithRetry
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.screen.settings.components.ContainerItems
import org.telegram.divo.screen.settings.components.MeasuringSystemDialog
import org.telegram.divo.screen.settings.components.ProfileRow
import org.telegram.divo.screen.settings.components.QrCodeBottomSheet
import org.telegram.divo.screen.settings.components.SettingsItemRow
import org.telegram.divo.screen.settings.components.SettingsTopBar
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    navigateToFillParameters: () -> Unit = {},
    navigateToProfile: (Int) -> Unit = {},
    navigateToSavedMessages: () -> Unit = {},
    navigateToNotifications: () -> Unit = {},
    navigateToPrivacy: () -> Unit = {},
    navigateToDataStorage: () -> Unit = {},
    navigateToAppearance: () -> Unit = {},
    navigateToSetUsername: () -> Unit = {},
    navigateToLanguage: () -> Unit = {},
    navigateToLogout: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarState = remember { AppSnackbarHostState() }
    val context = LocalContext.current

    val dividerColor = Color(0x1A000000)
    val scrollState = rememberScrollState()

    var showMeasuringSystemDialog by remember { mutableStateOf(false) }
    var showQrBottomSheet by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        viewModel.setIntent(SettingsViewIntent.OnRefresh)
        onPauseOrDispose { }
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect {
            when (it) {
                SettingsViewEffect.NavigateToAppearance -> {
                    navigateToAppearance()
                }

                SettingsViewEffect.NavigateToDataStorage -> {
                    navigateToDataStorage()
                }

                SettingsViewEffect.NavigateToLanguage -> {
                    navigateToLanguage()
                }

                SettingsViewEffect.NavigateToEditProfile -> {
                    navigateToProfile(state.userId)
                }

                SettingsViewEffect.NavigateToFillParameters -> {
                    navigateToFillParameters()
                }

                SettingsViewEffect.NavigateToNotifications -> {
                    navigateToNotifications()
                }

                SettingsViewEffect.NavigateToPrivacy -> {
                    navigateToPrivacy()
                }

                SettingsViewEffect.NavigateToProfile -> {
                    navigateToProfile(state.userId)
                }

                SettingsViewEffect.NavigateToPromo -> {
                    // TODO: Implement promo screen
                }

                SettingsViewEffect.NavigateToSavedMessages -> {
                    navigateToSavedMessages()
                }

                SettingsViewEffect.NavigateToSetUsername -> {
                    navigateToSetUsername()
                }

                is SettingsViewEffect.ShowError -> {
                    snackbarState.show(
                        ErrorWithRetry(
                            message = it.message,
                            actionLabel = context.getString(R.string.RetryLabel),
                            onRetry = { viewModel.setIntent(SettingsViewIntent.OnRefresh) }
                        )
                    )
                }

                SettingsViewEffect.NavigateToLogout -> navigateToLogout()
                SettingsViewEffect.ShowMeasuringSystemDialog -> showMeasuringSystemDialog = true
            }
        }
    }

    if (showMeasuringSystemDialog) {
        MeasuringSystemDialog(
            currentSystem = state.measuringSystem,
            onApply = { newSystem ->
                showMeasuringSystemDialog = false
                viewModel.setIntent(SettingsViewIntent.OnChangeMeasuringSystem(newSystem))
            },
            onDismiss = { showMeasuringSystemDialog = false }
        )
    }

    if (showQrBottomSheet) {
        QrCodeBottomSheet(
            userId = state.userId,
            message = "${state.userName} - ${state.role}",
            onDismiss = { showQrBottomSheet = false }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = AppTheme.colors.backgroundLight,
        topBar = {
            SettingsTopBar(
                onAction = { viewModel.setIntent(SettingsViewIntent.OnEditProfileClicked) },
                onQrCode = { showQrBottomSheet = true }
            )
        },
        snackbarHost = {
            AppSnackbarHost(
                state = snackbarState,
                bottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 74.dp
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                LottieProgressIndicator(Modifier.size(32.dp))
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
            ) {
                Spacer(Modifier.height(24.dp))

                ProfileRow(
                    name = state.userName,
                    avatar = state.avatarUrl,
                    phone = state.phoneNumber
                )

                Spacer(Modifier.height(24.dp))

                ContainerItems {
                    SettingsItemRow(
                        item = SettingsItem(
                            title = stringResource(R.string.SetUsernameLabel),
                            iconResId = R.drawable.ic_divo_settings_username,
                            intent = SettingsViewIntent.OnSetUsernameClicked
                        ),
                        viewModel = viewModel
                    )
                    if (state.isModel) {
                        HorizontalDivider(modifier = Modifier.padding(start = 39.dp), color = dividerColor)
                        SettingsItemRow(
                            item = SettingsItem(
                                title = stringResource(R.string.FillYourParametersLabel),
                                iconResId = R.drawable.ic_divo_settings_parameters,
                                intent = SettingsViewIntent.OnFillParametersClicked
                            ),
                            viewModel = viewModel
                        )
                    }
                }

//                Spacer(Modifier.height(16.dp))
//
//                PromoCard(
//                    headline = stringResource(R.string.PromoCardHeadline),
//                    body = stringResource(R.string.PromoCardBody),
//                    buttonText = stringResource(R.string.PromoCardBtn),
//                    onClick = { viewModel.setIntent(SettingsViewIntent.OnPromoClicked) }
//                )

                Spacer(Modifier.height(16.dp))


                ContainerItems {
                    SettingsItemRow(
                        item = SettingsItem(
                            title = stringResource(R.string.SavedMessagesLabel),
                            iconResId = R.drawable.ic_divo_saved_message_icon,
                            intent = SettingsViewIntent.OnSavedMessagesClicked
                        ),
                        viewModel = viewModel
                    )
                }

                Spacer(Modifier.height(16.dp))

                ContainerItems {
                    SettingsItemRow(
                        item = SettingsItem(
                            title = stringResource(R.string.NotificationsAndSoundsLabel),
                            iconResId = R.drawable.ic_divo_settings_notification,
                            intent = SettingsViewIntent.OnNotificationsClicked
                        ),
                        viewModel = viewModel
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 39.dp), color = dividerColor)
                    SettingsItemRow(
                        item = SettingsItem(
                            title = stringResource(R.string.PrivacyAndSecurityLabel),
                            iconResId = R.drawable.ic_divo_settings_privacy_and_security,
                            intent = SettingsViewIntent.OnPrivacyClicked
                        ),
                        viewModel = viewModel
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 39.dp), color = dividerColor)
                    SettingsItemRow(
                        item = SettingsItem(
                            title = stringResource(R.string.DataAndStorageLabel),
                            iconResId = R.drawable.ic_divo_settings_data_and_storage,
                            intent = SettingsViewIntent.OnDataStorageClicked
                        ),
                        viewModel = viewModel
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 39.dp), color = dividerColor)
                    SettingsItemRow(
                        item = SettingsItem(
                            title = stringResource(R.string.Language),
                            iconResId = R.drawable.msg_language,
                            intent = SettingsViewIntent.OnLanguageClicked
                        ),
                        viewModel = viewModel
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 39.dp), color = dividerColor)
                    SettingsItemRow(
                        item = SettingsItem(
                            title = stringResource(R.string.MeasuringSystemLabel),
                            iconResId = R.drawable.ic_divo_filter,
                            intent = SettingsViewIntent.OnMeasuringSystemClicked
                        ),
                        value = if (state.measuringSystem == DivoSettings.SYSTEM_METRIC) stringResource(R.string.MeasuringSystemMetric) else stringResource(R.string.MeasuringSystemImperial),
                        viewModel = viewModel,
                    )
//                    HorizontalDivider(modifier = Modifier.padding(start = 39.dp), color = dividerColor)
//                    SettingsItemRow(
//                        item = SettingsItem(
//                            title = stringResource(R.string.AppearanceLabel),
//                            iconResId = R.drawable.ic_divo_apperance,
//                            intent = SettingsViewIntent.OnAppearanceClicked
//                        ),
//                        viewModel = viewModel
//                    )
                }

                Spacer(Modifier.height(16.dp))

                ContainerItems {
                    SettingsItemRow(
                        item = SettingsItem(
                            title = stringResource(R.string.LogOutLabel),
                            iconResId = R.drawable.ic_divo_logout,
                            intent = SettingsViewIntent.OnLogoutClicked
                        ),
                        viewModel = viewModel
                    )
                }

                Spacer(Modifier.height(76.dp))
            }
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}