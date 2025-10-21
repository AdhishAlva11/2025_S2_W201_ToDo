package com.autgroup.s2025.w201.todo.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.ThemeUtils
import com.autgroup.s2025.w201.todo.LocaleUtils
import com.autgroup.s2025.w201.todo.classes.PlaceInfo
import com.autgroup.s2025.w201.todo.classes.Search
import com.autgroup.s2025.w201.todo.classes.Review
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*

class DisplayMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var searchData: Search? = null
    private lateinit var googleMap: GoogleMap
    private val client = OkHttpClient()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleUtils.applySavedLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_map)

        // Toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Map fragment
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Search bar opens SearchActivity
        findViewById<EditText>(R.id.search_bar).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        // Bottom navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomePageActivity::class.java))
                R.id.nav_favourites -> startActivity(Intent(this, FavouritesActivity::class.java))
                R.id.nav_itinerary -> startActivity(Intent(this, ItineraryActivity::class.java))
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
            }
            true
        }

        // Load search data from intent
        searchData = intent.getSerializableExtra("searchData") as? Search
        if (searchData == null) {
            Toast.makeText(this, getString(R.string.no_location_data), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Show selected interests overlay
        findViewById<TextView>(R.id.interestOverlay).text =
            searchData?.interests?.joinToString(", ")
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val lat = searchData?.lat
        val lng = searchData?.lng
        val placeName = searchData?.placeName ?: getString(R.string.selected_location)

        if (lat == null || lng == null) {
            Toast.makeText(this, getString(R.string.invalid_coordinates), Toast.LENGTH_SHORT).show()
            return
        }

        val location = LatLng(lat, lng)
        googleMap.addMarker(MarkerOptions().position(location).title(placeName))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14f))

        // Draw search radius circle
        googleMap.addCircle(
            CircleOptions()
                .center(location)
                .radius(searchData?.radius?.toDouble() ?: 5000.0)
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(2f)
        )

        // Map of interest → Google Places type
        val interestToType = mapOf(
            getString(R.string.restaurants) to "restaurant",
            getString(R.string.walking) to "park",
            getString(R.string.sports) to "stadium",
            getString(R.string.landmarks) to "tourist_attraction",
            getString(R.string.family_attractions) to "amusement_park",
            getString(R.string.culture) to "museum"
        )

        // Fetch nearby places
        searchData?.interests?.forEach { interest ->
            interestToType[interest]?.let { type ->
                fetchNearbyPlaces(location, type)
            }
        }

        // Marker click listener → show BottomSheetInfo
        googleMap.setOnMarkerClickListener { marker ->
            val info = marker.tag as? PlaceInfo
            if (info != null) {
                val bottomSheet = BottomSheetInfo.newInstance(info, lat, lng)
                bottomSheet.show(supportFragmentManager, "BottomSheetInfo")
            }
            true
        }
    }

    private fun fetchNearbyPlaces(location: LatLng, type: String, pageToken: String? = null) {
        val apiKey = getString(R.string.project_google_api_key)
        val radius = searchData?.radius ?: 3000
        val tokenParam = if (pageToken != null) "&pagetoken=$pageToken" else ""
        val currentLang = Locale.getDefault().language

        val url =
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?location=${location.latitude},${location.longitude}" +
                    "&radius=$radius&type=$type" +
                    "&language=$currentLang$tokenParam&key=$apiKey"

        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("PlacesAPI", getString(R.string.request_failed), e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return
                val json = JSONObject(body)
                val results = json.optJSONArray("results") ?: return

                runOnUiThread {
                    for (i in 0 until results.length()) {
                        val place = results.getJSONObject(i)
                        val placeId = place.getString("place_id")
                        fetchPlaceDetails(placeId) { placeInfo ->
                            runOnUiThread {
                                val markerPos = LatLng(placeInfo.lat!!, placeInfo.lng!!)
                                val marker = googleMap.addMarker(
                                    MarkerOptions().position(markerPos).title(placeInfo.name)
                                )
                                marker?.tag = placeInfo
                            }
                        }
                    }
                }

                // Next page token
                val nextPageToken = json.optString("next_page_token", null)
                if (!nextPageToken.isNullOrEmpty()) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        fetchNearbyPlaces(location, type, nextPageToken)
                    }, 2000)
                }
            }
        })
    }

    private fun fetchPlaceDetails(placeId: String, onResult: (PlaceInfo) -> Unit) {
        val apiKey = getString(R.string.project_google_api_key)
        val url =
            "https://maps.googleapis.com/maps/api/place/details/json" +
                    "?place_id=$placeId" +
                    "&fields=name,rating,formatted_address,opening_hours,reviews,geometry,price_level" +
                    "&language=${Locale.getDefault().language}" +
                    "&key=$apiKey"

        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("PlaceDetailsAPI", getString(R.string.request_failed), e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return
                val json = JSONObject(body).getJSONObject("result")

                val name = json.optString("name", getString(R.string.no_name))
                val address = json.optString("formatted_address", getString(R.string.no_address))
                val rating = json.optDouble("rating", 0.0)
                val openNow = if (json.has("opening_hours")) {
                    if (json.getJSONObject("opening_hours").optBoolean("open_now"))
                        getString(R.string.open_now)
                    else getString(R.string.closed)
                } else getString(R.string.hours_not_available)

                val geometry = json.getJSONObject("geometry").getJSONObject("location")
                val lat = geometry.getDouble("lat")
                val lng = geometry.getDouble("lng")

                val reviewsList = mutableListOf<Review>()
                if (json.has("reviews")) {
                    val reviews = json.getJSONArray("reviews")
                    for (i in 0 until reviews.length()) {
                        val r = reviews.getJSONObject(i)
                        reviewsList.add(
                            Review(
                                authorName = r.optString("author_name"),
                                rating = r.optDouble("rating"),
                                time = r.optString("relative_time_description"),
                                text = r.optString("text")
                            )
                        )
                    }
                }

                val priceLevel = if (json.has("price_level")) json.getInt("price_level") else null

                val placeInfo = PlaceInfo(
                    name = name,
                    address = address,
                    rating = rating,
                    openStatus = openNow,
                    lat = lat,
                    lng = lng,
                    reviews = reviewsList,
                    priceLevel = priceLevel
                )

                onResult(placeInfo)
            }
        })
    }
}