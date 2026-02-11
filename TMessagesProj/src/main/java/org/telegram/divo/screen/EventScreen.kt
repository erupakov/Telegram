package org.telegram.divo.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.telegram.divo.items.EventItemView

data class EventData(
    val name: String,
    val imageUrl: String,
    val ownerName: String,
    val ownerImage: String,
    val dateLocation: String,
    val duration: String
)

val mockEvents = listOf(
    EventData(
        name = "Fashion Gala",
        imageUrl = "https://picsum.photos/200/300?1",
        ownerName = "@FashionWeek",
        ownerImage = "https://picsum.photos/50/50?1",
        dateLocation = "Aug 9 · 6:00 PM · 🇺🇸 NYC",
        duration = "1d : 3h : 30m"
    ),
    EventData(
        name = "Art Expo",
        imageUrl = "https://picsum.photos/200/300?2",
        ownerName = "@ArtLovers",
        ownerImage = "https://picsum.photos/50/50?2",
        dateLocation = "Aug 12 · 4:00 PM · 🇫🇷 Paris",
        duration = "2d : 5h : 10m"
    ),
    EventData(
        name = "Fashion Gala",
        imageUrl = "https://picsum.photos/200/300?1",
        ownerName = "@FashionWeek",
        ownerImage = "https://picsum.photos/50/50?1",
        dateLocation = "Aug 9 · 6:00 PM · 🇺🇸 NYC",
        duration = "1d : 3h : 30m"
    ),
    EventData(
        name = "Art Expo",
        imageUrl = "https://picsum.photos/200/300?2",
        ownerName = "@ArtLovers",
        ownerImage = "https://picsum.photos/50/50?2",
        dateLocation = "Aug 12 · 4:00 PM · 🇫🇷 Paris",
        duration = "2d : 5h : 10m"
    ),
    EventData(
        name = "Fashion Gala",
        imageUrl = "https://picsum.photos/200/300?1",
        ownerName = "@FashionWeek",
        ownerImage = "https://picsum.photos/50/50?1",
        dateLocation = "Aug 9 · 6:00 PM · 🇺🇸 NYC",
        duration = "1d : 3h : 30m"
    ),
    EventData(
        name = "Art Expo",
        imageUrl = "https://picsum.photos/200/300?2",
        ownerName = "@ArtLovers",
        ownerImage = "https://picsum.photos/50/50?2",
        dateLocation = "Aug 12 · 4:00 PM · 🇫🇷 Paris",
        duration = "2d : 5h : 10m"
    ),
    EventData(
        name = "Fashion Gala",
        imageUrl = "https://picsum.photos/200/300?1",
        ownerName = "@FashionWeek",
        ownerImage = "https://picsum.photos/50/50?1",
        dateLocation = "Aug 9 · 6:00 PM · 🇺🇸 NYC",
        duration = "1d : 3h : 30m"
    ),
    EventData(
        name = "Art Expo",
        imageUrl = "https://picsum.photos/200/300?2",
        ownerName = "@ArtLovers",
        ownerImage = "https://picsum.photos/50/50?2",
        dateLocation = "Aug 12 · 4:00 PM · 🇫🇷 Paris",
        duration = "2d : 5h : 10m"
    ),
    // Add more mock events if needed
)

@Preview
@Composable
fun EventsScreen() {
    val events = mockEvents
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),

    ) {
        items(events) { event ->
            EventItemView(
                modifier = Modifier
                    .height(230.dp)
                    .fillMaxWidth(),
                eventName = event.name,
                eventImageUrl = event.imageUrl,
                eventOwnerName = event.ownerName,
                eventOwnerImage = event.ownerImage,
                dateLocationText = event.dateLocation,
                durationText = event.duration,
                onApplyClicked = {

                }
            )
        }
    }


}