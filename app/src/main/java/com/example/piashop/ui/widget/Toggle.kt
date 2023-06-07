package com.example.piashop.ui.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.piasdk.extensions.testElementInDebugMode

/** Widget is combination of text and switch/toggle button */
@Composable
fun Toggle(
    title: String,
    isSelected: Boolean,
    onSwitchChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title)
        Switch(
            modifier = Modifier.testElementInDebugMode("$title-$isSelected"),
            checked = isSelected,
            onCheckedChange = onSwitchChange
        )
    }
}