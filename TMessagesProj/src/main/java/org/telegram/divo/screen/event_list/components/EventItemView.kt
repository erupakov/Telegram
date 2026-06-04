package org.telegram.divo.screen.event_list.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.delay
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.utils.toEventDisplayDate
import org.telegram.divo.entity.Event
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EventItemView(
    modifier: Modifier = Modifier,
    event: Event,
    isModel: Boolean,
    onCardClick: () -> Unit = {},
    onCtaClicked: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .clickableWithoutRipple(onClick = onCardClick),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.onBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        val hazeState = remember { HazeState() }

        Box {
            val backgroundUrl = event.files.firstOrNull()?.fullUrl
                ?: event.creator?.avatar?.fullUrl
                ?: event.creator?.photo?.fullUrl.orEmpty()
            
            EventItemBackground(
                url = backgroundUrl,
                hazeState = hazeState
            )
            DurationChip(
                modifier = Modifier.padding(start = 12.dp, top = 12.dp),
                dateFrom = event.date,
                dateTo = event.dateTo,
                applicationDeadline = event.applicationDeadline,
                hazeState = hazeState
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.BottomStart),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    val hasPayment = event.paymentTypeId == 1
                    if (!event.type.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(if (hasPayment) 0.6f else 1f, fill = false)
                                .height(22.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(getEventTypeColor(event.typeId, event.type)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                text = event.type,
                                style = AppTheme.typography.helveticaNeueRegular,
                                fontSize = 10.sp,
                                color = AppTheme.colors.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (hasPayment) {
                        EventPreviewGlassChip(
                            hazeState = hazeState,
                            text = event.paymentType ?: "Paid",
                            iconResId = R.drawable.ic_divo_paid
                        )
                    }
                }

                Row(
                    modifier = Modifier.padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DivoAsyncImage(
                        model = event.creator?.avatar?.fullUrl,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape),
                        errorContent = {
                            Image(
                                painter = painterResource(R.drawable.divo_avatar_placeholder),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        modifier = Modifier.weight(1f, fill = false),
                        text = event.creator?.fullName.orEmpty(),
                        style = AppTheme.typography.helveticaNeueRegular,
                        fontSize = 10.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (event.creator?.isVerified == true) {
                        Spacer(Modifier.width(4.dp))
                        Image(
                            painter = painterResource(id = R.drawable.ic_divo_verified),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Text(
                    text = event.title.orEmpty(),
                    style = AppTheme.typography.textEventTitle.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                    color = AppTheme.colors.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = event.date.toEventDisplayDate(countryCode = event.countryCode, city = event.city),
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 10.sp,
                    color = AppTheme.colors.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (event.maxAttendees != null && event.maxAttendees > 0) {
                    Spacer(Modifier.height(10.dp))
                    val spotsLeft = event.maxAttendees - event.appliesCount
                    Box(
                        modifier = Modifier
                            .height(22.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            text = stringResource(id = R.string.EventSpotsLeft, spotsLeft),
                            style = AppTheme.typography.helveticaNeueRegular,
                            fontSize = 10.sp,
                            color = AppTheme.colors.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (isModel) {
                    Spacer(Modifier.height(10.dp))
                    val isApplied = event.isApplied
                    val isClosed = isEventClosed(event.date, event.dateTo, event.applicationDeadline)
                    
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
                        isClosed && !isApplied -> AppTheme.colors.backgroundLight
                        else -> AppTheme.colors.accentOrange
                    }
                    val buttonTextColor = when {
                        isClosed && !isApplied -> AppTheme.colors.textHintColor
                        else -> AppTheme.colors.onBackground
                    }

                    EventCtaButton(
                        text = stringResource(buttonTextId),
                        iconResId = buttonIconResId,
                        bgColor = buttonBgColor,
                        textColor = buttonTextColor,
                        enabled = !isClosed,
                        onClick = onCtaClicked,
                    )
                }
            }
        }
    }
}

@Composable
private fun DurationChip(
    modifier: Modifier = Modifier,
    dateFrom: String,
    dateTo: String,
    applicationDeadline: String?,
    hazeState: HazeState,
) {
    val context = LocalContext.current

    var countdownText by remember {
        mutableStateOf(resolveLabel(context, dateFrom, dateTo, applicationDeadline))
    }

    LaunchedEffect(dateFrom, dateTo, applicationDeadline) {
        while (true) {
            countdownText = resolveLabel(context, dateFrom, dateTo, applicationDeadline)
            delay(60_000L)
        }
    }

    Box(
        modifier = modifier
            .height(22.dp)
            .clip(RoundedCornerShape(12.dp))
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = Color.Black.copy(alpha = 0.3f),
                    blurRadius = 20.dp,
                    tints = listOf(HazeTint(Color.White.copy(alpha = 0.05f)))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 10.dp),
            text = countdownText,
            style = AppTheme.typography.helveticaNeueRegular,
            fontSize = 10.sp,
            color = AppTheme.colors.textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun resolveLabel(context: Context, dateFrom: String, dateTo: String, applicationDeadline: String?): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val from = runCatching { formatter.parse(dateFrom) }.getOrNull() ?: return ""
    val to = runCatching { formatter.parse(dateTo) }.getOrNull() ?: return ""
    val deadline = applicationDeadline?.let { runCatching { formatter.parse(it) }.getOrNull() }
    val now = Date()

    return when {
        now.after(to) -> context.getString(R.string.EventStatusCompleted)
        now.after(from) && now.before(to) -> context.getString(R.string.EventStatusInProgress)
        deadline != null && now.after(deadline) -> context.getString(R.string.EventStatusApplicationsClosed)
        now.after(from) -> context.getString(R.string.EventStatusApplicationsClosed) // Fallback if no deadline but event started
        else -> {
            val targetDate = deadline ?: from
            val diffMillis = targetDate.time - now.time
            val totalHours = diffMillis / 1000 / 60 / 60
            if (totalHours > 24) {
                val locale = org.telegram.messenger.LocaleController.getInstance().currentLocale ?: Locale.getDefault()
                val dateFormat = SimpleDateFormat("MMM dd", locale)
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

@Composable
private fun EventCtaButton(
    text: String,
    iconResId: Int? = null,
    bgColor: Color = AppTheme.colors.accentOrange,
    textColor: Color = AppTheme.colors.onBackground,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clickableWithoutRipple(enabled = enabled, onClick = onClick),
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (iconResId != null) {
                androidx.compose.material3.Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = textColor
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                modifier = Modifier,
                text = text,
                fontSize = 12.sp,
                style = AppTheme.typography.textButtonSmall,
                color = textColor,
            )
        }
    }
}

private fun isEventClosed(dateFrom: String, dateTo: String, applicationDeadline: String?): Boolean {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val from = runCatching { formatter.parse(dateFrom) }.getOrNull() ?: return false
    val to = runCatching { formatter.parse(dateTo) }.getOrNull() ?: return false
    val deadline = applicationDeadline?.let { runCatching { formatter.parse(it) }.getOrNull() }
    val now = Date()

    return when {
        now.after(to) -> true
        deadline != null && now.after(deadline) -> true
        now.after(from) -> true
        else -> false
    }
}

private fun getEventTypeColor(typeId: Int?, type: String?): Color {
    if (typeId != null) {
        return when (typeId) {
            1 -> Color(0xFF185FA5)
            281 -> Color(0xFF0F6E56)
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

@Composable
private fun EventPreviewGlassChip(
    hazeState: HazeState,
    text: String,
    iconResId: Int? = null
) {
    Box(
        modifier = Modifier
            .height(22.dp)
            .clip(RoundedCornerShape(14.dp))
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = Color.White.copy(alpha = 0.2f),
                    blurRadius = 20.dp,
                    tints = listOf(HazeTint(Color.White.copy(alpha = 0.1f)))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconResId != null && iconResId != 0) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = AppTheme.colors.onBackground,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                modifier = Modifier,
                text = text,
                style = AppTheme.typography.helveticaNeueRegular,
                fontSize = 10.sp,
                color = AppTheme.colors.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}