package com.autgroup.s2025.w201.todo.activities

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.classes.PlaceInfo
import com.autgroup.s2025.w201.todo.classes.Search
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class DisplayMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var searchData: Search? = null
    private lateinit var firebaseAuth: FirebaseAuth
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

            // Fetch nearby places dynamically
            searchData?.interests?.forEach { interest ->
                interestToType[interest]?.let { type ->
                    fetchNearbyPlaces(location, type)
                }
            }
        } else {
            Toast.makeText(this, "Invalid coordinates", Toast.LENGTH_SHORT).show()
        }

        // Show bottom sheet when marker is clicked
        googleMap.setOnMarkerClickListener { marker ->
            val info = marker.tag as? PlaceInfo
            val bottomSheet = BottomSheetInfo.newInstance(info)
            bottomSheet.show(supportFragmentManager, "BottomSheetInfo")
            true
        }
    }

    private fun fetchNearbyPlaces(location: LatLng, type: String) {
        val apiKey = getString(R.string.project_google_api_key)
        val url =
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?location=${location.latitude},${location.longitude}" +
                    "&radius=5000" +
                    "&type=$type" +
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
                        val name = place.getString("name")
                        val geometry = place.getJSONObject("geometry").getJSONObject("location")
                        val lat = geometry.getDouble("lat")
                        val lng = geometry.getDouble("lng")

                        val address = place.optString("vicinity", "No address available")
                        val rating = place.optDouble("rating", 0.0)
                        val openNow = if (place.has("opening_hours")) {
                            if (place.getJSONObject("opening_hours").optBoolean("open_now")) "Open now" else "Closed"
                        } else {
                            "Hours not available"
                        }

                        val markerPos = LatLng(lat, lng)
                        val marker = googleMap.addMarker(
                            MarkerOptions().position(markerPos).title(name)
                        )
                        marker?.tag = PlaceInfo(name, address, rating, openNow, lat, lng)
                    }
                }
            }
        })
    }
}
