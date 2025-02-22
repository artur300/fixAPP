package com.example.weatherapp.ui.fragments

import androidx.navigation.fragment.findNavController
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import com.bumptech.glide.Glide
import com.example.weatherapp.Animations.Click_button_animation
import com.example.weatherapp.R
import com.example.weatherapp.adapters.CityAutoCompleteAdapter
import com.example.weatherapp.databinding.FragmentCitySearchBinding
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.ui.CitySearchViewModel
import com.example.weatherapp.util.Resource
import com.example.weatherapp.util.WeatherIconProvider
import com.example.weatherapp.util.convertUnixToTime
import dagger.hilt.android.AndroidEntryPoint
import il.co.syntax.fullarchitectureretrofithiltkotlin.utils.autoCleared
import java.util.Locale

@AndroidEntryPoint
class CitySearchFragment : Fragment() {

    //  מחזיק את הביינדינג לעבודה עם ה-UI
    private var binding: FragmentCitySearchBinding by autoCleared()

    //  ViewModel שמנהל את הנתונים של החיפוש
    private val viewModel: CitySearchViewModel by viewModels()

    //  מתאם לרשימת ההשלמות האוטומטיות
    private lateinit var cityAdapter: CityAutoCompleteAdapter

    //  יצירת ה-View עם ה-Binding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCitySearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    //  אתחול המסך לאחר יצירתו
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()          // אתחול מתאם להשלמות אוטומטיות
        observeWeatherData()   // האזנה לשינויים במזג האוויר
        observeCityList()      // האזנה לשינויים ברשימת הערים

        setupSearchListener()  // מאזין לחיפוש עיר
        setupFavoriteButton()  // מאזין לכפתור הוספה למועדפים
        setupSearchButton()    // מאזין לכפתור החיפוש


        binding.btnHome.setOnClickListener {
            Click_button_animation.scaleView(it) {
                findNavController().popBackStack(R.id.weatherLocalFragment, false)
            }
        }

//------------------go to more details screen-------------
        binding.btnDetails.setOnClickListener {
            val city = viewModel.selectedCityName.value
            val country = viewModel.selectedCountryCode.value

            if (!city.isNullOrEmpty() && !country.isNullOrEmpty()) {
                val bundle = Bundle().apply {
                    putString("cityName", city)
                    putString("countryName", country)
                }
                findNavController().navigate(R.id.action_citySearchFragment_to_weatherDetailsFragment, bundle)
            } else {
                Toast.makeText(requireContext(), getString(R.string.enter_city_name), Toast.LENGTH_SHORT).show()
            }
        }
//------------------go to more details screen-------------

    }

    //  אתחול המתאם להשלמות אוטומטיות
    private fun initAdapter() {
        cityAdapter = CityAutoCompleteAdapter(requireContext(), emptyList())
        binding.etCityName.setAdapter(cityAdapter)
    }

    //  מעקב אחר רשימת הערים (לשדה החיפוש)
    private fun observeCityList() {
        viewModel.cityList.observe(viewLifecycleOwner) { cities ->
            cityAdapter.updateCities(cities)
        }
    }

    //  מעקב אחר נתוני מזג האוויר
    private fun observeWeatherData() {
        viewModel.weatherData.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> showWeatherData(resource.data)
                is Resource.Error -> showError(resource.message)
                is Resource.Loading -> showLoading()
            }
        }
    }

    //  עדכון מסך עם נתוני מזג האוויר שהתקבלו
    private fun showWeatherData(weather: WeatherResponse?) {
        hideLoading()

        with(binding) {
            tvCity.text = weather?.name ?: getString(R.string.city_n_a)
            tvCountry.text = Locale("", weather?.sys?.country ?: "").displayCountry
            tvTemperature.text = getString(R.string.label_temp, weather?.main?.temp ?: "--")
            tvFeelsLike.text = getString(R.string.label_feels_like, weather?.main?.feels_like ?: "--")
            tvHumidity.text = getString(R.string.label_humidity, weather?.main?.humidity ?: "--")
            tvWindSpeed.text = getString(R.string.label_wind, weather?.wind?.speed ?: "--")
            tvSunrise.text = getString(R.string.label_sunrise, convertUnixToTime(weather?.sys?.sunrise, weather?.timezone))
            tvSunset.text = getString(R.string.label_sunset, convertUnixToTime(weather?.sys?.sunset, weather?.timezone))
            tvWeatherDescription.text = getString(R.string.label_weather_description, weather?.weather?.firstOrNull()?.description ?: "--")

            val iconCode = weather?.weather?.firstOrNull()?.icon
            val iconRes = WeatherIconProvider.getWeatherIcon(iconCode)

            //----IMAGE------
            Glide.with(requireContext())
                .load(iconRes)
                .into(binding.ivWeatherIcon)
            //----IMAGE------


        }
    }

    //  הצגת הודעת שגיאה
    private fun showError(message: String?) {
        hideLoading()
        Toast.makeText(requireContext(), message ?: getString(R.string.error_fetch_weather), Toast.LENGTH_SHORT).show()
    }

    //  הצגת אנימציית טעינה
    private fun showLoading() {
        binding.tvCity.text = getString(R.string.loading)
        binding.progressBar.visibility = View.VISIBLE
    }

    //  הסתרת אנימציית טעינה
    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    //  האזנה לעדכונים בשדה החיפוש (לשאילתות בזמן אמת)
    private fun setupSearchListener() {
        binding.etCityName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    viewModel.searchCities(s.toString()) // חיפוש בזמן אמת
                }
            }
        })

        // בחירת עיר מתוך רשימת ההשלמות האוטומטיות
        binding.etCityName.setOnItemClickListener { _, _, position, _ ->
            val selectedCity = cityAdapter.getFullCityItem(position)
            val countryName = Locale("", selectedCity.country).displayCountry
            val formattedCity = "${selectedCity.name}, $countryName"

            binding.etCityName.setText(formattedCity)
            viewModel.selectedCityName.value = selectedCity.name
            viewModel.selectedCountryCode.value = selectedCity.country
        }
    }

    //  האזנה ללחיצה על כפתור חיפוש
    private fun setupSearchButton() {
        binding.btnSearch.setOnClickListener {
            val city = viewModel.selectedCityName.value
            val country = viewModel.selectedCountryCode.value

            if (!city.isNullOrEmpty() && !country.isNullOrEmpty()) {
                viewModel.getWeatherByCity(city, country) // חיפוש לפי עיר ומדינה
            } else {
                Toast.makeText(requireContext(), getString(R.string.enter_city_name), Toast.LENGTH_SHORT).show()
            }
        }
    }

    //  האזנה ללחיצה על כפתור הוספה למועדפים
    private fun setupFavoriteButton() {
        binding.ivHeartIcon.setOnClickListener { view ->
            Click_button_animation.scaleView(view) {
                val weather = viewModel.weatherData.value?.data
                if (weather != null) {
                    viewModel.saveWeatherToFavorites(weather)
                }
            }
        }
    }

}

