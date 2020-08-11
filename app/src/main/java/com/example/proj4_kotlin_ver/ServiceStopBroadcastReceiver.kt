package com.example.proj4_kotlin_ver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.ServiceCompat.stopForeground

class ServiceStopBroadcastReceiver: BroadcastReceiver() {

    // Serviceを停止する
    override fun onReceive(context: Context?, intent: Intent?) {
        val serviceIntent = Intent(context, GeoFencingService::class.java)
        context?.stopService(serviceIntent)
    }
}