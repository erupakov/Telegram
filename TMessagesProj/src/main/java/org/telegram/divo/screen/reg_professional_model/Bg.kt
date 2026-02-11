package org.telegram.divo.screen.reg_professional_model

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
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

/* ---------- palette ---------- */
private val Bg = Color(0xFF222222)
private val Copper = Color(0xFFC57B53)
private val FieldBg = Color(0xFF2B2B2B)
private val FieldBorder = Color(0xFF4A4A4A)
private val FieldHint = Color(0x99FFFFFF)

/* ---------- screen ---------- */
@Preview
@Composable
fun ApplyProfessionalModelScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSave: (fullName: String, gender: String?, country: String?, agency: String?, age: Int) -> Unit = { _, _, _, _, _ -> },
) {
    var fullName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf<String?>(null) }
    var country by remember { mutableStateOf<String?>(null) }
    var agency by remember { mutableStateOf<String?>(null) }
    var age by remember { mutableStateOf(17) }

    // dropdown state
    var genderMenu by remember { mutableStateOf(false) }
    var countryMenu by remember { mutableStateOf(false) }
    var agencyMenu by remember { mutableStateOf(false) }

    val genders = listOf("Male", "Female", "Non-binary")
    val countries = listOf("USA", "UK", "France", "Germany", "Armenia")
    val agencies = listOf("IMG", "Elite", "Ford", "Next")

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Bg)) {
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
                textAlign = TextAlign.Center,
                text = "APPLY AS A\nPROFESSIONAL MODEL",
                modifier = Modifier.fillMaxWidth(),
                color = AppTheme.colors.textColor
            )

            Spacer(Modifier.height(10.dp))

            TextDescription(
                text = "Fill out your profile details to apply as a professional model. You can update this information anytime.",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = AppTheme.colors.textSubtitleColor

            )

            Spacer(Modifier.height(24.dp))

            // avatar stub
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
                    null,
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
                    onValueChange = {},
                    placeholder = "Select a Gender",
                    readOnly = true,
                    trailing = { Chevron(expanded = genderMenu) },
                    onClick = { genderMenu = true }
                )
                SimpleDropdownMenu(
                    expanded = genderMenu,
                    onDismiss = { genderMenu = false },
                    items = genders,
                    onPick = { gender = it }
                )
            }
            Spacer(Modifier.height(12.dp))

            // Country dropdown
            Box {
                DivoTextFieldCard(
                    value = country ?: "",
                    onValueChange = {},
                    placeholder = "Choose a country",
                    readOnly = true,
                    trailing = { Chevron(expanded = countryMenu) },
                    onClick = { countryMenu = true }
                )
                SimpleDropdownMenu(
                    expanded = countryMenu,
                    onDismiss = { countryMenu = false },
                    items = countries,
                    onPick = { country = it }
                )
            }
            Spacer(Modifier.height(12.dp))

            // Agency dropdown
            Box {
                DivoTextFieldCard(
                    value = agency ?: "",
                    onValueChange = {},
                    placeholder = "Choose agency name",
                    readOnly = true,
                    trailing = { Chevron(expanded = agencyMenu) },
                    onClick = { agencyMenu = true }
                )
                SimpleDropdownMenu(
                    expanded = agencyMenu,
                    onDismiss = { agencyMenu = false },
                    items = agencies,
                    onPick = { agency = it }
                )
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



            Spacer(Modifier.height(16.dp))
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UIButtonBack(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                ,
                text = "Back"
            )
            UIButton(
                modifier = Modifier
                    .weight(1f)
                   ,
                text = "Save",
            ) { onSave(fullName, gender, country, agency, age) }
        }
    }
}

/* ---------- small pieces ---------- */

@Composable
private fun Chevron(expanded: Boolean) {
    val char = if (expanded) "▴" else "▾"
    Text(
        text = char,
        color = Color.White,
        fontSize = 16.sp
    )
}

/** Dropdown anchored to the parent Box (simple & reliable). */
@Composable
private fun SimpleDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    items: List<String>,
    onPick: (String) -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        items.forEach { item ->
            DropdownMenuItem(onClick = {
                onPick(item)
                onDismiss()
            }) {
                Text(
                    item,
                    color = Color.White,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


