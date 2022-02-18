package com.uptodd.uptoddapp.utilities

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.alarmsAndNotifications.receivers.NotificationBroadcastReceiver
import com.uptodd.uptoddapp.media.player.MediaStopReceiver
import com.uptodd.uptoddapp.utilities.AppNetworkStatus.Companion.context

class UptoddNotificationUtilities {

    companion object {

        private var notificationContext: Context? = null

        fun setAlarm(
            context: Context,
            time: Long,
            alarmRequestCode: Int,
            broadcastReceiver: Class<*> = NotificationBroadcastReceiver::class.java,
            intentStringExtras: HashMap<String, String> = HashMap(),
            intentIntegerExtras: HashMap<String, Int> = HashMap(),
            intentLongExtras: HashMap<String, Long> = HashMap(),
            intentBoolExtras: HashMap<String, Boolean> = HashMap()
        ) {
            val intent = Intent(context, broadcastReceiver)

            intentStringExtras.forEach { action: Map.Entry<String, String> ->
                intent.putExtra(action.key, action.value)
            }
            intentIntegerExtras.forEach { action: Map.Entry<String, Int> ->
                intent.putExtra(action.key, action.value)
            }
            intentLongExtras.forEach { action: Map.Entry<String, Long> ->
                intent.putExtra(action.key, action.value)
            }
            intentBoolExtras.forEach { action: Map.Entry<String, Boolean> ->
                intent.putExtra(action.key, action.value)
            }
            val pendingIntent = PendingIntent.getBroadcast(context, alarmRequestCode, intent, 0)
            val alarmManager =
                context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent)

            Log.i("alarm", "Alarm $alarmRequestCode set for ${AllUtil.getTimeFromMillis(time)}")
        }

        fun setRepeatingAlarm(
            context: Context,
            time: Long,
            interval: Long,
            alarmRequestCode: Int,
            broadcastReceiver: Class<*> = NotificationBroadcastReceiver::class.java,
            intentStringExtras: HashMap<String, String> = HashMap(),
            intentIntegerExtras: HashMap<String, Int> = HashMap(),
            intentLongExtras: HashMap<String, Long> = HashMap()
        ) {
            val intent = Intent(context, broadcastReceiver)

            intentStringExtras.forEach { action: Map.Entry<String, String> ->
                intent.putExtra(action.key, action.value)
            }
            intentIntegerExtras.forEach { action: Map.Entry<String, Int> ->
                intent.putExtra(action.key, action.value)
            }
            intentLongExtras.forEach { action: Map.Entry<String, Long> ->
                intent.putExtra(action.key, action.value)
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val pendingIntent = PendingIntent.getBroadcast(context, alarmRequestCode, intent, 0)
            val alarmManager =
                context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, interval, pendingIntent)

            Log.i(
                "alarm",
                "Alarm $alarmRequestCode set for ${AllUtil.getTimeFromMillis(time)} with next upcoming alarm at ${AllUtil.getTimeFromMillis(
                    time + interval
                )}"
            )
        }

        fun cancelAlarm(
            context: Context,
            alarmRequestCode: Int,
            broadcastReceiver: Class<*>
        ) {
            val intent = Intent(context, broadcastReceiver)
            val pendingIntent = PendingIntent.getBroadcast(context, alarmRequestCode, intent, 0)
            val alarmManager =
                context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }

        //NotificationBuilder
        fun mediaNotificationBuilder(
            notificationContext: Context,
            notificationTitle: String,
            notificationText: String,
            notificationIntent: Intent?,
            channelId: String,isPlaying:Boolean=true
        ): NotificationCompat.Builder {

            var pendingIntent:PendingIntent?=null
            if(notificationIntent==null)
            {

            }
            else
            {
                pendingIntent= TaskStackBuilder.create(notificationContext).run {
                    addNextIntentWithParentStack(notificationIntent)
                    getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
                }

            }

            val notificationBuilder = NotificationCompat.Builder(notificationContext, channelId)
            val remoteViews = RemoteViews(notificationContext.packageName, R.layout.layout_custom_notify_media)
            remoteViews.setTextViewText(R.id.title, notificationTitle)
            remoteViews.setTextViewText(R.id.text, notificationText)
            val stopIntent = Intent(notificationContext, MediaStopReceiver::class.java)
            stopIntent.putExtra("NotificationID", UPTODD_MEDIA_PLAYER_NOTIFICATION)
            val stopPendingIntent = PendingIntent.getBroadcast(notificationContext
                , 0, stopIntent, 0)

            if(isPlaying) {
                remoteViews.setImageViewResource(R.id.playPauseIcon,R.drawable.ic_baseline_pause_24)
            } else {
                remoteViews.setImageViewResource(R.id.playPauseIcon,R.drawable.ic_baseline_play_arrow_24)
            }
            remoteViews.setOnClickPendingIntent(R.id.playPauseIcon, stopPendingIntent)

            notificationBuilder.setCustomContentView(remoteViews)
                .setSmallIcon(R.drawable.exo_icon_play)
                .priority = NotificationCompat.PRIORITY_DEFAULT
            if(notificationIntent==null)
            {

            }
            else
            {
                notificationBuilder.setContentIntent(pendingIntent)
            }
            return notificationBuilder
        }

        fun notificationBuilder(
            notificationContext: Context,
            notificationTitle: String,
            notificationText: String,
            notificationIntent: Intent,
            channelId: String,
            priority: Int = NotificationCompat.PRIORITY_DEFAULT
        ): NotificationCompat.Builder {
            var requestCode=0
            when (channelId) {
                "MemoryBooster" -> requestCode=2
                "ActivitySample" -> requestCode=3
                "Podcast" -> requestCode=4
            }

            val pendingIntent: PendingIntent = TaskStackBuilder.create(notificationContext).run {
                addNextIntentWithParentStack(notificationIntent)
                getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            val notificationBuilder = NotificationCompat.Builder(notificationContext, channelId)
            // inflate the layout and set the values to our UI IDs
            val remoteViews = RemoteViews(notificationContext.packageName, R.layout.layout_custom_notification)
            remoteViews.setTextViewText(R.id.title, notificationTitle)
            remoteViews.setTextViewText(R.id.text, notificationText)
            notificationBuilder.setCustomContentView(remoteViews)
                .setCustomBigContentView(remoteViews)
                .setSmallIcon(R.drawable.app_icon)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .priority = priority


            return notificationBuilder
        }


        //Show Notification using notification builder
        fun notify(
            notificationContext: Context,
            notificationBuilder: NotificationCompat.Builder,
            notificationId: Int = 0
        ) {
            Companion.notificationContext = notificationContext
            with(NotificationManagerCompat.from(notificationContext)) {
                notify(notificationId, notificationBuilder.build())
            }
        }

        fun dismiss(
            notificationContext: Context? = Companion.notificationContext,
            notificationId: Int
        ) {
            if (notificationContext != null) {
                with(NotificationManagerCompat.from(notificationContext)) {
                    cancel(notificationId)
                }
            }
        }

        //Create a notification channel and register it with the device
//        fun createNotificationChannel(
//            context: Context,
//            channelName: String,
//            channelDescription: String,
//            channelId: String,
//            importanceLevel: Int = 0
//        ) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//                val channelImportance = when (importanceLevel) {
//                    0 -> NotificationManager.IMPORTANCE_DEFAULT
//                    1 -> NotificationManager.IMPORTANCE_MIN
//                    2 -> NotificationManager.IMPORTANCE_LOW
//                    3 -> NotificationManager.IMPORTANCE_HIGH
//                    4 -> NotificationManager.IMPORTANCE_MAX
//                    else -> NotificationManager.IMPORTANCE_NONE
//                }
//
//                val channel = NotificationChannel(channelId, channelName, channelImportance).apply {
//                    description = channelDescription
//                }
//                val notificationManager: NotificationManager =
//                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                notificationManager.createNotificationChannel(channel)
//            }
//        }
    }
}


fun NotificationManager.createUptoddNotificationChannels(
    channelId: String,
    channelName: String,
    channelDescription: String,
    priority: Int = NotificationManager.IMPORTANCE_DEFAULT
) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, channelName, priority).apply {
            description = channelDescription
        }

        createNotificationChannel(channel)
    }
}

fun NotificationManagerCompat.cancelUptoddNotification(notificationId: Int) {
    cancel(notificationId)
}

fun NotificationManagerCompat.UptoddNotify(
    notificationBuilder: NotificationCompat.Builder,
    notificationId: Int = 0
) {
    notify(notificationId, notificationBuilder.build())
}