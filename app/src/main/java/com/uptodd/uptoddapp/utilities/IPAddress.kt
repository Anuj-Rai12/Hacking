package com.uptodd.uptoddapp.utilities

import android.text.format.Formatter
import android.util.Log
import java.net.NetworkInterface
import java.net.SocketException


class IPAddress
{
    fun getLocalIpAddress(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress) {
                        val ip: String = Formatter.formatIpAddress(inetAddress.hashCode())
                        Log.d("div", "***** IP=$ip")
                        return ip
                    }
                }
            }
        } catch (ex: SocketException) {
            Log.d("div", "IPAddress L27$ex")
        }
        return null
    }
}