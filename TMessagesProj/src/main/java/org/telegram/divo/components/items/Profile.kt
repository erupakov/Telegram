package org.telegram.divo.components.items

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.components.RoleChip
import org.telegram.divo.components.TelegramUserAvatar
import org.telegram.divo.screen.profile.ProfileViewState
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R


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
fun DMButton(onClick: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = .15f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_divo_model_send_message),
            null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(6.dp))
        androidx.compose.material.Text(
            "Send DM",
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ProfileNameItem(
    modifier: Modifier = Modifier,
    uiState: ProfileViewState
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(horizontal = 16.dp)
    ) {

        Box(
            modifier = Modifier
                .size(82.dp),
            contentAlignment = Alignment.Center
        ) {
            TelegramUserAvatar(
                modifier  = Modifier
                    .size(68.dp)
                    .clip(CircleShape),
                photoUrl = uiState.userInfo?.avatarUrl,
                68
            )

            CircularProgressIndicator(
                progress = { 0.5f },
                modifier = Modifier.size(82.dp),
                strokeWidth = 2.dp,
                color = Color.Gray,
                trackColor = Color.Yellow,
                strokeCap = StrokeCap.Round

            )
        }

        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(
                modifier = Modifier.padding(end = 16.dp),
            ) {
                val iconId = "pro_badge"
                val text = buildAnnotatedString {
                    append(uiState.userInfo?.fullName?.uppercase().orEmpty())
                    append(" ")
                    appendInlineContent(iconId, "[icon]")
                }

                val inlineContent = mapOf(
                    iconId to InlineTextContent(
                        placeholder = Placeholder(
                            width = 24.sp,
                            height = 24.sp,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Top
                        )
                    ) {
                        Image(
                            modifier = Modifier.fillMaxSize().padding(top = 1.dp),
                            painter = painterResource(R.drawable.divo_pro_badge),
                            contentDescription = null,
                        )
                    }
                )

                Text(
                    text = text,
                    inlineContent = inlineContent,
                    style = AppTheme.typography.helveticaNeueLtCom,
                    color = AppTheme.colors.textColor,
                    fontSize = 34.sp,
                    lineHeight = 36.sp,
                    softWrap = true,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val age = uiState.userInfo?.birthday
                val city = uiState.userInfo?.city
                RoleChip(uiState.userInfo?.roleLabel?.lowercase().orEmpty())
                Spacer(modifier = Modifier.width(10.dp))
                if (age != null) {
                    Text(
                        text = "${uiState.formattedAge(age)} • ",
                        style = AppTheme.typography.helveticaNeueRegular,
                        fontSize = 14.sp,
                        color = AppTheme.colors.textColor,
                    )
                }
                if (city != null) {
                    Text(
                        text = uiState.countryFlagEmoji
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = city.name,
                        style = AppTheme.typography.helveticaNeueRegular,
                        fontSize = 14.sp,
                        color = AppTheme.colors.textColor,
                    )
                }
            }
        }
        Spacer(Modifier.width(10.dp))
    }
}
