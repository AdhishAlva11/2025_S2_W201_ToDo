package com.autgroup.s2025.w201.todo.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.ThemeUtils
import com.autgroup.s2025.w201.todo.LocaleUtils
import com.autgroup.s2025.w201.todo.classes.Search
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.util.*

class SearchActivity : AppCompatActivity() {

    private var currentPlace: Place? = null

    // Apply saved locale before Activity creation
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleUtils.applySavedLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme before inflating layout
        ThemeUtils.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val apiKey = getString(R.string.project_google_api_key)

        // Initialize Google Places with your app’s locale (API 23+ compatible)
        if (!Places.isInitialized()) {
            val currentLocale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // For Android 7.0 and above
                LocaleUtils.applySavedLocale(this).resources.configuration.locales[0]
            } else {
                // For Android 6.0 and below
                LocaleUtils.applySavedLocale(this).resources.configuration.locale
            }

            Places.initialize(applicationContext, apiKey, currentLocale)
        }

        // --- Setup Autocomplete fragment ---
        val autocompleteFragment = supportFragmentManager
            .findFragmentById(R.id.autocompleteFragment) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        )

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                currentPlace = place
                Toast.makeText(
                    this@SearchActivity,
                    getString(R.string.selected_place_toast, place.name ?: ""),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onError(status: Status) {
                Toast.makeText(
                    this@SearchActivity,
                    getString(R.string.autocomplete_error_toast, status.toString()),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // --- Search button click ---
        findViewById<Button>(R.id.searchButton).setOnClickListener {
            if (currentPlace == null) {
                Toast.makeText(
                    this,
                    getString(R.string.please_select_location),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val searchData = buildSearchFromUI(currentPlace)
                val intent = Intent(this, DisplayMapActivity::class.java)
                intent.putExtra("searchData", searchData)
                startActivity(intent)
            }
        }
    }

    private fun buildSearchFromUI(place: Place?): Search {
        // Use localized strings so filtering is consistent with app language
        val selectedInterests = listOf(
            R.id.checkboxFood   to getString(R.string.restaurants),
            R.id.checkboxWalking to getString(R.string.walking),
            R.id.checkboxSports  to getString(R.string.sports),
            R.id.checkboxViews   to getString(R.string.landmarks),
            R.id.checkboxFamily  to getString(R.string.family_attractions),
            R.id.checkboxCulture to getString(R.string.culture)
        ).mapNotNull { (id, label) ->
            if (findViewById<CheckBox>(id).isChecked) label else null
        }

        val radiusSpinner = findViewById<Spinner>(R.id.radiusSpinner)
        val selectedRadius = radiusSpinner.selectedItem?.toString()?.toIntOrNull() ?: 5000

        return Search.fromPlaceAndInterests(place, selectedInterests, selectedRadius)
    }
}
