package org.telegram.divo.screen.event_create.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.telegram.divo.common.compose.clickableWithoutRipple
import org.telegram.divo.components.bottomsheets.DivoBottomSheet
import org.telegram.divo.entity.EventType
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTypeBottomSheet(
    title: String,
    items: List<EventType>,
    selectedItem: EventType?,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onApply: (EventType) -> Unit,
    onDismiss: () -> Unit
) {
    var draftItem by remember { mutableStateOf(selectedItem) }

    DivoBottomSheet(
        sheetState = sheetState,
        title = title,
        iconClose = R.drawable.ic_divo_back,
        onDismiss = onDismiss,
        contentPadding = PaddingValues(horizontal = 16.dp),
        onSave = { draftItem?.let { onApply(it) } }
    ) {
        OptionsBlock(
            items = items,
            selectedItem = draftItem,
            onClick = { draftItem = it }
        )
    }
}

@Composable
private fun OptionsBlock(
    items: List<EventType>,
    selectedItem: EventType?,
    onClick: (EventType) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppTheme.colors.onBackground)
            .padding(horizontal = 16.dp),
    ) {

        itemsIndexed(
            items = items,
            key = { index, item -> item.id }
        ) { index, item ->
            val isSelected = selectedItem == item

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .clickableWithoutRipple { onClick(item) },
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 2.dp),
                    text = item.title.orEmpty(),
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (isSelected) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.ic_divo_apply),
                        tint = AppTheme.colors.accentOrange,
                        contentDescription = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            if (index != items.size - 1) {
                Divider()
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

