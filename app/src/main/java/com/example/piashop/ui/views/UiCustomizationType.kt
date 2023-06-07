package com.example.piashop.ui.views

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.example.piashop.ui.widget.ColorSelector
import com.example.piashop.ui.widget.ItemCounter
import com.example.piashop.ui.widget.Toggle


// SDK customisation definition region

sealed interface CustomizationType {
    val title: String
    val render: @Composable () -> Unit
}

fun Set<CustomizationType>.groupByType(): Map<String, List<CustomizationType>> {
    return groupBy {
        it.javaClass.simpleName.replace("Customizer", " customization")
    }
}

data class ColorCustomizer(
    override val title: String,
    val color0: Color = Color.Red,
    val color1: Color = Color.Blue,
    val onSelection: (Color) -> Unit
) : CustomizationType {
    override val render: @Composable () -> Unit
        get() = {
            ColorSelector(
                title = title,
                color0 = color0,
                color1 = color1,
                onSelection = onSelection
            )
        }
}

data class ShapeCustomizer(
    override val title: String,
    val isInitiallySelected: Boolean,
    val onToggle: (Boolean) -> Unit
) : CustomizationType {
    override val render: @Composable () -> Unit
        get() = {
            var isSelected by remember { mutableStateOf(isInitiallySelected) }
            Toggle(
                title = title,
                isSelected = isSelected,
                onSwitchChange = {
                    isSelected = it
                    onToggle(isSelected)
                }
            )
        }
}

data class DimensionCustomizer(
    override val title: String,
    val initialValue: Int,
    val onValueChange: (Int) -> Unit
    ) : CustomizationType {
    override val render: @Composable () -> Unit
        get() = {
            var count by remember { mutableStateOf(initialValue) }
            ItemCounter(
                title = title,
                count = count,
                onCountChange = {
                    count = it
                    onValueChange(count)
                }
            )
        }
}