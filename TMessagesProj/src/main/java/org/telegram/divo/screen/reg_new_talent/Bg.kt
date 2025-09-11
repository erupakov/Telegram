package org.telegram.divo.screen.reg_new_talent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
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
import org.telegram.messenger.R

private val Bg = Color(0xFF222222)

/** APPLY AS A NEW TALENT */
@Preview
@Composable
fun ApplyNewTalentScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSave: (fullName: String, gender: String?, country: String?, age: Int) -> Unit = { _, _, _, _ -> },
) {
    var fullName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf<String?>(null) }
    var country by remember { mutableStateOf<String?>(null) }
    var age by remember { mutableStateOf(17) }

    // dropdown state
    var genderMenu by remember { mutableStateOf(false) }
    var countryMenu by remember { mutableStateOf(false) }
    val genders = listOf("Male", "Female", "Non-binary")
    val countries = listOf("USA", "UK", "France", "Germany", "Armenia")

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
                text = "APPLY AS\nA NEW TALENT",
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            TextDescription(
                text = "Fill out your profile details to apply as a professional model. You can update this information anytime.",
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Avatar placeholder
            Box(
                Modifier
                    .size(104.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .border(2.dp, Color(0x66FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.divo_add_photo_ic),
                    contentDescription = null,
                    tint = Color(0x66FFFFFF),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            TextItemTitle(
                text = "YOUR PERSONAL DATA",
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Full name
            DivoTextFieldCard(
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = "Full Name"
            )
            Spacer(Modifier.height(12.dp))

            // Gender dropdown
            Box {
                DivoTextFieldCard(
                    value = gender ?: "",
                    onValueChange = { },
                    placeholder = "Select a Gender",
                    readOnly = true,
                    trailing = { Chevron(expanded = genderMenu) },
                    onClick = { genderMenu = true }
                )
                DropdownMenu(
                    expanded = genderMenu,
                    onDismissRequest = { genderMenu = false }
                ) {
                    genders.forEach {
                        DropdownMenuItem(onClick = {
                            gender = it
                            genderMenu = false
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

            Spacer(Modifier.height(18.dp))

            // Age slider
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Age (y.o)", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                Text("$age y.o", color = Color.White, fontSize = 14.sp)
            }
            Slider(
                value = age.toFloat(),
                onValueChange = { age = it.toInt().coerceIn(14, 45) },
                valueRange = 14f..45f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color(0xFF9B9B9B),
                    inactiveTrackColor = Color(0xFF5C5C5C)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("14", color = Color.White, fontSize = 14.sp)
                Text("45", color = Color.White, fontSize = 14.sp)
            }

            Spacer(Modifier.height(24.dp))
        }

        // Bottom buttons
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
                enabled = fullName.isNotBlank()
            ) { onSave(fullName, gender, country, age) }
        }
    }
}

/* simple chevron for dropdowns */
@Composable
private fun Chevron(expanded: Boolean) {
    Text(if (expanded) "▴" else "▾", color = Color.White, fontSize = 16.sp)
}
