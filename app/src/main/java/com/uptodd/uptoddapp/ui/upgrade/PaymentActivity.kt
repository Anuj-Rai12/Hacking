package com.uptodd.uptoddapp.ui.upgrade

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.paypal.android.sdk.payments.*
import com.paypal.android.sdk.payments.PaymentActivity
import com.paypal.checkout.PayPalCheckout
import com.paypal.checkout.approve.OnApprove
import com.paypal.checkout.cancel.OnCancel
import com.paypal.checkout.config.CheckoutConfig
import com.paypal.checkout.config.Environment
import com.paypal.checkout.config.SettingsConfig
import com.paypal.checkout.createorder.*
import com.paypal.checkout.error.OnError
import com.paypal.checkout.order.*
import com.paypal.checkout.paymentbutton.PayPalButton
import com.razorpay.Checkout
import com.razorpay.CheckoutActivity
import com.razorpay.PaymentResultListener
import com.uptodd.uptoddapp.BuildConfig
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import kotlinx.android.synthetic.main.fragment_order_details.*
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal


class PaymentActivity : AppCompatActivity(), PaymentResultListener
{
    companion object {
        private var checkout = Checkout()
        private var razorpayKey ="rzp_live_D7gCizgg0p9pKe"
            //"rzp_test_TiIY5TBUldHEak"
        private const val client_id ="AQuc-0JVLyhbDaFZl2-0z09kTqOciiyQURnbwbENRCJXrlH9lGTPTSjuF6TlArXdUR_fP0QMuSv5PZo-"
                //"AXwyY9-wRAW8u1xeQefUdgQcRFlvkxCKNuuMffA-jq1paLqwfeMTHqVhgiWPzrtWUOyDtpr77GxHpXJk"
                //"AQuc-0JVLyhbDaFZl2-0z09kTqOciiyQURnbwbENRCJXrlH9lGTPTSjuF6TlArXdUR_fP0QMuSv5PZo-"
        const val AMOUNT_KEY = "amount"
        const val PAYMENT_TYPE = "payment_type"
        const val PAYPAL_REQUEST_CODE = 123;
        var viewModel: UpgradeViewModel? = null
        const val PAYMENT_PRODUCT_MONTH = "product_month"
        const val PAYMENT_FAILED = "Payment Failed"
        const val PAYMENT_SUCCESS = "Payment Successfully"
    }

    private var productMonth = 0

    private val config =
        PayPalConfiguration() // Start with mock environment.  When ready,
            // switch to sandbox (ENVIRONMENT_SANDBOX)
            // or live (ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_PRODUCTION) // on below line we are passing a client id.
            .clientId(client_id)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val amount = intent.getDoubleExtra(AMOUNT_KEY, 0.0)
        val payment_type = intent.getStringExtra(PAYMENT_TYPE)

        productMonth = intent.getIntExtra(PAYMENT_PRODUCT_MONTH, 0)
        val config = CheckoutConfig(
            application = application,
            clientId = client_id,
            environment = Environment.LIVE,
            returnUrl = "${BuildConfig.APPLICATION_ID}://paypalpay",
            currencyCode = CurrencyCode.USD,
            userAction = UserAction.PAY_NOW,
            settingsConfig = SettingsConfig(
                loggingEnabled = true,shouldFailEligibility = true
            )
        )

        Log.d("return url","${BuildConfig.APPLICATION_ID}://paypalpay")
        PayPalCheckout.setConfig(config)

        if (payment_type == "india") {
            checkout.setKeyID(razorpayKey);
            Checkout.preload(applicationContext);
            startRazorPayPayment(amount)
        } else {

            PayPalCheckout.start(
                createOrder = CreateOrder { createOrderActions ->
                    val order = Order(
                        intent = OrderIntent.CAPTURE,
                        appContext = AppContext(
                            userAction = UserAction.PAY_NOW
                        ),
                        purchaseUnitList = listOf(
                            PurchaseUnit(
                                amount = Amount(
                                    currencyCode = CurrencyCode.USD,
                                    value = "$amount"
                                )
                            )
                        )
                    )

                    createOrderActions.create(order)
                }
                ,
                onApprove = OnApprove { approval ->


                    approval.orderActions.capture { captureOrderResult ->

                        if( captureOrderResult is CaptureOrderResult.Success)
                        {
                            captureOrderResult.orderResponse?.id?.let {
                                viewModel?.savePaymentDetails(this,
                                    it, productMonth)
                                captureOrderResult.orderResponse?.id?.let { Log.d("orderID", it) }
                            }
                             UpgradeViewModel.paymentStatus = PAYMENT_SUCCESS
                            finish()
                        }
                        else
                        {
                            UpgradeViewModel.paymentStatus = PAYMENT_FAILED
                            finish()
                        }
                        Log.d("CaptureOrder", "CaptureOrderResult: $captureOrderResult")
                    }
                }

                ,
                onError = OnError{
                    errorInfo ->
                    UpgradeViewModel.paymentStatus = PAYMENT_FAILED
                    finish()
                        Log.e("pay error", errorInfo.reason)
                },
                onCancel = OnCancel{
                    UpgradeViewModel.paymentStatus = PAYMENT_FAILED
                    finish()
                }
            )

            /*
            val intentP = Intent(this, PayPalService::class.java)
            intentP.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config)
            startService(intentP)
            getPayment(amount)
             */

        }
        viewModel = ViewModelProvider(this)[UpgradeViewModel::class.java]
    }


    /*
    private fun getPayment(amount: Double) {

        // Getting the amount from editText
        // Creating a paypal payment on below line.
        val payment = PayPalPayment(
            BigDecimal(amount), "USD", "Charge for $productMonth month plan",
            PayPalPayment.PAYMENT_INTENT_SALE
        )

        // Creating Paypal Payment activity intent
        val intent =
            Intent(this, PaymentActivity::class.java)

        //putting the paypal configuration to the intent
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config)

        // Puting paypal payment to the intent
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment)

        // Starting the intent activity for result
        // the request code will be used on the method onActivityResult
        startActivityForResult(intent, PAYPAL_REQUEST_CODE)
    }


     */
    private fun startRazorPayPayment(amounts: Double) {

        var paisa =
            amounts * 100 //Generate your razorpay key from Settings-> API Keys-> copy Key Id
        checkout.setKeyID(razorpayKey)

        val pref = UptoddSharedPreferences.getInstance(this)

        try {
            val options = JSONObject()
            options.put("name", pref.getName())
            options.put("description", "charge for $productMonth month plan")
            options.put("currency", "INR")
            options.put("amount", paisa) //make value dynamic
//            amount=1 //This  amount is to  credit //i.e value 100 paisa 1 rupees
            val preFill = JSONObject()
            preFill.put("email", pref.getEmail())
            preFill.put("contact", pref.getPhone())
            options.put("prefill", preFill)
            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

    }

    override fun onPaymentError(p0: Int, p1: String?) {

        UpgradeViewModel.paymentStatus = PAYMENT_FAILED
        finish()
    }

    override fun onPaymentSuccess(paymentId: String?) {
        UpgradeViewModel.paymentStatus = PAYMENT_SUCCESS
        paymentId?.let { Log.d("succces paymentID:id", it) }
        paymentId?.let { viewModel?.savePaymentDetails(this, it, productMonth) }
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === PAYPAL_REQUEST_CODE) {

            /*
            // If the result is OK i.e. user has not canceled the payment
            if (resultCode === Activity.RESULT_OK) {

                // Getting the payment confirmation
                val confirm: PaymentConfirmation =
                    data?.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION)!!

                // if confirmation is not null
                if (confirm != null) {
                    try {
                        // Getting the payment details
                        val paymentDetails = confirm.toJSONObject().toString(4)
                        // on below line we are extracting json response and displaying it in a text view.
                        val payObj = JSONObject(paymentDetails)
                        val payID = payObj.getJSONObject("response").getString("id")
                        val state =
                            payObj.getJSONObject("response").getString("state")
                        Log.d("state", state)
                        if (state == "approved") {
                            viewModel?.savePaymentDetails(this, payID, productMonth)
                            UpgradeViewModel.paymentStatus = PAYMENT_SUCCESS
                        } else
                            UpgradeViewModel.paymentStatus = PAYMENT_FAILED
                        finish()
                    } catch (e: JSONException) {
                        // handling json exception on below line
                        Log.e("Error", "an extremely unlikely failure occurred: ", e)
                    }
                }
            } else if (resultCode === Activity.RESULT_CANCELED) {
                // on below line we are checking the payment status.
                Log.i("paymentExample", "The user canceled.")
                UpgradeViewModel.paymentStatus = PAYMENT_FAILED
                finish()
            } else if (resultCode === PaymentActivity.RESULT_EXTRAS_INVALID) {
                // on below line when the invalid paypal config is submitted.
                Log.i(
                    "paymentExample",
                    "An invalid Payment or PayPalConfiguration was submitted. Please see the docs."
                )
                finish()
            }

             */
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
