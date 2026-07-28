package org.telegram.divo.components.bottomsheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.compose.clickableWithoutRipple
import org.telegram.divo.components.inputs.RoundedButton
import org.telegram.divo.components.media.DivoAsyncImage
import org.telegram.divo.components.media.LottieProgressIndicator
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.entity.UserGalleryItem
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePhotoSourceBottomSheet(
    profileId: Int,
    sheetState: SheetState,
    onPhotoSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var items by remember { mutableStateOf<List<UserGalleryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(profileId) {
        val res = DivoApi.userRepository.getUserGalleryList(profileId, 0, 100)
        if (res is DivoResult.Success) {
            items = res.value.items
        }
        isLoading = false
    }

    ModalBottomSheet(
        modifier = Modifier.statusBarsPadding(),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.colors.backgroundLight,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(36.dp, 4.dp)
                    .background(AppTheme.colors.textPrimary.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                RoundedButton(
                    modifier = Modifier
                        .size(40.dp),
                    resId = R.drawable.ic_divo_close_20,
                    iconSize = 28.dp,
                    background = AppTheme.colors.onBackground,
                    iconTint = AppTheme.colors.backgroundDark,
                    onClick = onDismiss
                )
                
                Text(
                    text = stringResource(R.string.ChoosePhoto),
                    style = AppTheme.typography.helveticaNeueLtCom,
                    fontSize = 18.sp,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(8.dp))

            if (isLoading) {
                LottieProgressIndicator(modifier = Modifier.padding(32.dp).size(40.dp))
            } else if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.NoPhotosYetBottomSheet),
                        style = AppTheme.typography.helveticaNeueLtCom,
                        fontSize = 24.sp,
                        color = AppTheme.colors.textPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp)
                    )
                }
                Spacer(Modifier.height(20.dp))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxWidth().weight(1f, false),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(items) { item ->
                        DivoAsyncImage(
                            model = item.photoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickableWithoutRipple { onPhotoSelected(item.photoUrl) },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}
