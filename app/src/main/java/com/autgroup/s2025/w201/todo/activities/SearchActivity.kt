package com.autgroup.s2025.w201.todo.activities

import android.os.Bundle
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.autgroup.s2025.w201.todo.R
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_search)

        // Initialize Places SDK using the key from strings.xml
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
                Toast.makeText(
                    this@SearchActivity,
                    "Selected: ${place.name}",
                    Toast.LENGTH_SHORT
                ).show()
                // Future: Use place.latLng to show on map or filter results
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                Toast.makeText(
                    this@SearchActivity,
                    "Error: $status",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // Access interest checkboxes
        val foodCheck = findViewById<CheckBox>(R.id.checkboxFood)
        val walkingCheck = findViewById<CheckBox>(R.id.checkboxWalking)
        val sportsCheck = findViewById<CheckBox>(R.id.checkboxSports)
        val viewsCheck = findViewById<CheckBox>(R.id.checkboxViews)
        val familyCheck = findViewById<CheckBox>(R.id.checkboxFamily)
        val cultureCheck = findViewById<CheckBox>(R.id.checkboxCulture)

        // Example: Collect selected interests (for future filtering)
        val selectedInterests = listOf(
            "Food" to foodCheck.isChecked,
            "Walking" to walkingCheck.isChecked,
            "Sports" to sportsCheck.isChecked,
            "Views" to viewsCheck.isChecked,
            "Family" to familyCheck.isChecked,
            "Culture" to cultureCheck.isChecked
        )


    }
}