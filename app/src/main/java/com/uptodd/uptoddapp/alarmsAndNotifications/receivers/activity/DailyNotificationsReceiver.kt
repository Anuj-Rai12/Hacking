package com.uptodd.uptoddapp.alarmsAndNotifications.receivers.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.uptodd.uptoddapp.SplashScreenActivity
import com.uptodd.uptoddapp.api.getPeriod
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.score.DAILY_TODO
import com.uptodd.uptoddapp.utilities.DAILY_ALARM_REQUEST_CODE
import com.uptodd.uptoddapp.utilities.UptoddNotificationUtilities
import com.uptodd.uptoddapp.utilities.UptoddNotify
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DailyNotificationsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        if (context == null) return

        val dataSource = UptoddDatabase.getInstance(context).todoDatabaseDao

        GlobalScope.launch {
            val pendingTodosCount =
                dataSource.getAllPendingTodosCountOfType(DAILY_TODO, period = getPeriod(context))
            if (pendingTodosCount != 0) {
                val notificationIntent = Intent(context, SplashScreenActivity::class.java)
                notificationIntent.putExtra("notificationIntent", 1)
                val builder = UptoddNotificationUtilities.notificationBuilder(
                    context,
                    "Activities Pending",
                    "You have $pendingTodosCount daily activities pending!",
                    notificationIntent,
                    "activity.daily"
                )
//                UptoddNotificationUtilities.notify(context, builder, DAILY_ALARM_REQUEST_CODE)
                NotificationManagerCompat.from(context).UptoddNotify(
                    builder,
                    DAILY_ALARM_REQUEST_CODE
                )
            } else {
                val notificationIntent = Intent(context, SplashScreenActivity::class.java)
                notificationIntent.putExtra("notificationIntent", 1)
                val builder = UptoddNotificationUtilities.notificationBuilder(
                    context,
                    "Congratulations",
                    "You have completed all daily activities today.",
                    notificationIntent,
                    "activity.daily"
                )
//                UptoddNotificationUtilities.notify(context, builder, DAILY_ALARM_REQUEST_CODE)
                NotificationManagerCompat.from(context).UptoddNotify(
                    builder,
                    DAILY_ALARM_REQUEST_CODE
                )
            }
        }
    }

}