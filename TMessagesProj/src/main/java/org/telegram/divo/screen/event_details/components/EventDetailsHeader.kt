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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

import org.telegram.divo.components.TelegramPhotoBackground

@Composable
fun EventDetailsHeader(
    event: EventDetails?,
    isModel: Boolean,
    isOwnEvent: Boolean,
    engagementsAlpha: Float = 1f,
    onEditEvent: () -> Unit,
    onCtaClicked: () -> Unit,
    onLikeClicked: () -> Unit,
) {
    val rawTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var topPadding by remember { mutableStateOf(rawTopPadding) }
    if (rawTopPadding > topPadding) {
        topPadding = rawTopPadding
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
    ) {
        TelegramPhotoBackground(
            photo = event?.files?.firstOrNull()?.fullUrl,
            fallbackResId = R.drawable.divo_event_placeholder,
            modifier = Modifier.fillMaxSize()
        )

        StatsSection(
            modifier = Modifier
                .padding(top = topPadding + 16.dp)
                .graphicsLayer { alpha = engagementsAlpha },
            event = event
        )
        ContentSection(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp),
            event = event,
            isOwnEvent = isOwnEvent,
            isModel = isModel,
            onEditEvent = onEditEvent,
            onCtaClicked = onCtaClicked
        )
    }
}

@Composable
private fun StatsSection(
    modifier: Modifier = Modifier,
    event: EventDetails?,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Space for fixed toolbar buttons
        Spacer(Modifier.height(48.dp))
        
        EngagementItem(
            resId = R.drawable.ic_divo_favorite,
            count = event?.appliesCount ?: 0
        )
        Spacer(Modifier.height(10.dp))
        EngagementItem(
            resId = R.drawable.ic_divo_visibility,
            count = event?.viewsCount ?: 0
        )
        Spacer(Modifier.height(10.dp))
        EngagementItem(
            resId = R.drawable.ic_divo_bookmark_glass,
            count = event?.userReachCount ?: 0
        )
    }
}

@Composable
private fun ContentSection(
    modifier: Modifier = Modifier,
    event: EventDetails?,
    isOwnEvent: Boolean,
    isModel: Boolean,
    onEditEvent: () -> Unit,
    onCtaClicked: () -> Unit,
) {
    Column(
        modifier = modifier
    ) {
        event?.let { eventDetails ->
            Row(
                modifier = Modifier.padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!eventDetails.type.isNullOrEmpty()) {
                    DivoChip(
                        modifier = Modifier.height(27.dp),
                        text = eventDetails.type,
                        background = getEventTypeColor(eventDetails.typeId, eventDetails.type),
                        textColor = Color.White,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                if (eventDetails.paymentTypeId == 1) { // Paid
                    DivoChip(
                        modifier = Modifier.height(27.dp),
                        text = eventDetails.paymentType.orEmpty(),
                        resId = R.drawable.ic_divo_paid,
                        background = Color.White.copy(alpha = 0.3f),
                        textColor = Color.White,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = eventDetails.title.orEmpty(),
                style = AppTheme.typography.displayLarge,
                color = AppTheme.colors.onBackground,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatEventSubtitle(eventDetails),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.onBackground
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusText = resolveEventStatus(
                        context = LocalContext.current,
                        dateFrom = eventDetails.date,
                        dateTo = eventDetails.dateTo,
                        applicationDeadline = eventDetails.applicationDeadline
                    )
                    if (statusText.isNotEmpty()) {
                        RoundedGlassContainer(
                            height = 36.dp,
                            background = Color.White.copy(alpha = 0.3f),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text(
                                text = statusText,
                                style = AppTheme.typography.helveticaNeueLtCom,
                                color = AppTheme.colors.onBackground,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                if (isModel) {
                    val isApplied = event.isApplied
                    val isClosed = isEventClosed(event.date ?: "", event.dateTo ?: "", event.applicationDeadline)
                    val buttonTextId = when {
                        isApplied -> R.string.ButtonApplied
                        isClosed -> R.string.ButtonClosed
                        else -> R.string.ButtonApply
                    }
                    val buttonIconResId = when {
                        isApplied -> R.drawable.divo_check_ic
                        else -> null
                    }
                    val buttonBgColor = when {
                        isClosed && !isApplied -> AppTheme.colors.onBackground
                        else -> AppTheme.colors.accentOrange
                    }
                    val buttonTextColor = when {
                        isClosed && !isApplied -> AppTheme.colors.textHintColor
                        else -> AppTheme.colors.onBackground
                    }

                    UIButtonNew(
                        text = stringResource(buttonTextId),
                        leadingIcon = buttonIconResId,
                        leadingIconTint = buttonTextColor,
                        textStyle = AppTheme.typography.helveticaNeueLtCom.copy(
                            fontSize = 14.sp,
                            color = buttonTextColor
                        ),
                        height = 36.dp,
                        background = buttonBgColor,
                        enabled = !isClosed,
                        onClick = onCtaClicked
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
    tint: Color = AppTheme.colors.onBackground,
    onClick: (() -> Unit)? = null
) {
    val baseModifier = Modifier.width(56.dp)
    val containerModifier = if (onClick != null) {
        baseModifier.clickableWithoutRipple { onClick() }
    } else {
        baseModifier
    }

    RoundedGlassContainer(
        modifier = containerModifier,
        height = 30.dp,
        space = 4.dp,
        contentPadding = PaddingValues(horizontal = 6.dp)
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(resId),
            contentDescription = null,
            tint = tint
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
    }
}

internal fun isEventClosed(dateFrom: String, dateTo: String, applicationDeadline: String?): Boolean {
    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
    val from = runCatching { formatter.parse(dateFrom) }.getOrNull() ?: return false
    val to = runCatching { formatter.parse(dateTo) }.getOrNull() ?: return false
    val deadline = applicationDeadline?.let { runCatching { formatter.parse(it) }.getOrNull() }
    val now = java.util.Date()

    return when {
        now.after(to) -> true
        deadline != null && now.after(deadline) -> true
        now.after(from) -> true
        else -> false
    }
}

private fun formatEventSubtitle(event: EventDetails): String {
    var subtitle = event.date?.toEventDisplayDate(
        countryCode = event.address?.countryCode,
        city = event.address?.cityName
    ).orEmpty()

    if (!event.cost.isNullOrEmpty() && (event.cost.toDoubleOrNull() ?: 0.0) > 0.0) {
        val formattedCost = event.cost.toDoubleOrNull()?.toInt()?.toString() ?: event.cost
        subtitle += " · $$formattedCost"
    }

    return subtitle
}

private fun resolveEventStatus(
    context: android.content.Context,
    dateFrom: String?,
    dateTo: String?,
    applicationDeadline: String?
): String {
    if (dateFrom == null || dateTo == null) return ""
    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
    val from = runCatching { formatter.parse(dateFrom) }.getOrNull() ?: return ""
    val to = runCatching { formatter.parse(dateTo) }.getOrNull() ?: return ""
    val deadline = applicationDeadline?.let { runCatching { formatter.parse(it) }.getOrNull() }
    val now = java.util.Date()

    return when {
        now.after(to) -> context.getString(R.string.EventStatusCompleted)
        now.after(from) && now.before(to) -> context.getString(R.string.EventStatusInProgress)
        deadline != null && now.after(deadline) -> {
            val locale = org.telegram.messenger.LocaleController.getInstance().currentLocale ?: java.util.Locale.getDefault()
            val dateFormat = java.text.SimpleDateFormat("MMM dd", locale)
            context.getString(R.string.EventStatusApplicationsClosedDate, dateFormat.format(deadline))
        }
        now.after(from) -> {
            val locale = org.telegram.messenger.LocaleController.getInstance().currentLocale ?: java.util.Locale.getDefault()
            val dateFormat = java.text.SimpleDateFormat("MMM dd", locale)
            context.getString(R.string.EventStatusApplicationsClosedDate, dateFormat.format(from))
        }
        else -> {
            val targetDate = deadline ?: from
            val diffMillis = targetDate.time - now.time
            val totalHours = diffMillis / 1000 / 60 / 60
            if (totalHours > 24) {
                val locale = org.telegram.messenger.LocaleController.getInstance().currentLocale ?: java.util.Locale.getDefault()
                val dateFormat = java.text.SimpleDateFormat("MMM dd", locale)
                context.getString(R.string.EventStatusDeadline, dateFormat.format(targetDate))
            } else {
                val totalMinutes = diffMillis / 1000 / 60
                val hours = totalMinutes / 60
                val minutes = totalMinutes % 60
                val formattedTime = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                context.getString(R.string.EventStatusClosesIn, formattedTime)
            }
        }
    }
}

private fun getEventTypeColor(typeId: Int?, type: String?): Color {
    if (typeId != null) {
        return when (typeId) {
            1 -> Color(0xFF185FA5) // Casting
            // TODO: add actual IDs here
            // X -> Color(0xFF534AB7) // Show
            281 -> Color(0xFF0F6E56) // Exhibition
            // X -> Color(0xFF888780) // Event
            // X -> Color(0xFFE8520A) // TFP
            // X -> Color(0xFF534AB7) // Dancer
            // X -> Color(0xFF0F6E56) // Hostess
            // X -> Color(0xFFE8520A) // Foreign / Contract
            else -> generateColorForId(typeId)
        }
    }
    return Color(0xFF185FA5)
}

private fun generateColorForId(id: Int): Color {
    val colors = listOf(
        Color(0xFF185FA5), Color(0xFF534AB7), Color(0xFF0F6E56),
        Color(0xFF888780), Color(0xFFE8520A), Color(0xFFD81B60),
        Color(0xFF8E24AA), Color(0xFF00897B), Color(0xFFF4511E)
    )
    return colors[id % colors.size]
}