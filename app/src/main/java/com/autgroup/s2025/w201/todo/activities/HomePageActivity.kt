package com.autgroup.s2025.w201.todo.activities


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.databinding.ActivityHomePageBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

// Our Activity class, representing the "Home Page" (the landing page with the map).
// It extends AppCompatActivity (so it can act as an Android screen)
// and implements OnMapReadyCallback (so we can respond when the map finishes loading).
class HomePageActivity : AppCompatActivity(), OnMapReadyCallback {

    // Lateinit means "this will be set later, before use"
    // mMap will hold a reference to the Google Map object once it's ready
    private lateinit var mMap: GoogleMap

    // Binding object for accessing the layout views safely
    private lateinit var binding: ActivityHomePageBinding
    // A constant request code we use to identify our location permission request

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    // The first lifecycle method that runs when this Activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using ViewBinding and set it as the content view
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Check if location permission is already granted
        if(ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED

            ) {
            // If not granted, request it from the user
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )

        }

        // Get a reference to the SupportMapFragment defined in activity_home_page.xml
        // This is the container that displays the Google Map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        // Tell the fragment to notify this activity (via onMapReady) when the map is ready to use
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    // This callback is triggered once the Google Map is fully loaded and ready for interaction
    override fun onMapReady(googleMap: GoogleMap) {
        // Save the map object into our mMap variable
        mMap = googleMap

        // If permission is granted, enable the "blue dot" showing the user's current location
        if(ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
            )
        {
            mMap.isMyLocationEnabled = true
        }
        // Define coordinates for Auckland (latitude, longitude)
        val auckland = LatLng(-36.8485, 174.7633)
        // Move the camera to Auckland with a zoom level of 12
        // (this acts as a default view until GPS updates the user's real location)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(auckland, 12f))
    }

    // This method is called after the user responds to our permission request popup
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permission: Array<out String>,
        grantResults: IntArray
    ){
        // Always call the parent implementation first
        super<AppCompatActivity>.onRequestPermissionsResult(requestCode, permission, grantResults)

        // Check if the response was for our location permission request
        if(requestCode == LOCATION_PERMISSION_REQUEST_CODE){
            // If the user granted permission (and the result array isn't empty)
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // Double-check permission again before enabling location (safety step)
                if(ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                    ){
                    // Enable the blue "My Location" dot on the map
                    mMap.isMyLocationEnabled = true

                }
            }
        }
    }
}