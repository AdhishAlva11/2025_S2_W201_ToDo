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

        val place = arguments?.getSerializable(ARG_PLACE) as? PlaceInfo ?: return

        val titleText = view.findViewById<TextView>(R.id.titleText)
        val addressText = view.findViewById<TextView>(R.id.addressText)
        val ratingText = view.findViewById<TextView>(R.id.ratingText)
        val openStatusText = view.findViewById<TextView>(R.id.openStatusText)
        val reviewsText = view.findViewById<TextView>(R.id.reviewsText)
        val addFavButton = view.findViewById<Button>(R.id.addFavouriteButton)
        val addToItineraryBtn = view.findViewById<Button>(R.id.btnAddToItinerary)

        // Populate text views
        titleText.text = place.name ?: "No Name"
        addressText.text = place.address ?: "No Address"
        ratingText.text = "⭐ ${place.rating ?: 0.0}"
        openStatusText.text = place.openStatus ?: "Hours unknown"

        // Show top 3 reviews if available
        reviewsText.text = place.reviews?.take(3)?.joinToString("\n\n") { review ->
            "${review.authorName} ⭐${review.rating}\n${review.text}"
        } ?: "No reviews available"

        firebaseAuth = FirebaseAuth.getInstance()

        // Add to favorites
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

    private fun showChooseItineraryDialog(place: PlaceInfo) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries")

        dbRef.get().addOnSuccessListener { snapshot ->
            val itineraryNames = snapshot.children.map { it.key ?: "" }

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Add to itinerary")
            builder.setItems(itineraryNames.toTypedArray()) { _, which ->
                val selected = itineraryNames[which]
                addActivityToItinerary(place, selected)
            }
            builder.show()
        }
    }

    private fun addActivityToItinerary(place: PlaceInfo, itineraryName: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance(
            "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
        ).getReference("$userId/Itineraries/$itineraryName")

        dbRef.push().setValue(place)
            .addOnSuccessListener {
                Toast.makeText(context, "Added to $itineraryName!", Toast.LENGTH_SHORT).show()
            }
    }
}