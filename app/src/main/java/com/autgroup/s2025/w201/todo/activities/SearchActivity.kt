package com.autgroup.s2025.w201.todo.activities

import android.os.Bundle
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.classes.Search
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

class SearchActivity : AppCompatActivity() {

    private var currentSearch: Search? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_search)

        // Initialize Places SDK
        val apiKey = getString(R.string.google_places_key)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        // Setup Autocomplete Fragment
        val autocompleteFragment = supportFragmentManager
            .findFragmentById(R.id.autocompleteFragment) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        )

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val search = buildSearchFromUI(place)
                currentSearch = search

                Toast.makeText(
                    this@SearchActivity,
                    "Search created: $search",
                    Toast.LENGTH_SHORT
                ).show()

                // Future: Use `search` to query/filter your results
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                Toast.makeText(
                    this@SearchActivity,
                    "Error: $status",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun buildSearchFromUI(place: Place?): Search {
        val foodCheck = findViewById<CheckBox>(R.id.checkboxFood)
        val walkingCheck = findViewById<CheckBox>(R.id.checkboxWalking)
        val sportsCheck = findViewById<CheckBox>(R.id.checkboxSports)
        val viewsCheck = findViewById<CheckBox>(R.id.checkboxViews)
        val familyCheck = findViewById<CheckBox>(R.id.checkboxFamily)
        val cultureCheck = findViewById<CheckBox>(R.id.checkboxCulture)

        val selectedInterests = mutableListOf<String>()
        if (foodCheck.isChecked) selectedInterests.add("Food")
        if (walkingCheck.isChecked) selectedInterests.add("Walking")
        if (sportsCheck.isChecked) selectedInterests.add("Sports")
        if (viewsCheck.isChecked) selectedInterests.add("Views")
        if (familyCheck.isChecked) selectedInterests.add("Family")
        if (cultureCheck.isChecked) selectedInterests.add("Culture")

        return Search.fromPlaceAndInterests(place, selectedInterests)
    }
}