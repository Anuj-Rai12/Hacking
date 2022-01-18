package com.uptodd.uptoddapp.utilities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RestartDownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.startService(Intent(context,DownloadService::class.java))
    }
}