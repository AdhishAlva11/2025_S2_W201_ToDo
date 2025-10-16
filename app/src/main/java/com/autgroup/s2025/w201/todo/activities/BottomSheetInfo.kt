package com.autgroup.s2025.w201.todo.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.ThemeUtils
import com.autgroup.s2025.w201.todo.classes.PlaceInfo
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class BottomSheetInfo : BottomSheetDialogFragment() {

    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        private const val ARG_PLACE = "place"

        fun newInstance(info: PlaceInfo): BottomSheetInfo {
            val fragment = BottomSheetInfo()
            val args = Bundle()
            args.putSerializable(ARG_PLACE, info)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply saved theme
        ThemeUtils.applySavedTheme(requireContext())

        firebaseAuth = FirebaseAuth.getInstance()

        // Get views
        val titleText = view.findViewById<TextView>(R.id.titleText)
        val addressText = view.findViewById<TextView>(R.id.addressText)
        val ratingText = view.findViewById<TextView>(R.id.ratingText)
        val openStatusText = view.findViewById<TextView>(R.id.openStatusText)
        val reviewsText = view.findViewById<TextView>(R.id.reviewsText)
        val priceText = view.findViewById<TextView>(R.id.priceText)
        val addFavButton = view.findViewById<Button>(R.id.addFavouriteButton)
        val addToItineraryBtn = view.findViewById<Button>(R.id.btnAddToItinerary)

        val place = arguments?.getSerializable(ARG_PLACE) as? PlaceInfo ?: return

        // Populate text views
        titleText.text = place.name ?: "No Name"
        addressText.text = place.address ?: "No Address"
        ratingText.text = "⭐ ${place.rating ?: 0.0}"
        openStatusText.text = place.openStatus ?: "Hours unknown"
        priceText.text = "💰 ${getPriceText(place.priceLevel)}"

        addressText.paintFlags = addressText.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG

        // Clickable address to open Google Maps
        addressText.setOnClickListener {
            if (place.lat != null && place.lng != null) {
                val gmmIntentUri = Uri.parse("geo:${place.lat},${place.lng}?q=${Uri.encode(place.address)}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                try {
                    startActivity(mapIntent)
                } catch (e: Exception) {
                    val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(place.address)}")
                    startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                }
            } else {
                Toast.makeText(context, "No location data available", Toast.LENGTH_SHORT).show()
            }
        }

        // Show top 3 reviews if available
        reviewsText.text = place.reviews?.take(3)?.joinToString("\n\n") { review ->
            "${review.authorName} ⭐${review.rating}\n${review.text}"
        } ?: "No reviews available"

        // Add to favourites
        addFavButton.setOnClickListener {
            val userId = firebaseAuth.currentUser?.uid ?: return@setOnClickListener
            val dbRef = FirebaseDatabase.getInstance(
                "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
            ).getReference("$userId/Favourites")

            dbRef.push().setValue(place)
                .addOnSuccessListener {
                    Toast.makeText(context, "Added to favourites!", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // Add to itinerary
        addToItineraryBtn.setOnClickListener {
            showChooseItineraryDialog(place)
        }
    }

    private fun getPriceText(level: Int?): String {
        return when (level) {
            0 -> "Free"
            1 -> "$ (Inexpensive)"
            2 -> "$$ (Moderate)"
            3 -> "$$$ (Expensive)"
            4 -> "$$$$ (Very Expensive)"
            else -> "Price info not available"
        }
    }

    private fun showChooseItineraryDialog(place: PlaceInfo) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries")

        dbRef.get().addOnSuccessListener { snapshot ->
            val itineraryNames = snapshot.children.map { it.key ?: "" }

            AlertDialog.Builder(requireContext())
                .setTitle("Choose Itinerary")
                .setItems(itineraryNames.toTypedArray()) { _, which ->
                    val selectedItinerary = itineraryNames[which]
                    showChooseDayDialog(place, selectedItinerary)
                }
                .show()
        }
    }

    private fun showChooseDayDialog(place: PlaceInfo, itineraryName: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries/$itineraryName")

        dbRef.get().addOnSuccessListener { snapshot ->
            val daysCount = snapshot.child("days").getValue(Int::class.java)
            val daysList = mutableListOf<String>()
            if (daysCount != null && daysCount > 0) {
                for (i in 1..daysCount) daysList.add("Day $i")
            } else {
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    if (key.startsWith("Day ", true)) daysList.add(key)
                }
                if (daysList.isEmpty()) daysList.add("Day 1")
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Choose Day")
                .setItems(daysList.toTypedArray()) { _, which ->
                    val selectedDay = daysList[which]
                    addActivityToItinerary(place, itineraryName, selectedDay)
                }
                .show()
        }
    }

    private fun addActivityToItinerary(place: PlaceInfo, itineraryName: String, dayName: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries/$itineraryName/$dayName")

        dbRef.push().setValue(place)
            .addOnSuccessListener {
                Toast.makeText(context, "Added to $itineraryName → $dayName!", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}