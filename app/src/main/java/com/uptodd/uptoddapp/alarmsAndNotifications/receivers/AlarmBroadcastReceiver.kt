package com.uptodd.uptoddapp.alarmsAndNotifications.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.uptodd.uptoddapp.SplashScreenActivity
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.utilities.UptoddNotificationUtilities
import com.uptodd.uptoddapp.utilities.UptoddNotify
import kotlinx.coroutines.*


class AlarmBroadcastReceiver : BroadcastReceiver() {

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun onReceive(context: Context?, intent: Intent?) {

        if (context == null && intent == null) return

        val notificationBody = intent?.getStringExtra("name")
        val alarmRequestCode = intent?.getIntExtra("alarmRequestCode", -1)
        if (alarmRequestCode == -1 || alarmRequestCode == null) return


        val notificationIntent = Intent(context, SplashScreenActivity::class.java)

        val notificationBuild = UptoddNotificationUtilities.notificationBuilder(
            context!!,
            "Activity Reminder",
            notificationBody ?: "Complete your activities",
            notificationIntent,
            "activitiesReminder",
            NotificationCompat.PRIORITY_DEFAULT
        )



        NotificationManagerCompat.from(context).UptoddNotify(notificationBuild, alarmRequestCode!!)

        // update that alarm was fired
        val todosDatabase = UptoddDatabase.getInstance(context).todoDatabaseDao

        uiScope.launch {
            withContext(Dispatchers.IO) {
                val todo = todosDatabase.getTodo(alarmRequestCode)
                if (todo != null) {
                    todo.isAlarmSet = false
                    todosDatabase.update(todo)
                }
            }
        }

        Log.d("Alarm broadcastReceiver", "alarm detected")
    }
}