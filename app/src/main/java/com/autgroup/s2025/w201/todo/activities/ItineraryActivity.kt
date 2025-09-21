package com.autgroup.s2025.w201.todo.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.classes.Itinerary
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ItineraryActivity : AppCompatActivity() {

    // In-memory list of itineraries
    private val itineraries = mutableListOf<Itinerary>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_itinerary)

        // Back button → go to HomePageActivity
        val backButton: ImageButton = findViewById(R.id.btnBack)
        backButton.setOnClickListener {
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Add Event show pop-up
        val addEvent: TextView = findViewById(R.id.tvAddEvent)
        addEvent.setOnClickListener {
            showAddEventDialog()
        }

        // ─── Bottom Navigation Setup ───
        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomePageActivity::class.java))
                    true
                }

                R.id.nav_itinerary -> {
                    // Only start if we’re NOT already in ItineraryActivity
                    true
                }

                R.id.nav_favourites -> {
                    startActivity(Intent(this, FavouritesActivity::class.java))
                    true
                }

                else -> false
            }
        }

        // Optionally: mark current menu as selected
        bottomNav.selectedItemId = R.id.nav_itinerary
    }


    private fun showAddEventDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_event, null)
        val etEventName = dialogView.findViewById<EditText>(R.id.etEventName)

        AlertDialog.Builder(this)
            .setTitle("Add Itinerary")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etEventName.text.toString()
                addItinerary(name)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun addItinerary(name: String) {
        // Save to Firebase as an empty itinerary
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries")

        dbRef.child(name).setValue(name)
            .addOnSuccessListener {
                Toast.makeText(this, "Itinerary '$name' created!", Toast.LENGTH_SHORT).show()
            }

        // Also add to your local list so you can show it instantly
        itineraries.add(Itinerary(name)) // your Itinerary model can just be name now

        // Add a card to the UI
        val itineraryList = findViewById<LinearLayout>(R.id.itineraryList)
        val cardView = layoutInflater.inflate(R.layout.trip_card_template, itineraryList, false)
        val tripTitle = cardView.findViewById<TextView>(R.id.tripTitle)
        tripTitle.text = name
        itineraryList.addView(cardView)
    }


    private fun showEditItineraryDialog(itinerary: Itinerary, cardView: View) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_event, null)
        val etEventName = dialogView.findViewById<EditText>(R.id.etEventName)


        // Pre-fill existing itinerary data
        etEventName.setText(itinerary.name)

        AlertDialog.Builder(this)
            .setTitle("Edit Event")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                // Update itinerary object
                itinerary.name = etEventName.text.toString()

                // Update the UI
                val tripTitle = cardView.findViewById<TextView>(R.id.tripTitle)
                val tripDate = cardView.findViewById<TextView>(R.id.tripDate)
                val tripDays = cardView.findViewById<TextView>(R.id.tripDays)

                tripTitle.text = itinerary.name
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}
