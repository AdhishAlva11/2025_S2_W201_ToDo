package com.autgroup.s2025.w201.todo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.classes.Search
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.io.Serializable

class SearchActivity : AppCompatActivity() {

    private var currentPlace: Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val apiKey = getString(R.string.google_places_key)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        val autocompleteFragment = supportFragmentManager
            .findFragmentById(R.id.autocompleteFragment) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        )

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                currentPlace = place
                Toast.makeText(this@SearchActivity, "Selected: ${place.name}", Toast.LENGTH_SHORT).show()
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                Toast.makeText(this@SearchActivity, "Error: $status", Toast.LENGTH_SHORT).show()
            }
        })

        findViewById<Button>(R.id.searchButton).setOnClickListener {
            if (currentPlace == null) {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show()
            } else {
                val searchData = buildSearchFromUI(currentPlace)
                val intent = Intent(this, DisplayMapActivity::class.java)
                intent.putExtra("searchData", searchData as Serializable)
                startActivity(intent)
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

        return Search.fromPlaceAndInterests(place, selectedInterests)
    }
}