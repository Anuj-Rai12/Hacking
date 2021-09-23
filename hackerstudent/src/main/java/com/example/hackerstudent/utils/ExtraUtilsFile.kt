package com.example.hackerstudent.utils

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.hackerstudent.ClientActivity
import com.example.hackerstudent.MainActivity.Companion.wifiManager
import com.example.hackerstudent.R
import com.example.hackerstudent.TAG
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialsOptions
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.text.SimpleDateFormat
import java.util.*
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

@SuppressLint("SimpleDateFormat")
fun getDateTime(): String {
    val current = Date()
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
    return formatter.format(current)
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
fun Activity.msg(
    title: String,
    setAction: String? = null,
    response: (() -> Unit)? = null,
    length: Int = Snackbar.LENGTH_LONG
) {
    val snackBar = Snackbar.make(findViewById(android.R.id.content), title, length)
    setAction?.let {
        snackBar.setAction(it) {
            response?.invoke()
        }.setActionTextColor(resources.getColor(R.color.color_green, null))
    }
    snackBar.setTextColor(resources.getColor(R.color.my_color, null))
    snackBar.view.setBackgroundColor(resources.getColor(R.color.light_grey, null))
    snackBar.show()
}

fun View.hide() {
    this.isVisible = false
}

fun View.show() {
    this.isVisible = true
}

fun AppCompatActivity.hide() {
    this.supportActionBar!!.hide()
}

fun AppCompatActivity.show() {
    this.supportActionBar!!.show()
}


fun getDiscount(currPrice: Double, mrpPrice: Double) =
    (((mrpPrice - currPrice) / mrpPrice) * 100).toInt()

fun Context.msg(title: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, title, length).show()
}

@SuppressLint("MissingPermission")
fun isEmpty(editText: EditText, context: Context, hint: String?): Boolean {
    return if (editText.text.toString().trim { it <= ' ' }.equals("", ignoreCase = true)) {
        editText.requestFocus()
        editText.isCursorVisible = true
        showKeyboard((context as Activity))
        Toast.makeText(context, hint, Toast.LENGTH_SHORT).show()
        true
    } else {
        false
    }
}

private fun showKeyboard(activity: Activity) {
    val inputManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT)
}

fun hideBottomNavBar() {
    ClientActivity.bottomNavBar?.hide()
}

fun showBottomNavBar() {
    ClientActivity.bottomNavBar?.show()
}

fun Activity.preventScreenShotOrVideoRecoding() {
    window.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
    )
}

fun Activity.removedScreenShotFlagOrVideoRecoding() {
    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
}

@RequiresApi(Build.VERSION_CODES.M)
fun Activity.changeStatusBarColor(color: Int = R.color.white) {
    this.window?.statusBarColor = resources.getColor(color, null)
}


fun Context.getFileDir(fileName: String): File {
    return File.createTempFile(fileName, GetConstStringObj.FileType, this.cacheDir)
}


fun Activity.getNightMode(): Boolean {
    val mode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return mode == Configuration.UI_MODE_NIGHT_YES
}

object GetConstStringObj {
    const val My_Dialog_Once = "my_Dialog_Once"
    const val USERS = "USERS"
    const val EMAIL = "EMAIL"
    const val VERSION = "version"
    const val BASE_URL = "https://zenquotes.io/"
    const val Get_End_Point = "api/today"
    const val EMAIL_VERIFICATION_LINK = "https://hackerstudent.verify.com/VerifyEmail"
    const val Rs = "₹"
    const val Create_course = "Course"
    const val Create_Module = "Module"
    const val Per_page = 3
    const val FIRSTNAME = "firstname"
    const val timeToSearch = 1000
    const val LASTNAME = "lastname"
    const val UN_WANTED = "UnWanted Error Found"
    const val NO_INTERNET = "No Internet Connection Found"
    const val RETRY = "RETRY"
    const val RazorPay = "rzp_test_fKS5TlkOK3F1fw"
    const val EMAIL_ADDRESS = "email"
    const val change_profile_name = "Change UserName"
    const val change_email_address = "Change Email Address"
    const val change_profile_password = "Change Password"
    const val LAND_SCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    const val UnSpecified = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    const val Portrait = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    const val FileType = ".pdf"
    const val Payment_COLOR = "#fb7268"
    const val Currency = "INR"
    const val Payment_ERROR =
        "If bank amount Is been dedicated ,during this transaction then take a screen shoot of this message and contact with me.\nOr else Ignore this message"
}