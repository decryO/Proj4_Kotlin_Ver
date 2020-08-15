package com.example.proj4_kotlin_ver.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.proj4_kotlin_ver.service.GeoFencingService

class ServiceStopBroadcastReceiver: BroadcastReceiver() {

    // Serviceを停止する
    override fun onReceive(context: Context?, intent: Intent?) {
        val serviceIntent = Intent(context, GeoFencingService::class.java)
        context?.stopService(serviceIntent)
    }
}