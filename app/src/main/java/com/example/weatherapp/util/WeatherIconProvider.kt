package com.example.weatherapp.util

import com.example.weatherapp.R

object WeatherIconProvider {
    fun getWeatherIcon(iconCode: String?) : Int{
        val iconPrefix = iconCode?.substring(0,2)
        return when(iconPrefix){
            "01" -> R.drawable.ic_sunny
            "02" -> R.drawable.ic_sunnycloudy
            "03" -> R.drawable.ic_cloudy
            "04" -> R.drawable.ic_very_cloudy
            "09" -> R.drawable.ic_rainshower
            "10" -> R.drawable.ic_rainy
            "11" -> R.drawable.ic_thunder
            "13" -> R.drawable.ic_snowy
            "50" -> R.drawable.ic_pressure
            else -> R.drawable.ic_launcher_foreground
        }
    }


    fun getWeatherCardGradient(iconCode: String?): Pair<Int, Int> {
        val iconPrefix = iconCode?.substring(0,2)
        return when(iconPrefix) {
            "01" -> Pair(R.color.white, R.color.sunny_end)  // יום שמשי
            "02" -> Pair(R.color.white, R.color.cloudy_sun_end)  // קצת עננים
            "03" -> Pair(R.color.white, R.color.cloudy_end)  // עננות בינונית
            "04" -> Pair(R.color.white, R.color.very_cloudy_end)  // מאוד מעונן
            "09" -> Pair(R.color.white, R.color.rainshower_end)  // גשם מקומי
            "10" -> Pair(R.color.white, R.color.rainy_end)  // גשם חזק
            "11" -> Pair(R.color.white, R.color.thunder_end)  // סופת רעמים
            "13" -> Pair(R.color.white, R.color.snowy_end)  // שלג
            "50" -> Pair(R.color.white, R.color.mist_end)  // ערפל
            else -> Pair(R.color.white, R.color.default_end)  // ברירת מחדל
        }
    }

}