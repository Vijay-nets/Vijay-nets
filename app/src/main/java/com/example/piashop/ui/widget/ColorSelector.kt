package com.example.piashop.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.piasdk.extensions.testElementInDebugMode

/** Widget provide way to select color between two color and highlight selected color in the text */
@Composable
fun ColorSelector(
    title: String,
    color0: Color,
    color1: Color,
    onSelection: (Color) -> Unit
) {
    var selectedColor by remember { mutableStateOf(color0) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, color = selectedColor)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val modifier: Modifier = Modifier
                .height(20.dp)
                .width(20.dp)

            Box(
                modifier = modifier
                    .background(color0)
                    .testElementInDebugMode(color0.value.toString())
                    .clickable {
                        selectedColor = color0
                        onSelection(selectedColor)
                    }
            )

            Box(
                modifier = modifier
                    .background(color1)
                    .testElementInDebugMode(color1.value.toString())
                    .clickable {
                        selectedColor = color1
                        onSelection(selectedColor)
                    }
            )
        }
    }
}

fun Color.Companion.parse(colorString: String): Color =
    Color(color = android.graphics.Color.parseColor(colorString))