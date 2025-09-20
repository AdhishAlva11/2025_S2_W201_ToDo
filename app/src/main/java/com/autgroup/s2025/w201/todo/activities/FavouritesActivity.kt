package com.autgroup.s2025.w201.todo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.autgroup.s2025.w201.todo.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavouritesActivity : AppCompatActivity() {

    // Firebase instances
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_favourites)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Back button
        val backButton: ImageButton = findViewById(R.id.btnBack)
        backButton.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
        }

        // Add Favourite (currently logs click)
        val addFavourite: TextView = findViewById(R.id.tvAddFavourite)
        addFavourite.setOnClickListener {
            println("Add Favourite clicked (not functional yet)")
        }

        // Footer Navigation
        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomePageActivity::class.java))
                    true
                }
                R.id.nav_itinerary -> {
                    startActivity(Intent(this, ItineraryActivity::class.java))
                    true
                }
                R.id.nav_favourites -> true // already here
                else -> false
            }
        }
        bottomNav.selectedItemId = R.id.nav_favourites

        // Load user's favourites from Firestore
        loadFavourites()
    }

    private fun loadFavourites() {
        val userId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .collection("favourites")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val title = doc.getString("title") ?: "Untitled"
                    val address = doc.getString("address") ?: "Unknown"
                    val openHours = doc.getString("openHours") ?: "-"
                    val rating = doc.getDouble("starRating") ?: 0.0

                    // For now just log the favourite
                    println("Favourite: $title, $address, $openHours, $rating")
                }
            }
            .addOnFailureListener { e ->
                println("Error loading favourites: $e")
            }
    }
}