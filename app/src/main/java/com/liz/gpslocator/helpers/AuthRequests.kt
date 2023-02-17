package com.liz.gpslocator.helpers

import com.liz.gpslocator.constants.Constants
import com.liz.gpslocator.data.LoginData
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class AuthRequests {

  private val client = OkHttpClient()
  private val gson = Gson()

  fun submitLoginData (data: LoginData): Call {
    val body = gson.toJson(data, LoginData::class.java)
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = RequestBody.create(mediaType, body)
    val builder = Request.Builder()
      .url("${Constants.URL}/auth/login")
      .post(requestBody)
      .build()
    return client.newCall(builder)
  }

}