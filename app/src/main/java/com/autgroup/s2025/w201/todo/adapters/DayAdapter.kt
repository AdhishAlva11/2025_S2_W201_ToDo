package com.autgroup.s2025.w201.todo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.autgroup.s2025.w201.todo.R

class DayAdapter(
    private val days: MutableList<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDay: TextView = itemView.findViewById(R.id.tvDay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val dayName = days[position]
        holder.tvDay.text = dayName
        holder.itemView.setOnClickListener { onClick(dayName) }
    }

    override fun getItemCount(): Int = days.size

    // Function to update the list dynamically
    fun updateList(newItems: List<String>) {
        days.clear()
        days.addAll(newItems)
        notifyDataSetChanged()
    }
}
