package com.example.hackingwork.auth

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hackingwork.R
import com.example.hackingwork.databinding.CreateUserAccountBinding
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.hackingwork.TAG
import com.example.hackingwork.utils.*
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

class CreateUserAccount : Fragment(R.layout.create_user_account),
    EasyPermissions.PermissionCallbacks {
    private lateinit var binding: CreateUserAccountBinding
    @SuppressLint("HardwareIds")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CreateUserAccountBinding.bind(view)
        try {
            if (check())
                Log.i(TAG, "onPermissionsGranted Form OnCreate: ${getTemped().line1Number}")
            else
                grantPermission()

        } catch (e: SecurityException) {
            grantPermission()
        }
        binding.nextBtn.setOnClickListener {
            //OTP Screen
            val action = CreateUserAccountDirections.actionGlobalPhoneNumberOtp()
            findNavController().navigate(action)
        }
        binding.backTo.setOnClickListener {
            //Back to Set On click
            val action =
                CreateUserAccountDirections.actionCreateUserAccountToLoginWithEmailPassword()
            findNavController().navigate(action)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun check()=checkPhoneSmsPermission(requireActivity()) && checkPhoneStatePermission(
                requireActivity()
            ) && checkPhoneNumberPermission(requireActivity())

    private fun getTemped() =
        activity?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        perms.forEach {
            if (EasyPermissions.permissionPermanentlyDenied(this, it)) {
                SettingsDialog.Builder(requireContext()).build().show()
            } else {
                grantPermission()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun grantPermission() {
        if (!checkPhoneNumberPermission(requireActivity())) {
            request(Manifest.permission.READ_PHONE_NUMBERS, REQUEST_PHONE_NUMBER, "Phone Number")
        }
        if (!checkPhoneSmsPermission(requireActivity())) {
            request(Manifest.permission.READ_SMS, REQUEST_SMS, "Read SMS")
        }
        if (!checkPhoneStatePermission(requireActivity())) {
            request(Manifest.permission.READ_PHONE_STATE, REQUEST_PHONE_STATE, "Phone State")
        }
    }

    private fun request(readPhoneNumbers: String, requestPhoneNumber: Int, type: String) {
        EasyPermissions.requestPermissions(
            this,
            "Kindly Give us $type permission,otherwise application may not work Properly.",
            requestPhoneNumber,
            readPhoneNumbers
        )
    }

    @SuppressLint("HardwareIds")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Log.i(TAG, "onPermissionsGranted: Permission Granted")
        try {
            if (check())
            Log.i(TAG, "onPermissionsGranted: ${getTemped().line1Number}")
            else
                grantPermission()

        } catch (e: SecurityException) {
            Log.i(TAG, "onPermissionsGranted: Security Error")
        }
    }
}