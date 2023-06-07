package com.example.piashop.merchantapi

import networking.AnyFetchError
import networking.FetchError


interface MerchantAPI {
    val baseURL: String
    val customerID: String
    val merchantID: String

    fun fetchPaymentMethods(
        success: (AvailablePaymentMethods) -> Unit,
        failure: (FetchError) -> Unit
    )

    fun registerPayment(
        registrationRequestBody: RegistrationRequestBody,
        success: (RegistrationResponse) -> Unit,
        failure: (RegistrationRequestError) -> Unit
    )

    fun commitPayment(
        processPaymentRequestBody: ProcessPaymentRequestBody,
        success: (CommitAndVerifyResponseBody) -> Unit,
        failure: (FetchError) -> Unit
    )

    fun verifyPayment(
        processPaymentRequestBody: ProcessPaymentRequestBody,
        success: (CommitAndVerifyResponseBody) -> Unit,
        failure: (FetchError) -> Unit
    )

    fun rollbackPayment(
        rollbackPaymentRequestBody: RollbackPaymentRequestBody,
        success: (String) -> Unit,
        failure: (FetchError) -> Unit
    )

    companion object {
        fun create(baseURL: String, customerID: Int, merchantID: String) : MerchantAPI {
            return RetrofitMerchantAPI(
                baseURL = baseURL,
                customerID = String.format("%06d", customerID),
                merchantID = merchantID,
                callTimeoutInSeconds = 30
            )
        }
    }

    fun discernRegistrationError(anyFetchError: AnyFetchError) : RegistrationRequestError {
        return when(anyFetchError) {
            is AnyFetchError.BadStatusCode -> when(anyFetchError.statusCode) {
                400 -> RegistrationRequestError.InvalidParameters(anyFetchError.description)
                500 -> RegistrationRequestError.ServerFail(anyFetchError.description)
                503 -> RegistrationRequestError.DownstreamPSPError(anyFetchError.description)
                else -> RegistrationRequestError.Network(underlyingError = anyFetchError)
            }
            else -> RegistrationRequestError.Network(underlyingError = anyFetchError)
        }
    }

    object Path {
        const val paymentMethods: String = "v2/payment/methods"
        const val registerPayment: String = "v2/payment/{merchantId}/register"
        const val processPayment: String = "v2/payment/{merchantId}/{transactionId}"
        const val rollbackPayment: String = "v2/payment/{merchantId}/{transactionId}"
    }

    object Query {
        const val customerID: String = "consumerId"
        const val merchantID: String = "merchantId"
        const val transactionID: String = "transactionId"
        const val operation: String = "operation"
    }

    object Header {
        private val accept = Pair("Accept", "application/json;charset=utf-8;version=2.0")
        private val contentType = Pair("Content-Type", accept.second)
        val all: Map<String, String> = mapOf(accept, contentType)
    }

}

sealed interface RegistrationRequestError{
    data class Network(val underlyingError: AnyFetchError) : RegistrationRequestError
    // 400 Invalid parameters
    data class InvalidParameters(val description: String) : RegistrationRequestError

    // 500 Server Failure
    data class ServerFail(val description: String) : RegistrationRequestError

    // 503 Downstream PSP Error
    data class DownstreamPSPError(val description: String) : RegistrationRequestError
}


//region Decoding Types

data class AvailablePaymentMethods(
    val cardVerificationRequired: Boolean,
    val methods: List<PaymentMethodDetail>,
    val tokens: List<TokenizedCard>
)

data class PaymentMethodDetail(
    val displayName: String,
    val fee: Int,
    val id: String
) {
    companion object {
        /// Method ID of card payment method (defined in sample merchant BE)
        val card = PaymentMethodDetail(id = "", displayName = "", fee = 0)
        /// Method ID of tokenized-card payment method (defined in sample merchant BE)
        val easyPay = PaymentMethodDetail(id = "EasyPayment", displayName = "Easy Payment", fee = 0)
        /// Method ID of ApplePay payment method (defined in sample merchant BE)
        val applePay = PaymentMethodDetail(id = "ApplePay", displayName = "Apple Pay", fee = 0)
        /// Method ID of S-Business card payment method (defined in sample merchant BE)
        val sBusinessCard = PaymentMethodDetail(id = "SBusinessCard", displayName = "SBusinessCard", fee = 0)
    }
}

data class TokenizedCard(
    val expiryDate: String,
    val issuer: String,
    val tokenId: String,
    val cardVerificationRequired: Boolean = false
)

data class RegistrationResponse(
    val transactionId: String,
    val redirectOK: String,
    val redirectCancel: String,
    val walletUrl: String
)

data class CommitAndVerifyResponseBody(
    val transactionId : String?,
    val authorizationId : String?,
    val responseCode : String?,
    val responseText : String?,
    val responseSource : String?,
    val executionTimestamp : String?,
)

//endregion

//region Encoding Types

data class RegistrationRequestBody(
    val customerId: String,
    val orderNumber: String,
    val method: PaymentMethodDetail? = null,
    val amount: Amount,
    val storeCard: Boolean,
    val customerEmail: String
    // TODO - check for line items
)

data class Amount(
    val currencyCode: String,
    val totalAmount: Int,
    val vatAmount: Int = 0
)

data class ProcessPaymentRequestBody(
    val transactionID: String
)

data class RollbackPaymentRequestBody(
    val transactionID: String
)

sealed class Operation(open val operation: String) {
    data class Commit(override val operation: String = "COMMIT") : Operation(operation)
    data class Verify(override val operation: String = "VERIFY") : Operation(operation)
}

//endregion

