package com.example.piashop.merchantapi


import networking.FetchError
import networking.RetrofitFetching
import networking.RetrofitResultProcessing
import networking.fetch
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.*


class RetrofitMerchantAPI(
    override val baseURL: String,
    override val customerID: String,
    override val merchantID: String,
    override val executor: RetrofitFetching.Executor = RetrofitFetching.callEnqueue,
    callTimeoutInSeconds: Long = 10,
) : MerchantAPI, RetrofitFetching {

    interface Endpoint {

        @GET(MerchantAPI.Path.paymentMethods)
        fun fetchPaymentMethods(
            @HeaderMap headers: Map<String, String> = MerchantAPI.Header.all,
            @Query(MerchantAPI.Query.customerID) customerID: String
        ): Call<AvailablePaymentMethods>

        @POST(MerchantAPI.Path.registerPayment)
        fun registerPayment(
            @HeaderMap headers: Map<String, String> = MerchantAPI.Header.all,
            @Path(MerchantAPI.Query.merchantID) merchantId: String,
            @Body requestBody: RegistrationRequestBody
        ): Call<RegistrationResponse>

        @PUT(MerchantAPI.Path.processPayment)
        fun processPayment(
            @HeaderMap headers: Map<String, String> = MerchantAPI.Header.all,
            @Path(MerchantAPI.Query.merchantID) merchantId: String,
            @Path(MerchantAPI.Query.transactionID) transactionId: String,
            @Body operation: Operation
        ): Call<CommitAndVerifyResponseBody>

        @DELETE(MerchantAPI.Path.rollbackPayment)
        fun rollbackPayment(
            @HeaderMap headers: Map<String, String> = MerchantAPI.Header.all,
            @Path(MerchantAPI.Query.merchantID) merchantId: String,
            @Path(MerchantAPI.Query.transactionID) transactionId: String,
        ): Call<String>

    }

    private val endpoint: Endpoint = Retrofit.Builder().customBuild(
        baseURL = baseURL,
        callTimeoutInSeconds = callTimeoutInSeconds
    ).create(Endpoint::class.java)

    private inline fun <reified RegistrationResponse> registrationResultProcessing() :
            RetrofitResultProcessing<RegistrationResponse, RegistrationRequestError> {
        return RetrofitResultProcessing(::discernRegistrationError)
    }

    //region MerchantAPI Implementation

    override fun fetchPaymentMethods(
        success: (AvailablePaymentMethods) -> Unit,
        failure: (FetchError) -> Unit
    ) {
        val endpoint = endpoint.fetchPaymentMethods(customerID = customerID)
        fetch(endpoint = endpoint, success = success, failure = failure)
    }

    override fun registerPayment(
        registrationRequestBody: RegistrationRequestBody,
        success: (RegistrationResponse) -> Unit,
        failure: (RegistrationRequestError) -> Unit
    ) {
        fetch(
            endpoint = endpoint.registerPayment(
                merchantId = merchantID,
                requestBody = registrationRequestBody
            ),
            resultProcessing = registrationResultProcessing(),
            success = success, failure = failure
        )
    }

    override fun commitPayment(
        processPaymentRequestBody: ProcessPaymentRequestBody,
        success: (CommitAndVerifyResponseBody) -> Unit,
        failure: (FetchError) -> Unit
    ) {
        val endpoint = endpoint.processPayment(
            merchantId = merchantID,
            transactionId = processPaymentRequestBody.transactionID,
            operation = Operation.Commit()
        )
        fetch(endpoint = endpoint, success = success, failure = failure)
    }

    override fun verifyPayment(
        processPaymentRequestBody: ProcessPaymentRequestBody,
        success: (CommitAndVerifyResponseBody) -> Unit,
        failure: (FetchError) -> Unit
    ) {
        val endpoint = endpoint.processPayment(
            merchantId = merchantID,
            transactionId = processPaymentRequestBody.transactionID,
            operation = Operation.Verify()
        )
        fetch(endpoint = endpoint, success = success, failure = failure)
    }

    override fun rollbackPayment(
        rollbackPaymentRequestBody: RollbackPaymentRequestBody,
        success: (String) -> Unit,
        failure: (FetchError) -> Unit
    ) {
        val endpoint = endpoint.rollbackPayment(
            merchantId = merchantID,
            transactionId = rollbackPaymentRequestBody.transactionID,
        )
        fetch(endpoint = endpoint, success = success, failure = failure)
    }

    //endregion
}
