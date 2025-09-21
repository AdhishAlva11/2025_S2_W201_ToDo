package com.autgroup.s2025.w201.todo.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.classes.Favourite

class FavouritesAdapter(
    private val favourites: List<Favourite>
) : RecyclerView.Adapter<FavouritesAdapter.FavouriteViewHolder>() {

    inner class FavouriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtFavTitle)
        val txtAddress: TextView = itemView.findViewById(R.id.txtFavAddress)
        val txtStatus: TextView = itemView.findViewById(R.id.txtFavStatus)
        val txtRating: TextView = itemView.findViewById(R.id.txtFavRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favourite, parent, false)
        return FavouriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {
        val favourite = favourites[position]
        holder.txtName.text = favourite.name
        holder.txtAddress.text = favourite.address
        holder.txtStatus.text = favourite.openStatus
        holder.txtRating.text = favourite.rating?.toString() ?: "N/A"
    }

    override fun getItemCount(): Int = favourites.size
}
