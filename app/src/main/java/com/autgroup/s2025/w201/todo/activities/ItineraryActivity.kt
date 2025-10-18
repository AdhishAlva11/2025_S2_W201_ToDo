package com.autgroup.s2025.w201.todo.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.ThemeUtils
import com.autgroup.s2025.w201.todo.LocaleUtils
import com.autgroup.s2025.w201.todo.adapters.ItineraryAdapter
import com.autgroup.s2025.w201.todo.classes.Itinerary
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ItineraryActivity : AppCompatActivity() {

    private val itineraries = mutableListOf<Itinerary>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItineraryAdapter

    // Ensure locale is applied before onCreate()
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleUtils.applySavedLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme before view inflation
        ThemeUtils.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_itinerary)

        recyclerView = findViewById(R.id.recyclerItineraries)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ItineraryAdapter(itineraries) { itinerary ->
            val intent = Intent(this, ItineraryDetailActivity::class.java)
            intent.putExtra("itineraryId", itinerary.id)   // pass ID
            intent.putExtra("itineraryName", itinerary.name)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // --- Filter spinner setup ---
        val spinnerFilter: Spinner = findViewById(R.id.spinnerFilter)
        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> itineraries.sortBy { it.name?.lowercase() ?: "" }          // A → Z
                    1 -> itineraries.sortByDescending { it.name?.lowercase() ?: "" } // Z → A
                    2 -> itineraries.sortBy { it.name?.length ?: 0 }               // Short → Long
                }
                adapter.notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // --- Add itinerary button ---
        findViewById<TextView>(R.id.tvAddEvent).setOnClickListener {
            showAddItineraryDialog()
        }

        // --- Load itineraries ---
        loadItineraries()

        // --- Bottom navigation setup ---
        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomePageActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_itinerary -> true
                R.id.nav_favourites -> {
                    startActivity(Intent(this, FavouritesActivity::class.java))
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
        bottomNav.selectedItemId = R.id.nav_itinerary
    }

    // --- Dialog to add new itinerary ---
    private fun showAddItineraryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_event, null)
        val etEventName = dialogView.findViewById<TextView>(R.id.etEventName)
        val etDays = dialogView.findViewById<TextView>(R.id.etDays)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_itinerary_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                val name = etEventName.text.toString().trim()
                val daysStr = etDays.text.toString().trim()
                val days = daysStr.toIntOrNull() ?: 1
                if (name.isNotEmpty()) addItinerary(name, days)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    // --- Add itinerary using unique ID (no overwriting) ---
    // --- Add itinerary using itinerary name as the key ---
    private fun addItinerary(name: String, days: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries")

        // Generate a unique ID for this itinerary
        val itineraryId = dbRef.push().key ?: return

        // Prepare the data with the name included
        val itineraryData = mutableMapOf<String, Any>()
        itineraryData["id"] = itineraryId       // unique ID
        itineraryData["name"] = name            // required for BottomSheetInfo
        itineraryData["days"] = days

        // Create empty day objects with "Day 1" format
        for (i in 1..days) {
            val dayKey = "Day$i"
            itineraryData[dayKey] = mapOf<String, Any>()
        }

        // Save under the generated ID
        dbRef.child(itineraryId).setValue(itineraryData)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    getString(R.string.itinerary_created, name),
                    Toast.LENGTH_SHORT
                ).show()
                // Refresh the local list to ensure BottomSheetInfo sees it
                loadItineraries()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    getString(R.string.failed_create_itinerary, it.message),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // --- Load itineraries with ID + name ---
    // --- Load itineraries (using itinerary name as the key) ---
    private fun loadItineraries() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itineraries.clear()
                for (itinerarySnapshot in snapshot.children) {
                    val id = itinerarySnapshot.child("id").getValue(String::class.java) ?: continue
                    val name = itinerarySnapshot.child("name").getValue(String::class.java) ?: continue
                    val days = itinerarySnapshot.child("days").getValue(Int::class.java) ?: 0

                    // Count activities across all days
                    var activityCount = 0
                    for (daySnapshot in itinerarySnapshot.children) {
                        if (daySnapshot.key?.startsWith("day_") == true) {
                            activityCount += daySnapshot.childrenCount.toInt()
                        }
                    }

                    itineraries.add(Itinerary(id = id, name = name, days = days, activityCount = activityCount))
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ItineraryActivity,
                    getString(R.string.failed_load_itineraries, error.message),
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}
