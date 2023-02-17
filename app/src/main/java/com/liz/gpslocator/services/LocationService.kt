package com.liz.gpslocator.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import com.google.android.gms.location.*
import com.liz.gpslocator.R
import com.liz.gpslocator.activities.MainActivity
import com.liz.gpslocator.contexts.LocationContext
import com.liz.gpslocator.data.LocationData
import com.liz.gpslocator.helpers.GPSRequests
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.sql.Timestamp
import java.util.*

class LocationService: Service() {

  // Late
  private lateinit var fusedLocationClient: FusedLocationProviderClient
  private lateinit var locationCallback: LocationCallback

  // Val
  val channelId = "location_service"
  val locationNotificationId = 123

  // Contexts
  val locationContext = LocationContext.instance

  // Helpers
  val gpsRequests = GPSRequests()

  override fun onStartCommand (intent: Intent?, flags: Int, startId: Int): Int {
    initLocationService()
    return super.onStartCommand(intent, flags, startId)
  }

  private fun initLocationService () {
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    startFusedLocation()
    createNotificationChannel()
    showNotification()
  }

  @SuppressLint("NewApi")
  private fun showNotification () {
    val notificationIntent = Intent(this, MainActivity::class.java)
    notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
    val notification = Notification
      .Builder(this, channelId)
      .setContentTitle(resources.getString(R.string.ls_notification_title))
      .setContentText(resources.getString(R.string.ls_notification_description))
      .setContentIntent(pendingIntent)
      .setSmallIcon(R.drawable.location_white)
      .build()
    startForeground(locationNotificationId, notification)
  }

  private fun createNotificationChannel () {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val serviceChannel = NotificationChannel(channelId, "Location Service", NotificationManager.IMPORTANCE_DEFAULT)
    val manager = getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(serviceChannel)
  }

  @SuppressLint("MissingPermission")
  private fun startFusedLocation() {
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    val locationRequest = LocationRequest
      .Builder(10000)
      .setMinUpdateIntervalMillis(20000)
      .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
      .build()
    val looper = Looper.getMainLooper()
    fusedLocationClient.requestLocationUpdates(locationRequest, getCurrentLocation(), looper)
  }

  private fun getCurrentLocation (): LocationCallback {
    locationCallback = object : LocationCallback() {
      override fun onLocationResult(locationResult: LocationResult) { handleLocations(locationResult.locations)}
    }
    return locationCallback
  }

  fun handleLocations(locations: MutableList<Location>) {
    val currentTimeMillis = System.currentTimeMillis()
    val timeStamp = Timestamp(currentTimeMillis)
    val locationList = mutableListOf<LocationData>()
    locations.forEach { locationList.add(LocationData(it.latitude, it.longitude, timeStamp)) }
    val linkedList = LinkedList(locationContext.locationList)
    linkedList.push(locationList[0])
    locationContext.locationList = linkedList
    postLocationData(locationList[0])
  }

  private fun postLocationData(locationData: LocationData) {
    val call = gpsRequests.submitGPSData(locationData)
    call.enqueue(object:Callback {
      override fun onFailure(call: Call, e: IOException) { println(e) }
      override fun onResponse(call: Call, response: Response) { println(response) }
    })
  }

  override fun onDestroy() {
    fusedLocationClient.removeLocationUpdates(locationCallback);
    stopSelf()
    super.onDestroy()
  }

  override fun onBind(p0: Intent?): IBinder? {
    TODO("Not yet implemented")
  }

}