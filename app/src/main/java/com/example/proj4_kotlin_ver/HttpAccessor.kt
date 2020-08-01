package com.example.proj4_kotlin_ver

import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.json.JSONObject

class HttpAccessor {

    // Fuelを用いてHTTP通信を行う
    fun getJson(url: String): ByteArray {
        return url.httpGet().response{ request, response, result ->
            when(result) {
                is Result.Success -> { println("Connected") }
                is Result.Failure -> { }
            }
        }.get().data
    }
}