package com.example.hackingwork.utils

import android.Manifest
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.android.gms.auth.GoogleAuthUtil
import com.vmadalin.easypermissions.EasyPermissions


const val REQUEST_PHONE_STATE = 101
const val REQUEST_SMS = 102
const val REQUEST_PHONE_NUMBER = 103

fun checkPhoneStatePermission(context: Context) =
    EasyPermissions.hasPermissions(context, Manifest.permission.READ_PHONE_STATE)

@RequiresApi(Build.VERSION_CODES.O)
fun checkPhoneNumberPermission(context: Context) =
    EasyPermissions.hasPermissions(context, Manifest.permission.READ_PHONE_NUMBERS)

fun checkPhoneSmsPermission(context: Context) =
    EasyPermissions.hasPermissions(context, Manifest.permission.READ_SMS)

fun getIntent(): Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    AccountManager.newChooseAccountIntent(
        null,
        null,
        arrayOf(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE),
        null,
        null,
        null,
        null
    )
} else {
    AccountManager.newChooseAccountIntent(
        null,
        null,
        arrayOf(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE),
        false,
        null,
        null,
        null,
        null
    )
}