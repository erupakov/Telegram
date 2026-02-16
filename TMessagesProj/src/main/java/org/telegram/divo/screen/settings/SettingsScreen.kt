package org.telegram.divo.screen.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.components.TelegramUserAvatar
import org.telegram.divo.components.TextTitle
import org.telegram.divo.style.DivoFont.HelveticaNeue
import org.telegram.messenger.R
import org.telegram.tgnet.TLRPC

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    navigateToFillParameters: () -> Unit = {},
    navigateToProfile: () -> Unit = {},
    navigateToSavedMessages: () -> Unit = {},
    navigateToStickers: () -> Unit = {},
    navigateToNotifications: () -> Unit = {},
    navigateToPrivacy: () -> Unit = {},
    navigateToDataStorage: () -> Unit = {},
    navigateToAppearance: () -> Unit = {},
    navigateToSetUsername: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    val ctaColor = Color(0xFFBE7852)
    val dividerColor = Color(0x1A000000)
    val scrollState = rememberScrollState()

    // Refresh user data when screen becomes visible
    LifecycleResumeEffect(Unit) {
        viewModel.setIntent(SettingsViewModel.SettingsViewIntent.OnRefresh)
        onPauseOrDispose { }
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect {
            when (it) {
                SettingsViewModel.SettingsViewEffect.NavigateToAppearance -> {
                    navigateToAppearance()
                }

                SettingsViewModel.SettingsViewEffect.NavigateToDataStorage -> {
                    navigateToDataStorage()
                }

                SettingsViewModel.SettingsViewEffect.NavigateToEditProfile -> {
                    navigateToProfile()
                }

                SettingsViewModel.SettingsViewEffect.NavigateToFillParameters -> {
                    navigateToFillParameters()
                }

                SettingsViewModel.SettingsViewEffect.NavigateToNotifications -> {
                    navigateToNotifications()
                }

                SettingsViewModel.SettingsViewEffect.NavigateToPrivacy -> {
                    navigateToPrivacy()
                }

                SettingsViewModel.SettingsViewEffect.NavigateToProfile -> {
                    navigateToProfile()
                }

                SettingsViewModel.SettingsViewEffect.NavigateToPromo -> {
                    // TODO: Implement promo screen
                }

                SettingsViewModel.SettingsViewEffect.NavigateToSavedMessages -> {
                    navigateToSavedMessages()
                }

                SettingsViewModel.SettingsViewEffect.NavigateToSetUsername -> {
                    navigateToSetUsername()
                }

                SettingsViewModel.SettingsViewEffect.NavigateToStickers -> {
                    navigateToStickers()
                }

                SettingsViewModel.SettingsViewEffect.ShowQrCode -> {
                    // TODO: Implement QR code screen
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.padding(top = 32.dp),
        topBar = {
            SettingsTopBar(
                title = "SETTINGS",
                actionText = "Edit",
                onAction = { viewModel.setIntent(SettingsViewModel.SettingsViewIntent.OnEditProfileClicked) },
                onQrCode = { viewModel.setIntent(SettingsViewModel.SettingsViewIntent.OnQrCodeClicked) }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            ProfileRow(
                userFull = state.userFull,
                onClick = { viewModel.setIntent(SettingsViewModel.SettingsViewIntent.OnOpenProfileClicked) }
            )

            HorizontalDivider(color = dividerColor)

            SettingsItemRow(
                item = SettingsViewModel.SettingsItem(
                    title = "Set Username",
                    iconResId = R.drawable.ic_divo_settings_username,
                    intent = SettingsViewModel.SettingsViewIntent.OnSetUsernameClicked
                ),
                viewModel = viewModel
            )

            HorizontalDivider(color = dividerColor)

            Spacer(Modifier.height(12.dp))

            PrimaryWideButton(
                text = "Fill your parameters",
                background = ctaColor,
                onClick = { viewModel.setIntent(SettingsViewModel.SettingsViewIntent.OnFillParametersClicked) }
            )

            Spacer(Modifier.height(12.dp))

            PromoCard(
                headline = "GET DISCOVERED IN THE FASHION WORLD",
                body = "Publish your profile as a model, join castings or add events as agency — be part of the global fashion network.",
                buttonText = "LEARN MORE",
                onClick = { viewModel.setIntent(SettingsViewModel.SettingsViewIntent.OnPromoClicked) }
            )

            Spacer(Modifier.height(16.dp))

            HorizontalDivider(color = dividerColor)

            SettingsGroup(
                dividerColor = dividerColor,
                items = state.settingsItems,
                viewModel = viewModel
            )

            Spacer(Modifier.height(56.dp))
        }
    }
}

@Composable
private fun SettingsTopBar(
    title: String,
    actionText: String,
    onAction: () -> Unit,
    onQrCode: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier.weight(0.2f),
            onClick = onQrCode
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_qr_code),
                contentDescription = "QR Code",
                tint = Color(0xFFBE7852)
            )
        }
        TextTitle(
            text = title,
            modifier = Modifier.weight(1f),
            fontFamily = HelveticaNeue,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = actionText,
            style = MaterialTheme.typography.labelLarge.copy(
                color = Color(0xFFBE7852),
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(0.2f)
                .clickable(onClick = onAction)
        )
    }
}

@Composable
private fun ProfileRow(
    userFull: TLRPC.UserFull?,
    onClick: () -> Unit
) {
    val user = userFull?.user

    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFE6E6E6)),
            contentAlignment = Alignment.Center
        ) {
            TelegramUserAvatar(
                user = user,
                modifier  = Modifier
                    .size(54.dp)
                    .clip(CircleShape),
                68
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = ((user?.first_name ?: "") + " " + (user?.last_name ?: "")).trim(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 19.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,

                )
            Text(
                text = if (user?.phone != null) "+${user.phone}" else "",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF7A7A7A),
                    fontSize = 15.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_divo_arrow_right),
            contentDescription = null,
            tint = Color(0xff3C3C43)
        )
    }
}

@Composable
private fun PrimaryWideButton(
    text: String,
    background: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = background),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
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
        shape = RoundedCornerShape(10.dp),
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
                Image(
                    painter = painterResource(R.drawable.divo_logo_onboarding),
                    contentDescription = null,
                    modifier = Modifier.height(40.dp),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        fontSize = 14.sp
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFEDEDED)
                    ),
                    maxLines = 2,
                    fontSize = 12.sp,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
                TextButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color(0xFFBE7852),
                        contentColor = Color.White
                    )
                ) {
                    Text(buttonText, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun SettingsGroup(
    items: List<SettingsViewModel.SettingsItem>,
    dividerColor: Color,
    rowPaddingH: Dp = 16.dp,
    viewModel: SettingsViewModel
) {
    Column(Modifier.fillMaxWidth()) {
        items.forEachIndexed { idx, it ->
            SettingsItemRow(
                item = it,
                viewModel = viewModel,
                rowPaddingH = rowPaddingH
            )
            if (idx != items.lastIndex) HorizontalDivider(color = dividerColor)
        }
    }
}

@Composable
private fun SettingsItemRow(
    item: SettingsViewModel.SettingsItem,
    viewModel: SettingsViewModel,
    rowPaddingH: Dp = 16.dp
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = { viewModel.setIntent(item.intent) })
            .padding(horizontal = rowPaddingH, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconWithBox(
            iconResId = item.iconResId
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
        )
        Spacer(Modifier.weight(1f))
        Icon(
            painter = painterResource(R.drawable.ic_divo_arrow_right),
            contentDescription = null,
            tint = Color(0xff3C3C43)
        )
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
            .background(Color(0xFFF2E4DD)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(iconResId),
            contentDescription = null,
            tint = Color(0xFFBF7A54)
        )
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}