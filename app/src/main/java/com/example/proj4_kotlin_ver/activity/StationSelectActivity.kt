package com.example.proj4_kotlin_ver.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import com.example.proj4_kotlin_ver.R
import kotlinx.android.synthetic.main.activity_station_select.*
import kotlinx.android.synthetic.main.activity_station_select.detail_tool_bar
import kotlinx.android.synthetic.main.activity_stations_detail.*

class StationSelectActivity : AppCompatActivity() {

    private lateinit var stations: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_select)

        setSupportActionBar(detail_tool_bar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        stations = intent.extras?.getStringArray("stations") as Array<String>

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stations)
        station_list.adapter = adapter

        station_list.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent()
            intent.putExtra("station", stations[position])
            intent.putExtra("position", position)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) finish()

        return super.onOptionsItemSelected(item)
    }
}