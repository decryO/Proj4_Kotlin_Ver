package com.example.proj4_kotlin_ver

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proj4_kotlin_ver.data.StationData

class SearchRecyclerViewAdapter(stationData: StationData): RecyclerView.Adapter<SearchViewHolder>() {

    private val sResult: StationData = stationData

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_list, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val result = sResult.response.station[position]
        holder.lineText?.text = result.line
        var text = if(result.prev.isNullOrEmpty()) "" else "${result.prev}⇔"
        text += result.name
        text += if(result.next.isNullOrEmpty()) "" else "⇔${result.next}"
        holder.stationsText?.text = text
    }

    override fun getItemCount(): Int {
        return sResult.response.station.size
    }
}