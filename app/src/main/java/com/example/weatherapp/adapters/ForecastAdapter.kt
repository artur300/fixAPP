package com.example.weatherapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ForecastViewholderBinding
import com.example.weatherapp.models.forecast.ForecastResponse
import com.example.weatherapp.util.WeatherIconProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ForecastAdapter : RecyclerView.Adapter<ForecastAdapter.WeatherViewHolder>(){

    inner class WeatherViewHolder(val binding : ForecastViewholderBinding) : RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<ForecastResponse.data>() {
        override fun areItemsTheSame(oldItem: ForecastResponse.data, newItem: ForecastResponse.data): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ForecastResponse.data,
            newItem: ForecastResponse.data
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val binding =
            ForecastViewholderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WeatherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val weatherData = differ.currentList[position]

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = dateFormat.parse(weatherData.dtTxt)

        val calendar = Calendar.getInstance()
        date?.let { calendar.time = it }

        val days = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val dayOfWeek = days[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val iconCode = weatherData.weather[0].icon
        val iconResult = WeatherIconProvider.getWeatherIcon(iconCode)

        holder.binding.apply {
            tvDay.text = dayOfWeek
            tvHour.text = "${hour}:00"
            tvTemperature.text = String.format(Locale.US, "%.0f°C", weatherData.main.temp)
            imgWeatherIcon.setImageResource(iconResult)
        }
    }

    override fun getItemCount() = differ.currentList.size
}