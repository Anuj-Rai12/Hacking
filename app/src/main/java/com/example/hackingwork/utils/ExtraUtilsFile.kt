package com.example.hackingwork.utils

import android.Manifest
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Patterns
import android.view.View
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.example.hackingwork.MainActivity.Companion.wifiManager
import com.example.hackingwork.R
import com.example.hackingwork.TAG
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialsOptions
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.material.snackbar.Snackbar
import com.vmadalin.easypermissions.EasyPermissions
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

fun getPathFile(file: String): List<String> {
    val tagArray = file.split("\\s*,\\s*".toRegex()).toTypedArray()
    return tagArray.toList()
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

fun checkGalleryPermission(context: Context) =
    EasyPermissions.hasPermissions(context, Manifest.permission.READ_EXTERNAL_STORAGE)

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

fun getPhoneNumber(credential: Credential): String? {
    val codedPhoneNumber = credential.id
    return if (codedPhoneNumber.contains("+91")) {
        codedPhoneNumber.split("+91").last()
    } else
        null
}

class GetUriFile : ActivityResultContract<InputData, OutPutData>() {
    override fun createIntent(context: Context, input: InputData?): Intent {
        return Intent(input?.intent)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): OutPutData {
        return OutPutData(requestCode = resultCode == Activity.RESULT_OK, uri = intent?.data)
    }
}

class InputData(
    val intent: Intent
)

class OutPutData(
    val requestCode: Boolean,
    val uri: Uri?
)

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
        }
    }
    snackBar.show()
}

fun View.show() {
    this.isVisible = true
}

fun View.hide() {
    this.isVisible = false
}

fun Activity.loadUrl(string: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(string))
    startActivity(browserIntent)
}

fun getMimeType(uri: Uri, context: Context): String? {
    return if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
        val cr: ContentResolver? = context.contentResolver
        cr?.getType(uri)
    } else {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(
            uri.toString()
        )
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            fileExtension.lowercase(Locale.getDefault())
        )
    }
}

@RequiresApi(Build.VERSION_CODES.M)
fun Activity.changeStatusBarColor(color: Int = R.color.white) {
    this.window?.statusBarColor = resources.getColor(color, null)
}

object GetConstStringObj {
    const val My_Dialog_Once = "my_Dialog_Once"
    const val USERS = "USERS"
    const val EMAIL = "EMAIL"
    const val UNPAID = "UNPAID"
    const val VERSION = "version"
    const val Admin = "ADMIN"
    const val UN_WANTED = "Un Wanted Error"
    const val EMAIL_VERIFICATION_LINK = "https://hackerstudent.verify.com/VerifyEmail"
    const val Create_Course_title = "Create Course"
    const val Create_Course_desc = "Are You Sure you Want to Create A New Course?"
    const val Create_course = "Course"
    const val Create_Module = "Module"
    const val Per_users = 3
    const val UDI = "UDI-VALUE"
    const val Phone = "PHONE-VALUE"
    const val TOKEN = "UDI-VALUE"
    const val VersionNote =
        "Seem like New Version is Available, so if your want to use this app so kindly Update the app"
}