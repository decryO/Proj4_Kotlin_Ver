package com.example.proj4_kotlin_ver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver: BroadcastReceiver() {

    private lateinit var geoCallback: GeoCallback

    interface GeoCallback {
        fun enteredStation()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            println("指定範囲に入りました")
        }
    }
}