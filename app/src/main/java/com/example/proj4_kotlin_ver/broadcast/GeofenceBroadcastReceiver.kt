package com.example.proj4_kotlin_ver.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.proj4_kotlin_ver.GeofenceEvent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import org.greenrobot.eventbus.EventBus

class GeofenceBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) return

        val geofenceTransition = geofencingEvent.geofenceTransition

        // HeadsetPlugreceiverのようにcontext as callbackとするとエラーになるのでEventBusを使用する
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            EventBus.getDefault().post(GeofenceEvent())
        }
    }
}