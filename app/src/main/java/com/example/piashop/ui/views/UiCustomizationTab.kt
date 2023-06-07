package com.example.piashop.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.piashop.ui.extensions.itemsWithTitle
import com.piasdk.api.CardProcess
import com.piasdk.api.CardView
import com.piasdk.api.MerchantDetails
import com.piasdk.api.PiaSDK
import com.piasdk.api.PiaSDK.Appearance.cardContainerStandard
import kotlin.random.Random

@Composable
fun UiCustomizationTab() {

    val canTokenizeCard = true
    val customizableComponents by remember { mutableStateOf(PiaSDK.Appearance.standard) }
    // Ignore `recomposeCardViewMerchantID` value, just used to recompose PiaSDK.CardView UI
    var recomposeCardViewMerchantID by remember { mutableStateOf("") }

    Column {
        PiaSDK.CardView(
            customizableComponents = customizableComponents,
            cardProcess = CardProcess.Payment(
                /* Ignore `recomposeCardViewMerchantID` */
                merchantDetails = MerchantDetails(merchantID = recomposeCardViewMerchantID),
                onTokenizeCardForLaterUse = if (canTokenizeCard) { _ -> } else null
            ),
            onCanSubmit = { /* Ignore */ },
            onResult = { /* Ignore */ }
        )

        UiCustomizationColumn(
            customizableComponents = customizableComponents,
            recompose = { recomposeCardViewMerchantID = Random.nextInt().toString() }
        )
    }
}

@Composable
fun UiCustomizationColumn(
    customizableComponents: PiaSDK.Appearance.CustomizableComponents,
    recompose: () -> Unit
) {
    val customizationTypes = customizableComponents.customizationTypes(recompose = recompose)
        .groupByType()

    LazyColumn(contentPadding = PaddingValues(8.dp)) {
        customizationTypes.forEach { section ->
            itemsWithTitle(section.value, section.key) { customizationType ->
                customizationType.render()
            }
        }
    }
}

private fun PiaSDK.Appearance.CustomizableComponents.customizationTypes(
    customizations: MutableSet<CustomizationType> = mutableSetOf(),
    component: PiaSDK.Appearance.UiCustomizable = this.container,
    recompose: () -> Unit
): Set<CustomizationType> {
    // Recursively iterates customizable components to produce list of customization types.
    val next: PiaSDK.Appearance.UiCustomizable = when (component) {
        is PiaSDK.Appearance.Container -> {
            ColorCustomizer(
                title = "Container background",
                color0 = Color(0xFF9fa8da),
                color1 = Color(0xFFcccab5)
            ) { color ->
                component.modifier = component.modifier.background(color)
                recompose()
            }.also { customizations.add(it) }

            this.cardContainer
        }
        is PiaSDK.Appearance.CardContainer -> {
            ColorCustomizer(
                title = "Card container background",
                color0 = Color(0xFF80DEEA),
                color1 = Color(0xFFE6CEFF)
            ) { color ->
                cardBackgroundColor = color
                (this.cardContainer as? PiaSDK.Appearance.CardContainer)?.changeCardViewAppearance()
                recompose()
            }.also { customizations.add(it) }

            // Note: We can add more customization types for each component if necessary
            ShapeCustomizer(title = "Rounded corner", isInitiallySelected = true) {
                cardBorder = cardBorder.copy(isRoundedCorner = it)
                (this.cardContainer as? PiaSDK.Appearance.CardContainer)?.changeCardViewAppearance()
                recompose()
            }.also { customizations.add(it) }

            DimensionCustomizer(title = "Border width", initialValue = 1) {
                cardBorder = cardBorder.copy(width = it)
                (this.cardContainer as? PiaSDK.Appearance.CardContainer)?.changeCardViewAppearance()
                recompose()
            }.also { customizations.add(it) }

            ColorCustomizer(
                title = "Border",
                color0 = Color(0xFF33691E),
                color1 = Color(0xFF880E4F)
            ) {
                cardBorder = cardBorder.copy(color = it)
                (this.cardContainer as? PiaSDK.Appearance.CardContainer)?.changeCardViewAppearance()
                recompose()
            }.also { customizations.add(it) }

            this.divider.horizontal
        }
        is PiaSDK.Appearance.Divider -> {
            val dividers = listOf(divider.horizontal, divider.vertical)
                    as? List<PiaSDK.Appearance.DividerStandard> ?: emptyList()

            ColorCustomizer(
                title = "Divider",
                color0 = Color(0xFF880E4F),
                color1 = Color(0xFF33691E)
            ) { color ->
                dividers.forEach { it.color = color }
                recompose()
            }.also { customizations.add(it) }

            DimensionCustomizer(title = "Divider width", initialValue = 1) { thickness ->
                dividers.forEach { it.thickness = thickness.dp }
                recompose()
            }.also { customizations.add(it) }

            this.authenticationIconButtonContent
        }
        is PiaSDK.Appearance.AuthenticationButtonContent -> {
            ColorCustomizer(
                title = "Authentication button",
                color0 = Color(0xFFA0D995),
                color1 = Color(0xFF7C3E66)
            ) { color ->
                (component as? PiaSDK.Appearance.AuthenticationButtonContentStandard)
                    ?.let { it.modifier = it.modifier.background(color = color) }
                recompose()
            }.also { customizations.add(it) }

            this.scanButtonImage
        }
        is PiaSDK.Appearance.ScanButtonImage -> {
            ColorCustomizer(
                title = "Scan button icon",
                color0 = Color(0xFF388E3C),
                color1 = Color(0xFF7D1E6A),
            ) { color ->
                (component as? PiaSDK.Appearance.ScanButtonImageStandard)?.iconColor = color
                recompose()
            }.also { customizations.add(it) }

            this.authenticationIndicator
        }
        is PiaSDK.Appearance.AuthenticationIndicator -> {
            ColorCustomizer(
                title = "Authentication progress indicator",
                color0 = Color(0xFF8E3200),
                color1 = Color(0xFFC9BC1F)
            ) { color ->
                (component as? PiaSDK.Appearance.AuthenticationIndicatorStandard)?.color = color
                recompose()
            }.also { customizations.add(it) }

            this.messageView.incompleteEntry
        }
        is PiaSDK.Appearance.MessageView -> {
            val messageViewStandard = component as PiaSDK.Appearance.MessageViewStandard

            ColorCustomizer(
                title = "Error text",
                color0 = Color(0xFFFBC02D),
                color1 = Color(0xFFEF5350)
            ) {
                messageViewStandard.textStyle = messageViewStandard.textStyle.copy(color = it)
                messageViewStandard.iconColor = it
                recompose()
            }.also { customizations.add(it) }

            DimensionCustomizer(title = "Error text size", initialValue = 14) {
                messageViewStandard.iconSize = it.sp
                messageViewStandard.textStyle = messageViewStandard.textStyle.copy(fontSize = it.sp)
                recompose()
            }.also { customizations.add(it) }

            this.text.tokenizeCardToggleText
        }
        is PiaSDK.Appearance.Text -> {
            ColorCustomizer(
                title = "Scan/Save card text",
                color0 = Color(0xFF7D1E6A),
                color1 = Color(0xFF388E3C)
            ) { color ->
                (this.text.tokenizeCardToggleText as? PiaSDK.Appearance.TokenizeCardToggleText)
                    ?.let { it.textStyle = it.textStyle.copy(color = color) }

                (this.text.scanButtonText as? PiaSDK.Appearance.ScanButtonText)
                    ?.let { it.textStyle = it.textStyle.copy(color = color) }
                recompose()
            }.also { customizations.add(it) }

            this.textField.cardNumber
        }
        is PiaSDK.Appearance.TextField -> {
            val textFields = (listOf(
                textField.cardNumber, textField.expiryDate, textField.securityCode
            ) as? List<PiaSDK.Appearance.TextFieldStandard>) ?: emptyList()

            ColorCustomizer(
                title = "Card view label",
                color0 = Color(0xFFB71C1C),
                color1 = Color(0xFF4A148C)
            ) { color ->
                textFields.forEach { it.labelTextStyle = it.labelTextStyle.copy(color = color) }
                recompose()
            }.also { customizations.add(it) }

            ColorCustomizer(
                title = "Card view text",
                color0 = Color(0xFFAA00FF),
                color1 = Color(0xFF1745B0)
            ) { color ->
                textFields.forEach { it.textStyle = it.textStyle.copy(color = color) }
                recompose()
            }.also { customizations.add(it) }

            ColorCustomizer(
                title = "Card view placeholder",
                color0 = Color(0xFF0088A3),
                color1 = Color(0xFF7200CA)
            ) { color ->
                textFields.forEach {
                    it.placeholderTextStyle = it.placeholderTextStyle.copy(color = color)
                }
                recompose()
            }.also { customizations.add(it) }

            ColorCustomizer(
                title = "Card view cursor",
                color0 = Color(0xFFFFB04C),
                color1 = Color(0xFF519657)
            ) { color ->
                textFields.forEach { it.cursorColor = color }
                recompose()
            }.also { customizations.add(it) }

            ColorCustomizer(
                title = "Card view error highlight",
                color0 = Color(0xFF98EE99),
                color1 = Color(0xFFFFCCBC)
            ) { color ->
                textFields.forEach { it.messageHighlightColor = color }
                recompose()
            }.also { customizations.add(it) }

            this.toggleView
        }
        is PiaSDK.Appearance.ToggleView -> {
            ColorCustomizer(
                title = "Save card switch",
                color0 = Color(0xFFA27B5C),
                color1 = Color(0xFF277BC0)
            ) {
                (component as? PiaSDK.Appearance.TokenizeViewStandard)?.color = {
                    SwitchDefaults.colors(checkedThumbColor = it)
                }

                recompose()
            }.also { customizations.add(it) }

            component
        }
    }

    return if (next == component) customizations else customizationTypes(
        customizations = customizations,
        component = next,
        recompose = recompose
    )
}

private var cardBorder = Border()

private val cardShape: Shape
    get() = if (cardBorder.isRoundedCorner) {
        RoundedCornerShape(10.dp)
    } else {
        RectangleShape
    }

private var cardBackgroundColor = PiaSDK.Appearance.colors.background

private fun PiaSDK.Appearance.CardContainer.changeCardViewAppearance(
    border: Border = cardBorder, backgroundColor: Color = cardBackgroundColor
) {
    modifier = Modifier
        .cardContainerStandard {
            clip(shape = cardShape).then(
                Modifier.border(width = border.width.dp, color = border.color, shape = cardShape)
            )
        }
        .background(color = backgroundColor, shape = cardShape)
}

/** Border properties for card entry box */
private data class Border(
    var width: Int = 1,
    var color: Color = PiaSDK.Appearance.colors.border,
    var isRoundedCorner: Boolean = true
)
