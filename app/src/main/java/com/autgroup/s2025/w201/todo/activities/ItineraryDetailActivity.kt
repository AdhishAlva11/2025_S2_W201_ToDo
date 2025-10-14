package com.autgroup.s2025.w201.todo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.ThemeUtils
import com.autgroup.s2025.w201.todo.adapters.DayAdapter
import com.autgroup.s2025.w201.todo.adapters.PlaceAdapter
import com.autgroup.s2025.w201.todo.classes.PlaceInfo
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ItineraryDetailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlaceAdapter
    private val activities = mutableListOf<PlaceInfo>()

    private lateinit var recyclerDays: RecyclerView
    private lateinit var daysAdapter: DayAdapter
    private val daysList = mutableListOf<String>()
    private var selectedDay: String = ""

    private lateinit var itineraryName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply dark/light theme before inflating the layout
        ThemeUtils.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_itinerary_detail)

        // Get itinerary name passed from previous screen
        itineraryName = intent.getStringExtra("itineraryName") ?: return

        // ---------------- Toolbar setup (XML back arrow + centered title) ----------------
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.setOnClickListener {
            val intent = Intent(this, ItineraryActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        val tvTitle = findViewById<TextView>(R.id.tvItineraryTitle)
        tvTitle.text = itineraryName
        // -------------------------------------------------------------------------------

        // ---------------- Days RecyclerView ----------------
        recyclerDays = findViewById(R.id.recyclerDays)
        recyclerDays.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        daysAdapter = DayAdapter(daysList) { dayName ->
            selectedDay = dayName
            loadActivities(dayName)
        }
        recyclerDays.adapter = daysAdapter

        // ---------------- Activities RecyclerView ----------------
        recyclerView = findViewById(R.id.recyclerViewItinerary)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PlaceAdapter(activities) { place, position ->
            AlertDialog.Builder(this)
                .setTitle("Delete Activity")
                .setMessage("Are you sure you want to delete '${place.name}' from $selectedDay?")
                .setPositiveButton("Delete") { _, _ ->
                    deletePlace(place, position)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        recyclerView.adapter = adapter

        // Populate days and load the first day's data
        loadDays()

        // ---------------- Bottom Navigation ----------------
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
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

    // ---------------- Firebase data loading ----------------
    private fun loadDays() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries/$itineraryName")

        dbRef.get().addOnSuccessListener { snapshot ->
            daysList.clear()

            // Prefer explicit "days" property if present
            val daysCount = snapshot.child("days").getValue(Int::class.java)
            if (daysCount != null && daysCount > 0) {
                for (i in 1..daysCount) daysList.add("Day $i")
            } else {
                // fallback: infer Day keys that start with "Day "
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    if (key.startsWith("Day ", ignoreCase = true)) {
                        daysList.add(key)
                    }
                }
                if (daysList.isEmpty()) {
                    daysList.add("Day 1") // default single day
                } else {
                    // sort Day 1, Day 2, ...
                    daysList.sortWith(compareBy {
                        it.removePrefix("Day ").trim().toIntOrNull() ?: Int.MAX_VALUE
                    })
                }
            }

            daysAdapter.notifyDataSetChanged()

            // Auto-select first day
            selectedDay = daysList.first()
            loadActivities(selectedDay)
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to load days: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadActivities(day: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries/$itineraryName/$day")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                activities.clear()
                for (child in snapshot.children) {
                    val place = child.getValue(PlaceInfo::class.java)
                    if (place != null) activities.add(place)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ItineraryDetailActivity,
                    "Failed to load activities: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun deletePlace(place: PlaceInfo, position: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries/$itineraryName/$selectedDay")

        dbRef.orderByChild("name").equalTo(place.name).get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { it.ref.removeValue() }
            activities.removeAt(position)
            adapter.notifyItemRemoved(position)
            Toast.makeText(this, "${place.name} removed", Toast.LENGTH_SHORT).show()
        }
    }
}
