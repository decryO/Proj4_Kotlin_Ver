package com.example.proj4_kotlin_ver

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proj4_kotlin_ver.data.StationData

class SearchRecyclerViewAdapter(stationData: StationData, private val itemClickListener: SearchViewHolder.ItemClickListener): RecyclerView.Adapter<SearchViewHolder>() {

    private var sRecyclerView: RecyclerView? = null
    private val sResult: StationData = stationData

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_list, parent, false)
        view.setOnClickListener { _ ->
            sRecyclerView?.let {
                itemClickListener.onItemClick(view, it.getChildAdapterPosition(view))
            }
        }
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

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        sRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        sRecyclerView = null
    }
}