package com.example.proj4_kotlin_ver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_alerm_stop.*

class AlermStopActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alerm_stop)

        alermStop.setOnClickListener {
            val serviceIntent = Intent(this, GeoFencingService::class.java)
            stopService(serviceIntent)
            val activityIntent = Intent(this, MapsActivity::class.java)
            startActivity(activityIntent)

            finish()
        }
    }
}