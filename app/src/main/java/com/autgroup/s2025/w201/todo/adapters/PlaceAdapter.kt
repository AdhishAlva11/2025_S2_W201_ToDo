package com.autgroup.s2025.w201.todo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.classes.PlaceInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class PlaceAdapter(
    private val places: MutableList<PlaceInfo>,
    private val itineraryName: String,
    private val selectedDay: String,
    private val onLongClick: (PlaceInfo, Int) -> Unit
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    private val firebaseUrl = "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtPlaceName)
        val txtAddress: TextView = itemView.findViewById(R.id.txtPlaceAddress)
        val txtStatus: TextView = itemView.findViewById(R.id.txtPlaceStatus)
        val txtRating: TextView = itemView.findViewById(R.id.txtPlaceRating)
        val chkCompleted: CheckBox = itemView.findViewById(R.id.chkCompleted)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]

        // Display place details
        holder.txtName.text = place.name ?: ""
        holder.txtAddress.text = place.address ?: ""
        holder.txtStatus.text = place.openStatus ?: ""
        holder.txtRating.text = place.rating?.toString() ?: "N/A"

        // Set checkbox and strike-through
        holder.chkCompleted.isChecked = place.isCompleted
        updateStrikeThrough(holder.txtName, place.isCompleted)

        // Checkbox click listener
        holder.chkCompleted.setOnClickListener {
            place.isCompleted = holder.chkCompleted.isChecked
            updateStrikeThrough(holder.txtName, place.isCompleted)

            // Update Firebase under correct day and unique key
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            place.firebaseKey?.let { key ->
                val dbRef = FirebaseDatabase.getInstance(firebaseUrl)
                    .getReference("$userId/Itineraries/$itineraryName/$selectedDay/$key")
                dbRef.child("isCompleted").setValue(place.isCompleted)
            }
        }

        // Long click for deletion
        holder.itemView.setOnLongClickListener {
            onLongClick(place, position)
            true
        }
    }

    private fun updateStrikeThrough(textView: TextView, completed: Boolean) {
        textView.paintFlags = if (completed)
            textView.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        else
            textView.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }

    override fun getItemCount(): Int = places.size
}
