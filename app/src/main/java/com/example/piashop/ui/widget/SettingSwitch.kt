package com.example.piashop.ui.widget

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.piashop.ui.extensions.*
import com.piasdk.extensions.testElementInDebugMode


/**
 * Icon for setting switch ui
 */
val switchImageVector : (Boolean) -> ImageVector = { checked ->
    if (checked) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle
}
// Add Icon Image Vector
val addImageVector : (Boolean) -> ImageVector = { checked ->
    Icons.Default.AddCircle
}

@Composable
fun SettingSwitchState(
    text: String,
    selected: Boolean,
    imageVector : (Boolean) -> ImageVector = switchImageVector,
    onClicked : (Boolean) -> Unit = { }
){
    var value by remember { mutableStateOf(text) }
    var selectionState by remember { mutableStateOf(selected) }

    // click handler
    val iconClick : () -> Unit = {
        selectionState = !selectionState
        onClicked(selectionState)
    }

    SettingSwitch(
        value = value,
        selectionState = selectionState,
        imageVector = imageVector,
        onClick = iconClick
    )
}

@Composable
fun SettingSwitchState(
    @StringRes textId: Int,
    selected: Boolean,
    imageVector: (Boolean) -> ImageVector = switchImageVector,
    onClicked: (Boolean) -> Unit = { }
) {
    SettingSwitchState(
        text = stringResource(id = textId),
        selected = selected,
        imageVector = imageVector,
        onClicked = onClicked
    )
}

/**
 * Switch ui for enable/disable setting
 */
@Composable
fun SettingSwitch(
    value: String,
    selectionState: Boolean,
    imageVector: (Boolean) -> ImageVector,
    onClick: () -> Unit
){
    TextField(
        modifier = Modifier.then(settingModifier.invoke()),
        shape = RoundedCornerShape(10.dp),
        textStyle = textStyle.invoke(),
        colors = textColor.invoke(),
        value = value,
        onValueChange = {},
        leadingIcon = { leadingIcon.invoke() },
        trailingIcon = {
            trailingIcon.invoke(
                imageVector(selectionState),
                Modifier
                    .testElementInDebugMode(
                        value.getTestTag("$selectionState")
                    )
                    .clickable(onClick = onClick)
            )
        },
        enabled = selectionState,
        singleLine = true,
        readOnly = false,
        maxLines = 1
    )
}


/** Extension function format test tag for UI automation */
private fun String.getTestTag(state: String = "") : String {
    return "${this.replace(" ", "")}-$state"
}

@Composable
@Preview
fun PreviewSettingSwitchState(){
    SettingSwitchState(
        text = "Use Test Environment",
        selected = false
    )
}