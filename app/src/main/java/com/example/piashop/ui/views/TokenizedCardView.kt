package com.example.piashop.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.piashop.R
import com.example.piashop.merchantapi.TokenizedCard

const val cardNumberResource = R.string.card_number
const val expiryDateResource = R.string.expiry_date
const val securityCodeResource = R.string.security_code

object TokenizedCardViewProperties {
    val height = 75.dp
    val titleFontSize: TextUnit = 10.sp
    val valueFontSize: TextUnit = 14.sp

    object Padding {
        val all = 10.dp
    }

    object Border {
        val width = 1.dp
    }

    object Scheme {
        val height = 40.dp
        val width = 70.dp
        val corner = 4.dp

        object Border {
            val width = 1.dp
        }
    }
}

// ui style definitions
interface ParentContainerStyle {
    val modifier: Modifier
        get() = Modifier
            .fillMaxWidth()
            .height(TokenizedCardViewProperties.height)
            .padding(all = TokenizedCardViewProperties.Padding.all)
            .composed {
                clip(MaterialTheme.shapes.medium)
            }
            .composed {
                background(MaterialTheme.colors.primary)
            }
            .composed {
                border(
                    width = TokenizedCardViewProperties.Border.width,
                    color = MaterialTheme.colors.onSurface,
                    shape = MaterialTheme.shapes.medium
                )
            }

    val horizontalArrangement: Arrangement.Horizontal get() = Arrangement.SpaceAround
    val verticalAlignment: Alignment.Vertical get() = Alignment.CenterVertically
}

interface SchemeStyle {
    val modifier: Modifier
        get() = Modifier
            .width(TokenizedCardViewProperties.Scheme.width)
            .height(TokenizedCardViewProperties.Scheme.height)
            .composed {
                clip(RoundedCornerShape(TokenizedCardViewProperties.Scheme.corner))
            }
            .composed {
                border(
                    width = TokenizedCardViewProperties.Scheme.Border.width,
                    color = MaterialTheme.colors.onSurface,
                    shape = MaterialTheme.shapes.medium
                )
            }
}

interface TitleTextStyle {
    val textStyle: @Composable (() -> TextStyle)
        get() = {
            TextStyle(
                fontSize = TokenizedCardViewProperties.titleFontSize,
                color = MaterialTheme.colors.onPrimary
            )
        }
}

interface ContentTextStyle {
    val textStyle: @Composable () -> TextStyle
        get() = {
            TextStyle(
                fontSize = TokenizedCardViewProperties.valueFontSize,
                color = MaterialTheme.colors.onPrimary
            )
        }
}

interface Style {
    val containerStyle: ParentContainerStyle get() = object : ParentContainerStyle {}
    val titleStyle: TitleTextStyle get() = object : TitleTextStyle {}
    val contentStyle: ContentTextStyle get() = object : ContentTextStyle {}
    val schemeStyle: SchemeStyle get() = object : SchemeStyle {}
}

@Composable
fun TokenizedCardView(
    style: Style = object : Style {},
    token: TokenizedCard,
    cardNumberTitle: String = stringResource(id = cardNumberResource),
    expiryDateTitle: String = stringResource(id = expiryDateResource),
    securityCodeTitle: String = stringResource(id = securityCodeResource),
    securityCode: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onValueChange: (String) -> Unit = {},
    selection: Selection
) {
    var isSelected by remember { mutableStateOf(selection.isSelected(paymentMethodId = token.tokenId)) }
    val borderWidth by derivedStateOf {
        if (isSelected) {
            1.5.dp
        } else {
            1.dp
        }
    }

    val onSelection: () -> Unit = {
        isSelected = !isSelected
        selection.toggle(
            selectable = PaymentMethodGroup.Token(token),
            onDeselect = { isSelected = false }
        )
    }

    Row(
        modifier = Modifier
            .clickable(onClick = onSelection)
            .border(
                width = borderWidth,
                color = if (isSelected) {
                    MaterialTheme.colors.secondary
                } else {
                    MaterialTheme.colors.secondary.copy(alpha = 0.4f)
                },
                shape = MaterialTheme.shapes.medium
            )
            .padding(12.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .schemaIconSize(token.issuer)
                .weight(0.2f, false),
            painter = painterResource(
                id = token.issuer.paymentMethodIconResource(LocalContext.current)
            ),
            contentDescription = null,
        )

        CustomText(
            title = cardNumberTitle,
            value = token.tokenId.takeLast(8),
            titleTextStyle = style.titleStyle.textStyle(),
            valueTextStyle = style.contentStyle.textStyle()
        )
        CustomText(
            title = expiryDateTitle,
            value = token.expiryDate,
            titleTextStyle = style.titleStyle.textStyle(),
            valueTextStyle = style.contentStyle.textStyle()
        )

        securityCode?.let {
            CustomTextField(
                title = securityCodeTitle,
                value = securityCode,
                visualTransformation = visualTransformation,
                onValueChange = onValueChange,
                titleTextStyle = style.titleStyle.textStyle(),
                valueTextStyle = style.contentStyle.textStyle()
            )
        }
    }
}

@Composable
private fun CustomText(
    title: String,
    value: String,
    titleTextStyle: TextStyle,
    valueTextStyle: TextStyle,
) {
    Column {
        Text(
            text = title,
            style = titleTextStyle
        )
        Text(
            text = value,
            style = valueTextStyle
        )
    }
}


@Composable
private fun CustomTextField(
    title: String,
    value: String,
    visualTransformation: VisualTransformation,
    onValueChange: (String) -> Unit,
    titleTextStyle: TextStyle,
    valueTextStyle: TextStyle,
) {
    val focusRequester = remember { FocusRequester() }
    Column(
        modifier = Modifier.clickable {
            focusRequester.requestFocus()
        }
    ) {
        Text(
            text = title,
            style = titleTextStyle
        )
        BasicTextField(
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .focusRequester(focusRequester = focusRequester),
            value = value,
            onValueChange = onValueChange,
            textStyle = valueTextStyle,
            visualTransformation = visualTransformation,
            singleLine = true,
            maxLines = 1,
            cursorBrush = SolidColor(MaterialTheme.colors.onPrimary),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )
    }
}