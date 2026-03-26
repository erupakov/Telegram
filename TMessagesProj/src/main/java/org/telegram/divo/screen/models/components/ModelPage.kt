package org.telegram.divo.screen.models.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.DivoChip
import org.telegram.divo.components.items.DMButton
import org.telegram.divo.entity.FeedItem
import org.telegram.divo.screen.gallery.GalleryItem
import org.telegram.divo.screen.models.Model
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun ModelPage(
    modifier: Modifier = Modifier,
    feed: FeedItem,
    models: List<Model>,
    cardHeight: Dp,
    onClick: (Int) -> Unit,
    onPhotoClicked: (List<GalleryItem>, Int) -> Unit,
) {
    val backgroundPhoto = feed.files.first().url
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(26.dp))
            .clickableWithoutRipple { onClick(feed.id) },
        contentAlignment = Alignment.TopEnd
    ) {
        DivoAsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = backgroundPhoto,
            loadingContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                )
            }
        )

        Column(
            Modifier
                .align(Alignment.TopEnd)
                .padding(end = 12.dp, top = 24.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            models[0].emotions.forEach { emotion ->
                ReactionPill(
                    emotion = emotion,
                    onClick = {
                        //viewModel.setIntent(ModelsViewIntent.OnEmotionClick(model.id, emotion))
                    }
                )
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f), // bottom strong
                            Color.Transparent                 // top invisible
                        ),
                        startY = Float.POSITIVE_INFINITY, // start bottom
                        endY = 0f                          // end top
                    )
                )
                .padding(vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White.copy(alpha = .9f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val avatarUrl = feed.previewImage.url
                    if (avatarUrl.isNotEmpty()) {
                        DivoAsyncImage(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape),
                            model = avatarUrl
                        )
                    } else {
                        val initials = feed.user.fullName.split(" ")
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .take(2)
                            .joinToString("")
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.accentOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = feed.user.fullName.uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.ExtraBold),
                        maxLines = 2
                    )
                    Spacer(Modifier.height(6.dp))

                    DivoChip(
                        text = feed.user.roleLabel,
                        resId = if (feed.user.role.isModel()) R.drawable.ic_divo_person_heart else R.drawable.ic_divo_agency,
                        background = Color(0xFF2262D8),
                        textColor = Color.White
                    )
                }
                Spacer(Modifier.width(10.dp))
            }

            Row(
                Modifier
                    .padding(top = 24.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DMButton(onClick = {
                    //viewModel.setIntent(ModelsViewIntent.OnSendDmClick(feed.id))
                })

                Spacer(Modifier.weight(1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = .15f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_divo_share_model),
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_divo_save_model),
                        contentDescription = "Bookmark",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                //viewModel.setIntent(ModelsViewIntent.OnBookmarkClick(model.id))
                            }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            ThumbsRow(
                feed = feed,
                onPhotoClicked = {
                    val items = feed.files.map { GalleryItem(it.order, it.url, false) }
                    onPhotoClicked(items, it)
                }
            )
        }
    }
}