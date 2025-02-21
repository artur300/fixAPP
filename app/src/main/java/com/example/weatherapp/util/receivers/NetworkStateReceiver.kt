package com.example.weatherapp.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class NetworkStateReceiver(private val listener: NetworkConnectionListener) : BroadcastReceiver() {

    interface NetworkConnectionListener {
        fun onConnect()
        fun onDisconnect()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            if (isNetworkAvailable(it)) {
                listener.onConnect()
            } else {
                listener.onDisconnect()
            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }

    fun subscribeWith(context: Context) {
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(this, filter)
    }

    fun unsubscribeWith(context: Context) {
        try {
            context.unregisterReceiver(this)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }
}

