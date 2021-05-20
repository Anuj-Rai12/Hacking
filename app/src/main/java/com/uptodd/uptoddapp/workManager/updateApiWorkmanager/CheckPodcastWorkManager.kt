package com.uptodd.uptoddapp.workManager.updateApiWorkmanager

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uptodd.uptoddapp.alarmsAndNotifications.receivers.NotificationBroadcastReceiver
import com.uptodd.uptoddapp.utilities.*
import kotlinx.coroutines.coroutineScope
import java.util.*

class CheckPodcastWorkManager(val context: Context, parameters: WorkerParameters) :
CoroutineWorker(context, parameters)  {
    override suspend fun doWork():  Result = coroutineScope {


        val notificationManager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("Podcast", "PodcastNotification", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "new channel"
            }
            notificationManager.createNotificationChannel(channel)
        }

            var cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY,11)
            cal.set(Calendar.MINUTE,0)
            val stringIntentExtras = HashMap<String, String>()
            stringIntentExtras["notificationTitle"] = "New Podcast Added"
            stringIntentExtras["notificationText"] = "Hey Mom/Dad, Check new Podcast Added for you."
            stringIntentExtras["type"]="Podcast"
            stringIntentExtras["notificationChannelId"] = "Podcast"
            val intIntentExtras = HashMap<String, Int>()
            intIntentExtras["notificationPriority"] = NotificationCompat.PRIORITY_DEFAULT
            intIntentExtras["notificationId"] =60007

        UptoddNotificationUtilities.setRepeatingAlarm(context
        ,cal.timeInMillis, AlarmManager.INTERVAL_DAY,DAILY_ALARM_REQUEST_CODE,NotificationBroadcastReceiver::class.java
        ,intentStringExtras = stringIntentExtras,intentIntegerExtras = intIntentExtras)



        Result.success()
    }






}