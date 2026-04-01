package org.telegram.divo.screen.event_list.components

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.screen.event_list.EventCtaType
import org.telegram.divo.style.AppTheme

@Composable
fun EventItemView(
    modifier: Modifier = Modifier,
    eventName: String,
    isModel: Boolean,
    eventImageUrl: String = "",
    ctaText: String,
    ctaType: EventCtaType,
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
                url = eventImageUrl,
                hazeState = hazeState
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .align(Alignment.BottomStart),
            ) {
                Text(
                    text = eventName.uppercase(),
                    style = AppTheme.typography.textEventTitle,
                    color = AppTheme.colors.textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "June 26 · 5:00 PM · \uD83C\uDDFA\uD83C\uDDF8 New York",
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

                    DurationChip(modifier = Modifier, text = "4d : 4h : 0m", hazeState = hazeState)

                    if (isModel) {
                        EventCtaButton(
                            text = ctaText,
                            type = ctaType,
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
    modifier: Modifier,
    text: String,
    hazeState: HazeState,
) {
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
            modifier = Modifier.padding(horizontal = 12.dp).offset(y = 1.dp),
            text = text,
            style = AppTheme.typography.helveticaNeueRegular,
            fontSize = 12.sp,
            color = AppTheme.colors.textColor,
        )
    }
}

@Composable
private fun EventCtaButton(
    text: String,
    type: EventCtaType,
    onClick: () -> Unit,
) {
    val (backgroundColor, contentColor) = when (type) {
        EventCtaType.Apply -> AppTheme.colors.accentOrange to AppTheme.colors.buttonTextColor
        EventCtaType.MyEvent -> Color(0xFFF2F2F2) to Color(0xFF2F2F2F)
    }

    Surface(
        modifier = Modifier
            .height(22.dp)
            .clickable(onClick = onClick),
        color = backgroundColor,
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
                color = contentColor,
            )
        }
    }
}