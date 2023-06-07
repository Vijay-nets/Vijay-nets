package com.example.piashop.integration

import com.example.piashop.merchantapi.PaymentMethodDetail
import com.piasdk.api.CardProcess
import com.piasdk.api.CardProcessResult
import kotlinx.coroutines.flow.StateFlow


//region - Shop Tab
interface ShopTabViewModelApi : PiaCardPaymentProcess {

    /** Readonly Ui state properties that reflect latest state.*/
    val uiState: StateFlow<ShopTabUiState>

    /** Fetch payment methods from merchant backend. Result is reflected via `uiState` update. */
    fun fetchPaymentMethods()

    /** Pay with the given [paymentMethodDetail] using PiaSDK. */
    fun payWith(paymentMethodDetail: PaymentMethodDetail)

    /** Update item quantity to [newQuantity] value. Result is reflected via `uiState` update. */
    fun updateItemQuantity(newQuantity:Int)

    /** Select [newCurrency]. Result is reflected via `uiState` update. */
    fun selectCurrency(newCurrency: Currency)
}
//endregion


//region - Merchant Tab

interface MerchantTabViewModelApi: PiaCardTokenizationProcess {
    // val uiState // TODO: Create the Ui state data class
    fun tokenizeNewCard() // TODO: Use `PaymentMethodDetail.easyPay`
}
//endregion


//region - User Tab
// TODO:
//endregion


//region - SDK Ui Tab
// TODO:
//endregion


//region Pia Card Processes

interface PiaCardPaymentProcess : PiaCardProcess {
    val cardPayment: CardProcess.Payment
}

interface PiaCardTokenizationProcess : PiaCardProcess {
    val cardTokenization: CardProcess.Tokenization
}

interface PiaCardProcess {
    /** Toggle submit button's (e.g. pay button) active state based on the [enable] value. */
    fun enableButtonForCardProcess(enable: Boolean)

    /** Returns [cardProcessResult]. Commit or rollback the transaction accordingly. */
    fun onResult(cardProcessResult: CardProcessResult)
}
//endregion
