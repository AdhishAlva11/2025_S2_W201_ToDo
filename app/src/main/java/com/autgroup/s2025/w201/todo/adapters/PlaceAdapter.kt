package com.autgroup.s2025.w201.todo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.classes.PlaceInfo

class PlaceAdapter(
    private val places: MutableList<PlaceInfo>,
    private val onLongClick: (PlaceInfo, Int) -> Unit
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtPlaceName)
        val txtAddress: TextView = itemView.findViewById(R.id.txtPlaceAddress)
        val txtStatus: TextView = itemView.findViewById(R.id.txtPlaceStatus)
        val txtRating: TextView = itemView.findViewById(R.id.txtPlaceRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        val context = holder.itemView.context

        holder.txtName.text = place.name ?: context.getString(R.string.unnamed)
        holder.txtAddress.text = place.address ?: context.getString(R.string.not_available)
        holder.txtStatus.text = place.openStatus ?: context.getString(R.string.not_available)
        holder.txtRating.text = place.rating?.toString() ?: context.getString(R.string.not_available)

        holder.itemView.setOnLongClickListener {
            onLongClick(place, position)
            true
        }
    }

    override fun getItemCount(): Int = places.size
}
