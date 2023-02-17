package com.liz.gpslocator.data

import java.sql.Timestamp

data class LocationData (
  val latitude:Double?,
  val longitude:Double?,
  val timestamp: Timestamp?
)