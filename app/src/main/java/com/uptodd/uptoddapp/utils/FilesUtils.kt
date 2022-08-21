package com.uptodd.uptoddapp.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.utilities.AddedPopUpDialog
import retrofit2.Retrofit
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

fun getDate(format: String = "yyyy-MM-dd"): String? {
    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern(format)
    return current.format(formatter)
}

fun View.showSnackBarMsg(msg: String, length: Int = Snackbar.LENGTH_SHORT, anchor: View) {
    Snackbar.make(this, msg, length)
        .setAnchorView(anchor)
        .show()
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

        object LoginResponse {
            const val email = "email"
            const val password = "password"
        }

    }

    object FreeParentingApi {
        //EndPoints
        const val Login = "intro-parenting-login"
        const val DemoContent = "demo-content"
        const val UPDATE_PROGRESS = "update-demo-progress"
        const val forget_Pass = "intro-parenting-forgot-password"
        const val CHANGE_PASS = "intro-parenting-new-password"
        const val Get_user_detail = "intro-parenting-user-details"
        const val Get_profile_section_update = "intro-parenting-update-details"
        const val upgrade_Request = "intro-parenting-upgrade-request"
    }
}


fun View.showSnackbar(msg: String, length: Int = Snackbar.LENGTH_SHORT, color: Int? = null) {
    val snackBar = Snackbar.make(this, msg, length)
    color?.let {
        snackBar.view.setBackgroundColor(it)
    }
    snackBar.show()
}


fun Fragment.removeItemFromBackStack() {
    val p = activity?.supportFragmentManager
    p?.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
}


inline fun <reified T> deserializeFromJson(jsonFile: String?): T? {
    val gson = Gson()
    return gson.fromJson(jsonFile, T::class.java)
}


@SuppressLint("CheckResult")
fun Context.showImage(id: String, view: ImageView, flag: Boolean) {
    "https://img.youtube.com/vi/$id/mqdefault.jpg".also { url ->
        val e = Glide.with(this)
            .load(Uri.parse(url))
        if (flag) {
            e.transform(CenterCrop(), RoundedCorners(20))
        }
        e.placeholder(R.drawable.loading_animation)
            .error(R.drawable.default_set_android_thumbnail)
            .into(view)
    }
}


fun View.setViewMovingAnimation() {
    val animation = TranslateAnimation(
        1500.0f,
        0.0f,
        0.0f,
        0.0f
    )
    animation.duration = 1500 // animation duration
    this.startAnimation(animation) //your_view for mine is imageView
}

inline fun <reified T> buildApi(retrofit: Retrofit): T {
    return retrofit.create(T::class.java)
}