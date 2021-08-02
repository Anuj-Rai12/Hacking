package com.example.hackingwork.utils

import android.accounts.AccountManager
import android.content.Intent
import android.os.Build
import android.util.Log
import android.util.Patterns
import com.example.hackingwork.TAG
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialsOptions
import com.google.android.gms.auth.api.credentials.HintRequest
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

fun getLocalIpAddress(): String? {
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

fun getPhoneNumber(credential: Credential): String? {
    val codedPhoneNumber = credential.id
    return if (codedPhoneNumber.contains("+91")) {
        codedPhoneNumber.split("+91").last()
    } else
        null
}
object GetConstStringObj {
    const val My_Dialog_Once = "my_Dialog_Once"
    const val USERS = "USERS"
    const val EMAIL = "EMAIL"
    const val VERSION = "version"
    const val EMAIL_VERIFICATION_LINK = "https://hackerstudent.verify.com/VerifyEmail"
}