package com.autgroup.s2025.w201.todo.adapters

import android.app.AlertDialog
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.classes.Itinerary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ItineraryAdapter(
    private val itineraries: MutableList<Itinerary>,
    private val onClick: (Itinerary) -> Unit
) : RecyclerView.Adapter<ItineraryAdapter.ItineraryViewHolder>() {

    inner class ItineraryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtItineraryName: TextView = itemView.findViewById(R.id.itineraryName)
        val btnMarkCompleted: Button = itemView.findViewById(R.id.btnMarkCompleted)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItineraryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_itinerary, parent, false)
        return ItineraryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItineraryViewHolder, position: Int) {
        val itinerary = itineraries[position]
        holder.txtItineraryName.text = itinerary.name ?: "Unnamed"

        // Temporary "completed" flag (not saved in DB)
        var isCompleted = false

        // Handle Mark as Completed button
        holder.btnMarkCompleted.setOnClickListener {
            isCompleted = !isCompleted

            if (isCompleted) {
                holder.txtItineraryName.paintFlags =
                    holder.txtItineraryName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                holder.btnMarkCompleted.text = "Completed ✅"
                holder.btnMarkCompleted.isEnabled = false // Optional: disable after marking
            } else {
                holder.txtItineraryName.paintFlags =
                    holder.txtItineraryName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                holder.btnMarkCompleted.text = "Mark as Completed"
                holder.btnMarkCompleted.isEnabled = true
            }
        }

        // Click open details
        holder.itemView.setOnClickListener {
            onClick(itinerary)
        }

        // Long-click delete
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