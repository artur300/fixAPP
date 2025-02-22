package com.example.weatherapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.adapters.FavoriteWeatherAdapter
import com.example.weatherapp.databinding.FragmentFavoritesBinding
import com.example.weatherapp.ui.CitySearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import il.co.syntax.fullarchitectureretrofithiltkotlin.utils.autoCleared
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.Animations.Click_button_animation
import com.example.weatherapp.R

@AndroidEntryPoint
class FavoritesFragment : Fragment() {

    private var binding: FragmentFavoritesBinding by autoCleared()
    private val viewModel: CitySearchViewModel by viewModels()
    private lateinit var favoriteAdapter: FavoriteWeatherAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeFavorites()
        refreshFavoriteWeather() // ✅ טוען נתונים מיד עם הכניסה

//--------------------sort----------------------
        var isAscending = true
        binding.btnSortTemperature.setOnClickListener {
            favoriteAdapter.sortFavoritesByTemperature(isAscending)
            isAscending = !isAscending
        }

        var isCityNameAscending = true
        binding.btnSortCityName.setOnClickListener {
            favoriteAdapter.sortFavoritesByCityName(isCityNameAscending)
            isCityNameAscending = !isCityNameAscending
        }

//--------------------sort----------------------
        binding.btnHome.setOnClickListener {
            Click_button_animation.scaleView(it) {
                findNavController().popBackStack(R.id.weatherLocalFragment, false)
            }
        }
    }

    private fun setupRecyclerView() {
        favoriteAdapter = FavoriteWeatherAdapter(viewModel) { favorite ->
            viewModel.removeWeatherFromFavorites(favorite)
        }

        binding.rvFavorites.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = favoriteAdapter
        }
    }

    private fun observeFavorites() {
        viewModel.favoriteWeatherList.observe(viewLifecycleOwner) { favorites ->
            binding.progressBar.visibility = View.GONE // ✅ הסתרת הטעינה כשהנתונים מוכנים
            binding.rvFavorites.visibility = View.VISIBLE // ✅ הצגת הרשימה

            // ✅ הוספת אנימציה לרשימה אחרי טעינה
            binding.rvFavorites.alpha = 0f
            binding.rvFavorites.animate().alpha(1f).setDuration(500).start()

            favoriteAdapter.submitList(favorites.distinctBy { "${it.cityName}, ${it.country}" })
        }
    }

    private fun refreshFavoriteWeather() {
        binding.progressBar.visibility = View.VISIBLE // ✅ הצגת טעינה
        binding.rvFavorites.visibility = View.GONE // ✅ הסתרת הרשימה כדי להרגיש שינוי

        viewModel.refreshFavoriteWeather { success ->
            binding.progressBar.visibility = View.GONE // ✅ הסתרת טעינה לאחר סיום
            binding.rvFavorites.visibility = View.VISIBLE // ✅ הצגת הרשימה שוב

            if (!success) {
                Toast.makeText(requireContext(), getString(R.string.error_refresh_favorites), Toast.LENGTH_SHORT).show()
            }
        }
    }
}


