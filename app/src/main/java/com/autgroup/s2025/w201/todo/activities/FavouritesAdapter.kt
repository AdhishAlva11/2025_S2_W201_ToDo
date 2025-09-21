package com.autgroup.s2025.w201.todo.activities

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.autgroup.s2025.w201.todo.R
import com.google.android.gms.maps.model.LatLng
import com.autgroup.s2025.w201.todo.classes.*

class FavouritesAdapter(
    private val favourites: List<Favourite>,
    private val onItemClick: (Favourite) -> Unit
) : RecyclerView.Adapter<FavouritesAdapter.FavouriteViewHolder>() {

    class FavouriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.txtFavTitle)
        val address: TextView = itemView.findViewById(R.id.txtFavAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favourite, parent, false)
        return FavouriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {
        val favourite = favourites[position]
        holder.title.text = favourite.title
        holder.address.text = favourite.address

        holder.itemView.setOnClickListener {
            onItemClick(favourite)  // Pass clicked favourite back to activity
        }
    }

    override fun getItemCount(): Int = favourites.size
}