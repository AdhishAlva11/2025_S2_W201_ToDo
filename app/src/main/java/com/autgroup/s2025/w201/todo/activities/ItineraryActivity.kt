package com.autgroup.s2025.w201.todo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.adapters.ItineraryAdapter
import com.autgroup.s2025.w201.todo.classes.Itinerary
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ItineraryActivity : AppCompatActivity() {

    private val itineraries = mutableListOf<Itinerary>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItineraryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_itinerary)

        recyclerView = findViewById(R.id.recyclerItineraries)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ItineraryAdapter(itineraries) { itinerary ->
            // On click: open itinerary details
            val intent = Intent(this, ItineraryDetailActivity::class.java)
            intent.putExtra("itineraryName", itinerary.name)
            startActivity(intent)
        }
        recyclerView.adapter = adapter



        // Add itinerary button
        findViewById<TextView>(R.id.tvAddEvent).setOnClickListener {
            showAddItineraryDialog()
        }

        // Load itineraries from Firebase
        loadItineraries()

        // Bottom navigation
        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomePageActivity::class.java))
                    true
                }
                R.id.nav_itinerary -> true
                R.id.nav_favourites -> {
                    startActivity(Intent(this, FavouritesActivity::class.java))
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
        bottomNav.selectedItemId = R.id.nav_itinerary
    }

    private fun showAddItineraryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_event, null)
        val etEventName = dialogView.findViewById<TextView>(R.id.etEventName)

        AlertDialog.Builder(this)
            .setTitle("Add Itinerary")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etEventName.text.toString().trim()
                if (name.isNotEmpty()) addItinerary(name)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addItinerary(name: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries")

        // Save as an empty itinerary (object)
        dbRef.child(name).setValue(name)
            .addOnSuccessListener {
                Toast.makeText(this, "Itinerary '$name' created!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadItineraries() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itineraries.clear()
                for (child in snapshot.children) {
                    val name = child.key ?: continue
                    itineraries.add(Itinerary(name))
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ItineraryActivity, "Failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
