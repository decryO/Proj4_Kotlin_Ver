package com.example.proj4_kotlin_ver

import android.app.*
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class GeoFencingService : Service(), HeadSetPlugReceiver.Callback {

    // 通知を作成する際に必要なID
    private lateinit var channelID: String
    private lateinit var headSetPlugReceiver: HeadSetPlugReceiver
    private lateinit var intentFilter: IntentFilter
    private var station: String? = null
    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private var radius: Double = 0.0
    // ヘッドホンが接続されているかのフラグ True:接続 false:切断
    private var headSetFlag = false

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet implemented");
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Foreground Serviceにするため通知を作成する
        channelID = getString(R.string.notify_channel_id)

        if (intent != null) {
            lat = intent.getDoubleExtra("Lat", 0.0)
            lng = intent.getDoubleExtra("Lng", 0.0)
            radius = intent.getDoubleExtra("radius", 0.0)
            station = intent.getStringExtra("station")
        }

        registerReceiver()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 通知のタイトル
            val name = getString(R.string.notify_name)

            // 通知の説明
            val descriptionText = "${station}${getString(R.string.notify_description)}"

            // 通知の重要度 ここでは通知バーに表示されるが音は出ない設定(IMPORTANCE_LOW)
            val importance = NotificationManager.IMPORTANCE_LOW

            val mChannel = NotificationChannel(channelID, name, importance)
            mChannel.apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)

            // 通知をタップしたときにアプリを起動するために必要
            val returnIntent = Intent(this, MainActivity::class.java)
            val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(this)
            stackBuilder.addNextIntentWithParentStack(returnIntent)
            val pendingIntent: PendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

            val notify = NotificationCompat
                .Builder(this, channelID)
                .apply {
                    setSmallIcon(R.drawable.ic_notify)
                    setContentText(descriptionText)
                    setContentTitle(name)
                    setContentIntent(pendingIntent)
                }.build()
            startForeground(1, notify)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(headSetPlugReceiver)
    }

    override fun onEventInvoked(state: Boolean) {
        headSetFlag = state
        if(state) {
            println("接続されている")
        }else{
            println("切断されている")
        }
    }

    // ヘッドホンが接続されたことを感知するブロードキャストレシーバーを追加する
    private fun registerReceiver() {
        headSetPlugReceiver = HeadSetPlugReceiver()
        intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG)
        intentFilter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED);
        registerReceiver(headSetPlugReceiver, intentFilter)
    }
}
