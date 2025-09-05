package com.autgroup.s2025.w201.todo.activities

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.classes.Search
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class DisplayMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var searchData: Search? = null
    private lateinit var googleMap: GoogleMap

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

        val overlay = findViewById<TextView>(R.id.interestOverlay)
        overlay.text = searchData?.interests?.joinToString(", ")
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val lat = searchData?.lat
        val lng = searchData?.lng
        val placeName = searchData?.placeName ?: "Selected Location"

        if (lat != null && lng != null) {
            val location = LatLng(lat, lng)
            googleMap.addMarker(MarkerOptions().position(location).title(placeName))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        } else {
            Toast.makeText(this, "Invalid coordinates", Toast.LENGTH_SHORT).show()
        }
    }
}
