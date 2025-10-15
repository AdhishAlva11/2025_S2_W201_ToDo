package com.autgroup.s2025.w201.todo.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.ThemeUtils
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 100
    private var imageUri: Uri? = null
    private val storageRef = FirebaseStorage.getInstance().reference
    private val sharedPrefs by lazy { getSharedPreferences("user_prefs", MODE_PRIVATE) }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val STORAGE_PERMISSION_REQUEST = 200
        private const val LOCATION_PERMISSION_REQUEST = 1
    }

    // Emergency number mapping by country
    private val emergencyNumbers = mapOf(
        "NZ" to "111", "AU" to "000", "US" to "911", "IN" to "112",
        "CN" to "110/120", "JP" to "119/110", "GB" to "999", "SG" to "999/995"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val settingsButton = findViewById<ImageButton>(R.id.btnSettings)
        val userName = findViewById<TextView>(R.id.user_name)
        val profileImage = findViewById<ImageView>(R.id.profile_image)
        val logoutButton = findViewById<Button>(R.id.btnLogout)
        val uploadButton = findViewById<ImageButton>(R.id.btnUploadPhoto)
        val emergencyContactView = findViewById<TextView>(R.id.emergency_contact)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // --- Toolbar Settings Button ---
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // --- Cached profile photo ---
        sharedPrefs.getString("photoUrl", null)?.let {
            Glide.with(this).load(it).circleCrop().into(profileImage)
        }

        // --- Upload photo ---
        uploadButton.setOnClickListener { checkAndOpenGallery() }

        // --- Load user info from Firebase ---
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val dbRef = FirebaseDatabase.getInstance(
                "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
            ).getReference("$userId/UserData")

            dbRef.get().addOnSuccessListener { snapshot ->
                val first = snapshot.child("userFirstName").getValue(String::class.java) ?: ""
                val last = snapshot.child("userLastName").getValue(String::class.java) ?: ""
                val photoUrl = snapshot.child("photoUrl").getValue(String::class.java)

                userName.text = "$first $last"
                if (!photoUrl.isNullOrEmpty()) {
                    Glide.with(this).load(photoUrl).circleCrop().into(profileImage)
                    sharedPrefs.edit().putString("photoUrl", photoUrl).apply()
                }
            }
        }

        // --- Emergency contact setup ---
        getEmergencyContact()

        // --- Logout logic ---
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            sharedPrefs.edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }

        // --- Bottom Navigation ---
        bottomNav.selectedItemId = R.id.nav_profile
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomePageActivity::class.java))
                R.id.nav_favourites -> startActivity(Intent(this, FavouritesActivity::class.java))
                R.id.nav_itinerary -> startActivity(Intent(this, ItineraryActivity::class.java))
                R.id.nav_profile -> return@setOnItemSelectedListener true
            }
            overridePendingTransition(0, 0)
            true
        }
    }

    // --- Permission check ---
    private fun checkAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) ==
                PackageManager.PERMISSION_GRANTED
            ) openGallery()
            else requestPermissions(
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                STORAGE_PERMISSION_REQUEST
            )
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
            ) openGallery()
            else requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_REQUEST
            )
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // --- Emergency Contact logic ---
    private fun getEmergencyContact() {
        val emergencyContactView = findViewById<TextView>(R.id.emergency_contact)
        emergencyContactView.text = getString(R.string.emergency_contact_not_available)

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location ?: return@addOnSuccessListener
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val countryCode = addresses[0].countryCode ?: return@addOnSuccessListener
                val number = emergencyNumbers[countryCode] ?: getString(R.string.emergency_contact_not_available)
                emergencyContactView.text = getString(R.string.emergency_contact_label, number)

                if (number != getString(R.string.not_available)) {
                    emergencyContactView.setOnClickListener {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                        startActivity(intent)
                    }
                }
            }
        }.addOnFailureListener {
            emergencyContactView.text = getString(R.string.emergency_contact_not_available)
        }
    }

    // --- Permission result handler ---
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_REQUEST && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        }
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            getEmergencyContact()
        }
    }

    // --- Gallery result handler ---
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data ?: return
            val profileImage = findViewById<ImageView>(R.id.profile_image)
            Glide.with(this).load(imageUri).circleCrop().into(profileImage)
            uploadImageToFirebase()
        }
    }

    // --- Firebase upload ---
    private fun uploadImageToFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val fileRef = storageRef.child("profile_images/$userId")

        imageUri?.let { uri ->
            fileRef.putFile(uri)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val newPhotoUrl = downloadUri.toString()
                        val dbRef = FirebaseDatabase.getInstance(
                            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
                        ).getReference("$userId/UserData")

                        dbRef.child("photoUrl").setValue(newPhotoUrl)
                        sharedPrefs.edit().putString("photoUrl", newPhotoUrl).apply()

                        Toast.makeText(this, getString(R.string.profile_photo_updated), Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, getString(R.string.upload_failed, e.message), Toast.LENGTH_LONG).show()
                }
        }
    }
}
