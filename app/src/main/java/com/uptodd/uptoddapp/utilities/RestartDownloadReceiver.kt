package com.uptodd.uptoddapp.utilities

import android.app.ActivityManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log


class RestartDownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intentN: Intent?) {
        val intent = Intent(context, DownloadService::class.java)

        if(isDownloadServiceRunning(context,DownloadService::class.java)) {
            try {
                context?.startService(intent)
            } catch (ex: IllegalStateException) {

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context?.startForegroundService(intent)
                    } else {
                        context?.startService(intent)
                    }
                } catch (e: Exception) {
                    Log.d("event", "error")
                }
            }
        }
    }

    private fun  isDownloadServiceRunning(context: Context?,serviceClass:Class<out Service>):Boolean {
        val activityManager = context?.getSystemService(Context.ACTIVITY_SERVICE)
                as ActivityManager
        for(service in activityManager.getRunningServices(Int.MAX_VALUE)){
            if(serviceClass.name==service.service.className){
                return true
            }
        }
        return false
    }
}