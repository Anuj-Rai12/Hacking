package com.uptodd.uptoddapp.utilities

import android.content.Context

class ScreenDpi(val context: Context) {

    fun getScreenDpiRatio(): Float {
        return context.resources.displayMetrics.density
    }

    fun getScreenDrawableType():String{
        val ratio=getScreenDpiRatio()
        if(ratio<=1.0)
            return "drawable-mdpi"
        else if(ratio<=1.5)
            return "drawable-hdpi"
        else if(ratio<=2.0)
            return "drawable-xhdpi"
        else if(ratio<=3.0)
            return "drawable-xxhdpi"
        else
            return "drawable-xxxhdpi"
    }
}