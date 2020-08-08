package com.example.proj4_kotlin_ver

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
import java.lang.reflect.Array.newInstance

class MapsFragment : Fragment(), OnMapReadyCallback, View.OnClickListener, MyDialogFragment.MyDialogFragmentListener {

    private lateinit var mMap: GoogleMap
    // デフォルトの座標(京都)
    private var latLng = LatLng(34.985458, 135.7577551)
    private var alertRadius: Double = 0.0

    private var selectedPrefecture: String = ""
    private var selectedLine: String = ""
    private var selectedStation: String = ""
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

    private lateinit var myDialog: MyDialogFragment

    companion object {
        fun newInstance(): MapsFragment {
            val fragment = MapsFragment()
            val args = Bundle()

            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)

        myDialog = MyDialogFragment()

        // 単調になるので下に切り分け
        selectPrefecture.setOnClickListener(this)
        selectLine.setOnClickListener(this)
        selectStation.setOnClickListener(this)
        alarmButton.setOnClickListener(this)

        // 路線などが選択されていない状態で駅選択ボタンなどが押せてしまうとおかしくなるので都道府県ボタンのみ押せるようにする
        buttonSetEnable(0)

        selectPrefecture.text = if(selectedPrefecture.isNotEmpty()) selectedPrefecture else "都道府県"
        selectLine.text = if(selectedLine.isNotEmpty()) selectedLine else "都道府県を選択してください"
        selectStation.text = if(selectedStation.isNotEmpty()) selectedStation else "路線を選択してください"

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

        // 駅が選択されていればアラームセットボタンを活性化、そうでなければ非活性化
        selectStation.addTextChangedListener(object : CustomTextWatcher{
            override fun afterTextChanged(s: Editable?) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13F))

                mMap.clear()

                mMap.addCircle(CircleOptions()
                    .center(latLng)
                    .radius(alertRadius)
                    .strokeColor(Color.RED)
                    .fillColor(0x220000FF)
                    .strokeWidth(5F)
                )
            }
        })
    }

    override fun onClick(v: View) {
        when(v.id) {

            R.id.selectPrefecture -> {
                val prefecturesArray: Array<String> = resources.getStringArray(R.array.prefectures)
                openListDialog(prefecturesArray, 1)
            }
            R.id.selectLine -> if(selectedPrefecture.isNotEmpty()) lineButtonSelected(getLineURL + selectedPrefecture)
            R.id.selectStation -> if(selectedLine.isNotEmpty()) stationButtonSelected(getStationURL + selectedLine)
            R.id.alarmButton -> alarmStartButtonSelected()
        }
    }

    private fun lineButtonSelected(url: String) {
        url.httpGet().responseJson { _, _, result ->
            when(result) {
                is Result.Success -> {
                    val responseJson = result.get()
                    lineArray = emptyArray()
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
                    stationData = mapper.readValue(result.value)
                    var nameArray: Array<String> = emptyArray()
                    stationData.response.station.forEach {
                        nameArray += it.name
                    }

                    openListDialog(nameArray, 3)
                }
                is Result.Failure -> { }
            }
        }
    }

    private fun alarmStartButtonSelected() {
        val serviceIntent = Intent(activity, GeoFencingService::class.java)
        serviceIntent.putExtra("Lat", latLng.latitude)
        serviceIntent.putExtra("Lng", latLng.longitude)
        serviceIntent.putExtra("radius", alertRadius)
        serviceIntent.putExtra("station", selectedStation)
        activity?.startForegroundService(serviceIntent)

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(getString(R.string.saved_station), selectedStation)
            putInt(getString(R.string.saved_radius), alertRadius.toInt())
            commit()
        }

        val transaction = activity?.supportFragmentManager?.beginTransaction().apply {
            val fragmentManager = fragmentManager
            if(fragmentManager != null) {
                val transaction = fragmentManager.beginTransaction()
                transaction.replace(R.id.container, AlarmStopFragment.newInstance())
                transaction.commit()
            }
        }
    }

    /// from 1: 都道府県 2: 路線 3: 駅
    private fun buttonSetEnable(from: Int) {
        selectLine.isEnabled = false
        selectStation.isEnabled = false
        alarmButton.isEnabled = false

        selectPrefecture.isEnabled = true
        if(from > 0) selectLine.isEnabled = true
        if(from > 1) selectStation.isEnabled = true
        if(from > 2) alarmButton.isEnabled = true
    }

    private fun openListDialog(strArray: Array<String>, from: Int) {
        val args = Bundle()
        args.putStringArray("arrays", strArray)
        args.putInt("from", from)
        myDialog.arguments = args
        myDialog.show(childFragmentManager, "simple")
    }

    override fun onDialogItemClick(value: Int, from: Int) {
        /* fromについて
        *   1 = 都道府県選択ボタン押下後、都道府県が選択された場合
        *   2 = 路線選択ボタン押下後、路線が選択された場合
        *   3 = 駅選択ボタン押下後、駅が選択された場合
        * */

        // value = -1 はダイアログのキャンセルボタンが押された際の値
        when(from) {
            1 -> {
                selectedPrefecture = prefecturesArray[value]
                selectedLine = ""
                selectedStation = ""
            }
            2 -> {
                selectedLine = lineArray[value]
                selectedStation = ""
            }
            3 -> {
                val chooseStation: StationDetail = stationData.response.station[value]
                latLng = LatLng(chooseStation.y, chooseStation.x)
                selectedStation = chooseStation.name
            }
        }

        buttonSetEnable(from)

        selectPrefecture.text = if(selectedPrefecture.isNotEmpty()) selectedPrefecture else "都道府県"
        selectLine.text = if(selectedLine.isNotEmpty()) selectedLine else "都道府県を選択してください"
        selectStation.text = if(selectedStation.isNotEmpty()) selectedStation else "路線を選択してください"
    }

}
