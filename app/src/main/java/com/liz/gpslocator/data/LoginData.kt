package com.liz.gpslocator.data

data class LoginData (
  val username:String?,
  val password:String?
)

data class LoginDataResponse (
  val token:String?
)