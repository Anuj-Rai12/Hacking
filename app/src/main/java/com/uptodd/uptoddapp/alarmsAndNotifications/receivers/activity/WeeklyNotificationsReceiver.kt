package com.uptodd.uptoddapp.alarmsAndNotifications.receivers.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.uptodd.uptoddapp.SplashScreenActivity
import com.uptodd.uptoddapp.api.getPeriod
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.score.WEEKLY_TODO
import com.uptodd.uptoddapp.utilities.UptoddNotificationUtilities
import com.uptodd.uptoddapp.utilities.UptoddNotify
import com.uptodd.uptoddapp.utilities.WEEKLY_ALARM_REQUEST_CODE
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class WeeklyNotificationsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        if (context == null) return

        val dataSource = UptoddDatabase.getInstance(context).todoDatabaseDao

        GlobalScope.launch {
            val pendingTodosCount =
                dataSource.getAllPendingTodosCountOfType(WEEKLY_TODO, period = getPeriod(context))
            if (pendingTodosCount != 0) {
                val notificationIntent = Intent(context, SplashScreenActivity::class.java)
                notificationIntent.putExtra("notificationIntent", 2)
                val builder = UptoddNotificationUtilities.notificationBuilder(
                    context,
                    "Activities Pending",
                    "You have $pendingTodosCount weekly activities pending!",
                    notificationIntent,
                    "activity.weekly"
                )
//                UptoddNotificationUtilities.notify(context, builder, WEEKLY_ALARM_REQUEST_CODE)
                NotificationManagerCompat.from(context).UptoddNotify(
                    builder,
                    WEEKLY_ALARM_REQUEST_CODE
                )
            } else {
                val notificationIntent = Intent(context, SplashScreenActivity::class.java)
                notificationIntent.putExtra("notificationIntent", 2)
                val builder = UptoddNotificationUtilities.notificationBuilder(
                    context,
                    "Congratulations",
                    "You have completed all weekly activities today.",
                    notificationIntent,
                    "activity.weekly"
                )
//                UptoddNotificationUtilities.notify(context, builder, WEEKLY_ALARM_REQUEST_CODE)
                NotificationManagerCompat.from(context).UptoddNotify(
                    builder,
                    WEEKLY_ALARM_REQUEST_CODE
                )
            }
        }.invokeOnCompletion {
//            var calendarInstance = Calendar.getInstance()
//            calendarInstance = AllUtil.getUntilNextDayOfWeekAfterHour(Calendar.SATURDAY, 20, calendarInstance)
//            calendarInstance.set(Calendar.HOUR_OF_DAY, 20)
//            calendarInstance.set(Calendar.MINUTE, 0)
//
//            UptoddNotificationUtilities.setAlarm(
//                context,
//                calendarInstance.timeInMillis,
//                WEEKLY_ALARM_REQUEST_CODE,
//                WeeklyNotificationsReceiver::class.java
//            )
        }
    }

}