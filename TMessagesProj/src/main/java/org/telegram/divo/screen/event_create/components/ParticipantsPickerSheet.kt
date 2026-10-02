package org.telegram.divo.screen.event_create.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.telegram.divo.components.bottomsheets.DivoBottomSheet
import org.telegram.divo.components.items.DivoWheelPicker
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantsPickerSheet(
    initialValue: Int = 0,
    onDismiss: () -> Unit,
    onSelected: (Int) -> Unit,
) {
    val items = remember { (1..100).map { it.toString() } }

    var selectedIndex by remember {
        val idx = if (initialValue in 1..100) initialValue - 1 else 0
        mutableIntStateOf(idx)
    }

    val itemHeight = 40.dp

    DivoBottomSheet(
        title = stringResource(R.string.EventMaxParticipants),
        iconClose = R.drawable.ic_divo_back,
        contentPadding = PaddingValues(horizontal = 16.dp),
        onDismiss = onDismiss,
        onSave = { onSelected(selectedIndex + 1) }
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.onBackground)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(41.dp))
                        .background(Color(0xFFEBEBEB))
                )

                DivoWheelPicker(
                    items = items,
                    initialIndex = selectedIndex,
                    itemHeight = itemHeight,
                    isCyclic = true,
                    onItemSelected = { index, _ -> selectedIndex = index }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
