package com.uptodd.uptoddapp.alarmsAndNotifications.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.uptodd.uptoddapp.SplashScreenActivity
import com.uptodd.uptoddapp.utilities.DEFAULT_NOTIFICATION_ID
import com.uptodd.uptoddapp.utilities.UptoddNotificationUtilities
import com.uptodd.uptoddapp.utilities.UptoddNotify


class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationIntent = Intent(context, SplashScreenActivity::class.java)
        if (intent.getBooleanExtra("hasActivityIntent", false)) {
            val bundle = Bundle()
            bundle.putString("activityIntent", intent.getStringExtra("activityIntent"))
        }

        val builder = UptoddNotificationUtilities.notificationBuilder(
            context,
            intent.getStringExtra("notificationTitle")!!,
            intent.getStringExtra("notificationText")!!,
            notificationIntent,
            intent.getStringExtra("notificationChannelId")!!,
            intent.getIntExtra("notificationPriority", NotificationCompat.PRIORITY_DEFAULT)
        )

        val notificationId = intent.getIntExtra("notificationId", DEFAULT_NOTIFICATION_ID)


        NotificationManagerCompat.from(context).UptoddNotify(
            builder,
            notificationId
        )
    }
}