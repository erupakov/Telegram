package org.telegram.divo.screen.event_list.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.delay
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.utils.toEventDisplayDate
import org.telegram.divo.entity.Event
import org.telegram.divo.screen.event_list.EventCtaType
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        val hazeState = remember { HazeState() }

        Box {
            EventItemBackground(
                url = event.creator?.avatar?.fullUrl.orEmpty(),
                hazeState = hazeState
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .align(Alignment.BottomStart),
            ) {
                Text(
                    text = event.title.orEmpty().uppercase(),
                    style = AppTheme.typography.textEventTitle,
                    color = AppTheme.colors.textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = event.date.toEventDisplayDate(city = event.city),
                    style = AppTheme.typography.textItemDate,
                    color = AppTheme.colors.textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    DurationChip(dateFrom = event.date, dateTo = event.dateTo, hazeState = hazeState)

                    if (isModel) {
                        EventCtaButton(
                            text = stringResource(R.string.ButtonApply),
                            onClick = onCtaClicked,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun DurationChip(
    dateFrom: String,
    dateTo: String,
    hazeState: HazeState,
) {
    val context = LocalContext.current

    var countdownText by remember {
        mutableStateOf(resolveLabel(context, dateFrom, dateTo))
    }

    LaunchedEffect(dateFrom, dateTo) {
        while (true) {
            countdownText = resolveLabel(context, dateFrom, dateTo)
            delay(60_000L)
        }
    }

    Box(
        modifier = Modifier
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
                .padding(horizontal = 12.dp)
                .offset(y = 1.dp),
            text = countdownText,
            style = AppTheme.typography.helveticaNeueRegular,
            fontSize = 12.sp,
            color = AppTheme.colors.textColor,
        )
    }
}

private fun resolveLabel(context: Context, dateFrom: String, dateTo: String): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val from = runCatching { formatter.parse(dateFrom) }.getOrNull() ?: return ""
    val to = runCatching { formatter.parse(dateTo) }.getOrNull() ?: return ""
    val now = Date()

    return when {
        now.after(to) -> context.getString(R.string.CountdownFinished)
        now.after(from) -> context.getString(R.string.CountdownStarted)
        else -> calculateCountdown(context, dateFrom, dateTo)
            ?: context.getString(R.string.CountdownFinished)
    }
}

private fun calculateCountdown(context: Context, dateFrom: String, dateTo: String): String? {
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val from = formatter.parse(dateFrom) ?: return null
        val to = formatter.parse(dateTo) ?: return null
        val now = Date()

        when {
            now.before(from) -> {
                val diffMillis = from.time - now.time
                formatCountdown(context, diffMillis)
            }
            now.after(to) -> null
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}

private fun formatCountdown(context: Context, diffMillis: Long): String {
    val totalMinutes = diffMillis / 1000 / 60
    val days = totalMinutes / (60 * 24)
    val hours = (totalMinutes % (60 * 24)) / 60
    val minutes = totalMinutes % 60
    return context.getString(R.string.CountdownFormat, days, hours, minutes)
}

@Composable
private fun EventCtaButton(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .height(22.dp)
            .clickable(onClick = onClick),
        color = AppTheme.colors.accentOrange,
        shape = RoundedCornerShape(12.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier.offset(y = 1.dp),
                text = text,
                style = AppTheme.typography.textButtonSmall,
                color = AppTheme.colors.onBackground,
            )
        }
    }
}