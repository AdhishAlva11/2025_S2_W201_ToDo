package com.autgroup.s2025.w201.todo.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.classes.Itinerary

class ItineraryAdapter(
    private val itineraries: List<Itinerary>,
    private val onItemClick: (Itinerary) -> Unit
) : RecyclerView.Adapter<ItineraryAdapter.ItineraryViewHolder>() {

    inner class ItineraryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itineraryName: TextView = itemView.findViewById(R.id.itineraryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItineraryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_itinerary, parent, false)
        return ItineraryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItineraryViewHolder, position: Int) {
        val itinerary = itineraries[position]
        holder.itineraryName.text = itinerary.name

        holder.itemView.setOnClickListener {
            onItemClick(itinerary)
        }
    }

    override fun getItemCount(): Int = itineraries.size
}
