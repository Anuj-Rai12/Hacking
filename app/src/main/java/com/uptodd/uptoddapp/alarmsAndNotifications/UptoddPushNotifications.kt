package com.uptodd.uptoddapp.alarmsAndNotifications

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.picasso.Picasso
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.SplashScreenActivity
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.UptoddNotificationUtilities
import com.uptodd.uptoddapp.utilities.UptoddNotify


class UptoddPushNotifications : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val preferences = getSharedPreferences("LOGIN_INFO", MODE_PRIVATE)
        if (preferences.contains("loggedIn") && preferences.getBoolean(
                "loggedIn",
                false
            ) && preferences.contains("userType")
        ) {
            val userType = preferences.getString("userType", "Normal")!!.toLowerCase()
            AllUtil.registerToken(userType)
        }

    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageReceived(remoteMessage)


        //Compulsory to send or else it will crash
        val title = remoteMessage.data["title"]
        val text = remoteMessage.data["text"]
        val notificationId = remoteMessage.data["notificationId"]?.toInt()


        //Optional
        val imageUrl = remoteMessage.data["imgUrl"]
        val activityToBeOpened = remoteMessage.data["activityIntent"]


        val intent = Intent(applicationContext, SplashScreenActivity::class.java)
        if (activityToBeOpened != null) {
            val extras = Bundle()
            extras.putString("activityIntent", activityToBeOpened)
            intent.putExtras(extras)
        }
        val builder = UptoddNotificationUtilities.notificationBuilder(
            applicationContext,
            title ?: "Course content",
            text ?: "View today's program",
            intent,
            "fcm.push_notification"
        )
        if (imageUrl != null) {
            val imageBitmap = Picasso.get()
                .load(imageUrl)
                .get()

            val logoBitmap = BitmapFactory.decodeResource(resources, R.drawable.app_icon)
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(imageBitmap)
                    .bigLargeIcon(logoBitmap)
                    .setBigContentTitle(title)
                    .setSummaryText(text)
            )
        }
        notificationId?.let {
            NotificationManagerCompat.from(applicationContext).UptoddNotify(builder, it)
        }
    }
}