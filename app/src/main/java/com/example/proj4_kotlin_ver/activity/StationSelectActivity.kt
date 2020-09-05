package com.example.proj4_kotlin_ver.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proj4_kotlin_ver.R
import com.example.proj4_kotlin_ver.SelectionRecyclerViewAdapter
import com.example.proj4_kotlin_ver.SelectionViewHolder
import kotlinx.android.synthetic.main.activity_station_select.*
import kotlinx.android.synthetic.main.activity_station_select.detail_tool_bar

class StationSelectActivity : AppCompatActivity(), SelectionViewHolder.ItemClickListener {

    private lateinit var stations: Array<String>
    private lateinit var adapter: SelectionRecyclerViewAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_select)

        setSupportActionBar(detail_tool_bar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        stations = intent.extras?.getStringArray("stations") as Array<String>
    }

    override fun onStart() {
        super.onStart()
        layoutManager = LinearLayoutManager(this)
        station_list.layoutManager = layoutManager

        adapter = SelectionRecyclerViewAdapter(stations, this)
        station_list.adapter = this.adapter

        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        station_list.addItemDecoration(itemDecoration)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) finish()

        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(itemView: View, position: Int) {
        val intent = Intent()
        intent.putExtra("station", stations[position])
        intent.putExtra("position", position)
        setResult(RESULT_OK, intent)
        finish()
    }
}