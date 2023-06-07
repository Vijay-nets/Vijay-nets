package com.example.piashop.integration

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.piashop.merchantapi.*
import com.example.piashop.persistence.baseURL
import com.example.piashop.persistence.configurations
import com.example.piashop.persistence.merchantID
import com.example.piashop.ui.views.checkout.CurrencyLocale
import com.example.piashop.ui.views.checkout.currencies
import com.piasdk.api.*
import com.piasdk.card.netaxept.RegistrationResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update


typealias Cents = Int
typealias Currency = CurrencyLocale

/** Shop tab readonly Ui state properties that's reflected on `ShopTab` tab/screen. */
data class ShopTabUiState(
    val itemPrice: Cents = 100, // 1 note
    val currency: Currency = currencies.first(),
    val itemQuantity: Int = 1,
    val displaysCardTokenizationToggle: Boolean = true,
    val paymentMethods: AvailablePaymentMethods? = null,
    private val mode: ShopTabUiMode = ShopTabUiMode.FetchingPaymentMethods
) {
    val totalAmount: Int get() = itemPrice * itemQuantity
    val isPaymentProcessing: Boolean get() = mode is ShopTabUiMode.Processing
    val isPayButtonEnabled: Boolean get() = mode is ShopTabUiMode.ReadyWithPaymentMethod
    var tokenizeCardForLaterUse: Boolean = false
        private set

    // Returns a lambda that toggles card-tokenization for later use.
    // Returns `null` if this option should be omitted from CardView interface.
    val setTokenizeCardForLaterUse: ((tokenize: Boolean) -> Unit)?
        get() = if (displaysCardTokenizationToggle)
            { tokenize -> this.tokenizeCardForLaterUse = tokenize }
        else null // Results in hiding card-tokenization toggle.

}

/**
 * Shop tab's (screen) mode at a given point. e.g. _fetching payment methods_ from merchant BE,
 * waiting for the user on idle mode to _select a payment method_ or processing payment.
 */
sealed interface ShopTabUiMode {
    /** Payment method fetch (from merchant BE) is in progress.*/
    object FetchingPaymentMethods : ShopTabUiMode

    /** Ui is idle waiting for user to select a payment method. */
    data class SelectingPaymentMethod(val available: AvailablePaymentMethods?) : ShopTabUiMode

    /** User has selected payment method and is yet to tap the pay button. */
    data class ReadyWithPaymentMethod(val selected: PaymentMethodDetail) : ShopTabUiMode

    /**
     * A payment process has been initiated. Includes merchant BE and PiaSdk processes.
     * The Ui mode returns to idle / [SelectingPaymentMethod] once a payment process is complete.
     */
    object Processing : ShopTabUiMode
}


//region - ShopTabViewModel

private class ShopTabViewModel(
    private val merchantAPI: MerchantAPI,
    useProdMerchantEnvironment: Boolean = false,
    uiState: ShopTabUiState,
    private var completionMessage: (CompletionMessage) -> Unit
) : ViewModel(),
    ShopTabViewModelApi,
    PiaCompletionCallback.CardProcess,
    PiaCardProcessIntegration<PaymentMethodDetail> {

    override val uiState = MutableStateFlow(uiState)

    override val cardProcess: CardProcess = CardProcess.Payment(
        merchantDetails = MerchantDetails(
            merchantID = merchantAPI.merchantID,
            isProd = useProdMerchantEnvironment
        ),
        onTokenizeCardForLaterUse = this.uiState.value.setTokenizeCardForLaterUse
    )

    //region - PiaCardProcessIntegration

    override val cardPayment: CardProcess.Payment
        get() = cardProcess as CardProcess.Payment

    override val cardProcessCompletion: PiaCompletionCallback.CardProcess = this

    override fun enableButtonForCardProcess(enable: Boolean) {
        uiState.update { state ->
            state.copy(
                mode = if (enable) {
                    ShopTabUiMode.ReadyWithPaymentMethod(selected = PaymentMethodDetail.card)
                } else {
                    ShopTabUiMode.SelectingPaymentMethod(
                        available = uiState.value.paymentMethods
                    )
                }
            )
        }
    }

    override fun registerPayment(
        paymentMethodDetail: PaymentMethodDetail,
        success: (RegistrationResponse.Success) -> Unit,
        failure: (RegistrationResponse.Failure<*>) -> Unit
    ) {
        uiState.update { state -> state.copy(mode = ShopTabUiMode.Processing) }

        merchantAPI.registerPayment(
            registrationRequestBody = uiState.value.makeRegistrationRequestBody(
                customerID = merchantAPI.customerID,
                method = null
            ),
            success = { registrationResponse ->
                RegistrationResponse.Success(
                    transactionID = registrationResponse.transactionId,
                    redirectURL = Uri.parse(registrationResponse.redirectOK)
                ).apply(success)
            },
            failure = { registrationRequestError ->
                RegistrationResponse.Failure(error = registrationRequestError)
                    .apply(failure)
            }
        )
    }
    //endregion

    // region - PiaCompletion + .CardProcess

    override fun onResult(cardProcessResult: CardProcessResult) {
        cardProcessCompletion.onProcessResult(cardProcessResult = cardProcessResult)
    }

    override fun onSuccess(success: Success) {
        merchantAPI.commitPayment(
            processPaymentRequestBody = ProcessPaymentRequestBody(
                transactionID = success.transactionDetails.transactionID
            ),
            success = { response ->
                sendMessage(
                    message = when (cardProcess) {
                        is CardProcess.Payment -> CompletionMessage.SuccessfulPayment
                        is CardProcess.Tokenization -> CompletionMessage.SuccessfulCardTokenization
                    }.apply { description = response.toString() }
                )
            },
            failure = { fetchError ->
                sendMessage(
                    message = CompletionMessage.CommitRequestFailure
                        .description(fetchError.toString())
                )
            }
        )
    }

    override fun onCancellation(cancellation: Cancellation) {
        sendMessage(message = CompletionMessage.Cancellation)
    }

    override fun <E> onRegistrationFailure(failure: RegistrationFailure<E>) {
        sendMessage(
            message = CompletionMessage.RegistrationRequestFailure
                .description(failure.registrationError.toString() )
        )
    }

    override fun onCardProcessFailure(cardProcessFailure: CardProcessFailure) {
        sendMessage(
            message = CompletionMessage.CardProcessFailure
                .description(cardProcessFailure.error.toString())
        )
    }
    //endregion

    //region - Ui State Updates

    private fun sendMessage(message: CompletionMessage){
        this.completionMessage.invoke(message)
        uiState.update { state ->
            state.copy(
                mode = ShopTabUiMode.SelectingPaymentMethod(available = state.paymentMethods)
            )
        }
    }

    override fun fetchPaymentMethods() {
        uiState.update { state -> state.copy(mode = ShopTabUiMode.FetchingPaymentMethods) }
        merchantAPI.fetchPaymentMethods(
            success = {
                uiState.update { state -> state.copy(paymentMethods = it) }
            },
            failure = { fetchError ->
                sendMessage(
                    message = CompletionMessage.PaymentMethodFetchFailure
                        .apply { this.description = fetchError.toString() }
                )
            }
        )
    }

    override fun payWith(paymentMethodDetail: PaymentMethodDetail) {
        onSubmitCardDetails(paymentMethodDetail)
    }

    override fun updateItemQuantity(newQuantity: Int) {
        uiState.update { state -> state.copy(itemQuantity = newQuantity) }
    }

    override fun selectCurrency(newCurrency: CurrencyLocale) {
        uiState.update { state -> state.copy(currency = newCurrency) }
    }
    //endregion

}
//endregion


/** Shopping completion messages for successful, cancelled or failed scenarios. */
enum class CompletionMessage(val source: String, var description: String? = null) {
    SuccessfulPayment(source = "Successful Card Payment"),
    SuccessfulCardTokenization(source = "Successful Card Tokenization"),
    Cancellation(source = "(Pia) Cancelled"),
    PaymentMethodFetchFailure(source = "(Merchant) Payment Method Fetch"),
    RegistrationRequestFailure(source = "(Merchant) Registration Request"),
    CommitRequestFailure(source = "(Merchant) Commit Request"),
    CardProcessFailure(source = "(Pia) Card Process")
}

fun CompletionMessage.description(description: String) : CompletionMessage {
    return this.apply { this.description = description }
}

/// Returns a registration request body formed with current values of `ShopUiState` instance.
private fun ShopTabUiState.makeRegistrationRequestBody(
    customerID: String,
    orderNumber: String = "PiaSDK-Android",
    method: PaymentMethodDetail?
): RegistrationRequestBody {
    return RegistrationRequestBody(
        amount = Amount(currencyCode = currency.currencyCode, totalAmount = totalAmount),
        customerId = customerID,
        orderNumber = orderNumber,
        // Merchant BE expects `null` value for card payments (internal logic)
        method = if (method == PaymentMethodDetail.card) null else method,
        storeCard = tokenizeCardForLaterUse,
        customerEmail = "in-app-support@nets.eu"
    )
}


//region - ShopTabViewModelApi Factory

fun ComponentActivity.shopTabViewModelApi(
    uiState: ShopTabUiState = ShopTabUiState(),
    completionMessage: (CompletionMessage) -> Unit
) : ShopTabViewModelApi {

    class ShopTabViewModelFactory(
        private val merchantAPI: MerchantAPI,
        private val useProdMerchantEnvironment: Boolean
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ShopTabViewModel(
                merchantAPI,
                useProdMerchantEnvironment, uiState,
                completionMessage = completionMessage
            ) as T
        }
    }

    return viewModels<ShopTabViewModel> {
        ShopTabViewModelFactory(
            merchantAPI = MerchantAPI.create(
                baseURL = configurations.baseURL,
                customerID = configurations.customerID.toInt(),
                merchantID = configurations.merchantID
            ),
            useProdMerchantEnvironment = !configurations.isTestMode
        )
    }.value
}
//endregion
