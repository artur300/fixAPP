package com.example.weatherapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.R
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.models.CityResponse
import com.example.weatherapp.models.FavoriteWeather
import com.example.weatherapp.repository.WeatherRepository
import com.example.weatherapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import android.util.Log
import java.util.Locale
import android.widget.Toast
import javax.inject.Inject

@HiltViewModel
class CitySearchViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val app: Application
) : AndroidViewModel(app) {


    //  משתנים לניהול הנתונים במסך חיפוש עיר
    val weatherData = MutableLiveData<Resource<WeatherResponse>>() // נתוני מזג אוויר
    val cityList = MutableLiveData<List<CityResponse>>() // רשימת הערים שמתאימות לחיפוש
    val isRefreshing = MutableLiveData<Boolean>() // אינדיקציה האם מתבצע רענון נתונים
    val selectedCityName = MutableLiveData<String?>() // שם העיר שנבחרה
    val selectedCountryCode = MutableLiveData<String?>() // קוד המדינה שנבחרה

    /**
     *   חיפוש מזג האוויר לעיר מסוימת
     */
    fun getWeatherByCity(city: String, country: String) {
        weatherData.value = Resource.Loading()
        selectedCityName.value = city
        selectedCountryCode.value = country

        val lang = if (Locale.getDefault().language == "iw") "he" else Locale.getDefault().language

        viewModelScope.launch {
            try {
                Log.d("WeatherAPI", "Requesting weather data with lang: $lang for city: $city, country: $country")

                val response = repository.getCoordinates(city, country)
                if (response.isSuccessful) {
                    val firstCity = response.body()?.firstOrNull()
                    firstCity?.let {
                        val weatherResponse = repository.getWeatherData(it.lat, it.lon, "metric", lang)
                        handleWeatherResponse(weatherResponse)
                    } ?: weatherData.postValue(Resource.Error(app.getString(R.string.error_city_not_found)))
                } else {
                    weatherData.postValue(Resource.Error(app.getString(R.string.error_fetch_coordinates)))
                }
            } catch (e: Exception) {
                weatherData.postValue(Resource.Error(app.getString(R.string.error_fetch_weather)))
                Log.e("WeatherAPI", "Error fetching weather data", e)
            }
        }
    }


    /**
     *  חיפוש רשימת ערים מתאימות לפי שם העיר שהמשתמש מקליד
     */
    fun searchCities(query: String) {
        if (query.length < 2) return

        viewModelScope.launch {
            try {
                val response = repository.getCitiesFromAPI(query)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    cityList.postValue(response.body()) // עדכון רשימת הערים בתוצאות
                } else {
                    cityList.postValue(emptyList()) // אם אין תוצאות, מחזירים רשימה ריקה
                }
            } catch (e: Exception) {
                cityList.postValue(emptyList())
                Log.e("CitySearchViewModel", "Error searching cities", e)
            }
        }
    }

    /**
     *  טיפול בתגובה ממזג האוויר ועדכון הנתונים
     */
    private fun handleWeatherResponse(response: retrofit2.Response<WeatherResponse>) {
        if (response.isSuccessful) {
            response.body()?.let { weather ->
                val updatedWeather = weather.copy(name = selectedCityName.value ?: weather.name)
                weatherData.postValue(Resource.Success(updatedWeather))
            } ?: weatherData.postValue(Resource.Error(app.getString(R.string.error_no_data)))
        } else {
            weatherData.postValue(Resource.Error(app.getString(R.string.error_fetch_weather)))
        }
    }



    // =====================  ניהול רשימת מועדפים  =====================

    //  רשימת המועדפים מתוך מסד הנתונים
    val favoriteWeatherList = repository.getFavoriteWeather()

    /**
     *  הוספת עיר לרשימת המועדפים
     */
    fun saveWeatherToFavorites(weather: WeatherResponse) {
        viewModelScope.launch {
            try {
                val existingFavorites = repository.getFavoriteWeatherList()

                // בדיקה אם העיר כבר קיימת במועדפים
                val isAlreadySaved = existingFavorites.any {
                    it.cityName == weather.name && it.country == weather.sys.country
                }

                if (isAlreadySaved) {
                    Toast.makeText(app, app.getString(R.string.favorite_already_exists, weather.name), Toast.LENGTH_SHORT).show()
                } else {
                    val favoriteWeather = FavoriteWeather(
                        cityName = weather.name,
                        country = weather.sys.country,
                        temperature = weather.main.temp,
                        description = weather.weather[0].description,
                        minTemp = weather.main.temp_min,
                        maxTemp = weather.main.temp_max,
                        feelsLike = weather.main.feels_like,
                        windSpeed = weather.wind.speed,
                        humidity = weather.main.humidity,
                        sunrise = weather.sys.sunrise,
                        sunset = weather.sys.sunset,
                        timezone = weather.timezone,
                        iconCode = weather.weather[0].icon,
                        lat = weather.coord.lat,
                        lon = weather.coord.lon
                    )
                    repository.insertFavoriteWeather(favoriteWeather)
                    Toast.makeText(app, app.getString(R.string.favorite_added, weather.name), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(app, app.getString(R.string.error_fetch_weather), Toast.LENGTH_SHORT).show()
                Log.e("CitySearchViewModel", "Error saving favorite weather", e)
            }
        }
    }

    /**
     *  מחיקת עיר מרשימת המועדפים
     */
    fun removeWeatherFromFavorites(favorite: FavoriteWeather) {
        viewModelScope.launch {
            repository.deleteFavoriteWeather(favorite)
        }
    }

    fun updateFavoriteNote(city: String, country: String, note: String) {
        viewModelScope.launch {
            repository.updateUserNote(city, country, note)
        }
    }

    /**
     *  עדכון רשימת המועדפים עם הנתונים החדשים מה-API
     */
    fun refreshFavoriteWeather(callback: (Boolean) -> Unit) {
        isRefreshing.postValue(true)
        val lang = if (Locale.getDefault().language == "iw") "he" else Locale.getDefault().language

        viewModelScope.launch {
            try {
                val updatedFavorites = favoriteWeatherList.value?.map { favorite ->
                    val response = repository.getWeatherData(favorite.lat, favorite.lon, "metric",lang)

                    if (response.isSuccessful) {
                        response.body()?.let { weather ->
                            favorite.copy(
                                temperature = weather.main.temp,
                                description = weather.weather[0].description,
                                minTemp = weather.main.temp_min,
                                maxTemp = weather.main.temp_max,
                                feelsLike = weather.main.feels_like,
                                windSpeed = weather.wind.speed,
                                humidity = weather.main.humidity,
                                sunrise = weather.sys.sunrise,
                                sunset = weather.sys.sunset,
                                timezone = weather.timezone,
                                iconCode = weather.weather[0].icon // ✅ הוספת עדכון לאייקון
                            )
                        } ?: favorite
                    } else {
                        favorite
                    }
                } ?: emptyList()

                repository.updateFavoriteWeather(updatedFavorites)
                isRefreshing.postValue(false)
                callback(true)
            } catch (e: Exception) {
                isRefreshing.postValue(false)
                callback(false)
            }
        }
    }


}

