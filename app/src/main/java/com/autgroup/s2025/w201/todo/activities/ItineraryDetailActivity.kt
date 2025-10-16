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
    private val firebaseUrl = "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"

    private val database by lazy { FirebaseDatabase.getInstance(firebaseUrl) }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_itinerary_detail)

        itineraryName = intent.getStringExtra("itineraryName") ?: return

        setupToolbar()
        setupDaysRecycler()
        setupActivitiesRecycler()
        loadDays()
        setupBottomNavigation()
    }

    // --------------------- Setup UI Components ---------------------
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.setOnClickListener {
            startActivity(Intent(this, ItineraryActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
            finish()
        }

        val tvTitle = findViewById<TextView>(R.id.tvItineraryTitle)
        tvTitle.text = itineraryName
    }

    private fun setupDaysRecycler() {
        recyclerDays = findViewById(R.id.recyclerDays)
        recyclerDays.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
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
            itineraryName = itineraryName,
            selectedDay = selectedDay,
            onLongClick = { place, position -> deletePlace(place, position) }
        )
        recyclerView.adapter = adapter
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, HomePageActivity::class.java)); true }
                R.id.nav_itinerary -> true
                R.id.nav_favourites -> { startActivity(Intent(this, FavouritesActivity::class.java)); true }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); overridePendingTransition(0, 0); true }
                else -> false
            }
        }
        bottomNav.selectedItemId = R.id.nav_itinerary
    }

    // --------------------- Firebase Methods ---------------------
    private fun getUserId(): String? = FirebaseAuth.getInstance().currentUser?.uid

    private fun loadDays() {
        val userId = getUserId() ?: return
        val dbRef = database.getReference("$userId/Itineraries/$itineraryName")

        dbRef.get().addOnSuccessListener { snapshot ->
            daysList.clear()

            val daysCount = snapshot.child("days").getValue(Int::class.java)
            if (daysCount != null && daysCount > 0) {
                for (i in 1..daysCount) daysList.add("Day $i")
            } else if (daysList.isEmpty()) {
                daysList.add("Day 1")
            }

            daysAdapter.notifyDataSetChanged()
            selectedDay = daysList.first()
            loadActivities(selectedDay)
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to load days: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadActivities(day: String) {
        val userId = getUserId() ?: return
        val dbRef = database.getReference("$userId/Itineraries/$itineraryName/$day")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                activities.clear()
                snapshot.children.forEach { child ->
                    val place = child.getValue(PlaceInfo::class.java)
                    place?.let {
                        it.firebaseKey = child.key
                        activities.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ItineraryDetailActivity, "Failed to load activities: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    fun addPlace(place: PlaceInfo) {
        val userId = getUserId() ?: return
        val dbRef = database.getReference("$userId/Itineraries/$itineraryName/$selectedDay")
        val key = dbRef.push().key ?: return

        place.firebaseKey = key
        dbRef.child(key).setValue(place)
            .addOnSuccessListener {
                activities.add(place)
                adapter.notifyItemInserted(activities.size - 1)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add place: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun deletePlace(place: PlaceInfo, position: Int) {
        val userId = getUserId() ?: return
        val key = place.firebaseKey ?: return
        val dbRef = database.getReference("$userId/Itineraries/$itineraryName/$selectedDay/$key")

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

    fun toggleCompletion(place: PlaceInfo) {
        val userId = getUserId() ?: return
        val key = place.firebaseKey ?: return
        val dbRef = database.getReference("$userId/Itineraries/$itineraryName/$selectedDay/$key")

        place.isCompleted = !place.isCompleted
        dbRef.child("isCompleted").setValue(place.isCompleted)
            .addOnSuccessListener { adapter.notifyDataSetChanged() }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
