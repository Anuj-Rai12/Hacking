package com.example.hackerstudent.utils

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.util.Log
import android.util.Patterns
import androidx.annotation.RequiresApi
import com.example.hackerstudent.MainActivity.Companion.wifiManager
import com.example.hackerstudent.R
import com.example.hackerstudent.TAG
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialsOptions
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.material.snackbar.Snackbar
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.regex.Pattern

fun hintRequest(): HintRequest = HintRequest.Builder()
    .setPhoneNumberIdentifierSupported(true)
    .build()

fun options(): CredentialsOptions = CredentialsOptions.Builder()
    .forceEnableSaveDialog()
    .build()

fun isValidPhone(phone: String): Boolean {
    val phonetic = "^[+]?[0-9]{10,13}\$"
    val pattern = Pattern.compile(phonetic)
    return pattern.matcher(phone).matches()
}

fun isValidEmail(target: CharSequence?): Boolean {
    return if (target == null) {
        false
    } else {
        Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }
}

fun checkFieldValue(string: String) = string.isEmpty() || string.isBlank()

fun isValidPassword(password: String): Boolean {
    val passwordREGEX = Pattern.compile(
        "^" +
                "(?=.*[0-9])" +         //at least 1 digit
                "(?=.*[a-z])" +         //at least 1 lower case letter
                "(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                "(?=.*[@#$%^&+=])" +    //at least 1 special character
                "(?=\\S+$)" +           //no white spaces
                ".{8,}" +               //at least 8 characters
                "$"
    )
    return passwordREGEX.matcher(password).matches()
}

@RequiresApi(Build.VERSION_CODES.M)
fun getIntent(): Intent = AccountManager.newChooseAccountIntent(
    null,
    null,
    arrayOf(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE),
    null,
    null,
    null,
    null
)

fun getLocalIpAddress(choose: Int = 0): String? {
    if (choose == 0)
        return getMacAddress()
    try {
        val en = NetworkInterface.getNetworkInterfaces()
        while (en.hasMoreElements()) {
            val into = en.nextElement()
            val enumIpAdder = into.inetAddresses
            while (enumIpAdder.hasMoreElements()) {
                val inetAddress = enumIpAdder.nextElement()
                if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                    return inetAddress.hostAddress
                }
            }
        }
    } catch (ex: SocketException) {
        Log.i(TAG, "getLocalIpAddress: ${ex.localizedMessage}")
        ex.printStackTrace()
    }
    return null
}

@SuppressLint("HardwareIds")
private fun getMacAddress(): String? {
    val wInfo = wifiManager?.connectionInfo
    return wInfo?.macAddress
}

fun getPathFile(file: String): List<String> {
    val tagArray = file.split("\\s*,\\s*".toRegex()).toTypedArray()
    return tagArray.toList()
}

fun getPhoneNumber(credential: Credential): String? {
    val codedPhoneNumber = credential.id
    return if (codedPhoneNumber.contains("+91")) {
        codedPhoneNumber.split("+91").last()
    } else
        null
}

@RequiresApi(Build.VERSION_CODES.M)
fun Activity.msg(title: String, setAction: String? = null, response: (() -> Unit)? = null) {
    val snackBar = Snackbar.make(findViewById(android.R.id.content), title, Snackbar.LENGTH_LONG)
    setAction?.let {
        snackBar.setAction(it) {
            response?.invoke()
        }.setActionTextColor(resources.getColor(R.color.color_green, null))
    }
    snackBar.setTextColor(resources.getColor(R.color.my_color, null))
    snackBar.view.setBackgroundColor(resources.getColor(R.color.light_grey, null))
    snackBar.show()
}

object GetConstStringObj {
    const val My_Dialog_Once = "my_Dialog_Once"
    const val USERS = "USERS"
    const val EMAIL = "EMAIL"
    const val VERSION = "version"
    const val BASE_URL = "https://zenquotes.io/"
    const val Get_End_Point = "api/today"
    const val EMAIL_VERIFICATION_LINK = "https://hackerstudent.verify.com/VerifyEmail"
}