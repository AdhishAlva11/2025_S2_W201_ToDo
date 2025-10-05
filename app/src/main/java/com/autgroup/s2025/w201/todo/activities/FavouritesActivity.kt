package com.autgroup.s2025.w201.todo.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.adapters.FavouritesAdapter
import com.autgroup.s2025.w201.todo.classes.Favourite
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FavouritesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var favouritesAdapter: FavouritesAdapter
    private val favouritesList = mutableListOf<Favourite>()
    private val filteredList = mutableListOf<Favourite>()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var spinnerCountryFilter: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourites)

        firebaseAuth = FirebaseAuth.getInstance()
        spinnerCountryFilter = findViewById(R.id.spinnerCountryFilter)

        // Adapter must use filteredList!
        recyclerView = findViewById(R.id.recyclerFavourites)
        favouritesAdapter = FavouritesAdapter(filteredList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = favouritesAdapter

        setupBottomNavigation()
        loadFavourites()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_favourites
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomePageActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_favourites -> true
                R.id.nav_itinerary -> {
                    startActivity(Intent(this, ItineraryActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFavourites() {
        val userId = firebaseAuth.currentUser?.uid ?: return
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

                // Setup spinner after data loads
                setupCountryFilter()

                // Initially show all favourites
                applyFilter("All")
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

    private fun setupCountryFilter() {
        val countries = favouritesList.mapNotNull { it.country }.distinct().sorted()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("All") + countries)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCountryFilter.adapter = adapter

        spinnerCountryFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position) as String
                applyFilter(selected)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun applyFilter(selectedCountry: String) {
        filteredList.clear()
        if (selectedCountry == "All") {
            filteredList.addAll(favouritesList)
        } else {
            filteredList.addAll(favouritesList.filter { it.country == selectedCountry })
        }

        // Sort by country then name
        filteredList.sortWith(compareBy({ it.country?.lowercase() ?: "" }, { it.name?.lowercase() ?: "" }))
        favouritesAdapter.notifyDataSetChanged()

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No favourites added yet", Toast.LENGTH_SHORT).show()
        }
    }
}