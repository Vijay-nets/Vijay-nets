package com.example.piashop.integration

// NOTE: This file can be used as integration starting point for customers therefore
//       does not contain any dependency to the Application (Shop App).

import com.piasdk.api.*
import com.piasdk.card.netaxept.RegistrationResponse


//region Pia Process Completion

/** Pia SDK results common to all payment types, i.e. card-processes and wallet payments. */
interface PiaCompletionCallback {
    fun onSuccess(success: Success)
    fun onCancellation(cancellation: Cancellation)
    fun <Error> onRegistrationFailure(failure: RegistrationFailure<Error>)

    /** Result handling specific to card processes. */
    interface CardProcess : PiaCompletionCallback {
        fun onCardProcessFailure(cardProcessFailure: CardProcessFailure)

        /** Maps `CardProcessResult` to the corresponding completion callback. */
        fun onProcessResult(cardProcessResult: CardProcessResult) {
            when (cardProcessResult) {
                is Success -> { onSuccess(cardProcessResult) }
                is Cancellation -> { onCancellation(cardProcessResult) }
                is RegistrationFailure<*> -> { onRegistrationFailure(cardProcessResult) }
                is CardProcessFailure -> { onCardProcessFailure(cardProcessResult) }
            }
        }
    }

}

//endregion

/**
 * Integration of Pia Card Process. Implement typically inside a ViewModel.
 * Can be used as a template to integrate card-payment or card-tokenization processes by
 * injecting [PaymentMethod] that represents your merchant backend model for payment method.
 */
interface PiaCardProcessIntegration <PaymentMethod> {

    val cardProcess: CardProcess
    val cardProcessCompletion: PiaCompletionCallback.CardProcess

    /** Make registration request to merchant BE and callback [success] or [failure] accordingly. */
    fun registerPayment(
        paymentMethod: PaymentMethod,
        success: (RegistrationResponse.Success) -> Unit,
        failure: (RegistrationResponse.Failure<*>) -> Unit
    )
}

fun <PaymentMethod> PiaCardProcessIntegration<PaymentMethod>.onSubmitCardDetails(
    paymentMethodDetail: PaymentMethod
) {
    cardProcess.submitCardDetails(onRegisterAndCallback =  { callback ->
        registerPayment(
            paymentMethod = paymentMethodDetail,
            success = { successResponse -> callback.invoke(successResponse) },
            failure = { failureResponse -> callback.invoke(failureResponse) }
        )
    })
}


//region Completion - Wallet Process

// TODO: Coming soon!

//endregion

