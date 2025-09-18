package org.telegram.divo.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp


@Composable
fun AgeSlider(){

    var age by remember { mutableStateOf(17f) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Age (y.o)", color = Color.Black.copy(alpha = 0.85f), fontSize = 14.sp)
            Text("$age y.o", color = Color.Black, fontSize = 14.sp)
        }
        Slider(
            value = age,
            onValueChange = { age = it.coerceIn(14f, 45f) },
            valueRange = 14f..45f,
            colors = SliderDefaults.colors(
                thumbColor = Color.Black,
                activeTrackColor = Color(0xFF9B9B9B),
                inactiveTrackColor = Color(0xFF5C5C5C)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("14", color = Color.Black, fontSize = 14.sp)
            Text("45", color = Color.Black, fontSize = 14.sp)
        }
    }

}