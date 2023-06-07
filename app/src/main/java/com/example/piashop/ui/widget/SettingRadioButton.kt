package com.example.piashop.ui.widget

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.RadioButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp
import com.example.piashop.ui.extensions.leadingIcon
import com.example.piashop.ui.extensions.settingModifier
import com.example.piashop.ui.extensions.textColor
import com.example.piashop.ui.extensions.textStyle


/** View for single item selection from multi-choice options */
@Composable
fun SettingRadioState(
    options: List<String>,
    selectedItem: Int,
    onOptionSelect: (String) -> Unit
) {
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(options[selectedItem]) }
    options.forEach { text ->
        TextField(
            modifier = Modifier.composed { settingModifier.invoke() },
            shape = RoundedCornerShape(10.dp),
            textStyle = textStyle.invoke(),
            colors = textColor.invoke(),
            value = text,
            onValueChange = {},
            leadingIcon = { leadingIcon() },
            trailingIcon = {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = {
                        onOptionSelected(text)
                        onOptionSelect(text)
                    }
                )
            },
            enabled = false,
            singleLine = true,
            readOnly = false,
            maxLines = 1
        )
    }
}
