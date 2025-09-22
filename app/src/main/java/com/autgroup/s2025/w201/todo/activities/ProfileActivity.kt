package com.autgroup.s2025.w201.todo.activities

import android.content.Intent
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

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        window.decorView.systemUiVisibility = 0

        val userName = findViewById<TextView>(R.id.user_name)
        val profileImage = findViewById<ImageView>(R.id.profile_image)
        val logoutButton = findViewById<Button>(R.id.btnLogout)

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
                    overridePendingTransition(0, 0) // no flicker animation
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
                R.id.nav_profile -> true // Already here
                else -> false
            }
        }
    }
}
