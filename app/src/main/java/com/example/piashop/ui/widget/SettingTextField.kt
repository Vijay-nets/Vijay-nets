package com.example.piashop.ui.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.piashop.ui.extensions.*
import com.example.piashop.ui.views.error.Error
import com.example.piashop.ui.views.error.FieldStatus
import com.example.piashop.ui.views.error.Success
import com.piasdk.extensions.testElementInDebugMode


@Composable
fun SettingTextFieldState(
    placeholder: String,
    text: String,
    keyboardType: KeyboardType,
    characterLimit: Int = -1,
    imageVector : (Boolean) -> ImageVector? = trailingIconImageVector,
    validation : (String) -> FieldStatus = { Success },
    success: (String) -> Unit
){
    val  compositionLocalProvider = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var errorMessage : String? by remember { mutableStateOf(null) }
    val rememberTextFieldState = remember {
        mutableStateOf(
            TextFieldValue(
                text = text,
                selection = TextRange(0)
            )
        )
    }
    var selectionState by remember { mutableStateOf(false) }

    // Validate field entry and show error or success
    val validator : () -> Unit = {
        when(
            val result = validation(rememberTextFieldState.value.text)
        ) {
            is Error -> {
                errorMessage = result.errorMessage
                selectionState = true
            }

            is Success -> {
                errorMessage = null
                compositionLocalProvider.clearFocus()
                // Update success callback
                success(rememberTextFieldState.value.text)
            }
        }
    }

    // Value change handler
    val onValueChange : (TextFieldValue) -> Unit = { newValue ->
        when {
            characterLimit == -1 -> { rememberTextFieldState.value = newValue }
            newValue.text.length <= characterLimit -> { rememberTextFieldState.value = newValue }
            else -> { validator() }
        }
    }
    // click handler
    val iconClick : () -> Unit = {
        selectionState = !selectionState
        // clear keyboard and focus
        if (!selectionState){
            validator()
        }
    }

    SettingTextField(
        focusRequester= focusRequester,
        placeholder= "Enter $placeholder",
        value = rememberTextFieldState.value,
        selectionState = selectionState,
        keyboardType = keyboardType,
        imageVector = imageVector,
        errorMessage = errorMessage,
        onValueChange = onValueChange,
        onClick = iconClick
    )

    DisposableEffect(selectionState) {
        // focus text field and open keyboard
        if (selectionState){
            focusRequester.requestFocus()
        }
        onDispose {}
    }
}

/**
 * Editable text field for setting item
 */
@Composable
fun SettingTextField(
    focusRequester: FocusRequester,
    placeholder: String,
    value: TextFieldValue,
    selectionState: Boolean,
    keyboardType: KeyboardType,
    imageVector: (Boolean) -> ImageVector?,
    errorMessage: String?,
    onValueChange: (TextFieldValue) -> Unit,
    onClick: () -> Unit
){
    TextField(
        modifier = Modifier
            .then(settingModifier())
            .testElementInDebugMode(placeholder.getTestTag("textField"))
            .focusRequester(focusRequester = focusRequester),
        shape = RoundedCornerShape(10.dp),
        textStyle = textStyle(),
        colors = textColor(),
        value = value,
        onValueChange = onValueChange,
        placeholder = { placeholderText(placeholder) },
        leadingIcon = { leadingIcon() },
        trailingIcon = {
            imageVector(selectionState)?.let { vector ->
                trailingIcon(vector,
                    Modifier
                        .testElementInDebugMode(placeholder.getTestTag("image"))
                        .clickable(onClick = onClick)
                )
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        enabled = selectionState,
        singleLine = true,
        readOnly = false,
        maxLines = 1,
    )
    errorMessage?.let { errorText(it) }
}

/** Extension function format test tag for UI automation */
private fun String.getTestTag(extension: String = "") : String {
    return "$extension${this.replace(" ", "")}"
}

@Composable
@Preview
fun PreviewSettingTextField(){
    SettingTextFieldState(
        placeholder = "customer id",
        text = "",
        keyboardType = KeyboardType.Text
    ) {}
}