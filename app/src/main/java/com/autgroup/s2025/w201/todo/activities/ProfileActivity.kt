package com.autgroup.s2025.w201.todo.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.autgroup.s2025.w201.todo.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.bumptech.glide.Glide
import android.location.Geocoder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    // Country emergency numbers
    private val emergencyNumbers = mapOf(
        "NZ" to "111",   // New Zealand
        "AU" to "000",   // Australia
        "US" to "911",   // United States
        "CA" to "911",   // Canada
        "GB" to "999",   // United Kingdom
        "IE" to "999/112",
        "IN" to "112",
        "PK" to "15/1122",
        "CN" to "110/120",
        "JP" to "119/110",
        "KR" to "112/119",
        "SG" to "999/995",
        "MY" to "999/991",
        "TH" to "191/1669",
        "PH" to "117/911",
        "ID" to "110/118",
        "VN" to "113/115/114",
        "BR" to "190/192/193",
        "AR" to "911/107",
        "CL" to "131/132",
        "CO" to "123",
        "MX" to "911",
        "PE" to "105/106",
        "EC" to "911",
        "UY" to "911/103",
        "ZA" to "10111/112",
        "NG" to "112/199",
        "EG" to "122/123",
        "MA" to "19/150",
        "RU" to "112/102/101",
        "TR" to "112/155/110",
        "SA" to "999/997/998",
        "AE" to "999/998/997",
        "IL" to "100/101/102",
        "FR" to "112/15/17/18",
        "DE" to "112/110",
        "NL" to "112",
        "BE" to "112",
        "LU" to "112",
        "CH" to "112/117/118",
        "ES" to "112",
        "PT" to "112",
        "IT" to "112/118/113",
        "SE" to "112",
        "NO" to "112/110/113",
        "DK" to "112",
        "FI" to "112",
        "PL" to "112/997",
        "GR" to "112/100/166",
        "CZ" to "112/155/158",
        "HU" to "112/104/107",
        "RO" to "112",
        "BG" to "112/150/166",
        "UA" to "112/101/102"
    )

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        window.decorView.systemUiVisibility = 0

        val userName = findViewById<TextView>(R.id.user_name)
        val profileImage = findViewById<ImageView>(R.id.profile_image)
        val logoutButton = findViewById<Button>(R.id.btnLogout)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // --- Firebase user info ---
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

                    userName.text = "$firstName $lastName"

                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(photoUrl)
                            .circleCrop()
                            .into(profileImage)
                    }
                }
            }
        }

        // --- Emergency contact setup ---
        getEmergencyContact()

        // --- Logout ---
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // --- Bottom Navigation ---
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_profile

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomePageActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_favourites -> {
                    startActivity(Intent(this, FavouritesActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_itinerary -> {
                    startActivity(Intent(this, ItineraryActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    // Helper function to fetch and display emergency contact
    private fun getEmergencyContact() {
        val emergencyContactView = findViewById<TextView>(R.id.emergency_contact)
        emergencyContactView.text = "Emergency Contact: Not Available"

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    val countryCode = addresses[0].countryCode
                    val number = emergencyNumbers[countryCode] ?: "Not Available"
                    emergencyContactView.text = "Emergency Contact: $number"
                }
            }
        }.addOnFailureListener {
            emergencyContactView.text = "Emergency Contact: Not Available"
        }
    }

    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getEmergencyContact()
        }
    }
}
