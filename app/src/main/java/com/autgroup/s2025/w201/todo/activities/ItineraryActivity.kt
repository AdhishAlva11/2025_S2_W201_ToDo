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

        // Add Event → show pop-up
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
        val etStartDate = dialogView.findViewById<EditText>(R.id.etStartDate)
        val etDuration = dialogView.findViewById<EditText>(R.id.etDuration)

        AlertDialog.Builder(this)
            .setTitle("Add Event")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etEventName.text.toString()
                val startDate = etStartDate.text.toString()
                val duration = etDuration.text.toString().toIntOrNull() ?: 1

                addItinerary(name, startDate, duration)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addItinerary(name: String, startDate: String, duration: Int) {
        val itinerary = Itinerary(name, startDate, duration)
        itineraries.add(itinerary)

        val itineraryList = findViewById<LinearLayout>(R.id.itineraryList)
        val cardView = layoutInflater.inflate(R.layout.trip_card_template, itineraryList, false)

        val tripTitle = cardView.findViewById<TextView>(R.id.tripTitle)
        val tripDate = cardView.findViewById<TextView>(R.id.tripDate)
        val tripDays = cardView.findViewById<TextView>(R.id.tripDays)
        val tripMenu: ImageButton = cardView.findViewById(R.id.tripMenu2)

        // Set UI values
        tripTitle.text = itinerary.name
        tripDate.text = getString(R.string.trip_start_date, itinerary.startDate)
        tripDays.text = getString(R.string.trip_duration, itinerary.duration)

        // Popup menu for edit/delete
        tripMenu.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.trip_card_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit -> {
                        showEditItineraryDialog(itinerary, cardView)
                        true
                    }
                    R.id.action_delete -> {
                        itineraryList.removeView(cardView)
                        itineraries.remove(itinerary)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        itineraryList.addView(cardView)
        Toast.makeText(this, "Event added!", Toast.LENGTH_SHORT).show()
    }

    private fun showEditItineraryDialog(itinerary: Itinerary, cardView: View) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_event, null)
        val etEventName = dialogView.findViewById<EditText>(R.id.etEventName)
        val etStartDate = dialogView.findViewById<EditText>(R.id.etStartDate)
        val etDuration = dialogView.findViewById<EditText>(R.id.etDuration)

        // Pre-fill existing itinerary data
        etEventName.setText(itinerary.name)
        etStartDate.setText(itinerary.startDate)
        etDuration.setText(itinerary.duration.toString())

        AlertDialog.Builder(this)
            .setTitle("Edit Event")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                // Update itinerary object
                itinerary.name = etEventName.text.toString()
                itinerary.startDate = etStartDate.text.toString()
                itinerary.duration = etDuration.text.toString().toIntOrNull() ?: 1

                // Update the UI
                val tripTitle = cardView.findViewById<TextView>(R.id.tripTitle)
                val tripDate = cardView.findViewById<TextView>(R.id.tripDate)
                val tripDays = cardView.findViewById<TextView>(R.id.tripDays)

                tripTitle.text = itinerary.name
                tripDate.text = getString(R.string.trip_start_date, itinerary.startDate)
                tripDays.text = getString(R.string.trip_duration, itinerary.duration)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}
