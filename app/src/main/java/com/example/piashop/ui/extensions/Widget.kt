package com.example.piashop.ui.extensions

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/**
 * Modifier for setting items
 */
val settingModifier: () -> Modifier by lazy {
    {
        Modifier
            .fillMaxWidth()
            .padding(top = 5.dp, bottom = 5.dp)
    }
}

/**
 * Placeholder text for setting item text field
 */
val placeholderText: @Composable (String) -> Unit by lazy {
    {
        Text(
            text = it,
            fontSize = 14.sp,
            fontFamily = FontFamily.SansSerif,
            fontStyle = FontStyle.Normal,
            color = MaterialTheme.colors.primary
        )
    }
}

/**
 * Text field color for setting items
 */
val textColor: @Composable () -> TextFieldColors by lazy {
    {
        TextFieldDefaults.textFieldColors(
            backgroundColor = MaterialTheme.colors.primaryVariant,
            disabledLabelColor = MaterialTheme.colors.primaryVariant,
            disabledIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    }
}

/**
 * Text field style for setting items
 */
val textStyle: @Composable () -> TextStyle by lazy {
    {
        TextStyle(
            fontSize = 14.sp,
            fontFamily = FontFamily.SansSerif,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary,
        )
    }
}

/**
 * Leading icon for setting item
 */
val leadingIcon: @Composable () -> Unit by lazy {
    {
        Icon(
            imageVector = Icons.Outlined.Info,
            tint = MaterialTheme.colors.primary,
            contentDescription = "info"
        )
    }
}

/**
 * Trailing icon for setting items
 */
val trailingIcon: @Composable (ImageVector, Modifier) -> Unit by lazy {
    { imageVector, modifier ->
        Icon(
            imageVector = imageVector,
            tint = MaterialTheme.colors.primary,
            contentDescription = "icon",
            modifier = modifier
        )
    }
}


/**
 * Icon for setting field ui
 */
val trailingIconImageVector: (Boolean) -> ImageVector = { checked ->
    if (checked) Icons.Default.Done else Icons.Default.Edit
}


/**
 * Error text for setting item text field
 */
val errorText: @Composable (String) -> Unit by lazy {
    {
        Text(
            text = it,
            fontSize = 14.sp,
            fontFamily = FontFamily.SansSerif,
            fontStyle = FontStyle.Normal,
            color = Color.Red
        )
    }
}