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
import com.google.android.gms.maps.model.MarkerOptions

import android.util.Log
import android.widget.TextView
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.bumptech.glide.Glide

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
        // Remove default "Home Page" title
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Search bar click - open SearchActivity
        val searchBar = findViewById<android.widget.EditText>(R.id.search_bar)
        searchBar.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

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

        // --- Access header layout from NavigationView ---
        val headerView = navigationView.getHeaderView(0)
        val userNameTextView = headerView.findViewById<TextView>(R.id.user_name)
        val profileImageView = headerView.findViewById<ImageView>(R.id.profile_icon)

// --- Fetch user data from Firebase ---
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val dbRef = FirebaseDatabase.getInstance(
                "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
            ).getReference(userId).child("UserData")

            dbRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val firstName = snapshot.child("userFirstName").getValue(String::class.java) ?: ""
                    val lastName = snapshot.child("userLastName").getValue(String::class.java) ?: ""
                    val photoUrl = snapshot.child("photoUrl").getValue(String::class.java)

                    // Update drawer header name
                    userNameTextView.text = "$firstName $lastName"

                    // If Google profile photo exists, load it
                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(photoUrl)
                            .circleCrop()
                            .into(profileImageView)
                    }
                } else {
                    Log.d("Firebase", "No user data found for $userId")
                }
            }.addOnFailureListener { e ->
                Log.e("Firebase", "Failed to fetch user data", e)
            }
        }


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
                            //mMap.addMarker(MarkerOptions().position(userLatLng).title("You Are Here!"))
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
