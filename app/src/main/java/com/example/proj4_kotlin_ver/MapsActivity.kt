package com.example.proj4_kotlin_ver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    // デフォルトの座標(京都)
    private var latLng = LatLng(34.985458, 135.7577551)
    private var alertRadius: Double = 0.0
    private lateinit var channelID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)

        channelID = getString(R.string.notify_channel_id)
        sliderText.text = "アラートラインのサイズ : " + (alertRadius / 1)

        bStart.setOnClickListener{
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
                notificationManager.notify(1, notify)
            }
        }

        selectToDoHu.setOnClickListener{
            ToDoHuText.text = "都道府県選択！！"
        }

        selectLine.setOnClickListener{
            lineText.text = "路線選択！！"
        }

        selectStation.setOnClickListener {
            stationText.text = "駅選択！！"
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(35.02139, 135.75556)
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13F))


        // スライダーが操作され、値が変更されたとき。
        slider.addOnChangeListener { slider, value, fromUser ->
            alertRadius = slider.value.toDouble()
            sliderText.text = "アラートラインのサイズ : " + (alertRadius / 1)

            mMap.clear()

            mMap.addCircle(CircleOptions()
                .center(latLng)
                .radius(alertRadius)
                .strokeColor(Color.RED)
                .fillColor(0x220000FF)
                .strokeWidth(5F)
            )
        }
    }
}