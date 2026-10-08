package com.intellipaat.course.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class ConnectivityChecker(context: Context) {

    // applicationContext lives as long as the app, so this never leaks an Activity.
    private val connectivityManager =
        context.applicationContext.getSystemService(ConnectivityManager::class.java)

    fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
