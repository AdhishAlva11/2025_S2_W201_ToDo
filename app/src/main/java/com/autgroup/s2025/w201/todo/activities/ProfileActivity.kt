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
import android.app.Activity
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    //For profile photo selection
    private val PICK_IMAGE_REQUEST = 100
    private var imageUri: Uri? = null
    private val storageRef = FirebaseStorage.getInstance().reference


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
    companion object {
        private const val STORAGE_PERMISSION_REQUEST = 200
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        window.decorView.systemUiVisibility = 0

        val userName = findViewById<TextView>(R.id.user_name)
        val profileImage = findViewById<ImageView>(R.id.profile_image)
        val logoutButton = findViewById<Button>(R.id.btnLogout)
        val uploadButton = findViewById<ImageButton>(R.id.btnUploadPhoto)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // --- Upload profile photo button ---
        uploadButton.setOnClickListener {
            checkAndOpenGallery()
        }

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
                    overridePendingTransition(0, 0); true
                }
                R.id.nav_favourites -> {
                    startActivity(Intent(this, FavouritesActivity::class.java))
                    overridePendingTransition(0, 0); true
                }
                R.id.nav_itinerary -> {
                    startActivity(Intent(this, ItineraryActivity::class.java))
                    overridePendingTransition(0, 0); true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    // --- Check permissions and open gallery ---
    private fun checkAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                requestPermissions(arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES), STORAGE_PERMISSION_REQUEST)
            }
        } else {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST)
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // --- Emergency contact ---
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


                    // Make number clickable if number is valid

                    if(number != "Not Available"){
                        emergencyContactView.setOnClickListener {
                            val intent = Intent(Intent.ACTION_DIAL)
                            intent.data = Uri.parse("tel:$number")
                            startActivity(intent)
                        }
                    }
                }
            }
        }.addOnFailureListener {
            emergencyContactView.text = "Emergency Contact: Not Available"
        }
    }

    // --- Handle permission result ---
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        }
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getEmergencyContact()
        }
    }

    // --- Handle gallery result ---
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            val profileImage = findViewById<ImageView>(R.id.profile_image)

            // Show selected image immediately
            Glide.with(this).load(imageUri).circleCrop().into(profileImage)

            // Upload to Firebase Storage
            uploadImageToFirebase()
        }
    }

    // --- Upload image to Firebase ---
    private fun uploadImageToFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val fileRef = storageRef.child("profile_images/$userId.jpg")

        imageUri?.let { uri ->
            fileRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    // Now the file is uploaded, get the download URL
                    fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val dbRef = FirebaseDatabase.getInstance(
                            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
                        ).getReference(userId).child("UserData")

                        dbRef.child("photoUrl").setValue(downloadUri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    // Show error message
                    Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }


}
