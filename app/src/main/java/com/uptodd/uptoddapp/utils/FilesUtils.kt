package com.uptodd.uptoddapp.utils

import android.app.Activity
import android.os.Build
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.utilities.AddedPopUpDialog
import retrofit2.Retrofit
import java.util.regex.Pattern


fun Activity.changeStatusBarColor(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.window?.statusBarColor = resources.getColor(color, null)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        this.window?.statusBarColor = resources.getColor(color)
    }
}

val getRandomBgColor
    get() = listOf(
        Pair(R.color.video_container, R.drawable.viedo_container_layout),
        Pair(R.color.video_container_2, R.drawable.video_container_layout_2),
        Pair(R.color.video_container_3, R.drawable.video_player_convertor_3),
        Pair(R.color.video_container_4, R.drawable.video_player_convetor_4)
    ).random()


val getAdaptorViewHolderBg
    get() = listOf(
        R.drawable.video_container_layout_3,
        R.drawable.viedo_container_layout_4,
        R.drawable.viedo_container_layout_5,
        R.drawable.viedo_container_layout_6
    )


fun Fragment.setUpErrorMessageDialog(
    title: String = "Oops something Went Wrong",
    content: String = "Cannot process the request for podcast ,so please Try Again.."
) {
    AddedPopUpDialog.showInfo(
        title,
        content,
        parentFragmentManager
    )
}


fun View.hide() {
    this.isVisible = false
}

fun View.show() {
    this.isVisible = true
}

fun Activity.toastMsg(string: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, string, length).show()
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}


fun setLogCat(title: String = "TAG", msg: String) {
    Log.i(title, "$title: $msg")
}

fun checkUserInput(string: String) = string.isNullOrEmpty() || string.isBlank()


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

fun getEmojiByUnicode(unicode: Int) = String(Character.toChars(unicode))


fun getPhoneNumber(num: String): Array<String?> {
    val str = arrayOfNulls<String>(2)
    for (i in num.indices) {
        val value = num.length - 1 - i
        if (value == 10) {
            str[0] = num.substring(0, i + 1)
            str[1] = num.substring(i + 1)
            return str
        }
    }
    return str
}


object FilesUtils {
    const val BASE_URL = "https://uptodd.com/api/"
    const val NO_INTERNET = "Device is Currently Offline!!"
    object DATASTORE {
        const val PERSISTENCE_Login = "LOGIN_INFO"
        const val LoginType = "userType"
        const val defValue = "Normal"
        const val FREE_LOGIN = "freeParentingLoginInfo"
        object LoginResponse{
            const val email="email"
            const val name="name"
            const val phone="phoneNumber"
        }
    }

    object FreeParentingApi {
        //EndPoints
        const val Login = "intro-program-login"
    }
}


fun View.showSnackbar(msg: String, length: Int = Snackbar.LENGTH_SHORT, color: Int? = null) {
    val snackBar = Snackbar.make(this, msg, length)
    color?.let {
        snackBar.view.setBackgroundColor(it)
    }
    snackBar.show()
}


inline fun <reified T> buildApi(retrofit: Retrofit): T {
    return retrofit.create(T::class.java)
}