package org.telegram.divo.screen.event_details.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.utils.DivoShareType
import org.telegram.divo.common.utils.DivoSharingHelper
import org.telegram.divo.common.utils.toEventDisplayDate
import org.telegram.divo.common.utils.toShortString
import org.telegram.divo.components.DivoChip
import org.telegram.divo.components.RoundedGlassButton
import org.telegram.divo.components.RoundedGlassContainer
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.entity.EventDetails
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun EventDetailsHeader(
    event: EventDetails?,
    isModel: Boolean,
    onMenuClicked: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Background(
            backgroundUrl = event?.creator?.photo?.fullUrl,
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 38.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ButtonsSection(
                modifier = Modifier,
                event = event,
                onMenuClicked = onMenuClicked,
                onBack = onBack
            )
            ContentSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                event = event,
                isModel = isModel
            )
        }
    }
}

@Composable
private fun ButtonsSection(
    modifier: Modifier = Modifier,
    event: EventDetails?,
    onMenuClicked: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RoundedGlassButton(
                onClick = onBack
            )
            RoundedGlassContainer(
                space = 10.dp
            ) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .clickableWithoutRipple {
                            DivoSharingHelper.share(
                                context = context,
                                type = DivoShareType.EVENT,
                                id = event?.id,
                                customMessage = "${event?.title} - ${event?.creator?.roleLabel}",
                                imageUrl = event?.creator?.photo?.fullUrl
                            )
                        },
                    painter = painterResource(R.drawable.ic_divo_share_model),
                    contentDescription = null,
                    tint = AppTheme.colors.onBackground
                )
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .clickableWithoutRipple { onMenuClicked() },
                    painter = painterResource(R.drawable.ic_ab_other),
                    contentDescription = null,
                    tint = AppTheme.colors.onBackground
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Spacer(Modifier.height(16.dp))
            EngagementItem(
                resId = R.drawable.ic_divo_favorite,
                count = event?.appliesCount ?: 0 //TODO понять что выводить
            )
            Spacer(Modifier.height(10.dp))
            EngagementItem(
                resId = R.drawable.ic_divo_visibility,
                count = event?.viewsCount ?: 0 //TODO понять что выводить
            )
            Spacer(Modifier.height(10.dp))
            EngagementItem(
                resId = R.drawable.ic_divo_bookmark_glass,
                count = event?.userReachCount ?: 0 //TODO понять что выводить
            )
        }
    }
}

@Composable
private fun ContentSection(
    modifier: Modifier = Modifier,
    event: EventDetails?,
    isModel: Boolean,
) {
    Column(
        modifier = modifier
    ) {
        event?.let {
            Text(
                text = it.title.orEmpty(),
                style = AppTheme.typography.displayLarge,
                color = AppTheme.colors.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                DivoChip(
                    text = it.type.orEmpty(),
                    contentPadding = PaddingValues(8.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = it.date?.toEventDisplayDate(it.address?.countryCode, it.address?.cityName).orEmpty(),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.onBackground
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RoundedGlassContainer(
                    height = 36.dp,
                    space = 4.dp
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_divo_person_heart),
                        contentDescription = null,
                        tint = AppTheme.colors.onBackground
                    )
                    Text(
                        modifier = Modifier.offset(y = 2.dp),
                        text = pluralStringResource(R.plurals.ParticipantsCount, event.appliesCount, event.appliesCount),
                        style = AppTheme.typography.helveticaNeueLtCom,
                        color = AppTheme.colors.onBackground,
                        fontSize = 13.sp
                    )
                }

                if (isModel) {
                    UIButtonNew(
                        text = stringResource(R.string.ButtonApply),
                        textStyle = AppTheme.typography.helveticaNeueLtCom.copy(
                            fontSize = 14.sp,
                            color = AppTheme.colors.onBackground
                        ),
                        height = 36.dp,
                        onClick = {}
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun EngagementItem(
    @DrawableRes resId: Int,
    count: Int,
) {
    RoundedGlassContainer(
        modifier = Modifier.width(56.dp),
        height = 30.dp,
        space = 4.dp,
        contentPadding = PaddingValues(horizontal = 6.dp)
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(resId),
            contentDescription = null,
            tint = AppTheme.colors.onBackground
        )
        Text(
            modifier = Modifier.offset(y = 0.5.dp),
            text = count.toShortString(),
            style = AppTheme.typography.helveticaNeueRegular,
            color = AppTheme.colors.onBackground,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun Background(
    modifier: Modifier = Modifier,
    backgroundUrl: String?,
) {
    val hazeState = remember { HazeState() }
    var componentHeight by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { componentHeight = it.height.toFloat() }
    ) {
        DivoAsyncImage(
            modifier = Modifier
                .hazeSource(state = hazeState),
            model = backgroundUrl,
            loadingContent = {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFBF7A54)))
            },
            errorContent = {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFBF7A54)))
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = Color.Black,
                        blurRadius = 30.dp,
                        tints = listOf(HazeTint(Color.Black.copy(alpha = 0.2f)))
                    )
                ) {
                    progressive = HazeProgressive.verticalGradient(
                        startY = componentHeight * 0.65f,
                        startIntensity = 0f,
                        endY = componentHeight * 0.8f,
                        endIntensity = 1f,
                        easing = LinearEasing
                    )
                }
        )
    }
}