package org.telegram.divo.screen.event_details.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.compose.clickableWithoutRipple
import org.telegram.divo.entity.EventAddress
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

/** Flat address text shown to the user: street/house/apt + city, falling back to the backend's formatted value. */
internal fun EventAddress.displayText(): String {
    val street = listOfNotNull(street, house, apartment)
        .filter { it.isNotBlank() }
        .joinToString(" ")
    return listOfNotNull(
        street.takeIf { it.isNotBlank() },
        cityName.takeIf { it.isNotBlank() }
    ).joinToString(", ")
        .ifBlank { formatted.orEmpty() }
}

@Composable
fun AddressCard(
    address: EventAddress,
    text: String
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickableWithoutRipple {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, buildMapsUri(text, address.latitude, address.longitude))
                )
            },
        color = AppTheme.colors.onBackground,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.drawable.ic_divo_map_pin_20),
                    contentDescription = null,
                    tint = AppTheme.colors.textPrimary.copy(0.6f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.EventLocationLabel),
                    style = AppTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                    color = AppTheme.colors.textPrimary.copy(0.6f)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = text,
                style = AppTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                color = AppTheme.colors.textPrimary,
                lineHeight = 20.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Deep link that opens the built-in maps app: a geo: link with coordinates when the event
 * has them, a maps search by address otherwise.
 */
private fun buildMapsUri(address: String, latitude: Double?, longitude: Double?): Uri {
    val query = Uri.encode(address)
    return if (latitude != null && longitude != null) {
        Uri.parse("geo:$latitude,$longitude?q=$query")
    } else {
        Uri.parse("https://www.google.com/maps/search/?api=1&query=$query")
    }
}
