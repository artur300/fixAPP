package com.example.weatherapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.weatherapp.databinding.FragmentWeatherSettingsBinding
import com.example.weatherapp.ui.WeatherViewModel
import com.example.weatherapp.util.LocationHelper
import dagger.hilt.android.AndroidEntryPoint
import il.co.syntax.fullarchitectureretrofithiltkotlin.utils.autoCleared

@AndroidEntryPoint
class WeatherSettingsFragment : Fragment() {
    private var binding: FragmentWeatherSettingsBinding by autoCleared()

    private val viewModel: WeatherViewModel by viewModels()

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) viewModel.requestLocation(requireContext())
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWeatherSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.switchGps.setOnClickListener {
            if(LocationHelper.hasLocationPermission(requireContext())){
                viewModel.requestLocation(requireContext())
            }else{
                LocationHelper.requestLocationPermission(locationPermissionRequest)
            }
        }
    }
}
