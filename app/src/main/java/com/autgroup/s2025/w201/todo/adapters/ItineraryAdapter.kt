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
        val context = holder.itemView.context

        // Localized fallback name
        holder.txtItineraryName.text = itinerary.name ?: context.getString(R.string.unnamed)

        // --- Click to open itinerary details ---
        holder.itemView.setOnClickListener {
            onClick(itinerary)
        }

        // --- Long-click to delete itinerary ---
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.delete_itinerary_title))
                .setMessage(context.getString(R.string.delete_itinerary_message, itinerary.name))
                .setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton
                    val dbRef = FirebaseDatabase.getInstance(
                        "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
                    ).getReference("$userId/Itineraries")

                    //  Delete using ID instead of name
                    val itineraryId = itinerary.id
                    if (!itineraryId.isNullOrEmpty()) {
                        dbRef.child(itineraryId).removeValue()
                            .addOnSuccessListener {
                                if (position in itineraries.indices) {
                                    itineraries.removeAt(position)
                                    notifyItemRemoved(position)
                                }
                            }
                            .addOnFailureListener { e ->
                                android.widget.Toast.makeText(
                                    context,
                                    context.getString(R.string.failed_delete_itinerary, e.message),
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            context.getString(R.string.invalid_itinerary_id),
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton(context.getString(R.string.no), null)
                .show()
            true
        }
    }

    override fun getItemCount(): Int = itineraries.size
}
