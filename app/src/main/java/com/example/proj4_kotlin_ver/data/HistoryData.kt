package com.example.proj4_kotlin_ver.data

import com.google.android.gms.maps.model.LatLng
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class HistoryData: RealmObject() {
    @PrimaryKey var id: Long = 0
    var dateTime: Date = Date()
    var station: String = ""
    var line: String = ""
    // LatLngで保存しようとするとサポートされていないと出るのでDouble型で保存する
    var lat: Double = 0.0
    var lng: Double = 0.0
    var radius: Double = 100.0
}