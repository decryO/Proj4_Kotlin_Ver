package com.example.proj4_kotlin_ver

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.search_list.view.*

class SearchViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    var lineText: TextView? = null
    var stationsText: TextView? = null

    init {
        lineText = itemView.lineText
        stationsText = itemView.stationsText
    }
}