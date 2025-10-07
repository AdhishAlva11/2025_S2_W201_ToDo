package com.autgroup.s2025.w201.todo.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.autgroup.s2025.w201.todo.R
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
import android.os.Handler
import android.os.Looper

class DisplayMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var searchData: Search? = null
    private lateinit var googleMap: GoogleMap
    private val client = OkHttpClient()

    private val interestToType = mapOf(
        "Restaurants" to "restaurant",
        "Walking" to "park",
        "Sports" to "stadium",
        "Landmarks" to "tourist_attraction",
        "Family Attractions" to "amusement_park",
        "Culture" to "museum"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_map)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        findViewById<EditText>(R.id.search_bar).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomePageActivity::class.java))
                R.id.nav_favourites -> startActivity(Intent(this, FavouritesActivity::class.java))
                R.id.nav_itinerary -> startActivity(Intent(this, ItineraryActivity::class.java))
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                else -> false
            }
            true
        }

        searchData = intent.getSerializableExtra("searchData") as? Search
        if (searchData == null) {
            Toast.makeText(this, "No location data received", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<TextView>(R.id.interestOverlay).text =
            searchData?.interests?.joinToString(", ")
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val lat = searchData?.lat
        val lng = searchData?.lng
        val placeName = searchData?.placeName ?: "Selected Location"

        if (lat != null && lng != null) {
            val location = LatLng(lat, lng)
            googleMap.addMarker(MarkerOptions().position(location).title(placeName))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14f))

            // Draw radius circle
            googleMap.addCircle(
                CircleOptions()
                    .center(location)
                    .radius(searchData?.radius?.toDouble() ?: 5000.0)
                    .strokeColor(Color.BLUE)
                    .fillColor(0x220000FF)
                    .strokeWidth(2f)
            )

            searchData?.interests?.forEach { interest ->
                interestToType[interest]?.let { type ->
                    fetchNearbyPlaces(location, type)
                }
            }
        } else {
            Toast.makeText(this, "Invalid coordinates", Toast.LENGTH_SHORT).show()
        }

        googleMap.setOnMarkerClickListener { marker ->
            val info = marker.tag as? PlaceInfo
            if (info != null) {
                val bottomSheet = BottomSheetInfo.newInstance(info)
                bottomSheet.show(supportFragmentManager, "BottomSheetInfo")
            }
            true
        }
    }

    private fun fetchNearbyPlaces(location: LatLng, type: String, pageToken: String? = null) {
        val apiKey = getString(R.string.project_google_api_key)
        val radius = searchData?.radius ?: 3000
        val tokenParam = if (pageToken != null) "&pagetoken=$pageToken" else ""

        val url =
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?location=${location.latitude},${location.longitude}" +
                    "&radius=$radius" +
                    "&type=$type" +
                    "$tokenParam" +
                    "&key=$apiKey"

        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("PlacesAPI", "Request failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return
                val json = JSONObject(body)
                val results = json.getJSONArray("results")

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

                // Handle pagination
                val nextPageToken = json.optString("next_page_token", null)
                if (!nextPageToken.isNullOrEmpty()) {
                    android.os.Handler(Looper.getMainLooper()).postDelayed({
                        fetchNearbyPlaces(location, type, nextPageToken)
                    }, 2000)
                }
            }
        })
    }

    private fun fetchPlaceDetails(placeId: String, onResult: (PlaceInfo) -> Unit) {
        val apiKey = getString(R.string.project_google_api_key)
        val url = "https://maps.googleapis.com/maps/api/place/details/json" +
                "?place_id=$placeId" +
                "&fields=name,rating,formatted_address,opening_hours,reviews,geometry" +
                "&key=$apiKey"

        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("PlaceDetailsAPI", "Request failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return
                val json = JSONObject(body).getJSONObject("result")

                val name = json.optString("name", "No name")
                val address = json.optString("formatted_address", "No address")
                val rating = json.optDouble("rating", 0.0)
                val openNow = if (json.has("opening_hours")) {
                    if (json.getJSONObject("opening_hours")
                            .optBoolean("open_now")
                    ) "Open now" else "Closed"
                } else "Hours not available"

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

                val placeInfo = PlaceInfo(
                    name = name,
                    address = address,
                    rating = rating,
                    openStatus = openNow,
                    lat = lat,
                    lng = lng,
                    reviews = reviewsList
                )

                onResult(placeInfo)
            }
        })
    }
}
