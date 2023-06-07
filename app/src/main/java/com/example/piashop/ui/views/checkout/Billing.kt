package com.example.piashop.ui.views.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.piashop.R
import com.example.piashop.ui.theme.PurchaseItemBackground
import com.example.piashop.ui.theme.Purple200
import com.example.piashop.ui.widget.Counter


/** Displays the item to be purchased with amount, currency and changeable item quantity */
@Composable
internal fun Billing(
    quantity: Int,
    amount: Int,
    selectedRegionalCurrency: CurrencyLocale,
    onItemQuantityChanged: (Int) -> Unit
) {
    val amountAndQuantityTextStyle = MaterialTheme.typography.body1.copy(
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colors.secondaryVariant
    )

    Card(
        modifier = Modifier
            .padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PurchaseItemBackground)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(42.dp),
                tint = Purple200,
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(24.dp))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.gift_card),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.amount_with_currency)
                            .format(
                                amount.localeCurrencyFormat(
                                    languageCode = selectedRegionalCurrency.languageCode,
                                    countryCode = selectedRegionalCurrency.countryCode
                                ),
                                selectedRegionalCurrency.currencyCode
                            ),
                        style = amountAndQuantityTextStyle
                    )

                    Counter(
                        minimumCount = 1,
                        countTextRes = R.string.item_quantity,
                        textStyle = amountAndQuantityTextStyle,
                        count = quantity,
                        iconTintColor = MaterialTheme.colors.secondaryVariant,
                        iconModifier = Modifier.size(18.dp),
                        onCountChange = onItemQuantityChanged
                    )
                }
            }
        }
    }
}