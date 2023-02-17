package com.liz.gpslocator.contexts

import com.liz.gpslocator.data.LocationData

class LocationContext {

  var locationList = mutableListOf<LocationData>()

  companion object {
    val instance = LocationContext()
  }

}