package com.sawacorp.displaysharepro

import android.content.res.Resources
import android.os.Build
import io.jsonwebtoken.lang.Strings.capitalize
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException

fun getMyIpV4Ip(): String {
    try {
        val en = NetworkInterface.getNetworkInterfaces()
        while (en.hasMoreElements()) {
            val networkInterface = en.nextElement()
            val enumIpAdr = networkInterface.inetAddresses
            while (enumIpAdr.hasMoreElements()) {
                val inetAddress = enumIpAdr.nextElement()
                if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                    return inetAddress.getHostAddress()?.toString() ?: ""
                }
            }
        }
    } catch (ex: SocketException) {
        println(ex.toString())
    }
    return ""
}

fun deleteSymbolsAfterLastDot(str: String): String {
    val index = str.lastIndexOf(".")
    return str.substring(0, index)
}

fun getMyDeviceName(): String {
    return capitalize(Build.MANUFACTURER) + " " + Build.MODEL
}

fun getScreenWidth(): Int {
    return Resources.getSystem().displayMetrics.widthPixels
}

fun getScreenHeight(): Int {
    return Resources.getSystem().displayMetrics.heightPixels
}