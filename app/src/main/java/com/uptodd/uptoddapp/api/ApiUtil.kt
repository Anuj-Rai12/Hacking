package com.uptodd.uptoddapp.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.uptodd.uptoddapp.utilities.KidsPeriod

fun getUserId(context: Context): Long? {

    var preferences: SharedPreferences =
        context.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
    if (preferences.contains("uid")) {
        Log.d("div", "ApiUtil L13 ${preferences.getString("uid", null)?.toInt()}")
        return preferences.getString("uid", null)?.toLong()
    } else return null
}

fun getPeriod(context: Context): Int {
    return KidsPeriod(context).getPeriod()
}

fun getMonth(context: Context): Int {
    return KidsPeriod(context).getKidsAge()
}