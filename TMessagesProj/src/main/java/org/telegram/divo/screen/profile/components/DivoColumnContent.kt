package org.telegram.divo.screen.profile.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

data class Playlist(
    val id: Int,
    val url: String,
    val label: String,
    val followers: String,
    val isPremium: Boolean
)

val mockData = listOf(
    Playlist(0, "https://divostorage.s3.eu-central-1.amazonaws.com/files/jY5GsdMDH7QS9tbtkIn1RziaqC0iXHBmkevhmtWx_tempo_v17_375x500.jpg", "Vogue Inside", "1 342 followers", false),
    Playlist(1, "https://divostorage.s3.eu-central-1.amazonaws.com/files/jY5GsdMDH7QS9tbtkIn1RziaqC0iXHBmkevhmtWx_tempo_v17_375x500.jpg", "Vogue Inside", "1 342 followers", false),
    Playlist(2, "https://divostorage.s3.eu-central-1.amazonaws.com/files/jY5GsdMDH7QS9tbtkIn1RziaqC0iXHBmkevhmtWx_tempo_v17_375x500.jpg", "Vogue Inside", "1 342 followers", true),
    Playlist(3, "https://divostorage.s3.eu-central-1.amazonaws.com/files/jY5GsdMDH7QS9tbtkIn1RziaqC0iXHBmkevhmtWx_tempo_v17_375x500.jpg", "Vogue Inside", "1 342 followers", false),
    Playlist(4, "https://divostorage.s3.eu-central-1.amazonaws.com/files/jY5GsdMDH7QS9tbtkIn1RziaqC0iXHBmkevhmtWx_tempo_v17_375x500.jpg", "Vogue Inside", "1 342 followers", false),
    Playlist(5, "https://divostorage.s3.eu-central-1.amazonaws.com/files/jY5GsdMDH7QS9tbtkIn1RziaqC0iXHBmkevhmtWx_tempo_v17_375x500.jpg", "Vogue Inside", "1 342 followers", false),
    Playlist(6, "https://divostorage.s3.eu-central-1.amazonaws.com/files/jY5GsdMDH7QS9tbtkIn1RziaqC0iXHBmkevhmtWx_tempo_v17_375x500.jpg", "Vogue Inside", "1 342 followers", true),
    Playlist(7, "https://divostorage.s3.eu-central-1.amazonaws.com/files/jY5GsdMDH7QS9tbtkIn1RziaqC0iXHBmkevhmtWx_tempo_v17_375x500.jpg", "Vogue Inside", "1 342 followers", false),
    Playlist(8, "https://divostorage.s3.eu-central-1.amazonaws.com/files/jY5GsdMDH7QS9tbtkIn1RziaqC0iXHBmkevhmtWx_tempo_v17_375x500.jpg", "Vogue Inside", "1 342 followers", false),
    Playlist(9, "https://divostorage.s3.eu-central-1.amazonaws.com/files/jY5GsdMDH7QS9tbtkIn1RziaqC0iXHBmkevhmtWx_tempo_v17_375x500.jpg", "Vogue Inside", "1 342 followers", false),
    Playlist(10, "https://divostorage.s3.eu-central-1.amazonaws.com/files/jY5GsdMDH7QS9tbtkIn1RziaqC0iXHBmkevhmtWx_tempo_v17_375x500.jpg", "Vogue Inside", "1 342 followers", true),
    Playlist(11, "https://divostorage.s3.eu-central-1.amazonaws.com/files/jY5GsdMDH7QS9tbtkIn1RziaqC0iXHBmkevhmtWx_tempo_v17_375x500.jpg", "Vogue Inside", "1 342 followers", false),
    Playlist(12, "https://divostorage.s3.eu-central-1.amazonaws.com/files/jY5GsdMDH7QS9tbtkIn1RziaqC0iXHBmkevhmtWx_tempo_v17_375x500.jpg", "Vogue Inside", "1 342 followers", false),
    Playlist(13, "https://divostorage.s3.eu-central-1.amazonaws.com/files/jY5GsdMDH7QS9tbtkIn1RziaqC0iXHBmkevhmtWx_tempo_v17_375x500.jpg", "Vogue Inside", "1 342 followers", false),
    Playlist(14, "https://divostorage.s3.eu-central-1.amazonaws.com/files/jY5GsdMDH7QS9tbtkIn1RziaqC0iXHBmkevhmtWx_tempo_v17_375x500.jpg", "Vogue Inside", "1 342 followers", true),
    Playlist(15, "https://divostorage.s3.eu-central-1.amazonaws.com/files/jY5GsdMDH7QS9tbtkIn1RziaqC0iXHBmkevhmtWx_tempo_v17_375x500.jpg", "Vogue Inside", "1 342 followers", false),
    Playlist(16, "https://divostorage.s3.eu-central-1.amazonaws.com/files/jY5GsdMDH7QS9tbtkIn1RziaqC0iXHBmkevhmtWx_tempo_v17_375x500.jpg", "Vogue Inside", "1 342 followers", false),
    Playlist(17, "https://divostorage.s3.eu-central-1.amazonaws.com/files/jY5GsdMDH7QS9tbtkIn1RziaqC0iXHBmkevhmtWx_tempo_v17_375x500.jpg", "Vogue Inside", "1 342 followers", false),
)

@Composable
fun DivoColumnContent(
    title: String,
    isEvent: Boolean = false
) {
    val mock = mockData.map { it.copy(followers = title) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = WindowInsets.navigationBars
                .asPaddingValues()
                .calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = mock,
            key = { it.id }
        ) {
            PlaylistItem(
                item = it,
                isEvent = isEvent,
                onClicked = {}
            )
        }
    }
}

@Composable
private fun PlaylistItem(
    item: Playlist,
    isEvent: Boolean,
    onClicked: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f).clickableWithoutRipple { onClicked(item.id) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DivoAsyncImage(
                modifier = Modifier.size(60.dp).clip(CircleShape),
                model = item.url,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        modifier = Modifier,
                        text = item.label,
                        style = AppTheme.typography.helveticaNeueRegular,
                        fontSize = 16.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.isPremium && !isEvent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Image(
                            modifier = Modifier.size(16.dp).offset(y = (-3).dp),
                            painter = painterResource(R.drawable.divo_pro_badge),
                            contentDescription = null,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.followers,
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 14.sp,
                    color = Color.Black.copy(0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Log.d("MyTag", isEvent.toString())
        if (isEvent) {
            UIButtonNew(
                modifier = Modifier
                    .height(32.dp),
                text = "Apply",
                textStyle = AppTheme.typography.textButton.copy(
                    fontSize = 14.sp
                ),
                shape = RoundedCornerShape(6.dp),
                onClick = {}
            )
        }
    }
}