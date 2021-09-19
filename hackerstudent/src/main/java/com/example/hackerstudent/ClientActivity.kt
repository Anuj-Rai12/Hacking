package com.example.hackerstudent

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.hackerstudent.databinding.ClientActitvityMainBinding
import com.example.hackerstudent.utils.*
import com.example.hackerstudent.viewmodels.PrimaryViewModel
import com.google.android.material.snackbar.Snackbar
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import dagger.hilt.android.AndroidEntryPoint
import me.ibrahimsn.lib.SmoothBottomBar
import javax.inject.Inject

@AndroidEntryPoint
class ClientActivity : AppCompatActivity(), PaymentResultWithDataListener {
    private lateinit var binding: ClientActitvityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private val primaryViewModel: PrimaryViewModel by viewModels()
    private var alertDialog: ExtraDialog? = null

    @Inject
    lateinit var successOrFailedPayment: SuccessOrFailedPayment

    companion object {
        var coursePrice: String? = null
        var bottomNavBar: SmoothBottomBar? = null
        var courseName: String? = null
    }

    @Inject
    lateinit var customProgress: CustomProgress

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.changeStatusBarColor()
        binding = ClientActitvityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.ClientContainerView) as NavHostFragment
        navController = navHostFragment.navController
        getUserInfo()
        bottomNavBar = binding.bottomBar
        appBarConfiguration =
            AppBarConfiguration(
                setOf(
                    R.id.homeScreenFragment,
                    R.id.exploreFragment,
                    R.id.profileFragment
                )
            )
        setupActionBarWithNavController(navController, appBarConfiguration)
        setupSmoothBottomMenu()
    }

    private fun setupSmoothBottomMenu() {
        val popupMenu = PopupMenu(this, null)
        popupMenu.inflate(R.menu.nav_bottom_menu)
        val menu = popupMenu.menu
        binding.bottomBar.setupWithNavController(menu, navController)
    }

    private fun getUserInfo() {
        primaryViewModel.userInfo.observe(this) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    dir(message = it.exception?.localizedMessage ?: GetConstStringObj.UN_WANTED)
                }
                is MySealed.Loading -> {
                    showLoading(it.data as String)
                }
                is MySealed.Success -> {
                    hideLoading()
                    it.data?.let { data ->
                        val user = data as CreateUserAccount?
                        Log.i(TAG, "getUserInfo Admin Activity: $user")
                        val mac = getLocalIpAddress()
                        Log.i(TAG, "getUserInfo: MAC-ADDRESS -> $mac")
                        user?.let { createUserAccount ->
                            if (createUserAccount.ipaddress != mac)
                                openDialog()
                            else
                                Log.i(TAG, "getUserInfo: Login Accepted")
                        }
                    }
                }
            }
        }
    }

    private fun dir(title: String = "Error", message: String = "") {
        val action = ClientActivityDirections.actionGlobalPasswordDialog(
            title = title,
            message = message
        )
        navController.navigate(action)
    }

    private fun hideLoading() = customProgress.hideLoading()
    private fun showLoading(msg: String) = customProgress.showLoading(this, msg)

    private fun openDialog() {
        alertDialog = ExtraDialog(
            title = "Invalid Attempt",
            Msg = "Please Try to Login In Same Device Where you Have Created this Account\nThank You",
            flag = true
        )
        alertDialog?.isCancelable = false
        alertDialog?.show(supportFragmentManager, GetConstStringObj.VERSION)
    }

    override fun onPause() {
        super.onPause()
        hideLoading()
        alertDialog?.dismiss()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        Log.i(TAG, "onBackPressed: Admin-Side ${MainActivity.emailAuthLink}")
        if (MainActivity.emailAuthLink == null)
            super.onBackPressed()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
        Log.i(TAG, "onPaymentSuccess: p0-> $p0")
        Log.i(TAG, "onPaymentSuccess: p1-> $p1")
        Log.i(TAG, "onPaymentSuccess: Course Name -> $courseName")
        Log.i(TAG, "onPaymentSuccess: Course Price -> $coursePrice")
        var p2Txt = ""
        p0?.let { p2Txt = "Payment Id ->  $it \n\n" }
        p1?.let { p2Txt = "$p2Txt More Info\n$p1\n\n" }
        val coursePurchase = CoursePurchase(
            name = courseName,
            date = getDateTime(),
            purchase = coursePrice,
            status = "Success",
            purchaseid = p0
        )
        addPaidCourse(coursePurchase, p2Txt)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun addPaidCourse(coursePurchase: CoursePurchase, info: String) {
        primaryViewModel.addPaidCourseToUser(coursePurchase).observe(this) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    dir(message = it.exception?.localizedMessage ?: GetConstStringObj.UN_WANTED)
                }
                is MySealed.Loading -> showLoading(it.data as String)
                is MySealed.Success -> {
                    hideLoading()
                    successOrFailedPayment.showPaymentDialog(
                        text = info,
                        file = R.raw.payment_successful,
                        this
                    )
                    this.msg("Successful Paid ", length = Snackbar.LENGTH_SHORT)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        Log.i(TAG, "onPaymentError: P0 -> $p0")
        Log.i(TAG, "onPaymentError: P1 -> $p1")
        var p2Txt = ""
        if (p0 != 0)
            p2Txt = "Error Id ->$p0\n"
        p1?.let { p2Txt = "$p2Txt$p1\n\n" }
        p2?.let { p2Txt = "$p2Txt$it\n\n" }
        successOrFailedPayment.showPaymentDialog(text = p2Txt, context = this)
        Log.i(TAG, "onPaymentError: P2 -> $p2")
        this.msg("Payment Failed", length = Snackbar.LENGTH_SHORT)
    }
}