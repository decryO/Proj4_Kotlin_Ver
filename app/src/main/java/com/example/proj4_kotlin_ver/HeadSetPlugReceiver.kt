package com.example.proj4_kotlin_ver

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class HeadSetPlugReceiver: BroadcastReceiver() {

    private lateinit var callback: Callback

    interface Callback {
        fun onEventInvoked(state: Boolean)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        val extras = intent?.extras
        callback = context as Callback

        // Bluetoothヘッドセット・ヘッドホンが接続されている
        if (action == BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED) {
            val state = extras!!.getInt(BluetoothProfile.EXTRA_STATE)

            if(state == BluetoothProfile.STATE_CONNECTED) {
                callback.onEventInvoked(true)
            }else {
                callback.onEventInvoked(false)
            }
            return
        }

        // 有線ヘッドセット・ヘッドホンが接続されている
        if(action == Intent.ACTION_HEADSET_PLUG) {
            val state = intent.getIntExtra("state", -1)

            if(state == 1) {
                callback.onEventInvoked(true)
            } else {
                callback.onEventInvoked(false)
            }
            return
        }
    }
}