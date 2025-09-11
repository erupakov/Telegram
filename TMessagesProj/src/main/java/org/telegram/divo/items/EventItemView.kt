package org.telegram.divo.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import org.telegram.divo.components.UIButtonSmall
import org.telegram.divo.style.AppTheme

@Preview
@Composable
private fun EventItemViewPreview(
) {
    EventItemView(
        modifier = Modifier.size(height = 230.dp, width = 182.dp),
        eventName = "fashion model event",
        eventImageUrl = "https://picsum.photos/200/300",
        eventOwnerName = "@NYFW",
        eventOwnerImage = "https://picsum.photos/200/300",
        dateLocationText = "May 27 · 5:00 PM · \uD83C\uDDFA\uD83C\uDDF8 New York",
        durationText = "4d : 4h : 0m",
        onApplyClicked = {

        }
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
    onApplyClicked: () -> Unit = {}
) {
    val painter = rememberAsyncImagePainter(eventImageUrl)

    Card(modifier = modifier, shape = RectangleShape) {
        Box(contentAlignment = Alignment.BottomStart) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painter,
                contentDescription = eventImageUrl,
                contentScale = ContentScale.FillBounds
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            ) {
                EventOwnerItem(text = eventOwnerName, imageUrl = eventOwnerImage)
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = eventName,
                    style = AppTheme.typography.textEventTitle,
                    color = AppTheme.colors.textColor,
                    minLines = 2,
                    maxLines = 2
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = dateLocationText,
                    style = AppTheme.typography.textItemDate,
                    color = AppTheme.colors.textHintColor
                )
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    Card(
                        modifier = Modifier.size(height = 22.dp, width = 68.dp),
                        backgroundColor = AppTheme.colors.containerDuration
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                durationText,
                                style = AppTheme.typography.helvetica_neue_regular_10,
                                color = AppTheme.colors.textColor
                            )
                        }

                    }

                    Spacer(Modifier.weight(1f))

                    UIButtonSmall()
                }
            }
        }
    }

}

@Composable
fun EventOwnerItem(
    text: String,
    imageUrl: String
) {
    val painter = rememberAsyncImagePainter(imageUrl)
    Row(modifier = Modifier, verticalAlignment = Alignment.CenterVertically) {
        Card(modifier = Modifier.size(28.dp), shape = CircleShape) {
            Image(
                modifier = Modifier.size(28.dp),
                painter = painter,
                contentDescription = null,
            )
        }
        Text(
            modifier = Modifier.padding(start = 4.dp),
            text = text,
            style = AppTheme.typography.textButtonSmall,
            color = AppTheme.colors.textColor
        )
    }
}
