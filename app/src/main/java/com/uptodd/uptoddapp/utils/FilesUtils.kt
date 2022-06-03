package com.uptodd.uptoddapp.utils

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import com.uptodd.uptoddapp.R


@RequiresApi(Build.VERSION_CODES.M)
fun Activity.changeStatusBarColor(color: Int) {
    this.window?.statusBarColor = resources.getColor(color, null)
}

val getRandomBgColor
    get() = listOf(
        Pair(R.color.video_container, R.drawable.viedo_container_layout),
        Pair(R.color.video_container_2, R.drawable.video_container_layout_2)
    ).random()


val getAdaptorViewHolderBg
    get() = listOf(
        R.drawable.video_container_layout_3,
        R.drawable.viedo_container_layout_4,
        R.drawable.viedo_container_layout_5,
        R.drawable.viedo_container_layout_6
    )