package com.uptodd.uptoddapp.alarmsAndNotifications.receivers.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.uptodd.uptoddapp.SplashScreenActivity
import com.uptodd.uptoddapp.api.getPeriod
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.score.ESSENTIALS_TODO
import com.uptodd.uptoddapp.utilities.ESSENTIALS_ALARM_REQUEST_CODE
import com.uptodd.uptoddapp.utilities.UptoddNotificationUtilities
import com.uptodd.uptoddapp.utilities.UptoddNotify
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EssentialsNotificationsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        if(context == null) return

        val dataSource = UptoddDatabase.getInstance(context).todoDatabaseDao
        GlobalScope.launch {
            val pendingTodosCount = dataSource.getAllPendingTodosCountOfType(
                ESSENTIALS_TODO,
                period = getPeriod((context))
            )
            if (pendingTodosCount != 0) {
                val notificationIntent = Intent(context, SplashScreenActivity::class.java)
                val builder = UptoddNotificationUtilities.notificationBuilder(
                    context,
                    "Activities Pending",
                    "You have $pendingTodosCount essential activities pending!",
                    notificationIntent,
                    "activity.essential"
                )
//                UptoddNotificationUtilities.notify(context, builder, ESSENTIALS_ALARM_REQUEST_CODE)
                NotificationManagerCompat.from(context).UptoddNotify(
                    builder,
                    ESSENTIALS_ALARM_REQUEST_CODE
                )
            }
        }
    }

}