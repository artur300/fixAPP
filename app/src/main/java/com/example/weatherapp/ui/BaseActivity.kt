package com.example.weatherapp.ui

import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.R
import com.example.weatherapp.util.receivers.NetworkStateReceiver

abstract class BaseActivity : AppCompatActivity(), NetworkStateReceiver.NetworkConnectionListener {

    private lateinit var networkStateReceiver: NetworkStateReceiver

    private var noInternetCard: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        networkStateReceiver = NetworkStateReceiver(this)
    }

    override fun onStart() {
        super.onStart()
        networkStateReceiver.subscribeWith(this)
    }

    override fun onStop() {
        super.onStop()
        networkStateReceiver.unsubscribeWith(this)
    }

    override fun onResume() {
        super.onResume()
        noInternetCard = findViewById(R.id.noInternetCard)
        noInternetCard?.visibility = View.GONE
    }

    override fun onConnect() {
        noInternetCard?.let { hideNoInternetMessage() }
    }

    override fun onDisconnect() {
        noInternetCard?.let { showNoInternetMessage() }
    }

    private fun showNoInternetMessage() {
        if (noInternetCard?.visibility == View.GONE) {
            noInternetCard?.visibility = View.VISIBLE
            noInternetCard?.startAnimation(AlphaAnimation(0f, 1f).apply { duration = 500 })
        }
    }

    private fun hideNoInternetMessage() {
        if (noInternetCard?.visibility == View.VISIBLE) {
            noInternetCard?.startAnimation(AlphaAnimation(1f, 0f).apply { duration = 500 })
            noInternetCard?.visibility = View.GONE
        }
    }
}