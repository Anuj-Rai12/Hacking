package com.uptodd.uptoddapp.media.player

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.SplashScreenActivity
import com.uptodd.uptoddapp.ui.todoScreens.TodosListActivity
import com.uptodd.uptoddapp.utilities.*

class BackgroundPlayer : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val toRun = intent?.extras?.getBoolean("toRun")!!
        //if activity is in background
        if (toRun) {
            val stopIntent = Intent(context, MediaStopReceiver::class.java)
            stopIntent.putExtra("NotificationID", UPTODD_MEDIA_PLAYER_NOTIFICATION)
            val notificationIntent = Intent(context, SplashScreenActivity::class.java)
            val extras = Bundle()
            if (intent.extras!!.getString("musicType") == "music") {
                stopIntent.putExtra("musicType", "music")
                extras.putString("activityIntent", "music")
            } else {
                stopIntent.putExtra("musicType", "poem")
                extras.putString("activityIntent", "poem")
            }
            val notificationBuilder = UptoddNotificationUtilities.mediaNotificationBuilder(
                context!!,
                "UpTodd",
                "Playing media in background...",
                notificationIntent,
                "notification.music_player",UpToddMediaPlayer.isPlaying
            )
            notificationBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
            if (UpToddMediaPlayer.isPlaying) {
//                UptoddNotificationUtilities.notify(
//                    context,
//                    notificationBuilder,
//                    UPTODD_MEDIA_PLAYER_NOTIFICATION
//                )
                NotificationManagerCompat.from(context).UptoddNotify(
                    notificationBuilder,
                    UPTODD_MEDIA_PLAYER_NOTIFICATION
                )
            } else {
//                UptoddNotificationUtilities.notify(
//                    context,
//                    notificationBuilder,
//                    UPTODD_MEDIA_PLAYER_NOTIFICATION
//                )
                NotificationManagerCompat.from(context).UptoddNotify(
                    notificationBuilder,
                    UPTODD_MEDIA_PLAYER_NOTIFICATION
                )
            }
        }
        //when activity is resumed, dismiss the notification
        else {
            if (context == null) return
            NotificationManagerCompat.from(context)
                .cancelUptoddNotification(UPTODD_MEDIA_PLAYER_NOTIFICATION)
        }
    }
}