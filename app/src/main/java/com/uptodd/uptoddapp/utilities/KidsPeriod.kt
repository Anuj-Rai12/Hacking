package com.uptodd.uptoddapp.utilities

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.time.LocalDate
import java.time.Period

class KidsPeriod(context: Context) {
    var preferences: SharedPreferences = context.getSharedPreferences(
        "LOGIN_INFO",
        Context.MODE_PRIVATE
    )
    var kidsDOB: String? = null

    fun getKidsDob(): String? {
        if (preferences.contains("kidsDob")) {
            Log.d("div", "KidsPeriod L14 ${preferences.getString("kidsDob", "")}")
            kidsDOB = preferences.getString("kidsDob", "")
            Log.d("div", "KidsPeriod L22 $kidsDOB")
            if (kidsDOB == "null" || kidsDOB == null) {
                Log.d("div", "KidsPeriod L21")
                return null
            } else
                return kidsDOB
        }
        return null
    }

    fun getPeriod(): Int {
        var period: Int = 0
        kidsDOB = getKidsDob()
        if (kidsDOB == "null" || kidsDOB == null)
            return 0
        else {
            val months = (Period.between(
                LocalDate.of(
                    kidsDOB!!.substring(0, 4).toInt(),
                    kidsDOB!!.substring(5, 7).toInt(),
                    kidsDOB!!.substring(8, 10).toInt()
                ),
                LocalDate.now()
            ).months)
            val years = (Period.between(
                LocalDate.of(
                    kidsDOB!!.substring(0, 4).toInt(),
                    kidsDOB!!.substring(5, 7).toInt(),
                    kidsDOB!!.substring(8, 10).toInt()
                ),
                LocalDate.now()
            ).years)
            period = years * 12 + months
            Log.d("div", "KidsPeriod L43 $period $years $months")
            if (period < 12)
                period = (((period) / 3) + 1) * 3
            else
                period = (((period) / 6) + 1) * 6
            Log.d("div", "KidsPeriod L45 $period")
            Log.d("div", "KidsPeriod L45 $period")
            return period
        }
    }

    fun getKidsAge(): Int {
        var period: Int = 0
        kidsDOB = getKidsDob()
        if (kidsDOB == "null" || kidsDOB == null)
            return 0
        else {
            val months = (Period.between(
                LocalDate.of(
                    kidsDOB!!.substring(0, 4).toInt(),
                    kidsDOB!!.substring(5, 7).toInt(),
                    kidsDOB!!.substring(8, 10).toInt()
                ),
                LocalDate.now()
            ).months)
            val years = (Period.between(
                LocalDate.of(
                    kidsDOB!!.substring(0, 4).toInt(),
                    kidsDOB!!.substring(5, 7).toInt(),
                    kidsDOB!!.substring(8, 10).toInt()
                ),
                LocalDate.now()
            ).years)
            period = years * 12 + months
            Log.d("div", "KidsPeriod L61 $period")
            return period
        }
    }
}