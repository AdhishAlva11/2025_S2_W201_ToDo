package com.autgroup.s2025.w201.todo.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.classes.Favourite
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FavouritesAdapter(
    private val favourites: MutableList<Favourite>
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
        holder.txtRating.text = favourite.rating?.toString() ?: holder.itemView.context.getString(R.string.not_available)

        holder.itemView.setOnLongClickListener {
            val context = holder.itemView.context
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.delete_favourite_title))
                .setMessage(context.getString(R.string.delete_favourite_message, favourite.name))
                .setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton
                    val dbRef = FirebaseDatabase.getInstance(
                        "https://todoauthentication-9a630-default-rtdb.firebaseio.com/"
                    ).getReference("$userId/Favourites")

                    // Delete from Firebase
                    dbRef.orderByChild("name").equalTo(favourite.name)
                        .get().addOnSuccessListener { snapshot ->
                            snapshot.children.forEach { it.ref.removeValue() }
                        }

                    // Delete locally
                    favourites.removeAt(position)
                    notifyItemRemoved(position)
                }
                .setNegativeButton(context.getString(R.string.no), null)
                .show()
            true
        }
    }

    override fun getItemCount(): Int = favourites.size
}
