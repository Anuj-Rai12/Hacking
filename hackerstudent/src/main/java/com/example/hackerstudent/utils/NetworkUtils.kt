package com.example.hackerstudent.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.example.hackerstudent.ClientApplication
import javax.inject.Inject

class NetworkUtils @Inject constructor() {

    fun isConnected(): Boolean {

        val manager: ConnectivityManager = ClientApplication.appContext().get()
            ?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val infoMobileData = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        val infoWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)

        var connMobileData = false
        var connWifi = false

        if (infoMobileData != null)
            connMobileData = infoMobileData.state == NetworkInfo.State.CONNECTED

        if (infoWifi != null)
            connWifi = infoWifi.state == NetworkInfo.State.CONNECTED

        return connMobileData || connWifi
    }
}