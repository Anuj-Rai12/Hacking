package com.uptodd.uptoddapp.utils

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.utilities.AddedPopUpDialog


fun Activity.changeStatusBarColor(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.window?.statusBarColor = resources.getColor(color, null)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        this.window?.statusBarColor = resources.getColor(color)
    }
}

fun Activity.getColorValue(color: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        resources.getColor(color, null)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        resources.getColor(color)
    } else {
        resources.getColor(color)
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
    var isErrorDialogBoxVisible = false
    val handler = Handler(Looper.getMainLooper())
    handler.post {
        if (!isErrorDialogBoxVisible && isAdded) {
            isErrorDialogBoxVisible = true
            AddedPopUpDialog.showInfo(
                title,
                content,
                parentFragmentManager
            )
        }
    }
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

