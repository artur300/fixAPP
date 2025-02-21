package com.example.weatherapp.ui.fragments

import android.Manifest
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.adapters.ForecastAdapter
import com.example.weatherapp.databinding.FragmentWeatherLocalBinding
import com.example.weatherapp.ui.WeatherViewModel
import com.example.weatherapp.util.Resource
import com.example.weatherapp.util.WeatherIconProvider
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.Animations.Click_button_animation
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.util.LocationHelper
import com.example.weatherapp.util.receivers.NetworkStateReceiver
import il.co.syntax.fullarchitectureretrofithiltkotlin.utils.autoCleared
import java.util.Locale

@AndroidEntryPoint
class WeatherLocalFragment : Fragment() {

    private var binding: FragmentWeatherLocalBinding by autoCleared()

    private val viewModel: WeatherViewModel by viewModels()

    private lateinit var forecastAdatper : ForecastAdapter

//    private val locationPermissionRequest =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted->
//            if(isGranted) viewModel.requestLocation(requireContext())
//            else binding.tvCity.text = "Location Permission denied" //TODO
//        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeatherLocalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecycleView()
        checkLocationPermission()

        viewModel.weatherData.observe(viewLifecycleOwner) { resource ->
            when(resource){
                is Resource.Success ->{
                    hideProgressBar()
                    updateUIWithWeather(resource.data)
                }
                is Resource.Error ->{
                    hideProgressBar()
                }
                is Resource.Loading ->{
                    showProgressBar()
                }
            }

        }

        viewModel.forecastData.observe(viewLifecycleOwner){resource->
            when(resource){
                is Resource.Success ->{
                    hideProgressBar()
                    resource.data?.list?.let { forecastList->
                        forecastAdatper.differ.submitList(forecastList)
                    }
                }
                is Resource.Error ->{
                    hideProgressBar()
                }
                is Resource.Loading ->{
                    showProgressBar()
                }
            }
        }



        var isNavigating = false
        binding.btnSearch.setOnClickListener {
            if (!isNavigating) {
                isNavigating = true
                Click_button_animation.scaleView(it) {
                    findNavController().navigate(R.id.action_weatherLocalFragment_to_citySearchFragment)
                    isNavigating = false
                }
            }
        }

        binding.btnFavorite.setOnClickListener {
            if (!isNavigating) {
                isNavigating = true
                Click_button_animation.scaleView(it) {
                    findNavController().navigate(R.id.action_weatherLocalFragment_to_favoritesFragment)
                    isNavigating = false
                }
            }
        }

        binding.btnSettings.setOnClickListener {
            if (!isNavigating) {
                isNavigating = true
                Click_button_animation.scaleView(it) {
                    findNavController().navigate(R.id.action_weatherLocalFragment_to_weatherSettingsFragment)
                    isNavigating = false
                }
            }
        }



        //---------------------------------------------
    }

    private fun checkLocationPermission() {
        if (LocationHelper.hasLocationPermission(requireContext())) {
            viewModel.requestLocation(requireContext())
//        } else {
//            LocationHelper.requestLocationPermission(locationPermissionRequest)
        }
    }

    private fun hideProgressBar(){
        binding.paginationProgressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar(){
        binding.paginationProgressBar.visibility = View.VISIBLE
    }

    private fun setUpRecycleView(){
        forecastAdatper = ForecastAdapter()
        binding.recycleView.apply {
            adapter = forecastAdatper
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun updateUIWithWeather(weather: WeatherResponse?) {
        weather?.let {
            val iconCode = it.weather?.get(0)?.icon
            val iconResult = WeatherIconProvider.getWeatherIcon(iconCode)


            // קבלת צבעים מתאימים לפי מזג האוויר
            val (startColor, endColor) = WeatherIconProvider.getWeatherCardGradient(iconCode)


            with(binding) {
                tvCity.text = it.name ?: getString(R.string.city_n_a)
                tvTemperature.text = getString(R.string.weather_temp, (it.main?.temp ?: 0.0).toFloat())
                tvDescriptionWeather.text = it.weather?.get(0)?.description ?: getString(R.string.weather_description_n_a)
                tvMinTemperature.text = getString(R.string.weather_min_temp, (it.main?.temp_min ?: 0.0).toFloat())
                tvMaxTemperature.text = getString(R.string.weather_max_temp, (it.main?.temp_max ?: 0.0).toFloat())
                tvFeelsLike.text = getString(R.string.weather_feels_like, (it.main?.feels_like ?: 0.0).toFloat())
                tvWind.text = getString(R.string.weather_wind_speed, (it.wind?.speed ?: 0.0).toFloat())
                ivIconWeather.setImageResource(iconResult)




                // שינוי רקע הכרטיסייה
                val gradientDrawable = android.graphics.drawable.GradientDrawable(
                    android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        ContextCompat.getColor(requireContext(), startColor),
                        ContextCompat.getColor(requireContext(), endColor)
                    )

                )
                gradientDrawable.cornerRadius = 50f  // עיגול פינות
                binding.cardView.background = gradientDrawable

                cardView.background = gradientDrawable


            }
        }
    }


}