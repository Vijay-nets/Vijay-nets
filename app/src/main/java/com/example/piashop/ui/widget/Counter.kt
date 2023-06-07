package com.example.piashop.ui.widget

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/** Displays the view to change item count */
@Composable
fun ItemCounter(
    title: String,
    count: Int,
    onCountChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title)
        Counter(
            count = count,
            onCountChange = onCountChange
        )
    }
}


/** Widget UI provides to increment and decrement count and receive via callback  */
@Composable
fun Counter(
    iconModifier: Modifier = Modifier.size(16.dp),
    textStyle: TextStyle = LocalTextStyle.current,
    iconTintColor: Color = LocalContentColor.current,
    count: Int,
    @StringRes countTextRes: Int? = null,
    minimumCount: Int = 0,
    onCountChange: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = iconModifier,
            onClick = {
                if (count > minimumCount) {
                    onCountChange(count - 1)
                }
            }
        ) {
            Icon(
                tint = iconTintColor,
                imageVector = Icons.Rounded.RemoveCircleOutline,
                contentDescription = null
            )
        }
        countTextRes?.let {
            Text(text = stringResource(id = it), style = textStyle)
        }
        Text(
            text = count.toString().padStart(2, '0'),
            style = textStyle
        )
        IconButton(
            modifier = iconModifier,
            onClick = {
                if (count >= minimumCount) {
                    onCountChange(count + 1)
                }
            }
        ) {
            Icon(
                tint = iconTintColor,
                imageVector = Icons.Rounded.AddCircleOutline,
                contentDescription = null
            )
        }
    }
}
