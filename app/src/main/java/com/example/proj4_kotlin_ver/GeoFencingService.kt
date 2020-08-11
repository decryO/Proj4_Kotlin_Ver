package com.example.proj4_kotlin_ver

import android.Manifest
import android.app.*
import android.bluetooth.BluetoothA2dp
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class GeoFencingService : Service(), GoogleApiClient.ConnectionCallbacks, HeadSetPlugReceiver.Callback {

    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var geofencingClient: GeofencingClient
    // 通知を作成する際に必要なID
    private lateinit var channelID: String
    private lateinit var channelID2: String

    private val mediaPlayer = MediaPlayer()
    private lateinit var headSetPlugReceiver: HeadSetPlugReceiver
    private lateinit var intentFilter: IntentFilter
    private lateinit var ringtone_uri: Uri

    private var geofenceList = mutableListOf<Geofence>()

    private var station: String? = null
    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private var radius: Float = 0.0F
    // ヘッドホンが接続されているかのフラグ True:接続 false:切断
    private var headSetFlag = false

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet implemented");
    }

    override fun onCreate() {
        super.onCreate()
        googleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .build()
        googleApiClient.connect()
        geofencingClient = LocationServices.getGeofencingClient(this)

        EventBus.getDefault().register(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Foreground Serviceにするため通知を作成する
        channelID = getString(R.string.notify_channel_id1)
        channelID2 = getString(R.string.notify_channel_id2)

        if (intent != null) {
            lat = intent.getDoubleExtra("Lat", 0.0)
            lng = intent.getDoubleExtra("Lng", 0.0)
            radius = intent.getFloatExtra("radius", 0.0F)
            station = intent.getStringExtra("station")
            ringtone_uri = Uri.parse(intent.getStringExtra("ringtone"))
        }

        registerReceiver()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 通知のタイトル
            // アプリごとの通知の詳細設定をする際ここの名前が一覧で出てくるためわかりやすい名前にする
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

    @Subscribe
    fun onEvent(event: GeofenceEvent) {
        // 通知を変えるために前の通知を消す
        stopForeground(true)

        // 有線・無線イヤホン等をしているときに限りアラームを鳴動させる
        if(headSetFlag){
            mediaPlayer.setDataSource(applicationContext, ringtone_uri)
            mediaPlayer.prepare()
            mediaPlayer.start()
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vib: LongArray = longArrayOf(100, 0, 100, 0, 100, 0)

            val name = getString(R.string.notify_enteredStation_name)
            val descriptionText = getString(R.string.notify_enteredStation_description)
            val importance = NotificationManager.IMPORTANCE_HIGH

            val mChannel = NotificationChannel(channelID2, name, importance)
            mChannel.apply {
                description = descriptionText
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                vibrationPattern = vib
                setSound(null, null)
                enableVibration(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)

            // 通知をタップしたときにアプリを起動するために必要
            val stopIntent = Intent(this, ServiceStopBroadcastReceiver::class.java)
            val stopPendingIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0)
            // 空のフルスクリーンIntentを設定することで通知を意図的に消すまで上に残り続ける
            val fullScreenPendingIntent = PendingIntent.getActivity(this, 0, Intent(), 0)

            val notify = NotificationCompat
                .Builder(this, channelID2)
                .apply {
                    setSmallIcon(R.drawable.ic_notify)
                    setContentTitle("${station}${getString(R.string.notify_enteredStation_title)}")
                    setContentText(descriptionText)
                    setCategory(NotificationCompat.CATEGORY_ALARM)
                    setOngoing(true)
                    setVibrate(vib)
                    setFullScreenIntent(fullScreenPendingIntent, true)
                    addAction(0, getString(R.string.alarm_stop), stopPendingIntent)
                }.build()
//            notificationManager.notify(SystemClock.uptimeMillis().toInt(), notify)
            startForeground(2, notify)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        googleApiClient.disconnect()
        unregisterReceiver(headSetPlugReceiver)
        EventBus.getDefault().unregister(this)
        geofencingClient?.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                println("削除官僚！")
            }
            addOnFailureListener {
                println("削除失敗！")
            }
        }
        if(mediaPlayer.isPlaying) mediaPlayer.stop()
    }

    // ヘッドセットが接続・切断されるとheadSetFlagを切り替える
    override fun onEventInvoked(state: Boolean) { headSetFlag = state }

    override fun onConnected(p0: Bundle?) {
        geofenceList.add(Geofence.Builder()
            .setRequestId("geofencing")
            .setCircularRegion(lat, lng, radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build())

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        geofencingClient.addGeofences(getGeofencingRequest(), geofencePendingIntent)?.run {
            addOnSuccessListener { }
            addOnFailureListener { }
        }
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }

    override fun onConnectionSuspended(p0: Int) {}

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
