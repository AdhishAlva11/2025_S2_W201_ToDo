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

// adding these imports so map opens on persons location no matter where they are
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.tasks.OnSuccessListener


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

    // location provider so it opens on persons location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // The first lifecycle method that runs when this Activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)



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
        //google map object is now ready so we assign it to our mMap variable
        mMap = googleMap

        // Enable zoom buttons (+ / - on the map)
        mMap.uiSettings.isZoomControlsEnabled = true

        //check if the user has already granted "Access_FINE_Location" permission
        //This is needed to show the blue "My Location" dot and move the camera to user

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //Permission granted - tun on the blue dot that tracks the user's location
            mMap.isMyLocationEnabled = true

            //Use the fusedLocationClient to get the devices last known location
            //This saves battery (instead of forcing a fresh GPS request) and is usually accurate

            fusedLocationClient.lastLocation.addOnSuccessListener(
                this, // The activity that listens for results

                object : com.google.android.gms.tasks.OnSuccessListener<android.location.Location?> {
                    // This callback runs when the location request succeeds
                    override fun onSuccess(location: android.location.Location?) {
                        if (location != null) {
                            //if valid location is returned, create a LatLng from Latitude/Longititude
                            val userLatLng = LatLng(location.latitude, location.longitude)
                            //move camera slowly to users position and zoom into level 12
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f))
                        } else {
                            //if location is null (eg GPS is off or first time)
                            //fall back to defualt location
                            val auckland = LatLng(-36.8485, 174.7633)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(auckland, 12f))
                        }
                    }
                }
            )
        } else {
            //if user hasnt granted permission
            //fall back to Auckland so the map still shows "something" instead of being blank
            val auckland = LatLng(-36.8485, 174.7633)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(auckland, 12f))
        }
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