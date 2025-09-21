package com.autgroup.s2025.w201.todo.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.classes.PlaceInfo
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class BottomSheetInfo : BottomSheetDialogFragment() {

    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_ADDRESS = "address"
        private const val ARG_RATING = "rating"
        private const val ARG_STATUS = "status"
        private const val ARG_LAT = "lat"
        private const val ARG_LNG = "lng"

        fun newInstance(info: PlaceInfo?): BottomSheetInfo {
            val fragment = BottomSheetInfo()
            val args = Bundle()
            args.putString(ARG_NAME, info?.name)
            args.putString(ARG_ADDRESS, info?.address)
            args.putDouble(ARG_RATING, info?.rating ?: 0.0)
            args.putString(ARG_STATUS, info?.openStatus)
            args.putDouble(ARG_LAT, info?.lat ?: 0.0)
            args.putDouble(ARG_LNG, info?.lng ?: 0.0)
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

        val titleText = view.findViewById<TextView>(R.id.titleText)
        val addressText = view.findViewById<TextView>(R.id.addressText)
        val ratingText = view.findViewById<TextView>(R.id.ratingText)
        val openStatusText = view.findViewById<TextView>(R.id.openStatusText)
        val addFavButton = view.findViewById<Button>(R.id.addFavouriteButton)
        val addToItineraryBtn = view.findViewById<Button>(R.id.btnAddToItinerary)

        val name = arguments?.getString(ARG_NAME) ?: "No Name"
        val address = arguments?.getString(ARG_ADDRESS) ?: "No Address"
        val rating = arguments?.getDouble(ARG_RATING, 0.0) ?: 0.0
        val status = arguments?.getString(ARG_STATUS) ?: "Hours unknown"
        val lat = arguments?.getDouble(ARG_LAT, 0.0) ?: 0.0
        val lng = arguments?.getDouble(ARG_LNG, 0.0) ?: 0.0

        titleText.text = name
        addressText.text = address
        ratingText.text = "⭐ $rating"
        openStatusText.text = status

        firebaseAuth = FirebaseAuth.getInstance()

        addFavButton.setOnClickListener {
            val userId = firebaseAuth.currentUser?.uid ?: return@setOnClickListener
            val dbRef = FirebaseDatabase.getInstance(
                "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
            ).getReference("$userId/Favourites")

            // Now you have full PlaceInfo with lat & lng
            val place = PlaceInfo(name, address, rating, status, lat, lng)

            dbRef.push().setValue(place)
                .addOnSuccessListener {
                    Toast.makeText(context, "Added to favourites!", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        addToItineraryBtn.setOnClickListener {
            // Create a PlaceInfo object from the arguments
            val place = PlaceInfo(
                name = arguments?.getString(ARG_NAME) ?: "",
                address = arguments?.getString(ARG_ADDRESS) ?: "",
                rating = arguments?.getDouble(ARG_RATING) ?: 0.0,
                openStatus = arguments?.getString(ARG_STATUS) ?: "",
                lat = arguments?.getDouble("lat") ?: 0.0,
                lng = arguments?.getDouble("lng") ?: 0.0
            )
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
            /*if (itineraryNames.isEmpty()) {
                Toast.makeText(context, "No itineraries yet. Create one first.", Toast.LENGTH_LONG).show()
                return@addOnSuccessListener
            }*/

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