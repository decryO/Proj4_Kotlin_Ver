package com.example.proj4_kotlin_ver

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.history_list.view.*

class HistoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    var stationText: TextView? = null
    var lineText: TextView? = null
    var dateText: TextView? = null
    var radiusText: TextView? = null

    init {
        stationText = itemView.stationText
        lineText = itemView.lineText
        dateText = itemView.dateText
        radiusText = itemView.radiusText
    }

    interface ItemClickListener {
        fun onItemClick(itemView: View, position: Int)
    }
}