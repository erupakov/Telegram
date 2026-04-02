package org.telegram.divo.screen.event_details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.utils.toEventDisplayDate
import org.telegram.divo.components.DivoChip
import org.telegram.divo.entity.Event
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun PreviousEvents(
    events: List<Event>,
    onClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = stringResource(R.string.PreviousEvents),
            style = AppTheme.typography.helveticaNeueLtCom,
            fontSize = 16.sp,
            color = AppTheme.colors.textPrimary,
        )
        Spacer(Modifier.height(14.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = events,
                key = { it.id }
            ) {
                Event(
                    event = it,
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
private fun Event(
    event: Event,
    onClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .width(133.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.onBackground)
            .clickableWithoutRipple { onClick(event.id) }
    ) {
        Box(
            modifier = Modifier
                .height(99.dp)
                .padding(2.5.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            DivoAsyncImage(
                model = event.creator?.photo?.fullUrl
            )
            DivoChip(
                modifier = Modifier.align(Alignment.TopStart).padding(6.dp),
                text = event.type.orEmpty()
            )
        }
        Spacer(Modifier.height(7.dp))
        Text(
            modifier = Modifier.padding(horizontal = 10.dp),
            text = event.title.orEmpty(),
            style = AppTheme.typography.helveticaNeueLtCom,
            fontSize = 11.sp,
            color = AppTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Text(
            modifier = Modifier.padding(horizontal = 10.dp),
            text = "2021-05-30 10:00:00".toEventDisplayDate("RU", "Moscow", false),
            style = AppTheme.typography.helveticaNeueRegular,
            fontSize = 10.sp,
            color = AppTheme.colors.textPrimary.copy(0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(10.dp))
    }
}