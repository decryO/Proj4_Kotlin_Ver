package com.example.proj4_kotlin_ver

import android.view.LayoutInflater
import android.view.ViewGroup
import android.text.format.DateFormat
import androidx.recyclerview.widget.RecyclerView
import com.example.proj4_kotlin_ver.data.HistoryData
import io.realm.RealmResults

class HistoryRecyclerViewAdapter(realmResults: RealmResults<HistoryData>, private val itemClickListener: HistoryViewHolder.ItemClickListener): RecyclerView.Adapter<HistoryViewHolder>() {

    private var hRecyclerView: RecyclerView? = null
    private val hResult: RealmResults<HistoryData> = realmResults

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_list, parent, false)
        view.setOnClickListener { _ ->
            hRecyclerView?.let {
                itemClickListener.onItemClick(view, it.getChildAdapterPosition(view))
            }
        }
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = hResult[position]
        holder.stationText?.text = history?.station
        holder.lineText?.text = history?.line
        holder.dateText?.text = DateFormat.format("yyyy/MM/dd kk:mm", history?.dateTime)
        holder.radiusText?.text = "半径${history?.radius?.toInt().toString()}メートル"
    }

    override fun getItemCount(): Int {
        return hResult.size
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        hRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        hRecyclerView = null
    }
}