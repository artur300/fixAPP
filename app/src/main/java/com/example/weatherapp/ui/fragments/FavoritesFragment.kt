package com.example.weatherapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    //  מחזיק את הביינדינג לעבודה עם ה-UI
    private var binding: FragmentFavoritesBinding by autoCleared()

    //  ViewModel שמנהל את נתוני המועדפים
    private val viewModel: CitySearchViewModel by viewModels()

    //  מתאם (Adapter) לרשימת הערים המועדפות
    private lateinit var favoriteAdapter: FavoriteWeatherAdapter

    //  יצירת ה-View עם ה-Binding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    //  אתחול המסך לאחר יצירתו
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()  // אתחול רשימת המועדפים
        observeFavorites()   // האזנה לשינויים ברשימת המועדפים

        binding.btnRefreshFavorites.setOnClickListener {
            refreshFavoriteWeather()  // רענון רשימת המועדפים בלחיצה
        }




        binding.btnHome.setOnClickListener {
            Click_button_animation.scaleView(it) {
                findNavController().popBackStack(R.id.weatherLocalFragment, false)
            }
        }

    }

    //  אתחול RecyclerView להצגת המועדפים
    private fun setupRecyclerView() {
        favoriteAdapter = FavoriteWeatherAdapter(viewModel) { favorite ->
            viewModel.removeWeatherFromFavorites(favorite) // מחיקת עיר מרשימת המועדפים
        }

        binding.rvFavorites.apply {
            layoutManager = LinearLayoutManager(requireContext()) // תצוגה אנכית
            adapter = favoriteAdapter
        }
    }

    //  מעקב אחר נתוני המועדפים ועדכון הרשימה
    private fun observeFavorites() {
        viewModel.favoriteWeatherList.observe(viewLifecycleOwner) { favorites ->
            favoriteAdapter.submitList(favorites.distinctBy { "${it.cityName}, ${it.country}" })
            //  מסנן כפילויות – אם יש נתונים כפולים, רק אחד יוצג
        }
    }

    //  רענון נתוני המועדפים מהשרת או ממקור הנתונים
    private fun refreshFavoriteWeather() {
        binding.progressBar.visibility = View.VISIBLE // הצגת progressBar בעת טעינה

        viewModel.refreshFavoriteWeather { success ->
            binding.progressBar.visibility = View.GONE // הסתרת progressBar לאחר סיום העדכון

            if (!success) {
                //  אם הרענון נכשל, ניתן להוסיף הודעת שגיאה למשתמש
            }
        }
    }


    //------edit card text-----------------------

}

