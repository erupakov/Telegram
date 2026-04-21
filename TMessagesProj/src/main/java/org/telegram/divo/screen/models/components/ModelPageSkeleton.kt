package org.telegram.divo.screen.models.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.telegram.divo.components.shimmer

@Composable
fun ModelPageSkeleton(
    modifier: Modifier = Modifier,
    cardHeight: Dp
) {
    Column() {
        repeat(2) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(cardHeight)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shimmer()
                        .background(Color.LightGray.copy(alpha = 0.2f))
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 20.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {

                        Column(Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .shimmer()
                                    .background(Color.LightGray.copy(alpha = 0.3f))
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Box(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(20.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .shimmer()
                                        .background(Color.LightGray.copy(alpha = 0.3f))
                                )
                                Spacer(Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .shimmer()
                                        .background(Color.LightGray.copy(alpha = 0.2f))
                                )
                            }
                        }

                        Column {
                            repeat(3) { index ->
                                Box(
                                    modifier = Modifier
                                        .width(65.dp)
                                        .height(30.dp)
                                        .clip(RoundedCornerShape(15.dp))
                                        .shimmer()
                                        .background(Color.LightGray.copy(alpha = 0.3f))
                                )
                                if (index < 2) Spacer(Modifier.height(10.dp))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp, start = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(5) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .shimmer()
                                    .background(Color.LightGray.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
            }
        }
    }
}
