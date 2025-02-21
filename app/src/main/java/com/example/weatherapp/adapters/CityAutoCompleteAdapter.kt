package com.example.weatherapp.adapters

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import com.example.weatherapp.models.CityResponse
import java.util.Locale

/**
 *  מתאם לרשימת השלמה אוטומטית של ערים.
 * מקבל רשימת ערים ומציג אותן ברשימה הנפתחת בעת הקלדת שם עיר.
 */
class CityAutoCompleteAdapter(
    context: Context,
    private var cityList: List<CityResponse>
) : ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line) {

    //  רשימה מסוננת של ערים לתצוגה
    private var filteredCities: List<CityResponse> = cityList

    /**
     *  מחזיר את כמות הערים המסוננות להצגה
     */
    override fun getCount(): Int = filteredCities.size

    /**
     *  מחזיר שם עיר בפורמט "שם עיר, שם מדינה" בהתאם למיקום
     */
    override fun getItem(position: Int): String {
        val city = filteredCities[position]
        val countryName = Locale("", city.country).displayCountry // ממיר קוד מדינה לשם מלא
        return "${city.name}, $countryName"
    }

    /**
     *  מחזיר את אובייקט העיר השלם למיקום נתון
     */
    fun getFullCityItem(position: Int): CityResponse {
        return filteredCities[position] // מחזיר את האובייקט השלם
    }

    /**
     *  מחזיר פילטר לרשימת הערים כדי לאפשר חיפוש בזמן אמת.
     */
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()

                // בדיקה אם המשתמש הזין משהו לחיפוש
                if (!constraint.isNullOrEmpty()) {
                    // מסנן את הערים שמתחילות בטקסט שהוזן
                    val filtered = cityList.filter {
                        it.name.startsWith(constraint.toString(), ignoreCase = true)
                    }
                    results.values = filtered
                    results.count = filtered.size
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredCities = results?.values as? List<CityResponse> ?: emptyList()
                notifyDataSetChanged() // עדכון הרשימה לאחר סינון
            }
        }
    }

    /**
     *  מעדכן את רשימת הערים החדשה ומסנן כפילויות
     */
    fun updateCities(newCities: List<CityResponse>) {
        // מסנן כפילויות לפי שילוב של שם עיר ומדינה
        val uniqueCities = newCities.distinctBy { "${it.name},${it.country}" }

        cityList = uniqueCities
        filteredCities = uniqueCities
        notifyDataSetChanged()
    }
}



