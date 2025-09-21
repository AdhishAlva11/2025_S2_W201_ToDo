package com.autgroup.s2025.w201.todo.activities

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.classes.Favourite
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class FavouritesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var favouritesAdapter: FavouritesAdapter
    private val favouritesList = mutableListOf<Favourite>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourites)

        recyclerView = findViewById(R.id.recyclerFavourites)
        recyclerView.layoutManager = LinearLayoutManager(this)

        favouritesAdapter = FavouritesAdapter(favouritesList)
        recyclerView.adapter = favouritesAdapter

        loadFavouritesFromFirebase()

        // Optional: Back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Optional: Bottom navigation handling
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Navigate to Home
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFavouritesFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().getReference("favourites").child(userId)

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favouritesList.clear()
                for (childSnapshot in snapshot.children) {
                    val favourite = childSnapshot.getValue(Favourite::class.java)
                    favourite?.let { favouritesList.add(it) }
                }
                favouritesAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FavouritesActivity, "Failed to load favourites.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
