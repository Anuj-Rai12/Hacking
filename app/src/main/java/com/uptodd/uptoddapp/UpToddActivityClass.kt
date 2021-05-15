package com.uptodd.uptoddapp

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.androidnetworking.AndroidNetworking
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.UPTODD_MEDIA_PLAYER_NOTIFICATION
import com.uptodd.uptoddapp.utilities.cancelUptoddNotification
import okhttp3.OkHttpClient
import java.util.*
import java.util.concurrent.TimeUnit

class UpToddActivityClass : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(UpToddLifecycleCallbacks())

        // initialize essential classes before activity starts
        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.initialize(applicationContext, okHttpClient)

        UptoddSharedPreferences.getInstance(applicationContext)

        AllUtil.loadPreferences(applicationContext,"LOGIN_INFO")
    }
}

class UpToddLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
        //to change language
        val sharedPreferences = activity.getSharedPreferences("language", Context.MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("lang", "en")!!
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources: Resources = activity.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        Log.i("language", "changing")
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
//        UptoddNotificationUtilities.dismiss(activity, UPTODD_MEDIA_PLAYER_NOTIFICATION)
        NotificationManagerCompat.from(activity)
            .cancelUptoddNotification(UPTODD_MEDIA_PLAYER_NOTIFICATION)
    }

}