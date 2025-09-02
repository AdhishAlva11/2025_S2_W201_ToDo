package com.autgroup.s2025.w201.todo.activities

// Used for starting new screens (Intents will open FavouritesActivity or later ItineraryActivity)
import android.content.Intent
import android.os.Bundle

// Provides the “hamburger” icon toggle for opening/closing the drawer
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity

// R contains all auto-generated resource references (layouts, menus, strings, IDs, etc.)
import com.autgroup.s2025.w201.todo.R

// View binding for activity_home_page.xml (avoids findViewById calls)
import com.autgroup.s2025.w201.todo.databinding.ActivityHomePageBinding

// Google Maps core classes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

// Permissions handling (needed for location access)
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

// Google Play Services Location API
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.OnSuccessListener

// UI components for Material Navigation Drawer
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout

class HomePageActivity : AppCompatActivity(), OnMapReadyCallback {

    // Map reference
    private lateinit var mMap: GoogleMap

    // View binding
    private lateinit var binding: ActivityHomePageBinding

    // Location provider client
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // DrawerLayout (container for the drawer + main content)
    private lateinit var drawerLayout: DrawerLayout
    // ActionBarDrawerToggle → handles syncing the hamburger icon with the drawer
    private lateinit var toggle: ActionBarDrawerToggle

    // Request code for location permission popup
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize location provider (FusedLocationProvider)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Inflate layout(using view binding)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Drawer Setup
        drawerLayout = findViewById(R.id.drawer_layout)

        //Tell the activity to use the toolbar from the layout
        setSupportActionBar(binding.toolbar)

        //Create the hamburger toggle and attach it to the drawer + toolbar
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,   // String resource shown when drawer is opened
            R.string.navigation_drawer_close   // String resource shown when drawer is closed
        )
        //Add listener so toggle knows when drawer is open/closer
        drawerLayout.addDrawerListener(toggle)

        //Sync toggle state (ensure hamburger icon updated correctley)
        toggle.syncState()

        // Handle drawer item clicks
        val navigationView: NavigationView = findViewById(R.id.navigation_view)

        navigationView.setNavigationItemSelectedListener(
            object : NavigationView.OnNavigationItemSelectedListener {
                override fun onNavigationItemSelected(menuItem: android.view.MenuItem): Boolean {
                    when (menuItem.itemId) {
                        // Open FavouritesActivity
                        R.id.nav_favourites -> {
                            startActivity(Intent(this@HomePageActivity, FavouritesActivity::class.java))
                            return true
                        }

                        // Placeholder for ItineraryActivity (not created yet)
                        R.id.nav_itinerary -> {
                            // TODO: Add startActivity(Intent(this@HomePageActivity, ItineraryActivity::class.java))
                            return true
                        }

                        else -> return false
                    }
                }
            }
        )


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

        // Enable gestures (pinch + scroll + rotate + tilt)
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isScrollGesturesEnabled = true
        mMap.uiSettings.isRotateGesturesEnabled = true
        mMap.uiSettings.isTiltGesturesEnabled = true

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
                        if ( location != null) {
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
