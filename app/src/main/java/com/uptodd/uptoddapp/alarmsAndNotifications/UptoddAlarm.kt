package com.uptodd.uptoddapp.alarmsAndNotifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.uptodd.uptoddapp.alarmsAndNotifications.receivers.AlarmBroadcastReceiver
import java.util.*


class UptoddAlarm {

    // in my code, I'll use todoId( primary key) as alarmRequestCode since it is always unique
    companion object {

        //Use alarm manager to set alarm with time in millis
        fun setAlarm(
            context: Context,
            time: Long,
            alarmRequestCode: Int,
            name: String,
            broadcastReceiver: Class<*> = AlarmBroadcastReceiver::class.java,
            recurring: Boolean = false
        ) {


            // if given alarm time is past time
            // cancel setting up the alarm


            val newTime = Calendar.getInstance()
            newTime.timeInMillis = time
            newTime.set(Calendar.SECOND, 0)

            val currentTime = Calendar.getInstance()
            currentTime.timeInMillis = System.currentTimeMillis()
            currentTime.set(Calendar.SECOND, 0)
            // not accounting for seconds
            if (newTime < currentTime) {
                Log.i(
                    "AlarmDebug", "Not setting up the alarm for id $alarmRequestCode " +
                            "and date ${newTime.time} ${currentTime.time}"
                )
                return
            }
            Log.i(
                "AlarmDebug", "Setting up the alarm for id $alarmRequestCode " +
                        "and date ${newTime.time}"
            )

            val intent = Intent(context, broadcastReceiver)
            intent.putExtra("name", name)
            intent.putExtra("alarmRequestCode", alarmRequestCode)
            val pendingIntent = PendingIntent.getBroadcast(context, alarmRequestCode, intent, 0)
            val alarmManager =
                context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
            if (recurring) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(         // set exact doesn't work before kitkat
                        AlarmManager.RTC_WAKEUP,
                        time,
                        pendingIntent
                    )
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        time,
                        pendingIntent
                    )
                }
            } else
                alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        }

        //Cancel the alarm with request code=alarmRequestCode
        fun cancelAlarm(
            context: Context,
            alarmRequestCode: Int,
            name: String = "",
            broadcastReceiver: Class<*> = AlarmBroadcastReceiver::class.java
        ) {
            val intent = Intent(context, broadcastReceiver)
            intent.putExtra("name", name)
            intent.putExtra("alarmRequestCode", alarmRequestCode)
            val pendingIntent = PendingIntent.getBroadcast(context, alarmRequestCode, intent, 0)
            val alarmManager =
                context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }


        fun setRepeatingAlarm(
            context: Context,
            time: Long,
            repeatFrequency: Long,
            alarmRequestCode: Int,
            name: String,
            broadcastReceiver: Class<*> = AlarmBroadcastReceiver::class.java
        ) {
            var alarmtimeInMillis = time

            val newTime = Calendar.getInstance().apply {
                timeInMillis = time
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val currentTime = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (newTime < currentTime) {
                Log.i(
                    "AlarmDebug", "Not setting up the alarm for id $alarmRequestCode " +
                            "and date ${newTime.time} ${currentTime.time}"
                )
                val tommorrow = Calendar.getInstance().apply {
                    timeInMillis = time
                    set(Calendar.DAY_OF_MONTH, get(Calendar.DAY_OF_MONTH) + 1)
                }
                alarmtimeInMillis = tommorrow.timeInMillis
            }
            Log.i(
                "AlarmDebug", "Setting up the alarm for id $alarmRequestCode " +
                        "and date $alarmtimeInMillis"
            )

            val intent = Intent(context, broadcastReceiver)
            intent.putExtra("name", name)
            intent.putExtra("alarmRequestCode", alarmRequestCode)
            val pendingIntent = PendingIntent.getBroadcast(context, alarmRequestCode, intent, 0)
            val alarmManager =
                context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                alarmtimeInMillis,
                repeatFrequency,
                pendingIntent
            )
        }
    }
}