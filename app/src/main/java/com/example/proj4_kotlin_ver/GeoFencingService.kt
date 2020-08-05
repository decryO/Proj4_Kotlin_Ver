package com.example.proj4_kotlin_ver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class GeoFencingService : Service() {

    // 通知を作成する際に必要なID
    private lateinit var channelID: String

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Foreground Serviceにするため通知を作成する
        channelID = getString(R.string.notify_channel_id)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 通知のタイトル
            val name = getString(R.string.notify_name)

            // 通知の説明
            val descriptionText = getString(R.string.notify_description)

            // 通知の重要度 ここでは通知バーに表示されるが音は出ない設定(IMPORTANCE_LOW)
            val importance = NotificationManager.IMPORTANCE_LOW

            val mChannel = NotificationChannel(channelID, name, importance)
            mChannel.apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)

            val notify = NotificationCompat
                .Builder(this, channelID)
                .apply {
                    setSmallIcon(R.drawable.ic_notify)
                    setContentText(descriptionText)
                    setContentTitle(name)
                }.build()
            startForeground(1, notify)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(Service.STOP_FOREGROUND_DETACH)
    }
}
