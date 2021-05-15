package com.uptodd.uptoddapp.alarmsAndNotifications.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.uptodd.uptoddapp.api.getPeriod
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.score.DAILY_TODO
import com.uptodd.uptoddapp.utilities.DAILY_ACTIVITY_STATUS_CHECK_NOTIFICATION
import com.uptodd.uptoddapp.utilities.UptoddNotificationUtilities
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class DailyActivityStatusCheck : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val dataSource = UptoddDatabase.getInstance(context!!).todoDatabaseDao
        GlobalScope.launch {
            val pendingTodosCount =
                dataSource.getAllPendingTodosCountOfType(DAILY_TODO, period = getPeriod(context))
            if (pendingTodosCount != 0) {
                val stringIntentExtras = HashMap<String, String>()
                stringIntentExtras["notificationTitle"] = "Today's Report"
                stringIntentExtras["notificationText"] =
                    "MOM/DAD, you missed $pendingTodosCount activities! Please don’t miss as it will hamper my growth"
                stringIntentExtras["notificationChannelId"] = "activity.daily"

                val intIntentExtras = HashMap<String, Int>()
                intIntentExtras["notificationPriority"] = NotificationCompat.PRIORITY_LOW
                intIntentExtras["notificationId"] = DAILY_ACTIVITY_STATUS_CHECK_NOTIFICATION

                UptoddNotificationUtilities.setAlarm(context,
                    System.currentTimeMillis(),
                    DAILY_ACTIVITY_STATUS_CHECK_NOTIFICATION,
                    broadcastReceiver = NotificationBroadcastReceiver::class.java,
                    intentIntegerExtras = intIntentExtras,
                    intentStringExtras = stringIntentExtras)
            } else {
                val stringIntentExtras = HashMap<String, String>()
                stringIntentExtras["notificationTitle"] = "Today's Report"
                stringIntentExtras["notificationText"] =
                    "MOM/DAD, thank you for completing all the activities for me."
                stringIntentExtras["notificationChannelId"] = "activity.daily"

                val intIntentExtras = HashMap<String, Int>()
                intIntentExtras["notificationPriority"] = NotificationCompat.PRIORITY_LOW
                intIntentExtras["notificationId"] = DAILY_ACTIVITY_STATUS_CHECK_NOTIFICATION

                UptoddNotificationUtilities.setAlarm(context,
                    System.currentTimeMillis(),
                    DAILY_ACTIVITY_STATUS_CHECK_NOTIFICATION,
                    broadcastReceiver = NotificationBroadcastReceiver::class.java,
                    intentIntegerExtras = intIntentExtras,
                    intentStringExtras = stringIntentExtras)
            }
        }
    }

}