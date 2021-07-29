package com.example.hackingwork

import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.hackingwork.databinding.ActivityMainBinding
import com.example.hackingwork.utils.*
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

const val TAG = "ANUJ"

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.i(TAG, "onCreate: Created Auth Activity")
        getPermission()
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.authFragmentView) as NavHostFragment
        navController = navHostFragment.findNavController()
        setupActionBarWithNavController(navController)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPermission() {
        if (!checkPhoneNumberPermission(this)) {
            request(Manifest.permission.READ_PHONE_NUMBERS, REQUEST_PHONE_NUMBER, "Phone Number")
        }
        if (!checkPhoneSmsPermission(this)) {
            request(Manifest.permission.READ_SMS, REQUEST_SMS, "Read SMS")
        }
        if (!checkPhoneStatePermission(this)) {
            request(Manifest.permission.READ_PHONE_STATE, REQUEST_PHONE_STATE, "Phone State")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        perms.forEach {
            if (EasyPermissions.permissionPermanentlyDenied(this, it)) {
                SettingsDialog.Builder(this).build().show()
            } else {
                getPermission()
            }
        }
    }

    private fun request(permissionType: String, code: Int, type: String) =
        EasyPermissions.requestPermissions(
            this,
            "Kindly Give us $type permission,otherwise application may not work Properly.",
            code,
            permissionType
        )

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Log.i(TAG, "onPermissionsGranted: Permission Granted")
    }
}