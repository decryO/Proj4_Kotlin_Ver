package com.example.proj4_kotlin_ver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.widget.addTextChangedListener
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_maps.*
import org.json.JSONArray
import org.json.JSONObject

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {

    private lateinit var mMap: GoogleMap
    // デフォルトの座標(京都)
    private var latLng = LatLng(34.985458, 135.7577551)
    private var alertRadius: Double = 0.0

    private var selectedPrefecture: String = ""
    private var selectedLine: String = ""
    // HeartRails様API URL 路線一覧
    private val getLineURL: String = "https://express.heartrails.com/api/json?method=getLines&prefecture="
    // HeartRails様API URL 駅一覧
    private val getStationURL: String = "https://express.heartrails.com/api/json?method=getStations&line="

    // 都道府県リスト
    private lateinit var prefecturesArray: Array<String>
    // 路線リスト
    private lateinit var lineArray: Array<String>
    // 駅リスト
    private lateinit var stationData: StationData

    private lateinit var channelID: String
    private lateinit var myDialog: MyDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)

        myDialog = MyDialogFragment()

        // 単調になるので下に切り分け
        selectPrefecture.setOnClickListener(this)
        selectLine.setOnClickListener(this)
        selectStation.setOnClickListener(this)
        bStart.setOnClickListener(this)

        channelID = getString(R.string.notify_channel_id)
        sliderText.text = "半径${alertRadius}メートルに入ると通知します"

        prefecturesArray = resources.getStringArray(R.array.prefectures)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13F))

        // スライダーが操作され、値が変更されたとき。
        slider.addOnChangeListener { slider, _, _ ->
            alertRadius = slider.value.toDouble()
            sliderText.text = "半径${alertRadius}メートルに入ると通知します"

            mMap.clear()

            mMap.addCircle(CircleOptions()
                .center(latLng)
                .radius(alertRadius)
                .strokeColor(Color.RED)
                .fillColor(0x220000FF)
                .strokeWidth(5F)
            )
        }

        stationText.addTextChangedListener(object : CustomTextWatcher{
            override fun afterTextChanged(s: Editable?) {
                println("Changed!!")
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13F))
            }
        })
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.selectPrefecture -> {
                val prefecturesArray: Array<String> = resources.getStringArray(R.array.prefectures)
                openListDialog(prefecturesArray, 1)
            }
            R.id.selectLine -> if(!selectedPrefecture.isNullOrEmpty()) lineButtonSelected(getLineURL + selectedPrefecture)
            R.id.selectStation -> if(!selectedLine.isNullOrEmpty()) stationButtonSelected(getStationURL + selectedLine)
            R.id.bStart -> setAlarmButtonSelected()
        }
    }

    private fun lineButtonSelected(url: String) {
        url.httpGet().responseJson { _, _, result ->
            when(result) {
                is Result.Success -> {
                    val responseJson = result.get()
                    lineArray = emptyArray<String>()
                    val lineObj: JSONArray = (responseJson.obj()["response"] as JSONObject).get("line") as JSONArray
                    lineArray = Array(lineObj.length()) {
                        lineObj.getString(it)
                    }

                    openListDialog(lineArray, 2)
                }
                is Result.Failure -> { }
            }
        }
    }

    private fun stationButtonSelected(url: String) {
        url.httpGet().responseString { _, _, result ->
            when(result) {
                is Result.Success -> {
                    val mapper = jacksonObjectMapper()
                    stationData = mapper.readValue<StationData>(result.value)
                    var nameArray: Array<String> = emptyArray<String>()
                    stationData.response.station.forEach {it ->
                        nameArray += it.name
                    }

                    openListDialog(nameArray, 3)
                }
                is Result.Failure -> { }
            }
        }
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

    private fun openListDialog(strArray: Array<String>, from: Int) {
        val args = Bundle()
        args.putStringArray("arrays", strArray)
        args.putInt("from", from)
        myDialog.arguments = args
        myDialog.show(supportFragmentManager, "simple")
    }

    fun onReturnValue(value: Int, from: Int) {
        /* fromについて
        *   1 = 都道府県選択ボタン押下後、都道府県が選択された場合
        *   2 = 路線選択ボタン押下後、路線が選択された場合
        *   3 = 駅選択ボタン押下後、駅が選択された場合
        * */
        when(from) {
            1 -> {
                selectedPrefecture = prefecturesArray[value]
                prefectureText.text = selectedPrefecture
            }
            2 -> {
                selectedLine = lineArray[value]
                lineText.text = selectedLine
            }
            3 -> {
                val chooseStation: StationDetail = stationData.response.station[value]
                latLng = LatLng(chooseStation.y, chooseStation.x)
                stationText.text = chooseStation.name
            }
        }
    }
}