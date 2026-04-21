package org.telegram.divo.screen.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.SnackbarEvent
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R
import org.telegram.tgnet.TLRPC

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
    navigateToSetUsername: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarState = remember { AppSnackbarHostState() }
    val context = LocalContext.current

    val dividerColor = Color(0x1A000000)
    val scrollState = rememberScrollState()

    // Refresh user data when screen becomes visible
    LifecycleResumeEffect(Unit) {
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

                SettingsViewEffect.ShowQrCode -> {
                    // TODO: Implement QR code screen
                }

                is SettingsViewEffect.ShowError -> {
                    snackbarState.show(
                        SnackbarEvent.ErrorWithRetry(
                            message = it.message,
                            actionLabel = context.getString(R.string.RetryLabel),
                            onRetry = { viewModel.setIntent(SettingsViewIntent.OnRefresh) }
                        )
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = AppTheme.colors.backgroundLight,
        topBar = {
            SettingsTopBar(
                onAction = { viewModel.setIntent(SettingsViewIntent.OnEditProfileClicked) },
                onQrCode = { viewModel.setIntent(SettingsViewIntent.OnQrCodeClicked) }
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
                modifier = Modifier.fillMaxSize().padding(padding),
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
                    avatar = state.avatarUrl
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

                Spacer(Modifier.height(16.dp))

                PromoCard(
                    headline = stringResource(R.string.PromoCardHeadline),
                    body = stringResource(R.string.PromoCardBody),
                    buttonText = stringResource(R.string.PromoCardBtn),
                    onClick = { viewModel.setIntent(SettingsViewIntent.OnPromoClicked) }
                )

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
                            title = stringResource(R.string.AppearanceLabel),
                            iconResId = R.drawable.ic_divo_apperance,
                            intent = SettingsViewIntent.OnAppearanceClicked
                        ),
                        viewModel = viewModel
                    )
                }

                Spacer(Modifier.height(76.dp))
            }
        }
    }
}

@Composable
private fun SettingsTopBar(
    onAction: () -> Unit,
    onQrCode: () -> Unit
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topBarHeight = 56.dp

    Box(
        modifier = Modifier
            .height(statusBarHeight + topBarHeight)
            .fillMaxWidth()
            .padding(top = statusBarHeight),
        contentAlignment = Alignment.CenterStart
    ) {
        RoundedButton(
            modifier = Modifier.padding(start = 16.dp).align(Alignment.CenterStart),
            resId = R.drawable.ic_qr_code,
            iconSize = 24.dp,
            iconTint = Color.Red,
            onClick = onQrCode
        )
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(R.string.SettingsTitle).uppercase(),
            style = AppTheme.typography.appBar
        )
        Text(
            modifier = Modifier.padding(end = 16.dp).align(Alignment.CenterEnd).clickableWithoutRipple { onAction() },
            text = stringResource(R.string.EditBtn),
            style = AppTheme.typography.helveticaNeueRegular,
            fontSize = 15.sp,
            color = AppTheme.colors.accentOrange
        )
    }
}

@Composable
private fun ProfileRow(
    name: String,
    avatar: String,
) {
    Column(
        Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DivoAsyncImage(
            modifier = Modifier
                .size(94.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.onBackground),
            model = avatar,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = name,
            style = AppTheme.typography.helveticaNeueRegular,
            color = AppTheme.colors.textPrimary,
            fontSize = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,

            )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "+7 999 999 99 99",
            style = AppTheme.typography.helveticaNeueRegular,
            color = Color.Red, //AppTheme.colors.textPrimary.copy(0.6f)
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun PromoCard(
    headline: String,
    body: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.img_divo_settings_item_cover),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(110.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xCC000000), Color.Transparent),
                            startY = 400f, endY = 0f
                        )
                    )
            )
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Spacer(Modifier.height(4.dp))
                Image(
                    painter = painterResource(R.drawable.divo_logo_onboarding),
                    contentDescription = null,
                    modifier = Modifier.height(30.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = headline,
                    style = AppTheme.typography.helveticaNeueLtCom.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        fontSize = 18.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = body,
                    style = AppTheme.typography.helveticaNeueLtCom.copy(
                        color = Color(0xFFEDEDED)
                    ),
                    maxLines = 2,
                    lineHeight = 14.sp,
                    fontSize = 12.sp,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
                TextButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(99.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.Red,
                    )
                ) {
                    Text(
                        modifier = Modifier.offset(y = 1.dp),
                        text = buttonText,
                        style = AppTheme.typography.helveticaNeueLtCom,
                        fontSize = 16.sp,
                        color = AppTheme.colors.buttonTextColor
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsItemRow(
    item: SettingsItem,
    viewModel: SettingsViewModel,

) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.onBackground)
            .clickableWithoutRipple(onClick = { viewModel.setIntent(item.intent) }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconWithBox(
            iconResId = item.iconResId
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = item.title,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.weight(1f))
        Icon(
            painter = painterResource(R.drawable.ic_divo_arrow_right_20),
            contentDescription = null,
            tint = AppTheme.colors.backgroundDark
        )
    }
}

@Composable
private fun ContainerItems(
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppTheme.colors.onBackground)
            .padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            content()
        }
    }
}

@Composable
fun IconWithBox(
    @DrawableRes
    iconResId: Int
) {
    Box(
        Modifier
            .size(29.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(AppTheme.colors.backgroundLight),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(iconResId),
            contentDescription = null,
            tint = AppTheme.colors.textPrimary.copy(0.8f)
        )
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}