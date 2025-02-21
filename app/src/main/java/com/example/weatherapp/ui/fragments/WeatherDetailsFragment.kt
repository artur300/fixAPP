package com.example.weatherapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.Animations.Click_button_animation
import com.example.weatherapp.R
import com.example.weatherapp.adapters.ForecastAdapter
import com.example.weatherapp.databinding.FragmentWeatherDetailsBinding
import com.example.weatherapp.ui.WeatherViewModel
import com.example.weatherapp.util.Resource
import com.example.weatherapp.util.WeatherIconProvider
import com.example.weatherapp.util.convertUnixToTime
import dagger.hilt.android.AndroidEntryPoint
import il.co.syntax.fullarchitectureretrofithiltkotlin.utils.autoCleared
import java.util.Locale

@AndroidEntryPoint
class WeatherDetailsFragment : Fragment() {

    private var binding: FragmentWeatherDetailsBinding by autoCleared()
    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var forecastAdapter: ForecastAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeatherDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cityName = arguments?.getString("cityName") ?: "N/A"
        val countryName = arguments?.getString("countryName") ?: "N/A"

        // הצגת שם העיר והמדינה כמו שהמשתמש חיפש
        binding.tvCity.text = cityName
        binding.tvCountry.text = Locale("", countryName).displayCountry


        if (cityName != "N/A" && countryName != "N/A") {
            weatherViewModel.getWeatherByCity(cityName, countryName)
        } else {
            Toast.makeText(requireContext(), "City not found", Toast.LENGTH_SHORT).show()
        }

        setupHourlyForecastRecyclerView()
        observeWeatherData()
        observeForecastData()


        binding.btnBack.setOnClickListener { view ->
            Click_button_animation.scaleView(view) {
                when (findNavController().previousBackStackEntry?.destination?.id) {
                    R.id.favoritesFragment -> findNavController().navigate(R.id.action_weatherDetailsFragment_to_favoritesFragment)
                    R.id.citySearchFragment -> findNavController().navigate(R.id.action_weatherDetailsFragment_to_citySearchFragment)
                    else -> findNavController().popBackStack()
                }
            }
        }











    }

    private fun observeWeatherData() {
        weatherViewModel.weatherData.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    resource.data?.let { weather ->
                        with(binding) {
                            tvTemperature.text = "${weather.main.temp}°C"
                            tvFeelsLike.text = "Feels like: ${weather.main.feels_like}°C"
                            tvHumidity.text = "Humidity: ${weather.main.humidity}%"
                            tvWindSpeed.text = "Wind: ${weather.wind.speed} m/s"
                            tvSunrise.text = "Sunrise: ${convertUnixToTime(weather.sys.sunrise, weather.timezone)}"
                            tvSunset.text = "Sunset: ${convertUnixToTime(weather.sys.sunset, weather.timezone)}"
                            tvWeatherDescription.text = weather.weather.firstOrNull()?.description ?: "--"

                            val iconCode = weather.weather.firstOrNull()?.icon
                            ivWeatherIcon.setImageResource(WeatherIconProvider.getWeatherIcon(iconCode))

                            tvMinMax.text = "High: ${weather.main.temp_max}° • Low: ${weather.main.temp_min}°"
                            tvPrecipitation.text = "Precipitation: ${weather.rain?.oneHour ?: 0} mm"
                            tvVisibility.text = "Visibility: ${(weather.visibility ?: 0) / 1000.0} km"
                        }
                        weatherViewModel.getForecast(weather.coord.lat, weather.coord.lon)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), resource.message ?: "Error fetching weather", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeForecastData() {
        weatherViewModel.forecastData.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let {
                        forecastAdapter.differ.submitList(it.list)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Error fetching forecast", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun setupHourlyForecastRecyclerView() {
        forecastAdapter = ForecastAdapter()
        binding.rvForecast.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = forecastAdapter
        }
    }
}


