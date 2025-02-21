package com.example.weatherapp.ui

import androidx.activity.enableEdgeToEdge
import android.os.Bundle
import androidx.navigation.findNavController
import com.example.weatherapp.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeatherActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_weather)
    }

    override fun onResume() {
        super.onResume()

        if (intent.getBooleanExtra("open_settings", false)) {
            val navController = findNavController(R.id.nav_host_fragment)
            navController.navigate(R.id.weatherSettingsFragment)
        }
    }
}
