# Google Pay Integration Example

This repository contains an example implementation of integrating Google Pay into an Android application using the DNA Payments SDK. This example demonstrates how to:

- Initialize the payment context using `DNAPayment.init()`.
- Verify if Google Pay is available using `DNAPayment.isPaymentPossible()`.
- Execute a payment transaction using `GooglePayHelper.execute()`.

## Features

- **Environment Support**: Supports both TEST and PRODUCTION environments.
- **UI**: Provides a sample implementation with a button for initiating payments and a progress bar to indicate processing. Please make sure the Google Pay button to initiate transaction adheres to the Google Pay [brand guidelines](https://developers.google.com/pay/api/android/guides/brand-guidelines). Google Pay provides the [PayButton API](https://developers.google.com/pay/api/android/guides/resources/pay-button-api) that lets you customize the Google Pay payment button theme, shape and corner roundness to match your UI design.
- **Customizable Payment Request**: Allows configuration of payment details, customer information, and order details.
- **Error Handling**: Includes basic error handling for common exceptions.

## Prerequisites

- An Android device or emulator with Google Play Services.
- DNA Payments SDK `dnasdk.aar` file, please find under the `libs` folder in the demo project. Note: Android AAR (Android Archive) library can be used in a Xamarin C# project, but it requires some additional steps because Xamarin does not natively support AAR files out of the box. Instead, you need to create a Xamarin binding library to wrap the AAR file so it can be used within your Xamarin project.


## Setup

1. **Add Dependencies**

   Place the `dnasdk.aar` file under the `libs` folder in your project directory and included in your `build.gradle` file:

   ```gradle
   implementation (fileTree(dir: "libs", include: ["dnasdk.aar"]))
   ```

2. **Update AndroidManifest.xml**

   Add the necessary permissions to allow the app to make network requests and accessing Google Pay APIs:

   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   <uses-permission android:name="com.google.android.gms.permission.PAYMENT" />
   ```

3. **Setup API Keys**

   Replace the placeholder values in the `authTokenRequest` configuration with your client ID, client secret, and terminal information.

## Code Explanation

### 1. Initializing the Wallet

The `initWallet` method initializes the payment context with `DNAPayment.init()` and checks if Google Pay is available:

```kotlin
private fun initWallet() {
    paymentsClient = DNAPayment.init(requireContext(), Environment.TEST)
    googlePayHelper = GooglePayHelper(this, statusCallback = object : StatusCallback {
        override fun onResponse(paymentResult: PaymentResult) {
            handlePaymentResult(paymentResult)
        }
    })
    lifecycle.addObserver(googlePayHelper)

    DNAPayment.isPaymentPossible(paymentsClient).addOnCompleteListener { task ->
        try {
            val result = task.getResult(ApiException::class.java)
            if (result) {
                payBtn.visibility = View.VISIBLE
            } else {
                Toast.makeText(requireContext(), "$result", Toast.LENGTH_LONG).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(requireContext(), "isPaymentPossible exception catched", Toast.LENGTH_LONG).show()
        }
    }
}
```

### 2. Executing a Payment

The `onPayClick` method configures the payment request and calls `GooglePayHelper.execute()` to initiate the payment process:

```kotlin
private fun onPayClick() {
    progressBar.visibility = View.VISIBLE
    googlePayHelper.execute(
        paymentsClient = paymentsClient,
        paymentRequest = PaymentRequest(
            currency = "GBP",
            paymentMethod = "googlepay",
            description = "Car Service",
            paymentSettings = PaymentSettings(
                terminalId = "your-terminal-id",
                returnUrl = "https://example.com/return",
                failureReturnUrl = "https://example.com/failure",
                callbackUrl = "https://example.com/callback",
                failureCallbackUrl = "https://example.com/failure-callback"
            ),
            customerDetails = CustomerDetails(
                accountDetails = AccountDetails(accountId = "uuid000001"),
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
            clientSecret = "your-client-secret",
            invoiceId = "1683194969490",
            terminal = "your-terminal-id",
            amount = 24,
            currency = "GBP",
            paymentFormURL = "https://test-pay.dnapayments.com/checkout/"
        )
    )
}
```

### 3. Handling the Payment Result

The `handlePaymentResult` method processes the payment response:

```kotlin
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
```

## Support

For any issues or questions, please contact [support@dnapayments.com](mailto:support@dnapayments.com).

