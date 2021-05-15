package com.uptodd.uptoddapp.alarmsAndNotifications.receivers.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.uptodd.uptoddapp.SplashScreenActivity
import com.uptodd.uptoddapp.api.getPeriod
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.score.MONTHLY_TODO
import com.uptodd.uptoddapp.ui.todoScreens.TodosListActivity
import com.uptodd.uptoddapp.utilities.MONTHLY_ALARM_REQUEST_CODE
import com.uptodd.uptoddapp.utilities.UptoddNotificationUtilities
import com.uptodd.uptoddapp.utilities.UptoddNotify
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MonthlyNotificationsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        if (context == null) return

        val dataSource = UptoddDatabase.getInstance(context).todoDatabaseDao
        GlobalScope.launch {
            val pendingTodosCount = dataSource.getAllPendingTodosCountOfType(
                MONTHLY_TODO,
                period = getPeriod((context))
            )
            if (pendingTodosCount != 0) {
                val notificationIntent = Intent(context, SplashScreenActivity::class.java)
                val builder = UptoddNotificationUtilities.notificationBuilder(
                    context,
                    "Activities Pending",
                    "You have $pendingTodosCount monthly activities pending!",
                    notificationIntent,
                    "activity.monthly"
                )
//                UptoddNotificationUtilities.notify(context, builder, MONTHLY_ALARM_REQUEST_CODE)
                NotificationManagerCompat.from(context).UptoddNotify(
                    builder,
                    MONTHLY_ALARM_REQUEST_CODE
                )
            } else {
                val notificationIntent = Intent(context, TodosListActivity::class.java)
                val builder = UptoddNotificationUtilities.notificationBuilder(
                    context,
                    "Congratulations",
                    "You have completed all monthly activities today.",
                    notificationIntent,
                    "activity.monthly"
                )
//                UptoddNotificationUtilities.notify(context, builder, MONTHLY_ALARM_REQUEST_CODE)
                NotificationManagerCompat.from(context).UptoddNotify(
                    builder,
                    MONTHLY_ALARM_REQUEST_CODE
                )
            }
        }
    }

}