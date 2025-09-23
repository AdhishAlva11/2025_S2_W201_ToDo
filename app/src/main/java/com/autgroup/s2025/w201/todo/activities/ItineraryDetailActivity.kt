package com.autgroup.s2025.w201.todo.activities

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.adapters.DayAdapter
import com.autgroup.s2025.w201.todo.adapters.PlaceAdapter
import com.autgroup.s2025.w201.todo.classes.PlaceInfo
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
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_itinerary_detail)

        itineraryName = intent.getStringExtra("itineraryName") ?: return

        // Back button
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        // Title
        val tvTitle = findViewById<TextView>(R.id.tvItineraryTitle)
        tvTitle.text = itineraryName

        // Days RecyclerView (horizontal)
        recyclerDays = findViewById(R.id.recyclerDays)
        recyclerDays.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        daysAdapter = DayAdapter(daysList) { dayName ->
            selectedDay = dayName
            loadActivities(dayName)
        }
        recyclerDays.adapter = daysAdapter

        // Activities RecyclerView
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

        // Populate day chips then load day 1
        loadDays()
    }

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
                    // default single day
                    daysList.add("Day 1")
                } else {
                    // sort Day 1, Day 2 ...
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
                Toast.makeText(this@ItineraryDetailActivity,
                    "Failed to load activities: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun deletePlace(place: PlaceInfo, position: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries/$itineraryName/$selectedDay")

        // remove children whose name equals place.name
        dbRef.orderByChild("name").equalTo(place.name).get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { it.ref.removeValue() }
            activities.removeAt(position)
            adapter.notifyItemRemoved(position)
            Toast.makeText(this, "${place.name} removed", Toast.LENGTH_SHORT).show()
        }
    }
}
