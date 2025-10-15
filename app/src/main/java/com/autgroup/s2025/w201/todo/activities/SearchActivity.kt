package com.autgroup.s2025.w201.todo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.ThemeUtils
import com.autgroup.s2025.w201.todo.classes.Search
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomnavigation.BottomNavigationView

class SearchActivity : AppCompatActivity() {

    private var currentPlace: Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Initialize Places API
        val apiKey = getString(R.string.project_google_api_key)
        if (!Places.isInitialized()) Places.initialize(applicationContext, apiKey)

        // Autocomplete fragment
        val autocompleteFragment = supportFragmentManager
            .findFragmentById(R.id.autocompleteFragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                currentPlace = place
                Toast.makeText(this@SearchActivity, "Selected: ${place.name}", Toast.LENGTH_SHORT).show()
            }

            override fun onError(status: Status) {
                Toast.makeText(this@SearchActivity, "Error: $status", Toast.LENGTH_SHORT).show()
            }
        })

        // Search button
        findViewById<Button>(R.id.searchButton).setOnClickListener {
            if (currentPlace == null) {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show()
            } else {
                val searchData = buildSearchFromUI(currentPlace)
                val intent = Intent(this, DisplayMapActivity::class.java)
                intent.putExtra("searchData", searchData)
                startActivity(intent)
            }
        }

        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomePageActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_favourites -> {
                    startActivity(Intent(this, FavouritesActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_itinerary -> {
                    startActivity(Intent(this, ItineraryActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun buildSearchFromUI(place: Place?): Search {
        val selectedInterests = listOf(
            R.id.checkboxFood to "Restaurants",
            R.id.checkboxWalking to "Walking",
            R.id.checkboxSports to "Sports",
            R.id.checkboxViews to "Landmarks",
            R.id.checkboxFamily to "Family Attractions",
            R.id.checkboxCulture to "Culture"
        ).mapNotNull { (id, label) ->
            if (findViewById<CheckBox>(id).isChecked) label else null
        }

        val radiusSpinner = findViewById<Spinner>(R.id.radiusSpinner)
        val selectedRadius = radiusSpinner.selectedItem.toString().toIntOrNull() ?: 5000

        return Search.fromPlaceAndInterests(place, selectedInterests, selectedRadius)
    }
}