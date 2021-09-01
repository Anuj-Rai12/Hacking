package com.uptodd.uptoddapp.helperClasses

import android.annotation.SuppressLint
import android.util.Log
import com.uptodd.uptoddapp.database.todo.TWENTY_FOUR_HOURS_IN_MILLI
import java.text.SimpleDateFormat
import java.util.*

class DateClass {

    @SuppressLint("SimpleDateFormat")
    private val dateTimeFormat: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")

    @SuppressLint("SimpleDateFormat")
    private val dateFormat: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy")

    @SuppressLint("SimpleDateFormat")
    private val timeFormat: SimpleDateFormat = SimpleDateFormat("HH:mm:ss")

    fun getCurrentDateTimeAsString(): String {
        return dateTimeFormat.format(Date())
    }

    fun convertTimeStringToDate(timeString: String): Date {
        return timeFormat.parse(timeString)
    }

    fun getCurrentDateAsString(): String {
        return dateFormat.format(Date())
    }

    fun getCurrentDateAsMillis(): Long {
        return Date().time
    }

    fun getCurrentDateStringAsYYYYMMDD(): String {
        val YYYYMMDDformat = SimpleDateFormat("yyyy-MM-dd")
        return YYYYMMDDformat.format(Date())
    }

    fun convertToDateTimeFromString(dateTimeString: String): Date? {
        if (dateTimeString == "null") return null
        return dateTimeFormat.parse(dateTimeString)
    }


    fun convertToDateFromString(dateString: String): Date? {
        if (dateString == "null") return null
        return dateFormat.parse(dateString)
    }

    fun convertTimeInMillisToString(time: Long): String {
        val date = Date(time)
        val YYYYMMDDformat = SimpleDateFormat("yyyy-MM-dd")
        return YYYYMMDDformat.format(date)

    }

    fun isTodoFresh(lastTodoFetchDate: String): Boolean {
        val lastTodoFetchedOn = dateFormat.parse(lastTodoFetchDate)
        val currentDate = dateFormat.parse(getCurrentDateAsString())

        if (lastTodoFetchedOn == null || currentDate == null) return true

        val lastFetched = Calendar.getInstance().apply {
            time = lastTodoFetchedOn
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val today = Calendar.getInstance().apply {
            time = currentDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        Log.i("h_debug", "")
        Log.i("h_debug", "${lastTodoFetchedOn} ${lastFetched.time}")
        Log.i("h_debug", "${currentDate} ${today.time}")
        Log.i("h_debug", "${today.timeInMillis} == ${lastFetched.timeInMillis}}")

        return (lastFetched.timeInMillis == today.timeInMillis)       // return true if to-do is fresh
    }

    fun convertToTimeInMilliFromTimeString(timeString: String): Long? { // this func is used get timeInMilli for today at given time
        return if (timeString != "null") {
            val dateAndTimeOfAlarmAsString = "${getCurrentDateAsString()} $timeString"
            val dateAndTimeOfAlarm = dateTimeFormat.parse(dateAndTimeOfAlarmAsString)
            dateAndTimeOfAlarm.time
        } else null
    }

    fun getDifferenceInDays(lastFetchedDate: String, currentDate: String): Int {
        val lastFetched = dateFormat.parse(lastFetchedDate)
        val today = dateFormat.parse(currentDate)

        val durationInMilli = today.time - lastFetched.time
        val days = durationInMilli / TWENTY_FOUR_HOURS_IN_MILLI
        return days.toInt()

    }
}