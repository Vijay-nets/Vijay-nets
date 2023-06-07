package com.example.piashop.ui.views

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.piashop.R
import com.example.piashop.merchantapi.PaymentMethodDetail
import com.example.piashop.merchantapi.TokenizedCard
import com.example.piashop.ui.extensions.CollapsibleView
import com.example.piashop.ui.theme.PaymentMethodBorder


interface Selection {
    /**
     * Item should call `toggle` after changing its own selection state.
     * The toggle is executed on `selectable` other than the caller to ensure single selection.
     */
    fun toggle(selectable: PaymentMethodGroup, onDeselect: () -> Unit)

    /**
     * Determines if the current payment method is selected or not
     */
    fun isSelected(paymentMethodId: String): Boolean
}

private object SelectionToggle : Selection {

    // Map of selectable items with unique ID and associated lambda to de-select the item.
    private val options: MutableMap<String, () -> Unit> = mutableMapOf()

    // Currently selected item.
    private var selected: PaymentMethodGroup? = null

    // Observing lambda that's invoked upon selection event.
    var onSelection: (PaymentMethodGroup?) -> Unit = {}

    override fun isSelected(paymentMethodId: String): Boolean {
        selected?.let {
            return when (it) {
                is PaymentMethodGroup.Other -> {
                    it.paymentMethodDetail.id == paymentMethodId
                }
                is PaymentMethodGroup.Token -> {
                    it.tokenizedCard.tokenId == paymentMethodId
                }
            }
        }
        return false
    }

    override fun toggle(selectable: PaymentMethodGroup, onDeselect: () -> Unit) {
        val key: String = when (selectable) {
            is PaymentMethodGroup.Other -> selectable.paymentMethodDetail.id
            is PaymentMethodGroup.Token -> selectable.tokenizedCard.tokenId
        }
        options[key] = onDeselect
        options.filter { it.key != key }.values.forEach {
            it.invoke()
        }
        selected = if (selected == selectable) null else selectable
        onSelection.invoke(selected)
    }
}

// region Payment method sections 
@Composable
fun PaymentMethodsView(
    paymentMethodSections: PaymentMethodSections?,
    onSelection: (PaymentMethodGroup?) -> Unit,
    cardEntryView: @Composable () -> Unit
) {
    if (paymentMethodSections == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(36.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colors.primaryVariant)
        }
        return
    }

    SelectionToggle.onSelection = onSelection

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TokenizedCardListView(tokens = paymentMethodSections.tokens, selection = SelectionToggle)
        WalletListView(wallets = paymentMethodSections.wallets, selection = SelectionToggle)
        FinnishBanksListView(
            finnishBanks = paymentMethodSections.finnishBanks, selection = SelectionToggle
        )
        cardEntryView()
    }
}

/** Represents list of tokenized card in an expandable list */
@Composable
private fun TokenizedCardListView(
    tokens: List<TokenizedCard>,
    isCollapsed: Boolean = true,
    selection: Selection
) {
    if (tokens.isNotEmpty()) {
        CollapsibleView(
            title = stringResource(R.string.tokenized_card),
            isCollapsed = isCollapsed,
            borderColor = PaymentMethodBorder
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                tokens.forEach { token ->
                    if (token.cardVerificationRequired) {
                        var securityCode by remember { mutableStateOf("") }
                        TokenizedCardView(
                            token = token,
                            securityCode = securityCode,
                            onValueChange = { securityCode = it },
                            selection = selection
                        )
                    } else {
                        TokenizedCardView(
                            token = token,
                            selection = selection
                        )
                    }
                }
            }
        }
    }
}

/** Returns drawable resource ID based on the payment method ID */
fun String.paymentMethodIconResource(context: Context): Int {
    return context.resources.getIdentifier(lowercase(), "drawable", context.packageName)
}

/** Returns Modifier with width and height of composable based on schema ID */
fun Modifier.schemaIconSize(schema: String): Modifier {
    return when (schema) {
        "AmericanExpress" -> this
            .width(40.dp)
            .height(40.dp)
        "Visa", "Visa19" -> this
            .width(35.dp)
            .height(10.dp)
        "Dankort", "DankortVisa" -> this
            .width(40.dp)
            .height(20.dp)
        else -> this
            .width(50.dp)
            .height(30.dp)
    }
}

/** Represents list of wallets in an expandable list */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WalletListView(
    wallets: List<PaymentMethodDetail>,
    isCollapsed: Boolean = true,
    selection: Selection
) {
    if (wallets.isNotEmpty()) {
        CollapsibleView(
            title = stringResource(R.string.wallet),
            isCollapsed = isCollapsed,
            borderColor = PaymentMethodBorder
        ) {
            // TODO: Replace with grid view
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                wallets.windowed(2, 2).forEach { walletSubList ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        walletSubList.forEach { wallet ->
                            SinglePaymentMethodView(paymentMethod = wallet, selection = selection)
                        }
                    }
                }
            }
        }
    }
}

/** Represents list of finnish banks in an expandable list */
@Composable
private fun FinnishBanksListView(
    finnishBanks: List<PaymentMethodDetail>,
    isCollapsed: Boolean = true,
    selection: Selection
) {
    if (finnishBanks.isNotEmpty()) {
        CollapsibleView(
            title = stringResource(R.string.finnish_bank),
            isCollapsed = isCollapsed,
            borderColor = PaymentMethodBorder
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                finnishBanks.windowed(2, 2).forEach { bankSubList ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bankSubList.forEach { bank ->
                            SinglePaymentMethodView(paymentMethod = bank, selection = selection)
                        }
                    }
                }
            }
        }
    }
}

/** Represents single payment method component */
@Composable
private fun RowScope.SinglePaymentMethodView(
    paymentMethod: PaymentMethodDetail,
    selection: Selection
) {
    // TODO: Issue while collapse/expand, click new item -> double item selected
    var isSelected by remember { mutableStateOf(selection.isSelected(paymentMethodId = paymentMethod.id)) }
    val borderWidth by derivedStateOf {
        if (isSelected) { 2.dp } else { 1.5.dp }
    }

    val onSelection: () -> Unit = {
        isSelected = !isSelected
        selection.toggle(
            selectable = PaymentMethodGroup.Other(paymentMethod),
            onDeselect = { isSelected = false }
        )
    }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onSelection)
            .fillMaxWidth()
            .weight(1f)
            .border(
                width = borderWidth,
                color = if (isSelected) {
                    MaterialTheme.colors.secondary
                } else {
                    MaterialTheme.colors.secondary.copy(alpha = 0.4f)
                },
                shape = RoundedCornerShape(15)
            )
            .padding(horizontal = 8.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier
                .fillMaxSize(),
            painter = painterResource(
                id = paymentMethod.id.paymentMethodIconResource(LocalContext.current)
            ),
            contentDescription = null,
            contentScale = ContentScale.Fit
        )
        /*Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {


            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = paymentMethod.displayName.replace("Paytrail", ""),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) {
                    MaterialTheme.colors.secondary
                } else {
                    MaterialTheme.colors.onPrimary.copy(alpha = 0.4f)
                }
            )
        }*/
    }
}