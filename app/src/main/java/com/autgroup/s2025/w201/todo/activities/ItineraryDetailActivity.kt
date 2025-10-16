package com.autgroup.s2025.w201.todo.activities

import android.content.Context
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
import com.autgroup.s2025.w201.todo.LocaleUtils
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

    // Apply saved locale before layout inflation
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleUtils.applySavedLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme before UI creation
        ThemeUtils.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_itinerary_detail)

        // ---------------- Toolbar setup ----------------
        itineraryName = intent.getStringExtra("itineraryName") ?: return
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
        // ------------------------------------------------

        // ---------------- Days RecyclerView ----------------
        recyclerDays = findViewById(R.id.recyclerDays)
        recyclerDays.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
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
                .setTitle(getString(R.string.delete_activity_title))
                .setMessage(getString(R.string.delete_activity_message, place.name, selectedDay))
                .setPositiveButton(getString(R.string.delete)) { _, _ ->
                    deletePlace(place, position)
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
        recyclerView.adapter = adapter

        // --- STEP 1 + 2: Run migration once before loading days ---
        migrateOldDayKeys()
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

    // --- Temporary migration (rename "Day 1" → "day_1") ---
    private fun migrateOldDayKeys() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries/$itineraryName")

        dbRef.get().addOnSuccessListener { snapshot ->
            for (child in snapshot.children) {
                val key = child.key ?: continue
                if (key.startsWith("Day ", ignoreCase = true)) {
                    val number = key.removePrefix("Day ").trim().toIntOrNull() ?: continue
                    val newKey = "day_$number"

                    val data = child.value
                    dbRef.child(newKey).setValue(data)
                    dbRef.child(key).removeValue()
                }
            }
        }
    }

    // ---------------- Firebase: Load Days ----------------
    private fun loadDays() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries/$itineraryName")

        dbRef.get().addOnSuccessListener { snapshot ->
            daysList.clear()

            val daysCount = snapshot.child("days").getValue(Int::class.java)
            if (daysCount != null && daysCount > 0) {
                for (i in 1..daysCount) {
                    val label = getString(R.string.day_format, i) // e.g., “Day 1”, “第 1 天”
                    daysList.add(label)
                }
            } else {
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    if (key.startsWith("day_", ignoreCase = true)) {
                        val num = key.removePrefix("day_").toIntOrNull() ?: continue
                        val label = getString(R.string.day_format, num)
                        daysList.add(label)
                    }
                }
                if (daysList.isEmpty()) {
                    daysList.add(getString(R.string.day_format, 1))
                }
            }

            daysAdapter.notifyDataSetChanged()
            selectedDay = daysList.first()
            loadActivities(selectedDay)
        }.addOnFailureListener { e ->
            Toast.makeText(this, getString(R.string.load_days_failed, e.message), Toast.LENGTH_LONG).show()
        }
    }

    // ---------------- Firebase: Load Activities ----------------
    private fun loadActivities(day: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val dayNumber = day.filter { it.isDigit() }.toIntOrNull() ?: 1
        val dayKey = "day_$dayNumber"

        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries/$itineraryName/$dayKey")

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
                    getString(R.string.load_activities_failed, error.message),
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    // ---------------- Firebase: Delete Activity ----------------
    private fun deletePlace(place: PlaceInfo, position: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val dayNumber = selectedDay.filter { it.isDigit() }.toIntOrNull() ?: 1
        val dayKey = "day_$dayNumber"

        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries/$itineraryName/$dayKey")

        dbRef.orderByChild("name").equalTo(place.name).get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { it.ref.removeValue() }
            activities.removeAt(position)
            adapter.notifyItemRemoved(position)
            Toast.makeText(this, getString(R.string.activity_removed, place.name), Toast.LENGTH_SHORT).show()
        }
    }
}
