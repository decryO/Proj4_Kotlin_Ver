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
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {

    private lateinit var mMap: GoogleMap
    // デフォルトの座標(京都)
    private var latLng = LatLng(34.985458, 135.7577551)
    private var alertRadius: Double = 0.0

    // HeartRails様API URL
    // 都道府県一覧
    private val getToDoHuURL: String = "https://express.heartrails.com/api/json?method=getPrefectures"
    private var selectedTodohu: String = ""
    // 路線一覧
    private val getLineURL: String = "https://express.heartrails.com/api/json?method=getLines&prefecture="
    private var selectedLine: String = ""
    // 駅一覧
    private val getStationURL: String = "https://express.heartrails.com/api/json?method=getStations&line="

    private lateinit var channelID: String
    private lateinit var dialog: DialogFragment
    private lateinit var httpAccessor: HttpAccessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)
        dialog = DialogFragment()
        httpAccessor = HttpAccessor()

        // 単調になるので下に切り分け
        selectToDoHu.setOnClickListener(this)
        selectLine.setOnClickListener(this)
        selectStation.setOnClickListener(this)
        bStart.setOnClickListener(this)

        channelID = getString(R.string.notify_channel_id)
        sliderText.text = "アラートラインのサイズ : " + (alertRadius / 1)
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

    override fun onClick(v: View) {
        val id = v.id
        when(id) {
            R.id.bStart -> setAlarmButtonSelected()
            R.id.selectToDoHu -> tlButtonSelected(getToDoHuURL)
            R.id.selectLine -> tlButtonSelected(getLineURL + selectedTodohu)
            R.id.selectStation -> dialog.show(supportFragmentManager, "simple")
        }
    }

    private fun tlButtonSelected(url: String) {
        println("start")
        ToDoHuText.text = String(httpAccessor.getJson(url))
        println("end")
//        url.httpGet().response { request, response, result ->
//            when(result) {
//                is Result.Success -> {
//                    ToDoHuText.text = String(response.data)
//
//                }
//                is Result.Failure -> {
//                    ToDoHuText.text = "失敗しました"
//                }
//            }
//        }
    }

    private fun setAlarmButtonSelected() {
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
}