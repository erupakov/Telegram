package org.telegram.divo.screen.event_create.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import org.telegram.divo.common.compose.clickableWithoutRipple
import org.telegram.divo.common.controllers.rememberMultipleGalleryLauncher
import org.telegram.divo.components.inputs.DivoTextField
import org.telegram.divo.components.media.LottieProgressIndicator
import org.telegram.divo.screen.event_create.Intent
import org.telegram.divo.screen.event_create.State
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R
import org.telegram.divo.dal.dto.payment.PaymentFrequencyDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThirdPage(
    state: State,
    isEdit: Boolean,
    onIntent: (Intent) -> Unit,
) {
    var showDeadlineDateSheet by rememberSaveable { mutableStateOf(false) }
    var showDeadlineTimeSheet by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DateTimeField(
                modifier = Modifier.weight(1f),
                date = state.deadlineDate,
                title = stringResource(R.string.EventApplicationDeadlineDate),
                placeholder = stringResource(R.string.EventDatePlaceholder),
                trailingIcon = R.drawable.ic_divo_calendar_20,
                onClick = { showDeadlineDateSheet = true }
            )
            DateTimeField(
                modifier = Modifier.weight(1f),
                date = state.deadlineTime,
                title = stringResource(R.string.EventApplicationDeadlineTime),
                placeholder = stringResource(R.string.EventTimePlaceholder),
                trailingIcon = R.drawable.ic_divo_clock_20,
                onClick = { showDeadlineTimeSheet = true }
            )
        }

        Spacer(Modifier.height(16.dp))

        // Paid toggle
        SwitchItem(
            text = stringResource(R.string.EventPaid),
            checked = state.isPaid,
            onChanged = { onIntent(Intent.OnIsPaidToggled(it)) }
        )

        // Rate fields — shown only when paid
        if (state.isPaid) {
            Spacer(Modifier.height(20.dp))
            SectionHeader(text = stringResource(R.string.EventRate))
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DivoTextField(
                    modifier = Modifier.weight(1f),
                    value = state.eventRate,
                    onValueChange = { onIntent(Intent.OnEventRateChanged(it)) },
                    placeholder = stringResource(R.string.EventRatePlaceholder),
                    backgroundColor = AppTheme.colors.onBackground,
                    cornerRadius = 41.dp,
                    trailingIcon = R.drawable.ic_divo_paid,
                    placeholderColor = AppTheme.colors.textPrimary.copy(0.4f),
                    textStyle = AppTheme.typography.bodyLarge.copy(
                        color = AppTheme.colors.textPrimary
                    ),
                    keyboardType = KeyboardType.Number,
                    horizontalContentPadding = 16.dp
                )

                PaymentFrequencyDropdown(
                    modifier = Modifier.weight(1f),
                    frequencies = state.paymentFrequencies,
                    selected = state.selectedPaymentFrequency,
                    onSelected = { onIntent(Intent.OnPaymentFrequencySelected(it)) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Public toggle
        SwitchItem(
            text = stringResource(R.string.EventPublicEvent),
            checked = state.isPublicEvent,
            onChanged = { onIntent(Intent.OnIsPublicToggled(it)) }
        )
        Spacer(Modifier.height(6.dp))
        SectionHeader(
            modifier = Modifier.padding(start = 16.dp),
            text = stringResource(R.string.EventVisibleToAllUsers)
        )

        Spacer(Modifier.height(16.dp))

        // Photo gallery
        SectionHeader(text = stringResource(R.string.EventPhotoGallery))
        Spacer(Modifier.height(14.dp))

        GallerySection(
            uris = state.galleryUris.drop(1),
            onPhotosAdded = { onIntent(Intent.OnGalleryPhotosAdded(it)) },
            onPhotoRemoved = { onIntent(Intent.OnGalleryPhotoRemoved(it)) }
        )

        Spacer(Modifier.height(if (isEdit) 140.dp else 76.dp))
    }

    // Deadline date picker
    if (showDeadlineDateSheet) {
        EventDatePickerSheet(
            initialDate = state.deadlineDate,
            maxDate = state.eventDate,
            onDismiss = { showDeadlineDateSheet = false },
            onDateSelected = { date ->
                onIntent(Intent.OnDeadlineDateChanged(date))
                showDeadlineDateSheet = false
            }
        )
    }

    // Deadline time picker
    if (showDeadlineTimeSheet) {
        EventTimePickerSheet(
            initialTime = state.deadlineTime,
            onDismiss = { showDeadlineTimeSheet = false },
            onTimeSelected = { time ->
                onIntent(Intent.OnDeadlineTimeChanged(time))
                showDeadlineTimeSheet = false
            }
        )
    }
}

@Composable
private fun GallerySection(
    uris: List<Uri>,
    onPhotosAdded: (List<Uri>) -> Unit,
    onPhotoRemoved: (Uri) -> Unit,
) {
    val openGallery = rememberMultipleGalleryLauncher { picked ->
        onPhotosAdded(picked)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Grid of selected photos
        if (uris.isNotEmpty()) {
            // Fixed-height grid (not nested scroll — we wrap in a fixed box)
            val rowCount = (uris.size + 2) / 3  // ceil(n/3)
            val gridHeight = (rowCount * 110 + (rowCount - 1) * 8).dp

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gridHeight),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(0.dp),
                userScrollEnabled = false  // outer Column scrolls
            ) {
                items(uris) { uri ->
                    PhotoThumbnail(
                        uri = uri,
                        onRemove = { onPhotoRemoved(uri) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
        }

        // Add button
        GalleryButton(
            isUploading = false,
            onClick = openGallery
        )
    }
}

@Composable
private fun PhotoThumbnail(
    uri: Uri,
    onRemove: () -> Unit,
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Image(
            painter = rememberAsyncImagePainter(uri),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Remove button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .clickableWithoutRipple { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun GalleryButton(
    isUploading: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(AppTheme.colors.accentOrange)
            .padding(horizontal = 16.dp)
            .clickableWithoutRipple { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isUploading) {
            LottieProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = AppTheme.colors.onBackground
            )
            Spacer(Modifier.width(4.dp))
            Text(
                modifier = Modifier.offset(y = 1.dp),
                text = stringResource(R.string.EventUploadingPhotos),
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 16.sp,
                color = AppTheme.colors.onBackground
            )
        } else {
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource(R.drawable.ic_divo_add_a_photo),
                contentDescription = null,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                modifier = Modifier.offset(y = 1.dp),
                text = stringResource(R.string.EventUploadEventPhotos),
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 16.sp,
                color = AppTheme.colors.onBackground
            )
        }
    }
}

@Composable
private fun PaymentFrequencyDropdown(
    modifier: Modifier = Modifier,
    frequencies: List<PaymentFrequencyDto>,
    selected: PaymentFrequencyDto?,
    onSelected: (PaymentFrequencyDto) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clickableWithoutRipple { expanded = true }
                .clip(RoundedCornerShape(46.dp))
                .background(AppTheme.colors.onBackground)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.padding(top = 1.dp),
                text = selected?.title ?: stringResource(R.string.EventRatePlaceholder),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = AppTheme.colors.textPrimary
            )
        }

        DropdownMenu(
            modifier = Modifier
                .width(160.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(AppTheme.colors.onBackground),
            containerColor = Color.Transparent,
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            frequencies.forEach { frequency ->
                DropdownMenuItem(
                    modifier = Modifier.background(Color.Transparent),
                    text = {
                        Text(
                            text = frequency.title,
                            style = AppTheme.typography.bodyLarge,
                            color = if (frequency.id == selected?.id) AppTheme.colors.accentOrange
                                    else AppTheme.colors.textPrimary
                        )
                    },
                    onClick = {
                        onSelected(frequency)
                        expanded = false
                    }
                )
            }
        }
    }
}