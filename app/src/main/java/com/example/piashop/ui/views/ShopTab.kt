package com.example.piashop.ui.views

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.piashop.R
import com.example.piashop.integration.ShopTabViewModelApi
import com.example.piashop.merchantapi.AvailablePaymentMethods
import com.example.piashop.merchantapi.PaymentMethodDetail
import com.example.piashop.merchantapi.TokenizedCard
import com.example.piashop.ui.views.checkout.*
import com.piasdk.extensions.testElementInDebugMode


/** Payment methods grouped based on their internal data-types. */
sealed interface PaymentMethodGroup {
    data class Token(val tokenizedCard: TokenizedCard) : PaymentMethodGroup
    data class Other(val paymentMethodDetail: PaymentMethodDetail) : PaymentMethodGroup

    fun isEqual(to: PaymentMethodDetail) : Boolean {
        return when(this) {
            is Token -> to == PaymentMethodDetail.easyPay
            is Other -> to == this.paymentMethodDetail
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun ShopTab(
    viewModelApi: ShopTabViewModelApi,
    cardEntryView: @Composable () -> Unit
) {
    val uiState by viewModelApi.uiState.collectAsState()
    val selectedCurrency: CurrencyLocale = uiState.currency
    var selectedPaymentMethod: PaymentMethodGroup? by remember { mutableStateOf(PaymentMethodGroup.Other(PaymentMethodDetail.card)) }

    val isPayButtonEnabled: Boolean by derivedStateOf {
        val cardPayment = selectedPaymentMethod?.let {
            it.isEqual(to = PaymentMethodDetail.easyPay)
        }

        selectedPaymentMethod?.let { selected ->
            selected.isEqual(to = PaymentMethodDetail.easyPay)
        } ?: false
    }

    LaunchedEffect(Unit) {
        viewModelApi.fetchPaymentMethods()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            item {
                Billing(
                    quantity = uiState.itemQuantity,
                    amount = uiState.totalAmount,
                    selectedRegionalCurrency = selectedCurrency,
                    onItemQuantityChanged = viewModelApi::updateItemQuantity
                )
            }

            item {
                CurrencySelector(
                    currencies = { currencies },
                    currentSelection = selectedCurrency,
                    onSelect = viewModelApi::selectCurrency,
                )
            }

            item {
                PaymentMethodsView(
                    paymentMethodSections = uiState.paymentMethods?.asPaymentMethodSections,
                    onSelection = { selectedPaymentMethod = it },
                    cardEntryView = cardEntryView
                )
            }
        }
        Button(
            enabled = uiState.isPayButtonEnabled,
            modifier = Modifier
                .testElementInDebugMode("btnPay")
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                disabledBackgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.4f),
                disabledContentColor = MaterialTheme.colors.onPrimary.copy(
                    alpha = ContentAlpha.disabled
                )
            ),
            onClick = {
                selectedPaymentMethod?.let {
                    when(it) {
                        is PaymentMethodGroup.Other -> it.paymentMethodDetail
                        is PaymentMethodGroup.Token -> PaymentMethodDetail.easyPay
                    }.let { viewModelApi.payWith(paymentMethodDetail = it)}
                }
            }
        ) {
            if (uiState.isPaymentProcessing) {
                PaymentProcessLoading()
            } else {
                Text(
                    text = stringResource(id = R.string.pay_button_label).format(
                        uiState.totalAmount.localeCurrencyFormat(
                            languageCode = selectedCurrency.languageCode,
                            countryCode = selectedCurrency.countryCode
                        ),
                        selectedCurrency.currencyCode
                    )
                )
            }
        }
    }
}

// endregion

/** An object containing list of each section shown in payment method list UI */
data class PaymentMethodSections(
    val tokens: List<TokenizedCard>,
    val wallets: List<PaymentMethodDetail> = listOf(),
    val finnishBanks: List<PaymentMethodDetail> = listOf(),
    val sBusinessCards: List<PaymentMethodDetail> = listOf(),
    val cardSchemeIDs: List<String> = listOf()
)

/** Mobile wallet constant IDs matching merchant BE IDs */
private enum class MobileWallet(val id: String) {
    ApplePay("ApplePay"),
    PayPal("PayPal"),
    Vipps("Vipps"),
    Swish("SwishM"), // Note: Swish's payment ID is `SwishM`
    MobilePay("MobilePay")
}

/** Returns payment method sections instance after mapping `AvailablePaymentMethods`. */
val AvailablePaymentMethods.asPaymentMethodSections: PaymentMethodSections get() {

    val walletIDs: List<String> = MobileWallet.values().map { it.id.lowercase() }

    return methods
        .filter { it.id != PaymentMethodDetail.easyPay.id }
        .filter { it.id != PaymentMethodDetail.applePay.id }
        .sortedBy { it.id }
        // Grab the `tokens` and fill the other payment method sections in the fold operation.
        .fold(
            initial = PaymentMethodSections(tokens = tokens.map {
                it.copy(cardVerificationRequired = cardVerificationRequired)
            })
        ) { sections, paymentMethod ->

            return@fold if (paymentMethod.id.lowercase().contains("paytrail")) {
                sections.copy(finnishBanks = sections.finnishBanks + paymentMethod)
            } else {
                when (paymentMethod.id.lowercase()) {
                    in walletIDs ->
                        sections.copy(wallets = sections.wallets + paymentMethod)
                    PaymentMethodDetail.sBusinessCard.id.lowercase() ->
                        sections.copy(sBusinessCards = sections.sBusinessCards + paymentMethod)
                    else ->
                        sections.copy(cardSchemeIDs = sections.cardSchemeIDs + paymentMethod.id)
                }
            }
        }
}