package com.autgroup.s2025.w201.todo.adapters

import android.app.AlertDialog
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
    private val onLongClick: (PlaceInfo, Int) -> Unit,
    private val onToggleCompleted: (PlaceInfo) -> Unit
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

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
        val context = holder.itemView.context

        holder.txtName.text = place.name ?: context.getString(R.string.unnamed)
        holder.txtAddress.text = place.address ?: context.getString(R.string.not_available)
        holder.txtStatus.text = place.openStatus ?: context.getString(R.string.not_available)
        holder.txtRating.text = place.rating?.toString() ?: context.getString(R.string.not_available)

        // Set checkbox and strike-through
        holder.chkCompleted.isChecked = place.completed
        holder.txtName.paintFlags = if (place.completed)
            holder.txtName.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        else
            holder.txtName.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()

        // Checkbox click listener
        holder.chkCompleted.setOnClickListener {
            onToggleCompleted(place)
        }

        // Long click for deletion with confirmation dialog
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete Activity")
                .setMessage("Are you sure you want to delete '${place.name}'?")
                .setPositiveButton("Yes") { _, _ ->
                    onLongClick(place, position)
                }
                .setNegativeButton("No", null)
                .show()
            true
        }
    }

    override fun getItemCount(): Int = places.size
}
