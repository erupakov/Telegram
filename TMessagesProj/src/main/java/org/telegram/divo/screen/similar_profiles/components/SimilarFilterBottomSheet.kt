package org.telegram.divo.screen.similar_profiles.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import org.telegram.divo.style.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimilarFilterBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.colors.backgroundLight,
    ) {
        Column {

        }
    }
}