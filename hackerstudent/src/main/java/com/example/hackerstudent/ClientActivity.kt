package com.example.hackerstudent

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hackerstudent.databinding.ClientActitvityMainBinding
import com.example.hackerstudent.utils.*
import com.example.hackerstudent.viewmodels.PrimaryViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClientActivity : AppCompatActivity() {
    private lateinit var binding: ClientActitvityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private val primaryViewModel: PrimaryViewModel by viewModels()
    private var alertDialog: ExtraDialog? = null

    @Inject
    lateinit var customProgress: CustomProgress
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ClientActitvityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.ClientContainerView) as NavHostFragment
        navController = navHostFragment.navController
        getUserInfo()
        appBarConfiguration = AppBarConfiguration(navController.graph, binding.root)
        binding.myDrawer.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun getUserInfo() {
        primaryViewModel.userInfo.observe(this) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    val action = ClientActivityDirections.actionGlobalPasswordDialog(
                        title = "Error",
                        message = it.exception?.localizedMessage ?: "No Error"
                    )
                    navController.navigate(action)
                }
                is MySealed.Loading -> {
                    showLoading(it.data as String)
                }
                is MySealed.Success -> {
                    hideLoading()
                    it.data?.let { data ->
                        val user = data as CreateUserAccount?
                        Log.i(TAG, "getUserInfo Admin Activity: $user")
                        val mac= getLocalIpAddress()
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
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        Log.i(TAG, "onBackPressed: Admin-Side ${MainActivity.emailAuthLink}")
        if (MainActivity.emailAuthLink == null)
            super.onBackPressed()
    }
}