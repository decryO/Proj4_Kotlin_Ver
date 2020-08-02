package com.example.proj4_kotlin_ver

import com.github.kittinunf.fuel.core.ResponseDeserializable

data class StationData(
    val response: Stations
)

data class Stations(
    val station: List<StationDetail>
)

data class StationDetail(
    val name: String,
    val prefecture: String,
    val line: String,
    // Lng 経度
    val x: Double,
    // Lat 緯度
    val y: Double,
    val postal: Int,
    val prev: String?,
    val next: String?
)