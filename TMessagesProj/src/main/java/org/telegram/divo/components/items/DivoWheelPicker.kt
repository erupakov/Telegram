package org.telegram.divo.components.items

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.telegram.divo.style.AppTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DivoWheelPicker(
    modifier: Modifier = Modifier,
    items: List<String>,
    initialIndex: Int = 0,
    itemHeight: Dp = 40.dp,
    visibleItemsCount: Int = 5,
    isCyclic: Boolean = false,
    onItemSelected: (index: Int, item: String) -> Unit
) {
    if (items.isEmpty()) return

    val centerIndex = visibleItemsCount / 2
    val actualItemCount = if (isCyclic) Int.MAX_VALUE else items.size

    val actualStartIndex = if (isCyclic) {
        val middle = Int.MAX_VALUE / 2
        middle - (middle % items.size) + initialIndex
    } else {
        initialIndex
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = actualStartIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(listState, items, isCyclic) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { if (isCyclic) it % items.size else it }
            .distinctUntilChanged()
            .collect { index ->
                val safeIndex = index.coerceIn(0, items.size - 1)
                onItemSelected(safeIndex, items[safeIndex])
            }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * visibleItemsCount),
            contentPadding = PaddingValues(vertical = itemHeight * centerIndex)
        ) {
            items(actualItemCount) { index ->
                val realIndex = if (isCyclic) index % items.size else index

                val isSelected by remember(index) {
                    derivedStateOf { listState.firstVisibleItemIndex == index }
                }

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[realIndex],
                        fontSize = 20.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) Color.Black else Color.Gray,
                        modifier = Modifier.alpha(if (isSelected) 1f else 0.4f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * centerIndex)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AppTheme.colors.onBackground,
                            Color(0x00F3F3F3),
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * centerIndex)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x00F3F3F3),
                            AppTheme.colors.onBackground
                        )
                    )
                )
        )
    }
}