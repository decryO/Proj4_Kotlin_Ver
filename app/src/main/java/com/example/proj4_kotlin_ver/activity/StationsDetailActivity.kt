package com.example.proj4_kotlin_ver.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.example.proj4_kotlin_ver.R
import kotlinx.android.synthetic.main.activity_stations_detail.*

class StationsDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stations_detail)

        setSupportActionBar(detail_tool_bar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        detail_url.setOnClickListener {
            val url = Uri.parse(getString(R.string.heartRailsURL))
            val intent = Intent(Intent.ACTION_VIEW, url)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) finish()

        return super.onOptionsItemSelected(item)
    }
}