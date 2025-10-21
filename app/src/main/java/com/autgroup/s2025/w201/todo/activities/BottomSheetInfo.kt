package com.autgroup.s2025.w201.todo.activities

import android.content.Context
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

    // --- Correct locale handling for Fragments ---
    override fun onAttach(context: Context) {
        super.onAttach(context)
        val localizedContext = com.autgroup.s2025.w201.todo.LocaleUtils.applySavedLocale(context)
        val newConfig = localizedContext.resources.configuration
        requireContext().resources.updateConfiguration(
            newConfig,
            localizedContext.resources.displayMetrics
        )
    }

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
        val shareLocationBtn = view.findViewById<Button>(R.id.btnShareLocation)

        // --- Populate place info ---
        titleText.text = place.name ?: getString(R.string.no_name)
        addressText.text = place.address ?: getString(R.string.no_address)
        ratingText.text = getString(R.string.rating_with_star, place.rating ?: 0.0)
        openStatusText.text = place.openStatus ?: getString(R.string.hours_unknown)
        priceText.text = getString(R.string.price_with_symbol, getPriceText(place.priceLevel))
        reviewsText.text = place.reviews?.take(3)?.joinToString("\n\n") { review ->
            "${review.authorName} ⭐${review.rating}\n${review.text}"
        } ?: getString(R.string.no_reviews_available)

        // ✅ --- Make address clickable to open in Google Maps ---
        addressText.setOnClickListener {
            if (place.lat != null && place.lng != null) {
                // Open Google Maps at the specific coordinates
                val gmmIntentUri = Uri.parse("geo:${place.lat},${place.lng}?q=${Uri.encode(place.address)}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")

                try {
                    startActivity(mapIntent)
                } catch (e: Exception) {
                    // Fallback: open in browser if Maps app isn’t available
                    val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(place.address)}")
                    startActivity(Intent(Intent.ACTION_VIEW, webUri))
                }
            } else {
                Toast.makeText(context, getString(R.string.invalid_coordinates), Toast.LENGTH_SHORT).show()
            }
        }

        firebaseAuth = FirebaseAuth.getInstance()

        // --- Add to favourites ---
        addFavButton.setOnClickListener {
            val userId = firebaseAuth.currentUser?.uid ?: return@setOnClickListener
            val dbRef = FirebaseDatabase.getInstance(
                "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
            ).getReference("$userId/Favourites")

            dbRef.push().setValue(place)
                .addOnSuccessListener {
                    Toast.makeText(
                        context,
                        getString(R.string.added_to_favourites),
                        Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        getString(R.string.failed_generic, e.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        // --- Add to itinerary ---
        addToItineraryBtn.setOnClickListener {
            showChooseItineraryDialog(place)
        }

        // --- Share location link ---
        shareLocationBtn.setOnClickListener {
            val placeName = place.name ?: getString(R.string.unknown_place)
            val placeAddress = place.address ?: return@setOnClickListener

            // Create a Google Maps link
            val mapsLink = "https://www.google.com/maps/search/?api=1&query=${placeAddress.replace(" ", "+")}"

            // Create share intent
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    android.content.Intent.EXTRA_TEXT,
                    getString(R.string.share_message, placeName, mapsLink)
                )
            }

            // Launch share chooser
            startActivity(android.content.Intent.createChooser(shareIntent, getString(R.string.share_via)))
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

    // --- Step 1: Choose itinerary ---
    private fun showChooseItineraryDialog(place: PlaceInfo) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries")

        dbRef.get().addOnSuccessListener { snapshot ->
            val itineraryNames = mutableListOf<String>()
            val itineraryIds = mutableListOf<String>()

            for (child in snapshot.children) {
                val id = child.key ?: continue
                val name = child.child("name").getValue(String::class.java)
                if (name.isNullOrBlank() || id.startsWith("day_", true)) continue
                itineraryIds.add(id)
                itineraryNames.add(name)
            }

            if (itineraryIds.isEmpty()) {
                Toast.makeText(context, getString(R.string.no_itineraries_found), Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.choose_itinerary))
                .setItems(itineraryNames.toTypedArray()) { _, which ->
                    val selectedId = itineraryIds[which]
                    val selectedName = itineraryNames[which]
                    showChooseDayDialog(place, selectedId, selectedName)
                }
                .show()
        }
    }

    // --- Step 2: Choose day ---
    private fun showChooseDayDialog(place: PlaceInfo, itineraryId: String, itineraryName: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries/$itineraryId")

        dbRef.get().addOnSuccessListener { snapshot ->
            val daysCount = snapshot.child("days").getValue(Int::class.java)
            val daysList = mutableListOf<Pair<String, String>>()

            if (daysCount != null && daysCount > 0) {
                for (i in 1..daysCount) {
                    val dayKey = "Day$i"
                    val dayLabel = getString(R.string.day_number, i)
                    daysList.add(Pair(dayKey, dayLabel))
                }
            } else {
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    if (key.startsWith("Day", ignoreCase = true)) {
                        val num = key.removePrefix("Day").toIntOrNull() ?: 1
                        val dayLabel = getString(R.string.day_number, num)
                        daysList.add(Pair(key, dayLabel))
                    }
                }
            }

            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.choose_day))
                .setItems(daysList.map { it.second }.toTypedArray()) { _, which ->
                    val (dayKey, dayLabel) = daysList[which]
                    addActivityToItinerary(place, itineraryId, itineraryName, dayKey, dayLabel)
                }
                .show()
        }
    }

    // --- Step 3: Add place to itinerary/day ---
    private fun addActivityToItinerary(
        place: PlaceInfo,
        itineraryId: String,
        itineraryName: String,
        dayKey: String,
        dayLabel: String
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries/$itineraryId/$dayKey")

        dbRef.push().setValue(place)
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    getString(R.string.added_to_itinerary, itineraryName, dayLabel),
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    getString(R.string.failed_generic, e.message),
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}