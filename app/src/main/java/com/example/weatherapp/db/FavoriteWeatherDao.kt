package com.example.weatherapp.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.weatherapp.models.FavoriteWeather

@Dao
interface FavoriteWeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favoriteWeather: FavoriteWeather)

    @Query("SELECT * FROM favorite_weather")
    fun getAllFavorites(): LiveData<List<FavoriteWeather>>

    @Query("SELECT * FROM favorite_weather")
    suspend fun getAllFavoritesList(): List<FavoriteWeather>

    @Query("UPDATE favorite_weather SET userNote = :note WHERE cityName = :cityName AND country = :country")
    suspend fun updateUserNote(cityName: String, country: String, note: String)

    @Delete
    suspend fun deleteFavorite(favoriteWeather: FavoriteWeather)
}
