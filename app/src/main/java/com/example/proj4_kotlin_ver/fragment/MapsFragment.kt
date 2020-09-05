package com.example.proj4_kotlin_ver.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.proj4_kotlin_ver.*
import com.example.proj4_kotlin_ver.activity.PrefectureSelectActivity
import com.example.proj4_kotlin_ver.activity.SearchActivity
import com.example.proj4_kotlin_ver.data.HistoryData
import com.example.proj4_kotlin_ver.dialog.PermissionDENIEDDialogFragment
import com.example.proj4_kotlin_ver.service.GeoFencingService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_maps.*
import java.util.*

class MapsFragment : Fragment(), OnMapReadyCallback, View.OnClickListener,
    PermissionDENIEDDialogFragment.PermissionDENIEDDialogListener {

    private lateinit var mMap: GoogleMap
    private lateinit var realm: Realm
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var ringtoneString: String? = null
    // デフォルトの座標(東京)
    private var latLng = LatLng(35.681236, 139.767125)
    // 現在位置を取得するかしないかのフラグ
    private var station_unselect_flag = true
    private var alertRadius: Double = 100.0

    private var selectedLine: String = ""
    private var selectedStation: String = ""

    private val deniedDialog =
        PermissionDENIEDDialogFragment()

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

        realm = Realm.getDefaultInstance()

        searchBtn.setOnClickListener(this)
        listBtn.setOnClickListener(this)

        alarmButton.setOnClickListener(this)

        sliderText.text = getString(R.string.slider_text, alertRadius.toInt())
    }

    override fun onResume() {
        super.onResume()

        // 目的の駅が指定されていないときのみ自身の位置情報を取得し、Mapに描画する
        if(station_unselect_flag) {
            val fineLocationPermission = activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) } == PackageManager.PERMISSION_GRANTED
            if(fineLocationPermission) {
                fusedLocationClient = context?.let { LocationServices.getFusedLocationProviderClient(it) }
                fusedLocationClient?.let {  it.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        latLng = LatLng(location.latitude, location.longitude)
                        mMap.clear()
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13F))
                        mMap.addMarker(MarkerOptions().position(latLng).title("現在位置"))
                    }
                }}
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13F))

        // スライダーが操作され、値が変更されたとき。
        slider.addOnChangeListener { slider, _, _ ->
            alertRadius = slider.value.toDouble()
            sliderText.text = getString(R.string.slider_text, alertRadius.toInt())

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
        select_station_text.addTextChangedListener(object :
            CustomTextWatcher {
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
            R.id.searchBtn -> {
                startActivityForResult(Intent(activity, SearchActivity::class.java), 200)
            }
            R.id.listBtn -> {
                startActivityForResult(Intent(activity, PrefectureSelectActivity::class.java), 0)
            }
            R.id.alarmButton -> {
                val fineLocationPermission = activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) } == PackageManager.PERMISSION_GRANTED
                if(fineLocationPermission) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val backgroundPermission = activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_BACKGROUND_LOCATION) } == PackageManager.PERMISSION_GRANTED

                        if(!backgroundPermission) {
                            deniedDialog.show(childFragmentManager, "simple2")
                        }else alarmStartButtonSelected()
                    }
                }else alarmStartButtonSelected()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) when(requestCode) {
            // 0 = リストから選択  200 = 検索から選択    2つとも駅名と駅座標を返すので一つにまとめている
            0, 200 -> {
                station_unselect_flag = false
                val extras = data?.extras
                if(extras != null) {
                    val station = extras.getString("station")
                    val line = extras.getString("line")
                    latLng = LatLng(extras.getDouble("lat"), extras.getDouble("lng"))

                    if (station != null) {
                        selectedStation = station
                        select_station_text.text = station
                    }
                    if (line != null) {
                       selectedLine = line
                    }
                    setAlarmBtnEnable()
                }
            }
        }
    }

    private fun alarmStartButtonSelected() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(getString(R.string.saved_station), selectedStation)
            putInt(getString(R.string.saved_radius), alertRadius.toInt())
            commit()
        }

        realm.executeTransaction {
            val maxId = realm.where<HistoryData>().max("id")
            val id = (maxId?.toLong() ?: 0L) + 1L
            val historyData = realm.createObject<HistoryData>(id)
            historyData.dateTime = Date()
            historyData.station = selectedStation
            historyData.line = selectedLine
            historyData.lat = latLng.latitude
            historyData.lng = latLng.longitude
            historyData.radius = alertRadius
        }

        ringtoneString = sharedPref.getString(getString(R.string.saved_ringtone), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE).toString())

        val serviceIntent = Intent(activity, GeoFencingService::class.java)
        serviceIntent.putExtra("Lat", latLng.latitude)
        serviceIntent.putExtra("Lng", latLng.longitude)
        serviceIntent.putExtra("radius", alertRadius.toFloat())
        serviceIntent.putExtra("station", selectedStation)
        serviceIntent.putExtra("ringtone", ringtoneString)
        activity?.startForegroundService(serviceIntent)

        activity?.supportFragmentManager?.beginTransaction().apply {
            val fragmentManager = fragmentManager
            if(fragmentManager != null) {
                val transaction = fragmentManager.beginTransaction()
                transaction.replace(R.id.container, AlarmStopFragment.newInstance())
                transaction.commit()
            }
        }
    }

    private fun setAlarmBtnEnable() {
        if(selectedStation.isNotEmpty()) alarmButton.isEnabled = true
    }

    override fun onDENIEDDialogClick() {
        activity?.finish()
    }
}
