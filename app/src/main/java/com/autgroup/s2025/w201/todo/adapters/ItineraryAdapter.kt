package com.autgroup.s2025.w201.todo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.classes.Itinerary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.app.AlertDialog

class ItineraryAdapter(
    private val itineraries: MutableList<Itinerary>,
    private val onClick: (Itinerary) -> Unit
) : RecyclerView.Adapter<ItineraryAdapter.ItineraryViewHolder>() {

    inner class ItineraryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtItineraryName: TextView = itemView.findViewById(R.id.itineraryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItineraryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_itinerary, parent, false)
        return ItineraryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItineraryViewHolder, position: Int) {
        val itinerary = itineraries[position]
        holder.txtItineraryName.text = itinerary.name ?: "Unnamed"

        // Click to open details
        holder.itemView.setOnClickListener {
            onClick(itinerary)
        }

        // Long-click to delete
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete Itinerary")
                .setMessage("Are you sure you want to delete '${itinerary.name}'?")
                .setPositiveButton("Yes") { _, _ ->
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton
                    val dbRef = FirebaseDatabase.getInstance(
                        "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
                    ).getReference("$userId/Itineraries")

                    dbRef.child(itinerary.name ?: "").removeValue()
                    itineraries.removeAt(position)
                    notifyItemRemoved(position)
                }
                .setNegativeButton("No", null)
                .show()
            true
        }
    }

    override fun getItemCount(): Int = itineraries.size
}
