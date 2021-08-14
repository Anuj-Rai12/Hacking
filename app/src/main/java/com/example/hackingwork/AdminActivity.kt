package com.example.hackingwork

import android.Manifest
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
import com.example.hackingwork.databinding.AdminActitvityMainBinding
import com.example.hackingwork.utils.*
import com.example.hackingwork.viewmodels.PrimaryViewModel
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val REQUEST_GAL = 102

@AndroidEntryPoint
class AdminActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var binding: AdminActitvityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private val primaryViewModel: PrimaryViewModel by viewModels()
    private var alertDialog: ExtraDialog? = null

    @Inject
    lateinit var customProgress: CustomProgress

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminActitvityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getPermission()
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.adminContainerView) as NavHostFragment
        navController = navHostFragment.navController
        getUserInfo()
        appBarConfiguration = AppBarConfiguration(navController.graph, binding.root)
        binding.myDrawer.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun getPermission() {
        if (!checkGalleryPermission(this))
            request()
    }

    private fun request(
        camera: String = Manifest.permission.READ_EXTERNAL_STORAGE,
        code: Int=REQUEST_GAL,
        s: String="Gallery"
    ) = EasyPermissions.requestPermissions(
        this,
        "Kindly Give us $s permission,otherwise application may not work Properly.",
        code,
        camera
    )

    private fun getUserInfo() {
        primaryViewModel.userInfo.observe(this) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    val action = AdminActivityDirections.actionGlobalPasswordDialog(
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
                        val ip = getLocalIpAddress()
                        user?.let { createUserAccount ->
                            if (createUserAccount.ipaddress != ip)
                                openDialog()
                            else
                                Log.i(TAG, "getUserInfo: Login Accepted")
                        }
                    }
                }
            }
        }
    }

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

    private fun hideLoading() = customProgress.hideLoading()
    private fun showLoading(msg: String) = customProgress.showLoading(this, msg)

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        Log.i(TAG, "onBackPressed: Admin-Side ${MainActivity.emailAuthLink}")
        if (MainActivity.emailAuthLink == null)
            super.onBackPressed()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        perms.forEach {
            if (EasyPermissions.permissionPermanentlyDenied(this, it)) {
                SettingsDialog.Builder(this).build().show()
            } else
                getPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Log.i(TAG, "onPermissionsGranted: Permission Is Granted")
    }
}