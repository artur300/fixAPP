package com.example.weatherapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.Animations.Click_button_animation
import com.example.weatherapp.R
import com.example.weatherapp.adapters.FavoriteWeatherAdapter
import com.example.weatherapp.databinding.FragmentFavoritesBinding
import com.example.weatherapp.ui.CitySearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import il.co.syntax.fullarchitectureretrofithiltkotlin.utils.autoCleared
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        showToast(getString(R.string.loading_favorites))
        binding.progressBar.visibility = View.VISIBLE
        binding.rvFavorites.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            delay(300)
            refreshFavoriteWeather()
        }



        //-------------------- מיון נתונים ----------------------
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
        //-------------------------------------------------------

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

            if (favorites.isEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
                binding.rvFavorites.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.rvFavorites.visibility = View.VISIBLE

                //  אנימציה חלקה להצגת הרשימה
                binding.rvFavorites.alpha = 0f
                binding.rvFavorites.animate().alpha(1f).setDuration(500).start()

                favoriteAdapter.submitList(favorites.distinctBy { "${it.cityName}, ${it.country}" })
            }
        }
    }

    private fun refreshFavoriteWeather() {

        binding.progressBar.visibility = View.VISIBLE
        binding.rvFavorites.visibility = View.GONE

        viewModel.viewModelScope.launch {
            delay(800) //  מבטיח שה-progressBar יופיע לפחות 800ms

            viewModel.refreshFavoriteWeather { success ->
                binding.progressBar.visibility = View.GONE
                binding.rvFavorites.visibility = View.VISIBLE

                if (!success) {
                    Toast.makeText(requireContext(), getString(R.string.error_refresh_favorites), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}

