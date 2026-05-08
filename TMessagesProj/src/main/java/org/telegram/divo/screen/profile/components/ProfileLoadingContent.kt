package org.telegram.divo.screen.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import org.telegram.divo.components.RoundedGlassButton
import org.telegram.divo.components.shimmer
import org.telegram.divo.style.AppTheme

@Composable
fun ProfileLoadingContent(
    onNavigateBack: () -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val statusBarHeight = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()

    Box(modifier = Modifier.fillMaxSize().background(AppTheme.colors.backgroundLight)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.5f)
                        .shimmer()
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Box(Modifier.size(250.dp, 28.dp).clip(RoundedCornerShape(12.dp)).shimmer())
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.size(140.dp, 16.dp).clip(RoundedCornerShape(10.dp)).shimmer())
                        Spacer(Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(Modifier.size(120.dp, 40.dp).clip(RoundedCornerShape(20.dp)).shimmer())
                            Spacer(Modifier.width(12.dp))
                            Box(Modifier.size(40.dp, 40.dp).clip(CircleShape).shimmer())
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(12.dp)).shimmer())
                }
            }

            item {
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth().height(98.dp).padding(horizontal = 16.dp).clip(RoundedCornerShape(16.dp)).shimmer())
            }

            item {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .shimmer()
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(Modifier.fillMaxWidth(0.5f).height(24.dp).padding(horizontal = 16.dp).clip(RoundedCornerShape(16.dp)).shimmer())
                }
            }

            item {
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(3) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Box(Modifier.weight(1f).aspectRatio(1f).shimmer())
                            Box(Modifier.weight(1f).aspectRatio(1f).shimmer())
                            Box(Modifier.weight(1f).aspectRatio(1f).shimmer())
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = statusBarHeight + 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RoundedGlassButton(
                    onClick = onNavigateBack
                )
                Box(Modifier.size(80.dp, 40.dp).clip(RoundedCornerShape(20.dp)).shimmer())
            }
            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.width(60.dp).height(30.dp).clip(CircleShape).shimmer())
            Spacer(Modifier.height(10.dp))
            Box(modifier = Modifier.width(60.dp).height(30.dp).clip(CircleShape).shimmer())
            Spacer(Modifier.height(10.dp))
            Box(modifier = Modifier.width(60.dp).height(30.dp).clip(CircleShape).shimmer())
        }
    }
}