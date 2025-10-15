package com.autgroup.s2025.w201.todo.activities

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
        ThemeUtils.applySavedTheme(requireContext())

        val place = arguments?.getSerializable(ARG_PLACE) as? PlaceInfo ?: return

        val titleText = view.findViewById<TextView>(R.id.titleText)
        val addressText = view.findViewById<TextView>(R.id.addressText)
        val ratingText = view.findViewById<TextView>(R.id.ratingText)
        val openStatusText = view.findViewById<TextView>(R.id.openStatusText)
        val reviewsText = view.findViewById<TextView>(R.id.reviewsText)
        val priceText = view.findViewById<TextView>(R.id.priceText)
        val addFavButton = view.findViewById<Button>(R.id.addFavouriteButton)
        val addToItineraryBtn = view.findViewById<Button>(R.id.btnAddToItinerary)

        // Populate text views
        titleText.text = place.name ?: getString(R.string.no_name)
        addressText.text = place.address ?: getString(R.string.no_address)
        ratingText.text = getString(R.string.rating_with_star, place.rating ?: 0.0)
        openStatusText.text = place.openStatus ?: getString(R.string.hours_unknown)
        priceText.text = getString(R.string.price_with_symbol, getPriceText(place.priceLevel))
        reviewsText.text = place.reviews?.take(3)?.joinToString("\n\n") { review ->
            "${review.authorName} ⭐${review.rating}\n${review.text}"
        } ?: getString(R.string.no_reviews_available)

        firebaseAuth = FirebaseAuth.getInstance()

        // Add to favourites
        addFavButton.setOnClickListener {
            val userId = firebaseAuth.currentUser?.uid ?: return@setOnClickListener
            val dbRef = FirebaseDatabase.getInstance(
                "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
            ).getReference("$userId/Favourites")

            dbRef.push().setValue(place)
                .addOnSuccessListener {
                    Toast.makeText(context, getString(R.string.added_to_favourites), Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, getString(R.string.failed_generic, e.message), Toast.LENGTH_LONG).show()
                }
        }

        // Add to itinerary
        addToItineraryBtn.setOnClickListener {
            showChooseItineraryDialog(place)
        }
    }

    private fun getPriceText(level: Int?): String {
        return when (level) {
            0 -> getString(R.string.price_free)
            1 -> getString(R.string.price_inexpensive)
            2 -> getString(R.string.price_moderate)
            3 -> getString(R.string.price_expensive)
            4 -> getString(R.string.price_very_expensive)
            else -> getString(R.string.price_not_available)
        }
    }

    private fun showChooseItineraryDialog(place: PlaceInfo) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries")

        dbRef.get().addOnSuccessListener { snapshot ->
            val itineraryNames = snapshot.children.map { it.key ?: "" }

            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.choose_itinerary))
                .setItems(itineraryNames.toTypedArray()) { _, which ->
                    val selectedItinerary = itineraryNames[which]
                    showChooseDayDialog(place, selectedItinerary)
                }
                .show()
        }
    }

    private fun showChooseDayDialog(place: PlaceInfo, itineraryName: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries/$itineraryName")

        dbRef.get().addOnSuccessListener { snapshot ->
            val daysCount = snapshot.child("days").getValue(Int::class.java)
            val daysList = mutableListOf<String>()

            if (daysCount != null && daysCount > 0) {
                for (i in 1..daysCount) daysList.add(getString(R.string.day_number, i))
            } else {
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    if (key.startsWith("Day ", true)) daysList.add(key)
                }
                if (daysList.isEmpty()) daysList.add(getString(R.string.day_number, 1))
            }

            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.choose_day))
                .setItems(daysList.toTypedArray()) { _, which ->
                    val selectedDay = daysList[which]
                    addActivityToItinerary(place, itineraryName, selectedDay)
                }
                .show()
        }
    }

    private fun addActivityToItinerary(place: PlaceInfo, itineraryName: String, dayName: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries/$itineraryName/$dayName")

        dbRef.push().setValue(place)
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    getString(R.string.added_to_itinerary, itineraryName, dayName),
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, getString(R.string.failed_generic, e.message), Toast.LENGTH_LONG).show()
            }
    }
}
