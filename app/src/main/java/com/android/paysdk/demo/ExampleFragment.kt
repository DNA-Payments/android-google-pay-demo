package com.android.paysdk.demo

import android.google.paysdk.data.model.AuthTokenRequest
import android.google.paysdk.data.model.PaymentResult
import android.google.paysdk.data.model.StatusCallback
import android.google.paysdk.data.model.request.AccountDetails
import android.google.paysdk.data.model.request.AddressInfo
import android.google.paysdk.data.model.request.CustomerDetails
import android.google.paysdk.data.model.request.DeliveryDetails
import android.google.paysdk.data.model.request.OrderLine
import android.google.paysdk.data.model.request.PaymentRequest
import android.google.paysdk.data.model.request.PaymentSettings
import android.google.paysdk.domain.Environment
import android.google.paysdk.payment.DNAPayment
import android.google.paysdk.payment.GooglePayHelper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.PaymentsClient

/**
 * Main activity
 *
 * This main activity allows the user to fill in payment data (amount, order id, etc.).
 *
 * Before retrieving this payment data, the following steps must be completed:

 * Initialize the payment context with the `DNAPayment.init()` method.
 * Check if payment is possible with the `DNAPayment.isPaymentPossible()` method.

 * After retrieving this payment data, the following steps must be completed:

 * The `DNAPayment.execute()` method is executed.
 * The payment result is handled by the `handlePaymentResult()` method.

 * For readability purposes in this example, we do not use logs.
 *
 * @author DNA Network
 */

class ExampleFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initWallet()
        return inflater.inflate(R.layout.fragment_example, container, false)
    }

    private lateinit var payBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var paymentsClient: PaymentsClient

    private lateinit var googlePayHelper: GooglePayHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        payBtn = view.findViewById(R.id.payBtn)
        progressBar = view.findViewById(R.id.progressBar)

        payBtn.setOnClickListener {
            onPayClick()
        }
    }

    private fun initWallet() {
        // Environment TEST or PRODUCTION
        paymentsClient =
            DNAPayment.init(requireContext(), Environment.TEST)
        googlePayHelper =
            GooglePayHelper(this, statusCallback = object : StatusCallback {
                override fun onResponse(paymentResult: PaymentResult) {
                    handlePaymentResult(paymentResult)
                }
            })
        lifecycle.addObserver(googlePayHelper)

        DNAPayment.isPaymentPossible(paymentsClient).addOnCompleteListener { task ->
            try {
                val result = task.getResult(ApiException::class.java)
                if (result) {
                    // show Google Pay as a payment option
                    payBtn.visibility = View.VISIBLE
                } else {

                    Toast.makeText(requireContext(), "$result", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(
                    requireContext(),
                    "isPaymentPossible exception catched",
                    Toast.LENGTH_LONG
                )
                    .show()
            }

        }
    }

    private fun onPayClick() {
        progressBar.visibility = View.VISIBLE
        googlePayHelper.execute(
            paymentsClient = paymentsClient,
            paymentRequest = PaymentRequest(
                currency = "GBP",
                paymentMethod = "googlepay",
                description = "Car Service",
                paymentSettings = PaymentSettings(
                    terminalId = "8911a14f-61a3-4449-a1c1-7a314ee5774c",
                    returnUrl = "https://example.com/return",
                    failureReturnUrl = "https://example.com/failure",
                    callbackUrl = "https://example.com/callback",
                    failureCallbackUrl = "https://example.com/failure-callback"
                ),
                customerDetails = CustomerDetails(
                    accountDetails = AccountDetails(
                        accountId = "uuid000001"
                    ),
                    billingAddress = AddressInfo(
                        firstName = "John",
                        lastName = "Doe",
                        addressLine1 = "123 Main Street",
                        postalCode = "12345",
                        city = "Anytown",
                        country = "GB"
                    ),
                    deliveryDetails = DeliveryDetails(
                        deliveryAddress = AddressInfo(
                            firstName = "Jane",
                            lastName = "Doe",
                            addressLine1 = "456 Elm Street",
                            postalCode = "54321",
                            city = "Anytown",
                            country = "GB"
                        )
                    ),
                    email = "aaa@dnapayments.com",
                    mobilePhone = "+441234567890"
                ),
                orderLines = listOf(
                    OrderLine(
                        name = "Running shoe",
                        quantity = 1,
                        unitPrice = 24,
                        taxRate = 20,
                        totalAmount = 24,
                        totalTaxAmount = 4
                    )
                ),
                deliveryType = "service",
                invoiceId = "1683194969490",
                amount = 24.0
            ),
            authTokenRequest = AuthTokenRequest(
                grantType = "client_credentials",
                scope = "payment integration_hosted integration_embedded integration_seamless",
                clientId = "Test Merchant",
                clientSecret = "PoF84JqIG8Smv5VpES9bcU31kmfSqLk8Jdo7",
                invoiceId = "1683194969490",
                terminal = "8911a14f-61a3-4449-a1c1-7a314ee5774c",
                amount = 24,
                currency = "GBP",
                paymentFormURL = "https://test-pay.dnapayments.com/checkout/"
            )
        )
    }

    /**
     * Handle payment result
     *
     * @param result PaymentResult
     */
    private fun handlePaymentResult(result: PaymentResult) {
        progressBar.visibility = View.GONE

        if (result.success) {
            Toast.makeText(requireContext(), "Payment successful", Toast.LENGTH_LONG).show()
            return
        }

        Toast.makeText(
            requireContext(),
            "Payment failed. errorCode = " + result.errorCode?.name + " and description = " + result.errorDescription,
            Toast.LENGTH_LONG
        ).show()
    }
}