package org.telegram.divo.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import org.telegram.divo.screen.event_list.EventListViewModel
import org.telegram.divo.style.AppTheme

@Preview
@Composable
private fun EventItemViewPreview() {
    EventItemView(
        eventName = "Fashion Model Event",
        eventImageUrl = "https://picsum.photos/200/300",
        eventOwnerName = "@NYFW",
        eventOwnerImage = "https://picsum.photos/200/301",
        dateLocationText = "May 27 · 5:00 PM · 🇺🇸 New York",
        durationText = "4d : 4h : 0m",
        ctaText = "Apply",
        ctaType = EventListViewModel.EventCtaType.Apply,
    )
}

@Composable
fun EventItemView(
    modifier: Modifier = Modifier,
    eventName: String,
    eventImageUrl: String,
    eventOwnerName: String,
    eventOwnerImage: String,
    dateLocationText: String,
    durationText: String,
    ctaText: String,
    ctaType: EventListViewModel.EventCtaType,
    onCardClick: () -> Unit = {},
    onCtaClicked: () -> Unit = {},
) {
    val painter = rememberAsyncImagePainter(eventImageUrl)
    val gradientOverlay = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x66000000),
                Color(0x00000000),
                Color(0xE6000000),
            ),
        )
    }

    Card(
        modifier = modifier
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RectangleShape
    ) {
        Box {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painter,
                contentDescription = eventName,
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradientOverlay),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .align(Alignment.BottomStart),
            ) {
                EventOwnerItem(text = eventOwnerName, imageUrl = eventOwnerImage)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = eventName.uppercase(),
                    style = AppTheme.typography.textEventTitle,
                    color = AppTheme.colors.textColor,
                    maxLines = 2,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = dateLocationText,
                    style = AppTheme.typography.textItemDate,
                    color = Color(0xFFE6E6E6),
                    maxLines = 2,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DurationChip(text = durationText)
                    EventCtaButton(
                        text = ctaText,
                        type = ctaType,
                        onClick = onCtaClicked,
                    )
                }
            }
        }
    }
}

@Composable
private fun DurationChip(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AppTheme.colors.containerDuration,
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp),
            text = text,
            style = AppTheme.typography.helvetica_neue_regular_10,
            color = AppTheme.colors.textColor,
        )
    }
}

@Composable
private fun EventCtaButton(
    text: String,
    type: EventListViewModel.EventCtaType,
    onClick: () -> Unit,
) {
    val (backgroundColor, contentColor) = when (type) {
        EventListViewModel.EventCtaType.Apply -> AppTheme.colors.buttonColor to AppTheme.colors.buttonTextColor
        EventListViewModel.EventCtaType.MyEvent -> Color(0xFFF2F2F2) to Color(0xFF2F2F2F)
    }

    Surface(
        modifier = Modifier
            .height(28.dp)
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = AppTheme.typography.textButtonSmall,
                color = contentColor,
            )
        }
    }
}

@Composable
fun EventOwnerItem(
    text: String,
    imageUrl: String,
) {
    val painter = rememberAsyncImagePainter(imageUrl)
    Row(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                modifier = Modifier.matchParentSize(),
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = text,
            style = AppTheme.typography.textButtonSmall,
            color = Color.White,
        )
    }
}
