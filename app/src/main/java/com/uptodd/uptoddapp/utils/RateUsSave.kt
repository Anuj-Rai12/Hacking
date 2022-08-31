package com.uptodd.uptoddapp.utils

import android.util.Log
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RateUsSave(private val uptoddSharedPreferences: UptoddSharedPreferences) {

    private val months = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

    fun saveForFirst15Day(subSubscribeDate: String) {

        val dateArray = subSubscribeDate.split("-".toRegex())
        val currentDate = dateArray[dateArray.size - 1].toInt()
        var currentMonth = dateArray[1].toInt()

        //ADD Sub Date with +15
        var setDay = currentDate + 15
        if (setDay > months[currentMonth - 1]) {//30,31 even 28
            setDay -= months[currentMonth - 1]
            currentMonth = (currentMonth + 1) % 12
        }

        Log.i("RATEUS", "Item save $setDay in this $currentMonth type ${RateUs.DATE_15}")
        uptoddSharedPreferences.saveRatingDate(setDay, currentMonth, RateUs.DATE_15)
    }


    fun saveDateOnEvery30Day(currentMonth: Int, currentDay: Int) {
        //ADDING FOR +30
        val newMonth = (currentMonth + 1) % 12
        var day = currentDay
        if (months[newMonth - 1] < day) {
            day = months[newMonth - 1]
        }

        Log.i("RATEUS", "Item SAVE $day in this $newMonth type ${RateUs.DATE_30}")
        uptoddSharedPreferences.saveRatingDate(day, newMonth, RateUs.DATE_30)
    }


    fun shouldShowDialogBox(): Boolean {
        val currentDate = getDate()
        val dateArr = currentDate!!.split("-".toRegex())
        val currentDay = dateArr.last().toInt()
        val currentMonth = dateArr[1].toInt()

        val day = uptoddSharedPreferences.getRatingDay()
        val month = uptoddSharedPreferences.getRatingMonth()
        val type = uptoddSharedPreferences.getRatingType()

        if (type.isNullOrEmpty() || month == -1 || day == -1) {
            return false
        }

        if (type == RateUs.DATE_15 && currentDay >= day && currentMonth >= month) {
            return true
        } else if (type == RateUs.DATE_15 && currentMonth > month) {
            return true
        }

        if (type == RateUs.DATE_30 && currentDay >= day && currentMonth >= month) {
            return true
        } else if (type == RateUs.DATE_30 && currentMonth > month) {
            return true
        }

        return false
    }

    private fun getDate(format: String = "yyyy-MM-dd"): String? {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern(format)
        return current.format(formatter)
    }
}