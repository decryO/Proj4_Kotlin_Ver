package com.example.proj4_kotlin_ver

import android.Manifest
import android.app.*
import android.bluetooth.BluetoothA2dp
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.*
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
    private lateinit var headSetPlugReceiver: HeadSetPlugReceiver
    private lateinit var intentFilter: IntentFilter

    private var geofenceList = mutableListOf<Geofence>()

    private var station: String? = null
    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private var radius: Float = 0.0F
    // ヘッドホンが接続されているかのフラグ True:接続 false:切断
    private var headSetFlag = false

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        println("こねくてっど！")
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
        channelID = getString(R.string.notify_channel_id)

        if (intent != null) {
            lat = intent.getDoubleExtra("Lat", 0.0)
            lng = intent.getDoubleExtra("Lng", 0.0)
            radius = intent.getFloatExtra("radius", 0.0F)
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

    @Subscribe
    fun onEvent(event: GeofenceEvent) {
        println("とうちゃく！！")
        stopForeground(true)
        println("通知削除！！")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 通知のタイトル
            val name = "駅に到着しました！！"

            // 通知の説明
            val descriptionText = "停止を押すとアラームを停止します"

            // 通知の重要度 ここでは通知バーに表示されるが音は出ない設定(IMPORTANCE_LOW)
            val importance = NotificationManager.IMPORTANCE_HIGH

            val mChannel = NotificationChannel("dasfgsefa", name, importance)
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
                .Builder(this, "dasfgsefa")
                .apply {
                    setSmallIcon(R.drawable.ic_notify)
                    setContentText(descriptionText)
                    setContentTitle(name)
                    setContentIntent(pendingIntent)
                    setAutoCancel(true)
                }.build()
            notificationManager.notify(SystemClock.uptimeMillis().toInt(), notify)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(headSetPlugReceiver)
        EventBus.getDefault().unregister(this)
    }

    override fun onEventInvoked(state: Boolean) {
        headSetFlag = state
        if(state) {
            println("接続されている")
        }else{
            println("切断されている")
        }
    }

    override fun onConnected(p0: Bundle?) {
        println("こねくてっど！")
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
            addOnSuccessListener {
                println("あははははっははははははは")
            }
            addOnFailureListener {
                println("っかかあっかかかかっかっかか")
            }
        }
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }

    override fun onConnectionSuspended(p0: Int) {

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
