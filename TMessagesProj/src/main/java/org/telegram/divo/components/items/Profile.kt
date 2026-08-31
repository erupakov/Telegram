package org.telegram.divo.components.items

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.analytics.AnalyticsEvent
import org.telegram.divo.analytics.DivoAnalytics
import org.telegram.divo.common.compose.clickableWithoutRipple
import org.telegram.divo.common.utils.formattedAge
import org.telegram.divo.common.utils.toCountryFlagEmoji
import org.telegram.divo.components.media.DivoAvatar
import org.telegram.divo.components.inputs.DivoChip
import org.telegram.divo.screen.profile.ProfileViewState
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R
import org.telegram.ui.LaunchActivity
import org.telegram.ui.Stories.recorder.StoryRecorder

@Preview
@Composable
fun ButtonAddWorkHistory(
    modifier: Modifier = Modifier,
    value: String = "Add work history",
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .background(
                color = AppTheme.colors.blackAlpha12,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(R.drawable.ic_divo_add),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = value,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview
@Composable
fun ProfileSocialItem(
    modifier: Modifier = Modifier,
    @DrawableRes
    iconResId: Int = R.drawable.divo_pro_badge,
    value: String = "mock link",
) {
    Box(
        modifier = modifier
            .height(68.dp)
            .background(
                color = AppTheme.colors.blackAlpha12,
                shape = RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painterResource(iconResId),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

enum class BioDestination(
    val route: String,
    val label: String,
    val contentDescription: String
) {
    BIOGRAPHY("biography", "Biography", "biography"),
    APPEARANCE("Appearance", "Appearance", "Appearance"),
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ProfileBioItem(
    modifier: Modifier = Modifier,
    bio: String = "France's Top Model, World's Best Model 2024 Winner. France's Top Model France's Top ModelFrance's Top ModelFrance's Top ModelFrance's Top Model",
    appearance: String = "France's Top Model, World's Best Model 2024 Winner. France's Top Model France's Top ModelFrance's Top ModelFrance's Top ModelFrance's Top Model",
) {

    val pagerState = rememberPagerState(pageCount = {
        BioDestination.entries.size
    })
    Column(
        modifier = modifier.background(AppTheme.colors.blackAlpha12, shape = RoundedCornerShape(6.dp)),
    ) {
        val startDestination = BioDestination.BIOGRAPHY
        var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

        PrimaryTabRow(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = selectedDestination,
            containerColor = AppTheme.colors.blackAlpha12,
            indicator = {
                Spacer(
                    Modifier
                        .width(56.dp)
                        .height(4.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(
                                topStart = 5.dp,
                                topEnd = 5.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 0.dp
                            )
                        )
                )
            }
        ) {
            BioDestination.entries.forEachIndexed { index, destination ->
                Tab(
                    modifier = Modifier.width(100.dp),
                    selected = selectedDestination == index,
                    onClick = {
                        selectedDestination = index
                    },
                    selectedContentColor = Color.White,
                    text = {
                        Text(
                            text = destination.label
                        )
                    }
                )
            }
        }
        HorizontalPager(
            state = pagerState
        ) { page ->
            val text = if (page == 0) {
                bio
            } else {
                appearance
            }

            BioPageItem(
                text = text,
                buttonText = "see more",
                onClick = {}
            )
        }
    }
}

@Composable
fun BioPageItem(
    text: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
    ) {

        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 8.dp, end = 8.dp)
            ) {
                Text(text, color = Color.White, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }

            Box(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {})
            ) {
                Text(
                    text = buttonText,
                    color = Color.White,
                    modifier = Modifier.padding(8.dp)
                )

            }
        }
    }
}

@Composable
fun ProfileNameItem(
    modifier: Modifier = Modifier,
    uiState: ProfileViewState
) {
    val context = LocalContext.current

    val account = org.telegram.messenger.UserConfig.selectedAccount
    val controller = org.telegram.messenger.MessagesController.getInstance(account).storiesController
    val dialogId = if (uiState.isOwnProfile) {
        org.telegram.messenger.UserConfig.getInstance(account).clientUserId
    } else {
        uiState.userInfo.telegramId ?: 0L
    }

    var totalCount = 0
    var unreadCount = 0
    var hasStories = false
    var isLoading = false
    
    var updateTrigger by remember { mutableStateOf(0) }

    DisposableEffect(account) {
        val observer = org.telegram.messenger.NotificationCenter.NotificationCenterDelegate { _, _, _ ->
            updateTrigger++
        }
        val center = org.telegram.messenger.NotificationCenter.getInstance(account)
        center.addObserver(observer, org.telegram.messenger.NotificationCenter.storiesUpdated)
        center.addObserver(observer, org.telegram.messenger.NotificationCenter.storiesListUpdated)
        center.addObserver(observer, org.telegram.messenger.NotificationCenter.fileUploaded)
        center.addObserver(observer, org.telegram.messenger.NotificationCenter.fileUploadFailed)
        center.addObserver(observer, org.telegram.messenger.NotificationCenter.updateInterfaces)
        
        onDispose {
            center.removeObserver(observer, org.telegram.messenger.NotificationCenter.storiesUpdated)
            center.removeObserver(observer, org.telegram.messenger.NotificationCenter.storiesListUpdated)
            center.removeObserver(observer, org.telegram.messenger.NotificationCenter.fileUploaded)
            center.removeObserver(observer, org.telegram.messenger.NotificationCenter.fileUploadFailed)
            center.removeObserver(observer, org.telegram.messenger.NotificationCenter.updateInterfaces)
        }
    }

    if (dialogId != 0L) {
        val trigger = updateTrigger // read to subscribe to state changes
        val peerStories = try { 
            controller.dialogListStories?.find { org.telegram.messenger.DialogObject.getPeerDialogId(it.peer) == dialogId }
        } catch (e: Exception) { null }

        if (uiState.isOwnProfile) {
            hasStories = try { controller.hasSelfStories() } catch (e: Exception) { false }
        } else {
            hasStories = try { controller.hasStories(dialogId) } catch (e: Exception) { false }
        }
        isLoading = try { controller.hasUploadingStories(dialogId) } catch (e: Exception) { false }
        unreadCount = try { controller.getUnreadStoriesCount(dialogId) } catch (e: Exception) { 0 }
        totalCount = peerStories?.stories?.size ?: if (hasStories) 1 else 0
    }
    
    var isOnlineReal = false
    if (dialogId != 0L) {
        val trigger = updateTrigger
        val user = org.telegram.messenger.MessagesController.getInstance(account).getUser(dialogId)
        if (user != null && user.status != null) {
            isOnlineReal = user.status.expires > org.telegram.tgnet.ConnectionsManager.getInstance(account).currentTime
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .requiredSize(if (hasStories || isLoading) 68.dp else 64.dp)
                    .clickableWithoutRipple {
                        val fragment = LaunchActivity.getLastFragment() ?: return@clickableWithoutRipple
                        if (uiState.isOwnProfile && !hasStories) {
                            DivoAnalytics.logEvent(AnalyticsEvent.StoryAddClicked("profile"))
                            StoryRecorder.getInstance(fragment.parentActivity, account).open(null)
                        } else if (dialogId != 0L) {
                            val peerIds = arrayListOf(dialogId)
                            DivoAnalytics.logEvent(AnalyticsEvent.StoryOpened("profile_details"))
                            fragment.getOrCreateStoryViewer().open(
                                fragment.context, null, peerIds, 0, null, null, null, false
                            )
                        }
                    }
                    .then(
                        if (isLoading) Modifier
                        else if (!hasStories) Modifier
                        else Modifier.drawBehind {
                            val strokeWidth = 3.dp.toPx()
                            val gapAngle = if (totalCount > 1) 10f else 0f
                            val safeTotalCount = totalCount.coerceAtLeast(1)
                            val sweepAngle = (360f - (gapAngle * safeTotalCount)) / safeTotalCount

                            val unreadBrush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF990000), Color(0xFF000000))
                            )
                            val readBrush = Brush.horizontalGradient(
                                colors = listOf(Color.LightGray, Color.Gray)
                            )

                            var startAngle = -90f
                            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                            val safeUnreadCount = unreadCount.coerceAtMost(safeTotalCount)
                            val readCount = (safeTotalCount - safeUnreadCount).coerceAtLeast(0)

                            for (i in 0 until readCount) {
                                drawArc(
                                    brush = readBrush,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                    size = arcSize,
                                    topLeft = topLeft
                                )
                                startAngle += sweepAngle + gapAngle
                            }

                            for (i in 0 until safeUnreadCount) {
                                drawArc(
                                    brush = unreadBrush,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                    size = arcSize,
                                    topLeft = topLeft
                                )
                                startAngle += sweepAngle + gapAngle
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                DivoAvatar(
                    imageUrl = uiState.userInfo.avatarUrl,
                    isOnline = isOnlineReal,
                    showBorder = false,
                    avatarSize = 64.dp
                )
                if (isLoading) {
                    androidx.compose.material.CircularProgressIndicator(
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF990000),
                        strokeWidth = 3.dp
                    )
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    modifier = Modifier.weight(1f, fill = false),
                    text = uiState.userInfo.displayName,
                    style = AppTheme.typography.displayLarge.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    color = AppTheme.colors.textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (uiState.userInfo.isPremium) {
                    Spacer(Modifier.width(8.dp))
                    Image(
                        modifier = Modifier
                            .size(20.dp),
                        painter = painterResource(R.drawable.divo_premium_bage),
                        contentDescription = null,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val age = uiState.userInfo.birthday
                val city = uiState.userInfo.city
                DivoChip(
                    text = uiState.userInfo.roleLabel,
                    resId = if (uiState.userInfo.role.isModel()) R.drawable.ic_divo_person_heart else R.drawable.ic_divo_agency,
                    background = Color(0xFF2262D8),
                    textColor = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                if (age.isNotEmpty()) {
                    val ageText = age.formattedAge()
                    val textToDisplay = if (city != null) "$ageText · " else ageText
                    Text(
                        text = textToDisplay,
                        style = AppTheme.typography.helveticaNeueRegular,
                        fontSize = 14.sp,
                        color = AppTheme.colors.textColor,
                    )
                }
                if (city != null) {
                    Text(
                        text = uiState.userInfo.city.countryCode.toCountryFlagEmoji()
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = city.name,
                        style = AppTheme.typography.helveticaNeueRegular,
                        fontSize = 14.sp,
                        color = AppTheme.colors.textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
