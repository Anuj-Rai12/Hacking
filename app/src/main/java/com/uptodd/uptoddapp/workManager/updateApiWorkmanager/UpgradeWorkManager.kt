package com.uptodd.uptoddapp.workManager.updateApiWorkmanager

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uptodd.uptoddapp.alarmsAndNotifications.receivers.NotificationBroadcastReceiver
import com.uptodd.uptoddapp.utilities.DAILY_ALARM_REQUEST_CODE
import com.uptodd.uptoddapp.utilities.UptoddNotificationUtilities
import kotlinx.coroutines.coroutineScope
import java.util.*


class UpgradeWorkManager(val context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result = coroutineScope {


        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "Upgrade",
                "UpgradeNotification",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "new channel"
            }
            notificationManager.createNotificationChannel(channel)
        }

        var cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 11)
        cal.set(Calendar.MINUTE, 0)
        val stringIntentExtras = HashMap<String, String>()
        stringIntentExtras["notificationTitle"] = "Upgrade Now:More than 50% Off gift for you"
        stringIntentExtras["notificationText"] =
            "Unlock baby's gentleness by premium program help your baby with complete program"
        stringIntentExtras["type"] = "Upgrade"
        stringIntentExtras["notificationChannelId"] = "Upgrade"
        val intIntentExtras = HashMap<String, Int>()
        intIntentExtras["notificationPriority"] = NotificationCompat.PRIORITY_DEFAULT
        intIntentExtras["notificationId"] = 60023

        UptoddNotificationUtilities.setRepeatingAlarm(
            context
            , cal.timeInMillis, AlarmManager.INTERVAL_DAY,
            DAILY_ALARM_REQUEST_CODE,
            NotificationBroadcastReceiver::class.java
            , intentStringExtras = stringIntentExtras, intentIntegerExtras = intIntentExtras
        )



        Result.success()
    }
}






