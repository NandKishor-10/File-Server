package com.nandkishor.fileserver.server

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import java.net.Inet4Address

fun getLocalIpAddress(context: Context): String {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return "Unavailable"

    val network = connectivityManager.activeNetwork ?: return "Unavailable"
    val linkProperties: LinkProperties = connectivityManager.getLinkProperties(network) ?: return "Unavailable"

    val ipv4 = linkProperties.linkAddresses
        .map { it.address }
        .firstOrNull { it is Inet4Address }

    return ipv4?.hostAddress ?: "Unavailable"
}