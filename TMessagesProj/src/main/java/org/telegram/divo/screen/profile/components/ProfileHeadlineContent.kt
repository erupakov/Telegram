package org.telegram.divo.screen.profile.components

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.utils.DivoShareType
import org.telegram.divo.common.utils.DivoSharingHelper
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.RoundedGlassButton
import org.telegram.divo.components.RoundedGlassContainer
import org.telegram.divo.components.TelegramPhotoBackground
import org.telegram.divo.components.items.ProfileNameItem
import org.telegram.divo.entity.SocialNetworkType
import org.telegram.divo.screen.profile.ProfileViewState
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R
import org.telegram.messenger.R.drawable

@Composable
fun ProfileHeadlineContent(
    modifier: Modifier = Modifier,
    uiState: ProfileViewState,
    onEditLinksClicked: () -> Unit,
    showWorkHistory: () -> Unit,
    onStatsClicked: (StatsType) -> Unit,
    onSocialLinkClicked: (SocialNetworkType) -> Unit = {},
    onSendDMClicked: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val engagementsTopPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 64.dp

    Box(
        modifier = modifier
    ) {
        TelegramPhotoBackground(
            modifier = Modifier.fillMaxSize(),
            photo = uiState.userInfo.photoUrl,
        )

        if (uiState.backgroundChanging) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 100.dp)
            ) {
                LottieProgressIndicator(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center),
                    color = Color.White
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            EngagementsColumn(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = engagementsTopPadding, start = 16.dp, end = 16.dp),
                uiState = uiState,
                onLikesClick = { id, isLiked -> },//TODO
                onViewsClick = {},//TODO
                onMarkClick = {},//TODO
                onStatsClicked = onStatsClicked
            )

            Column(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            ) {
                ProfileNameItem(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    uiState
                )

                if (!uiState.isOwnProfile) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    ) {
                        RoundedGlassContainer(
                            height = 40.dp,
                            background = Color.Red, //AppTheme.colors.onBackground.copy(alpha = 0.3f)
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickableWithoutRipple { onSendDMClicked() }
                            ) {
                                Icon(
                                    modifier = Modifier.size(16.dp),
                                    painter = painterResource(drawable.ic_divo_send),
                                    contentDescription = null,
                                    tint = AppTheme.colors.onBackground,
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    modifier = Modifier.offset(y = 1.dp),
                                    text = stringResource(R.string.SendDM),
                                    style = AppTheme.typography.helveticaNeueLtCom,
                                    color = AppTheme.colors.onBackground,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        RoundedGlassButton(
                            resId = R.drawable.ic_divo_share_model,
                            iconSize = 24.dp,
                            onClick = {
                                val user = uiState.userInfo

                                DivoSharingHelper.share(
                                    context = context,
                                    scope = scope,
                                    type = DivoShareType.PROFILE,
                                    id = user.id,
                                    customMessage = "${user.fullName} - ${user.roleLabel}",
                                    imageUrl = user.photoUrl
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(22.dp))
            }
        }
    }
}