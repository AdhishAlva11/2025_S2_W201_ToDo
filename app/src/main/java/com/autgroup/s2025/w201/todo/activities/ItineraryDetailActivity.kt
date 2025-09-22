package com.autgroup.s2025.w201.todo.activities

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.classes.PlaceInfo
import com.autgroup.s2025.w201.todo.adapters.PlaceAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ItineraryDetailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlaceAdapter
    private val activities = mutableListOf<PlaceInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_itinerary_detail)

        val itineraryName = intent.getStringExtra("itineraryName") ?: "Itinerary"

        // Set title as itinerary name
        val tvTitle = findViewById<TextView>(R.id.tvItineraryTitle) // add a TextView in XML for title
        tvTitle.text = itineraryName

        // Back button
        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.setOnClickListener {
            finish() // Simply finish this activity to go back
        }

        recyclerView = findViewById(R.id.recyclerViewItinerary)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PlaceAdapter(activities)
        recyclerView.adapter = adapter

        loadActivities(itineraryName)
    }

    private fun loadActivities(itineraryName: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries/$itineraryName")

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
                Toast.makeText(this@ItineraryDetailActivity, "Failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }


}
