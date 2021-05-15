package com.uptodd.uptoddapp.utilities

import android.content.Context
import android.util.DisplayMetrics

class Conversion {
    companion object{
        fun convertDpToPixel(dp: Float, context: Context): Int {
            return (dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
        }
    }
}