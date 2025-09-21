package com.autgroup.s2025.w201.todo.activities

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.classes.Favourite
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FavouritesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var favouritesAdapter: FavouritesAdapter
    private val favouritesList = mutableListOf<Favourite>()
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourites)

        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize RecyclerView and Adapter
        recyclerView = findViewById(R.id.recyclerFavourites)
        favouritesAdapter = FavouritesAdapter(favouritesList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = favouritesAdapter

        // Optional: Back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Load favourites from Firebase
        loadFavourites()
    }

    private fun loadFavourites() {
        val userId = firebaseAuth.currentUser?.uid ?: return

        // Correct Firebase Realtime Database reference
        val favouritesRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Favourites")

        favouritesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favouritesList.clear()
                for (childSnapshot in snapshot.children) {
                    val favourite = childSnapshot.getValue(Favourite::class.java)
                    favourite?.let { favouritesList.add(it) }
                }
                Log.d("FavouritesActivity", "Loaded ${favouritesList.size} favourites")
                favouritesAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@FavouritesActivity,
                    "Failed to load favourites: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("FavouritesActivity", "Firebase error: ${error.message}")
            }
        })
    }
}
