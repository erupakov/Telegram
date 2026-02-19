package org.telegram.divo.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import org.telegram.divo.style.AppTheme
import org.telegram.divo.style.DivoFont


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AgeSlider(
    initialAge: Float = 17f,
    onAgeSelected: (Int) -> Unit = {}
){
    var age by remember { mutableStateOf(initialAge) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Age (y.o)", color = AppTheme.colors.textColor, fontSize = 14.sp, fontFamily = DivoFont.HelveticaNeue)
            Text("${age.toInt()} y.o", color = AppTheme.colors.textColor, fontSize = 14.sp, fontFamily = DivoFont.HelveticaNeue)
        }
        Slider(
            value = age,
            onValueChange = {
                val clamped = it.coerceIn(14f, 45f)
                age = clamped
                onAgeSelected(clamped.toInt())
            },
            valueRange = 14f..45f,
            colors = SliderDefaults.colors(
                thumbColor = AppTheme.colors.buttonColor,
                activeTrackColor = Color(0xFF9B9B9B),
                inactiveTrackColor = Color(0xFF5C5C5C)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("14", color = AppTheme.colors.textColor, fontSize = 14.sp, fontFamily = DivoFont.HelveticaNeue)
            Text("45", color = AppTheme.colors.textColor, fontSize = 14.sp, fontFamily = DivoFont.HelveticaNeue)
        }
    }

}