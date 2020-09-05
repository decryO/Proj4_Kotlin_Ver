package com.example.proj4_kotlin_ver

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.selection_list.view.*

class SelectionViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    var listItemText: TextView? = null

    init {
        listItemText = itemView.listItemText
    }

    interface ItemClickListener {
        fun onItemClick(itemView: View, position: Int)
    }
}