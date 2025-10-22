package com.autgroup.s2025.w201.todo.activities

import android.Manifest
import android.app.Activity
import android.content.Context
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
import com.autgroup.s2025.w201.todo.LocaleUtils
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
        "NZ" to "111",            // New Zealand
        "AU" to "000",            // Australia
        "US" to "911",            // United States
        "CA" to "911",            // Canada
        "GB" to "999",            // United Kingdom
        "IE" to "999/112",        // Ireland
        "IN" to "112",            // India
        "PK" to "15/1122",        // Pakistan
        "CN" to "110/120",        // China
        "JP" to "119/110",        // Japan
        "KR" to "112/119",        // South Korea
        "SG" to "999/995",        // Singapore
        "MY" to "999/991",        // Malaysia
        "TH" to "191/1669",       // Thailand
        "PH" to "117/911",        // Philippines
        "ID" to "110/118",        // Indonesia
        "VN" to "113/115/114",    // Vietnam
        "BR" to "190/192/193",    // Brazil
        "AR" to "911/107",        // Argentina
        "CL" to "131/132",        // Chile
        "CO" to "123",            // Colombia
        "MX" to "911",            // Mexico
        "PE" to "105/106",        // Peru
        "EC" to "911",            // Ecuador
        "UY" to "911/103",        // Uruguay
        "ZA" to "10111/112",      // South Africa
        "NG" to "112/199",        // Nigeria
        "EG" to "122/123",        // Egypt
        "MA" to "19/150",         // Morocco
        "RU" to "112/102/101",    // Russia
        "TR" to "112/155/110",    // Turkey
        "SA" to "999/997/998",    // Saudi Arabia
        "AE" to "999/998/997",    // United Arab Emirates
        "IL" to "100/101/102",    // Israel
        "FR" to "112/15/17/18",   // France
        "DE" to "112/110",        // Germany
        "NL" to "112",            // Netherlands
        "BE" to "112",            // Belgium
        "LU" to "112",            // Luxembourg
        "CH" to "112/117/118",    // Switzerland
        "ES" to "112",            // Spain
        "PT" to "112",            // Portugal
        "IT" to "112/118/113",    // Italy
        "SE" to "112",            // Sweden
        "NO" to "112/110/113",    // Norway
        "DK" to "112",            // Denmark
        "FI" to "112",            // Finland
        "PL" to "112/997",        // Poland
        "GR" to "112/100/166",    // Greece
        "CZ" to "112/155/158",    // Czech Republic
        "HU" to "112/104/107",    // Hungary
        "RO" to "112",            // Romania
        "BG" to "112/150/166",    // Bulgaria
        "UA" to "112/101/102"     // Ukraine
    )

    // Apply locale before onCreate()
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleUtils.applySavedLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before layout inflation
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
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location ?: return@addOnSuccessListener
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val countryCode = addresses[0].countryCode ?: return@addOnSuccessListener
                val number = emergencyNumbers[countryCode]
                    ?: getString(R.string.emergency_contact_not_available)
                emergencyContactView.text =
                    getString(R.string.emergency_contact_label, number)

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
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_REQUEST &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            openGallery()
        }
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
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

                        Toast.makeText(
                            this,
                            getString(R.string.profile_photo_updated),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        getString(R.string.upload_failed, e.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}
