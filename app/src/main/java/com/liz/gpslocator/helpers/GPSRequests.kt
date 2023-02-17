package com.liz.gpslocator.helpers

import com.liz.gpslocator.constants.Constants
import com.liz.gpslocator.data.LocationData
import com.google.gson.Gson
import com.liz.gpslocator.contexts.MainContext
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class GPSRequests {

  // Contexts
  private val mainContext = MainContext.instance

  private val client = OkHttpClient()
  private val gson = Gson()

  fun submitGPSData (data: LocationData): Call {
    val body = gson.toJson(data, LocationData::class.java)
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = RequestBody.create(mediaType, body)
    val request =  Request.Builder()
      .url("${Constants.URL}/gps")
      .post(requestBody)
      .addHeader("token", mainContext.token!!)
      .build()
    return client.newCall(request)
  }

}