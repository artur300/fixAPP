package com.example.weatherapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.Animations.Click_button_animation
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ItemFavoriteWeatherBinding
import com.example.weatherapp.models.FavoriteWeather
import com.example.weatherapp.ui.CitySearchViewModel
import com.example.weatherapp.util.WeatherIconProvider
import com.example.weatherapp.util.convertUnixToTime
import androidx.navigation.findNavController
import android.os.Bundle
import java.util.Locale

class FavoriteWeatherAdapter(
    private val viewModel: CitySearchViewModel,
    private val onDeleteClick: (FavoriteWeather) -> Unit
) : RecyclerView.Adapter<FavoriteWeatherAdapter.FavoriteViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<FavoriteWeather>() {
        override fun areItemsTheSame(oldItem: FavoriteWeather, newItem: FavoriteWeather): Boolean {
            return oldItem.cityName == newItem.cityName && oldItem.country == newItem.country
        }

        override fun areContentsTheSame(oldItem: FavoriteWeather, newItem: FavoriteWeather): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, differCallback)

    inner class FavoriteViewHolder(private val binding: ItemFavoriteWeatherBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(favorite: FavoriteWeather) {
            val countryName = Locale("", favorite.country).displayCountry

            with(binding) {
                tvCityAndCountry.text = "${favorite.cityName}, $countryName"
                tvTemperature.text = itemView.context.getString(R.string.label_temp, favorite.temperature ?: "--")
                tvDescription.text = itemView.context.getString(R.string.label_weather_description, favorite.description ?: "--")
                tvMinTemperature.text = itemView.context.getString(R.string.label_temp_min, favorite.minTemp ?: "--")
                tvMaxTemperature.text = itemView.context.getString(R.string.label_temp_max, favorite.maxTemp ?: "--")
                tvFeelsLike.text = itemView.context.getString(R.string.label_feels_like, favorite.feelsLike ?: "--")
                tvHumidity.text = itemView.context.getString(R.string.label_humidity, favorite.humidity ?: "--")
                tvWindSpeed.text = itemView.context.getString(R.string.label_wind, favorite.windSpeed ?: "--")
                tvSunrise.text = itemView.context.getString(R.string.label_sunrise, convertUnixToTime(favorite.sunrise, favorite.timezone))
                tvSunset.text = itemView.context.getString(R.string.label_sunset, convertUnixToTime(favorite.sunset, favorite.timezone))

                val iconRes = WeatherIconProvider.getWeatherIcon(favorite.iconCode)
                ivWeatherIcon.setImageResource(iconRes)

                val (startColor, endColor) = WeatherIconProvider.getWeatherCardGradient(favorite.iconCode)
                val gradientDrawable = android.graphics.drawable.GradientDrawable(
                    android.graphics.drawable.GradientDrawable.Orientation.BL_TR,
                    intArrayOf(
                        itemView.context.getColor(startColor),
                        itemView.context.getColor(endColor)
                    )
                )
                gradientDrawable.cornerRadius = 25f
                XmlWeatherListCard.background = gradientDrawable

                etUserNote.setText(favorite.userNote ?: "")
                etUserNote.isEnabled = false

                btnEditNote.setOnClickListener { view ->
                    Click_button_animation.scaleView(view) {
                        toggleEditMode()
                    }
                }

                btnSaveNote.setOnClickListener { view ->
                    Click_button_animation.scaleView(view) {
                        val newNote = etUserNote.text.toString()
                        viewModel.updateFavoriteNote(favorite.cityName, favorite.country, newNote)

                        etUserNote.isEnabled = false
                        btnEditNote.setColorFilter(itemView.context.getColor(R.color.black))
                        Toast.makeText(itemView.context, itemView.context.getString(R.string.note_saved), Toast.LENGTH_SHORT).show()
                    }
                }

                ivDeleteFavorite.setOnClickListener { view ->
                    Click_button_animation.scaleView(view) {
                        onDeleteClick(favorite)
                        deleteItem(favorite)
                    }
                }

                var isNavigating = false
                binding.btnDetails.setOnClickListener { view ->
                    if (!isNavigating) {
                        isNavigating = true
                        Click_button_animation.scaleView(view) {
                            val bundle = Bundle().apply {
                                putString("cityName", favorite.cityName)
                                putString("countryName", Locale("", favorite.country).displayCountry)
                            }
                            itemView.findNavController().navigate(R.id.action_favoritesFragment_to_weatherDetailsFragment, bundle)
                            isNavigating = false
                        }
                    }
                }
            }
        }

        private fun toggleEditMode() {
            val isEditable = binding.etUserNote.isEnabled
            binding.etUserNote.isEnabled = !isEditable

            val newColor = if (!isEditable) R.color.bright_blue else R.color.black
            binding.btnEditNote.setColorFilter(itemView.context.getColor(newColor))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteWeatherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount() = differ.currentList.size

    fun submitList(newList: List<FavoriteWeather>) {
        differ.submitList(newList)
    }

    fun deleteItem(favorite: FavoriteWeather) {
        val updatedList = differ.currentList.toMutableList()
        updatedList.remove(favorite)
        submitList(updatedList)
    }

    fun sortFavoritesByTemperature(ascending: Boolean) {
        val sortedList = if (ascending) {
            differ.currentList.sortedBy { it.temperature ?: Double.MIN_VALUE }
        } else {
            differ.currentList.sortedByDescending { it.temperature ?: Double.MIN_VALUE }
        }
        submitList(sortedList)
    }

    fun sortFavoritesByCityName(ascending: Boolean) {
        val sortedList = if (ascending) {
            differ.currentList.sortedBy { it.cityName.lowercase() }
        } else {
            differ.currentList.sortedByDescending { it.cityName.lowercase() }
        }
        submitList(sortedList)
    }
}


