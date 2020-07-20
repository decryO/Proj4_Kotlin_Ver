package com.example.proj4_kotlin_ver

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)

        sliderText.text = "アラートラインのサイズ : " + alertRadius


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
            sliderText.text = "アラートラインのサイズ : " + alertRadius

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