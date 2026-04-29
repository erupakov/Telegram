package org.telegram.divo.screen.profile.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.hazeEffect
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.utils.formattedAge
import org.telegram.divo.common.utils.toCountryFlagEmoji
import org.telegram.divo.components.DivoPopupMenu
import org.telegram.divo.components.PopupMenuItem
import org.telegram.divo.components.RoundedGlassButton
import org.telegram.divo.components.RoundedGlassContainer
import org.telegram.divo.screen.profile.ProfileViewState
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun ToolBarBackground(
    modifier: Modifier = Modifier,
    transitionProgress: Float,
    hazeState: dev.chrisbanes.haze.HazeState? = null,
    lowerTabsOffsetPx: Float = Float.MAX_VALUE,
    isTabsPinned: Boolean = false
) {
    val statusBarPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    val extendedHeight = 48.dp
    val density = androidx.compose.ui.platform.LocalDensity.current
    val fullHeightPx = with(density) { (statusBarPadding + 56.dp + extendedHeight).toPx() }

    val currentBlurHeightPx = if (isTabsPinned) fullHeightPx else minOf(fullHeightPx, lowerTabsOffsetPx)

    val endYPx = with(density) { (statusBarPadding + 56.dp + extendedHeight).toPx() }
    val startYPx = with(density) { (statusBarPadding + 46.dp).toPx() }

    if (hazeState != null && transitionProgress > 0f) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(with(density) { currentBlurHeightPx.toDp() })
                .graphicsLayer { alpha = transitionProgress }
                .hazeEffect(
                    state = hazeState,
                    style = dev.chrisbanes.haze.HazeStyle(
                        backgroundColor = AppTheme.colors.backgroundLight,
                        blurRadius = 40.dp,
                        tints = listOf(
                            dev.chrisbanes.haze.HazeTint(AppTheme.colors.backgroundLight.copy(alpha = 0.75f))
                        )
                    )
                ) {
                    progressive = dev.chrisbanes.haze.HazeProgressive.verticalGradient(
                        startY = startYPx,
                        startIntensity = 1f,
                        endY = endYPx,
                        endIntensity = 0f,
                        easing = LinearEasing
                    )
                }
        )
    }
}

@Composable
fun ToolBarContent(
    modifier: Modifier = Modifier,
    uiState: ProfileViewState,
    isOwnProfile: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onEditProfileClicked: () -> Unit = {},
    onEditBackgroundClicked: () -> Unit = {},
    onEditSocialLinksClicked: () -> Unit = {},
    onManageWorkExperienceClicked: () -> Unit = {},
    onFindSimilarProfiles: () -> Unit = {},
    onReportProfile: () -> Unit = {},
    onBlockProfile: () -> Unit = {},
    isSolid: Boolean = false,
    transitionProgress: Float = 0f,
) {
    var showEditMenu by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val buttonBgColor = lerp(AppTheme.colors.onBackground.copy(alpha = 0.2f), Color.White, transitionProgress)
    val buttonIconColor = lerp(Color.White, Color.Black, transitionProgress)
    val buttonBorderColor = lerp(AppTheme.colors.onBackground.copy(alpha = 0.4f), Color.Transparent, transitionProgress)

    val options = listOf(
        PopupMenuItem(R.string.FindSimilarProfiles, onFindSimilarProfiles, R.drawable.ic_divo_face_rec),
        PopupMenuItem(R.string.ReportThisProfile, onReportProfile, R.drawable.ic_divo_report),
        PopupMenuItem(R.string.BlockProfile, onBlockProfile, R.drawable.ic_divo_block),
    )

    val editOptions = remember {
        buildList {
            add(PopupMenuItem(R.string.EditProfile, onEditProfileClicked))
            add(PopupMenuItem(R.string.ChangeProfileBackground, onEditBackgroundClicked))
            add(PopupMenuItem(R.string.EditSocialLinks, onEditSocialLinksClicked))
            if (uiState.isModel) add(PopupMenuItem(R.string.ManageWorkExperience, onManageWorkExperienceClicked))
        }
    }

    val statusBarPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    val menuOffset = IntOffset(x = -32, y = 0)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = statusBarPadding + 8.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        RoundedGlassButton(
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterStart),
            background = buttonBgColor,
            iconTint = buttonIconColor,
            borderColor = buttonBorderColor,
            onClick = onNavigateBack
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 56.dp),
            contentAlignment = Alignment.Center
        ) {
            TitleContent(
                uiState = uiState,
                isSolid = isSolid
            )
        }

        if (!isOwnProfile) {
            RoundedGlassButton(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                resId = R.drawable.ic_divo_menu_24,
                iconSize = 24.dp,
                background = buttonBgColor,
                iconTint = buttonIconColor,
                borderColor = buttonBorderColor,
                onClick = { showMenu = true }
            )
        } else {
            RoundedGlassContainer(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                contentPadding = PaddingValues(start = 10.dp, end = 14.dp),
                background = buttonBgColor,
                borderColor = buttonBorderColor
            ) {
                Icon(
                    modifier = Modifier
                        .size(22.dp)
                        .clickableWithoutRipple {  }, //TODO
                    painter = painterResource(R.drawable.ic_divo_rounded_plus),
                    contentDescription = null,
                    tint = buttonIconColor
                )
                Spacer(Modifier.width(18.dp))
                Icon(
                    modifier = Modifier
                        .size(16.dp)
                        .clickableWithoutRipple { showEditMenu = true },
                    painter = painterResource(R.drawable.ic_divo_edit_24),
                    contentDescription = null,
                    tint = buttonIconColor
                )
            }
        }

        DivoPopupMenu(
            visible = showMenu,
            onDismiss = { showMenu = false },
            items = options,
            offset = menuOffset
        )

        DivoPopupMenu(
            visible = showEditMenu,
            onDismiss = { showEditMenu = false },
            items = editOptions,
            offset = menuOffset
        )
    }
}

@Composable
private fun TitleContent(
    modifier: Modifier = Modifier,
    uiState: ProfileViewState,
    isSolid: Boolean = false
) {
    val animatable = remember { Animatable(0f) }
    val context = LocalContext.current
    
    LaunchedEffect(isSolid) {
        animatable.animateTo(
            targetValue = if (isSolid) 1f else 0f,
            animationSpec = tween(if (isSolid) 200 else 100)
        )
    }

    Column(
        modifier = modifier
            .graphicsLayer {
                val progress = animatable.value
                alpha = progress
                translationY = (1f - progress) * 15f
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = uiState.userInfo.fullName,
            style = AppTheme.typography.helveticaNeueRegular,
            fontSize = 14.sp,
            color = AppTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            uiState.userInfo.let { userInfo ->
                Text(
                    text = userInfo.roleLabel.lowercase(),
                    color = AppTheme.colors.textPrimary,
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 12.sp,
                )
                if (userInfo.birthday.isNotEmpty()) {
                    Text(
                        text = " · ${userInfo.birthday.formattedAge(context)} · ",
                        style = AppTheme.typography.helveticaNeueRegular,
                        color = AppTheme.colors.textPrimary,
                        fontSize = 12.sp,
                    )
                } else {
                    Spacer(modifier = Modifier.width(2.dp))
                }

                if (userInfo.city != null) {
                    Text(
                        text = userInfo.city.countryCode.toCountryFlagEmoji()
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = userInfo.city.name,
                        style = AppTheme.typography.helveticaNeueRegular,
                        color = AppTheme.colors.textPrimary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}