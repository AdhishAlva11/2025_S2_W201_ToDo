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
import com.google.firebase.database.FirebaseDatabase

class ItineraryDetailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlaceAdapter
    private val activities = mutableListOf<PlaceInfo>()

    private lateinit var recyclerDays: RecyclerView
    private lateinit var daysAdapter: DayAdapter
    private val daysList = mutableListOf<String>()
    private var selectedDay: String = ""

    private lateinit var itineraryId: String
    private lateinit var itineraryName: String
    private val firebaseUrl = "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
    private val database by lazy { FirebaseDatabase.getInstance(firebaseUrl) }

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
        itineraryId = intent.getStringExtra("itineraryId") ?: return
        itineraryName = intent.getStringExtra("itineraryName") ?: "Itinerary"

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            startActivity(Intent(this, ItineraryActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
            finish()
        }

        val tvTitle = findViewById<TextView>(R.id.tvItineraryTitle)
        tvTitle.text = itineraryName
        // ------------------------------------------------

        setupDaysRecycler()
        setupActivitiesRecycler()
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
                    true
                }
                else -> false
            }
        }
        bottomNav.selectedItemId = R.id.nav_itinerary
    }

    // ---------------- Recycler setups ----------------
    private fun setupDaysRecycler() {
        recyclerDays = findViewById(R.id.recyclerDays)
        recyclerDays.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        daysAdapter = DayAdapter(daysList) { dayName ->
            selectedDay = dayName
            loadActivities(dayName)
        }
        recyclerDays.adapter = daysAdapter
    }

    private fun setupActivitiesRecycler() {
        recyclerView = findViewById(R.id.recyclerViewItinerary)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = PlaceAdapter(
            activities,
            itineraryName,
            selectedDay,
            onLongClick = { place, position ->
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.delete_activity_title))
                    .setMessage(getString(R.string.delete_activity_message, place.name, selectedDay))
                    .setPositiveButton(getString(R.string.delete)) { _, _ ->
                        deletePlace(place, position)
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            },
            onToggleCompleted = { place ->
                toggleCompletion(place)
            }
        )

        recyclerView.adapter = adapter
    }

    // ---------------- Firebase: Load Days ----------------
    private fun loadDays() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = database.getReference("$userId/Itineraries/$itineraryId")

        dbRef.get().addOnSuccessListener { snapshot ->
            daysList.clear()

            val daysCount = snapshot.child("days").getValue(Int::class.java)
            if (daysCount != null && daysCount > 0) {
                for (i in 1..daysCount) {
                    daysList.add(getString(R.string.day_format, i))
                }
            } else {
                // fallback if missing "days" field
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    if (key.startsWith("day_", ignoreCase = true)) {
                        val num = key.removePrefix("day_").toIntOrNull() ?: continue
                        daysList.add(getString(R.string.day_format, num))
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
        val dayKey = "Day $dayNumber"

        val dbRef = database.getReference("$userId/Itineraries/$itineraryId/$dayKey")

        dbRef.get().addOnSuccessListener { snapshot ->
            activities.clear()
            snapshot.children.forEach { child ->
                val place = child.getValue(PlaceInfo::class.java)
                place?.let {
                    it.firebaseKey = child.key
                    activities.add(it)
                }
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener { e ->
            Toast.makeText(
                this,
                getString(R.string.load_activities_failed, e.message),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // ---------------- Firebase: Delete Activity ----------------
    private fun deletePlace(place: PlaceInfo, position: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val key = place.firebaseKey ?: return
        val dbRef = database.getReference("$userId/Itineraries/$itineraryId/$selectedDay/$key")

        dbRef.removeValue()
            .addOnSuccessListener {
                activities.removeAt(position)
                adapter.notifyItemRemoved(position)
                Toast.makeText(this, "${place.name} removed", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to remove place: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ---------------- Toggle completion ----------------
    private fun toggleCompletion(place: PlaceInfo) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val key = place.firebaseKey ?: return
        val dbRef = database.getReference("$userId/Itineraries/$itineraryId/$selectedDay/$key")

        val updatedPlace = place.copy(completed = !place.completed)
        val index = activities.indexOfFirst { it.firebaseKey == place.firebaseKey }

        if (index != -1) {
            activities[index] = updatedPlace
            adapter.notifyItemChanged(index)
        }

        dbRef.child("completed").setValue(updatedPlace.completed)
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ---------------- Helper ----------------
    private fun getUserId(): String? = FirebaseAuth.getInstance().currentUser?.uid
}
