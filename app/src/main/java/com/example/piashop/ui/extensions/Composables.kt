package com.example.piashop.ui.extensions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** A container view that toggles visibility of its content by collapsing on click. */
// Due to: `AnimatedVisibility` used for collapse animation.
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CollapsibleView(
    modifier: Modifier = Modifier,
    title: String,
    isCollapsed: Boolean = false,
    borderColor: Color,
    content: @Composable () -> Unit
) {
    var isCollapsed by remember { mutableStateOf(isCollapsed) }

    val iconAngle by animateFloatAsState(
        targetValue = if (!isCollapsed) -180f else 0f,
        animationSpec = tween(durationMillis = 200)
    )

    Column(
        modifier = modifier.border(
            width = 1.2.dp,
            color = borderColor,
            shape = MaterialTheme.shapes.medium
        )
    ) {
        // Title and expand/collapse arrow icon
        Row(
            modifier = Modifier
                .clickable(onClick = { isCollapsed = !isCollapsed })
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title)
            Icon(
                modifier = Modifier.rotate(iconAngle),
                imageVector = Icons.Rounded.ArrowDropDown,
                contentDescription = "Expand or collapse"
            )
        }
        // Content which will be visible/invisible
        AnimatedVisibility(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            visible = !isCollapsed
        ) {
            content.invoke()
        }
    }
}

inline fun <T> LazyListScope.itemsWithTitle(
    items: List<T>,
    title: String,
    crossinline itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    item {
        Text(text = title)
    }
    items(items.size) {
        itemContent(items[it])
    }
}