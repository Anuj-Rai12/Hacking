package com.uptodd.uptoddapp.alarmsAndNotifications.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.work.ListenableWorker
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.SplashScreenActivity
import com.uptodd.uptoddapp.api.getMonth
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.utilities.*
import com.uptodd.uptoddapp.utilities.AppNetworkStatus.Companion.context
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getBooleanExtra("hasActivityIntent", false)) {
            val bundle = Bundle()
            bundle.putString("activityIntent", intent.getStringExtra("activityIntent"))
        }
        if(intent.getStringExtra("type")!=null)
            checkPodcastAdded(context,intent)
        else
            showNotification(context,intent)
    }

   private fun showNotification(context: Context,intent: Intent)
    {
        val notificationIntent = Intent(context, SplashScreenActivity::class.java)

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

    fun checkPodcastAdded(context: Context,intent: Intent)
    {
        val uid = AllUtil.getUserId()
        val months= getMonth(context!!)
        val lang= AllUtil.getLanguage()

        val size=UptoddDatabase.getInstance(context).activityPodcastDao.getAll().value?.size

        AndroidNetworking.get("https://uptodd.com/api/activitypodcast?userId={userId}&months={months}&lang={lang}")
            .addPathParameter("userId", uid.toString())
            .addPathParameter("months", months.toString())
            .addPathParameter("lang",lang)
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    if (response == null) return

                    try {

                        val data = response.get("data") as JSONArray

                        if(data.length()>size!!)
                        {
                           showNotification(context,intent)
                            context.getSharedPreferences("last_updated", Context.MODE_PRIVATE).edit().putLong("last_checked",-1).apply()
                        }

                    }
                    catch (exception:Exception)
                    {

                    }
                }


                override fun onError(anError: ANError?) {
                    ListenableWorker.Result.retry()
                }

            })
    }
}