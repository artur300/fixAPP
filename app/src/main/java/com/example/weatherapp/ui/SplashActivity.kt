package com.example.weatherapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ActivitySplashBinding
import com.example.weatherapp.util.LocationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var binding: ActivitySplashBinding

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted ->
            if(isGranted){
                viewModel.requestLocation(applicationContext)
                observeLoadingState()
            }else{
                val intent = Intent(this, WeatherActivity::class.java)
                intent.putExtra("open_settings", true)
                startActivity(intent)
                finish()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.splashAnimation.setAnimation(R.raw.splash)
        binding.splashAnimation.playAnimation()

       checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if(LocationHelper.hasLocationPermission(this)){
            viewModel.requestLocation(applicationContext)
            observeLoadingState()
        }else{
            LocationHelper.requestLocationPermission(locationPermissionRequest)
        }
    }

    private fun observeLoadingState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    if (!isLoading) {
                        binding.splashAnimation.cancelAnimation() // Stop animation before moving
                        startActivity(Intent(this@SplashActivity, WeatherActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }
}