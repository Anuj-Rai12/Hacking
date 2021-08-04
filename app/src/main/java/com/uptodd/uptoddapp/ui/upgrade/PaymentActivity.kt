package com.uptodd.uptoddapp.ui.upgrade

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.paypal.checkout.PayPalCheckout
import com.paypal.checkout.approve.OnApprove
import com.paypal.checkout.cancel.OnCancel
import com.paypal.checkout.config.CheckoutConfig
import com.paypal.checkout.config.Environment
import com.paypal.checkout.config.SettingsConfig
import com.paypal.checkout.createorder.*
import com.paypal.checkout.error.OnError
import com.paypal.checkout.order.*
import com.razorpay.Checkout
import com.razorpay.CheckoutActivity
import com.razorpay.PaymentResultListener
import com.uptodd.uptoddapp.BuildConfig
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import org.json.JSONObject


class PaymentActivity : AppCompatActivity(), PaymentResultListener
{
    companion object {
        private var checkout = Checkout()
        var razorpayKey ="rzp_live_D7gCizgg0p9pKe"
            //"rzp_live_D7gCizgg0p9pKe"
            //"rzp_test_TiIY5TBUldHEak"
        private const val client_id ="AQuc-0JVLyhbDaFZl2-0z09kTqOciiyQURnbwbENRCJXrlH9lGTPTSjuF6TlArXdUR_fP0QMuSv5PZo-"
                //"AXwyY9-wRAW8u1xeQefUdgQcRFlvkxCKNuuMffA-jq1paLqwfeMTHqVhgiWPzrtWUOyDtpr77GxHpXJk"
                //"AQuc-0JVLyhbDaFZl2-0z09kTqOciiyQURnbwbENRCJXrlH9lGTPTSjuF6TlArXdUR_fP0QMuSv5PZo-"
        const val AMOUNT_KEY = "amount"
        const val PAYMENT_TYPE = "payment_type"
        var viewModel: UpgradeViewModel? = null
        const val PAYMENT_PRODUCT_MONTH = "product_month"
        const val PAYMENT_FAILED = "Payment Failed"
        const val PAYMENT_SUCCESS = "Payment Successfully"
    }

    private var productMonth = 0



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



        }
        viewModel = ViewModelProvider(this)[UpgradeViewModel::class.java]
    }


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
            options.put("amount", paisa)

            val preFill = JSONObject()
            pref.getEmail().let {   preFill.put("email",it)}
            pref.getPhone().let {  preFill.put("contact",it) }
            options.put("prefill", preFill)
            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

    }

    override fun onPaymentError(p0: Int, p1: String?) {


        UpgradeViewModel.paymentStatus = PAYMENT_FAILED
        finishAffinity()
    }

    override fun onPaymentSuccess(paymentId: String?) {


        UpgradeViewModel.paymentStatus = PAYMENT_SUCCESS
        paymentId?.let { Log.d("succces paymentID:id", it) }
     //
        finishAffinity()
    }

    override fun onDestroy() {
        Checkout.clearUserData(this)
        super.onDestroy()
    }




}
