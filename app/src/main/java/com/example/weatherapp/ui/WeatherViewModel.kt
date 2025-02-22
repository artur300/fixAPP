package com.example.weatherapp.ui

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.models.forecast.ForecastResponse
import com.example.weatherapp.repository.WeatherRepository
import com.example.weatherapp.util.Resource
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    application: Application
) : AndroidViewModel(application) {





    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    val weatherData: MutableLiveData<Resource<WeatherResponse>> = MutableLiveData()
    val forecastData: MutableLiveData<Resource<ForecastResponse>> = MutableLiveData()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private var lastKnownLocation: Pair<Double, Double>? = null

    init {
        viewModelScope.launch {
            //delay(2000) // Simulate initial loading
            requestLocation(application.applicationContext) // Fetch location & weather
        }
    }

    fun requestLocation(context: Context) {
        lastKnownLocation?.let { (lat, lon) ->
            getWeather(lat, lon)
            getForecast(lat, lon)
            return
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            weatherData.postValue(Resource.Error("Location permission not granted"))
            forecastData.postValue(Resource.Error("Location permission not granted"))
            viewModelScope.launch { _isLoading.emit(false) }
            return
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                location?.let {
                    lastKnownLocation = it.latitude to it.longitude
                    getWeather(it.latitude, it.longitude)
                    getForecast(it.latitude, it.longitude)
                } ?: run {
                    weatherData.postValue(Resource.Error("Location unavailable"))
                    forecastData.postValue(Resource.Error("Location unavailable"))
                    viewModelScope.launch { _isLoading.emit(false) }
                }
            }
            .addOnFailureListener { exception ->
                weatherData.postValue(Resource.Error("Failed to get location: ${exception.message}"))
                forecastData.postValue(Resource.Error("Failed to get location: ${exception.message}"))
                viewModelScope.launch { _isLoading.emit(false) }
            }
    }

    fun getWeather(lat: Double, lon: Double, unit: String = "metric") {
        weatherData.postValue(Resource.Loading())
        val lang = if (Locale.getDefault().language == "iw") "he" else Locale.getDefault().language
        viewModelScope.launch {
            try {
                val response = repository.getWeatherData(lat, lon, unit,lang)
                if (response.isSuccessful) {
                    response.body()?.let {
                        weatherData.postValue(Resource.Success(it))
                        _isLoading.value = false // Stop loading after data is loaded
                    } ?: run {
                        weatherData.postValue(Resource.Error("No data found"))
                        _isLoading.value = false
                    }
                } else {
                    weatherData.postValue(Resource.Error(response.message()))
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                weatherData.postValue(Resource.Error("Error fetching weather"))
                _isLoading.value = false
            }
        }
    }

    fun getForecast(lat: Double, lon: Double, unit: String = "metric") {
        val lang = if (Locale.getDefault().language == "iw") "he" else Locale.getDefault().language
        forecastData.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = repository.getForecastData(lat, lon, unit,lang)
                if (response.isSuccessful) {
                    response.body()?.let {
                        forecastData.postValue(Resource.Success(it))
                    } ?: run {
                        forecastData.postValue(Resource.Error("No forecast data found"))
                    }
                } else {
                    forecastData.postValue(Resource.Error(response.message()))
                }
            } catch (e: Exception) {
                forecastData.postValue(Resource.Error("Error fetching forecast"))
            }
        }
    }


    // בשביל להציג דף של ירין
    fun getWeatherByCity(city: String, country: String) {
        weatherData.postValue(Resource.Loading())
        val lang = if (Locale.getDefault().language == "iw") "he" else Locale.getDefault().language

        viewModelScope.launch {
            try {
                val response = repository.getCoordinates(city, country)
                if (response.isSuccessful) {
                    val firstCity = response.body()?.firstOrNull()
                    firstCity?.let {
                        val weatherResponse = repository.getWeatherData(it.lat, it.lon, "metric", lang)
                        if (weatherResponse.isSuccessful) {
                            weatherResponse.body()?.let {
                                weatherData.postValue(Resource.Success(it))
                            } ?: weatherData.postValue(Resource.Error("No data found"))
                        } else {
                            weatherData.postValue(Resource.Error(weatherResponse.message()))
                        }
                    } ?: weatherData.postValue(Resource.Error("City not found"))
                } else {
                    weatherData.postValue(Resource.Error("Failed to fetch city coordinates"))
                }
            } catch (e: Exception) {
                weatherData.postValue(Resource.Error("Error fetching weather for city"))
            }
        }
    }

}