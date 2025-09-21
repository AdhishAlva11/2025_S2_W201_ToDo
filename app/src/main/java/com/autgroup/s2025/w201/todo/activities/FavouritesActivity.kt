package com.autgroup.s2025.w201.todo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autgroup.s2025.w201.todo.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.autgroup.s2025.w201.todo.classes.Favourite
import com.autgroup.s2025.w201.todo.activities.FavouritesAdapter

class FavouritesActivity : AppCompatActivity() {

    // Firebase references
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // RecyclerView and adapter
    private lateinit var recyclerView: RecyclerView
    private val favouritesList = mutableListOf<Favourite>()
    private lateinit var adapter: FavouritesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_favourites)

        // Setup Firebase authentication and database
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Setup RecyclerView with adapter
        recyclerView = findViewById(R.id.recyclerFavourites)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FavouritesAdapter(favouritesList) { favourite ->
            // Handle user tapping a favourite → go to detail/location screen
            val intent = Intent(this@FavouritesActivity, DisplayMapActivity::class.java).apply {
                putExtra("placeName", favourite.title)
                putExtra("lat", favourite.lat)
                putExtra("lng", favourite.lng)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Apply system bar padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Back button logic
        val backButton: ImageButton = findViewById(R.id.btnBack)
        backButton.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
        }

        // Footer navigation
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
                R.id.nav_favourites -> true
                else -> false
            }
        }
        bottomNav.selectedItemId = R.id.nav_favourites

        // Load favourites for the logged-in user
        loadFavourites()
    }

    // Retrieve favourites from Firebase Realtime Database
    private fun loadFavourites() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val favouritesRef = database.child("users").child(userId).child("favourites")

        favouritesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favouritesList.clear()
                for (favSnapshot in snapshot.children) {
                    val favourite = favSnapshot.getValue(Favourite::class.java)
                    if (favourite != null) {
                        favouritesList.add(favourite)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error loading favourites: ${error.message}")
            }
        })
    }
}
