package com.example.proj4_kotlin_ver

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class SelectionRecyclerViewAdapter(itemArray: Array<String>, private val itemClickListener: SelectionViewHolder.ItemClickListener): RecyclerView.Adapter<SelectionViewHolder>() {

    private var selectRecyclerView: RecyclerView? = null
    private val items: Array<String> = itemArray

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.selection_list, parent, false)
        view.setOnClickListener { _ ->
            selectRecyclerView?.let {
                itemClickListener.onItemClick(view, it.getChildAdapterPosition(view))
            }
        }
        return SelectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectionViewHolder, position: Int) {
        val item = items[position]
        holder.listItemText?.text = item
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        selectRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        selectRecyclerView = null
    }

}