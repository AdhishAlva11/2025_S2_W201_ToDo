package com.autgroup.s2025.w201.todo.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.autgroup.s2025.w201.todo.databinding.ActivityHomePageBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

// Imports for location services
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.OnSuccessListener

class HomePageActivity : AppCompatActivity(), OnMapReadyCallback {

    // Map reference
    private lateinit var mMap: GoogleMap

    // View binding
    private lateinit var binding: ActivityHomePageBinding

    // Location provider client
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Request code for location permission popup
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Inflate layout
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if location permission is granted; if not, request it
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        // Find the MapFragment in activity_home_page.xml
        val mapFragment = supportFragmentManager
            .findFragmentById(com.autgroup.s2025.w201.todo.R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // Called when the Google Map is ready to use
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Enable zoom controls (+/- buttons on the map)
        mMap.uiSettings.isZoomControlsEnabled = true

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Show "My Location" blue dot
            mMap.isMyLocationEnabled = true

            // Get the last known location using FusedLocationProviderClient
            fusedLocationClient.lastLocation.addOnSuccessListener(
                this,
                object : OnSuccessListener<android.location.Location?> {
                    override fun onSuccess(location: android.location.Location?) {
                        if (location != null) {
                            // If location available → move camera to user’s location
                            val userLatLng = LatLng(location.latitude, location.longitude)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f))
                        } else {
                            // If null → fallback to Auckland
                            val auckland = LatLng(-36.8485, 174.7633)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(auckland, 12f))
                        }
                    }
                }
            )
        } else {
            // If no permission → fallback to Auckland
            val auckland = LatLng(-36.8485, 174.7633)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(auckland, 12f))
        }
    }

    // Handle user’s response to the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super<AppCompatActivity>.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Enable location if granted
                    mMap.isMyLocationEnabled = true
                }
            }
        }
    }
}
