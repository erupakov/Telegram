package org.telegram.divo.screen.reg_agency

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.components.DivoTextFieldCard
import org.telegram.divo.components.TextDescription
import org.telegram.divo.components.TextItemTitle
import org.telegram.divo.components.TextTitle
import org.telegram.divo.components.UIButton
import org.telegram.divo.components.UIButtonBack
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

private val Bg = Color(0xFF222222)

/** Apply as Agencies & Brands screen */
@Preview
@Composable
fun ApplyAgenciesBrandsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSave: (agencyName: String, country: String?, website: String?) -> Unit = { _, _, _ -> },
) {
    var agencyName by remember { mutableStateOf("") }
    var country by remember { mutableStateOf<String?>(null) }
    var website by remember { mutableStateOf("") }

    // dropdown state
    val countries = listOf("USA", "UK", "France", "Germany", "Armenia")
    var countryMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        // Scrollable content
        Column(
            modifier = modifier
                .fillMaxWidth()
                .weight(1f)
                .systemBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            TextTitle(
                text = "APPLY AS\nA AGENCIES & BRANDS",
                textAlign = TextAlign.Center,
                color = AppTheme.colors.textColor,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            TextDescription(
                text = "Fill out your profile details to apply as a professional model. You can update this information anytime.",
                textAlign = TextAlign.Center,
                color = AppTheme.colors.textSubtitleColor,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Avatar stub
            Box(
                Modifier
                    .size(104.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .border(2.dp, Color(0x66FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(R.drawable.divo_add_photo_ic),
                    contentDescription = null,
                    tint = Color(0x66FFFFFF),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            TextItemTitle(
                text = "YOUR PERSONAL DATA",
                color = AppTheme.colors.textColor,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Agency name
            DivoTextFieldCard(
                value = agencyName,
                onValueChange = { agencyName = it },
                placeholder = "Name Agency"
            )

            Spacer(Modifier.height(12.dp))

            // Country dropdown
            Box {
                DivoTextFieldCard(
                    value = country ?: "",
                    onValueChange = { },
                    placeholder = "Choose a country",
                    readOnly = true,
                    trailing = { Chevron(expanded = countryMenu) },
                    onClick = { countryMenu = true }
                )
                DropdownMenu(
                    expanded = countryMenu,
                    onDismissRequest = { countryMenu = false }
                ) {
                    countries.forEach {
                        DropdownMenuItem(onClick = {
                            country = it
                            countryMenu = false
                        }) {
                            Text(
                                text = it,
                                color = Color.White,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Website
            DivoTextFieldCard(
                value = website,
                onValueChange = { website = it },
                placeholder = "Enter name your website"
            )

            Spacer(Modifier.height(24.dp))
        }

        // Bottom actions
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UIButtonBack(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                text = "Back"
            )
            UIButton(
                modifier = Modifier.weight(1f),
                text = "Save",
                enabled = agencyName.isNotBlank()
            ) { onSave(agencyName, country, website) }
        }
    }
}

/* --- small chevron helper --- */
@Composable
private fun Chevron(expanded: Boolean) {
    Text(if (expanded) "▴" else "▾", color = Color.White, fontSize = 16.sp)
}
