package com.example.proj4_kotlin_ver.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.proj4_kotlin_ver.*
import com.example.proj4_kotlin_ver.activity.PrefectureSelectActivity
import com.example.proj4_kotlin_ver.activity.SearchActivity
import com.example.proj4_kotlin_ver.dialog.PermissionDENIEDDialogFragment
import com.example.proj4_kotlin_ver.service.GeoFencingService

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_maps.*

class MapsFragment : Fragment(), OnMapReadyCallback, View.OnClickListener,
    PermissionDENIEDDialogFragment.PermissionDENIEDDialogListener {

    private lateinit var mMap: GoogleMap
    private var ringtoneString: String? = null
    // デフォルトの座標(京都)
    private var latLng = LatLng(34.985458, 135.7577551)
    private var alertRadius: Double = 100.0

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

        searchBtn.setOnClickListener(this)
        listBtn.setOnClickListener(this)

        alarmButton.setOnClickListener(this)

        sliderText.text = getString(R.string.slider_text, alertRadius.toInt())
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
//                openListDialog(prefecturesArray, 1)
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
                val extras = data?.extras
                if(extras != null) {
                    val station = extras.getString("station")
                    latLng = LatLng(extras.getDouble("lat"), extras.getDouble("lng"))
                    if (station != null) {
                        selectedStation = station
                        select_station_text.text = station
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
