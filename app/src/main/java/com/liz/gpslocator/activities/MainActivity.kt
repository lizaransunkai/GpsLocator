package com.liz.gpslocator.activities
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.liz.gpslocator.services.LocationService
import com.liz.gpslocator.R
import com.liz.gpslocator.contexts.LocationContext
import com.liz.gpslocator.data.LocationData
import com.liz.gpslocator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  // Permissions
  private val coarseLocationPermission = android.Manifest.permission.ACCESS_COARSE_LOCATION
  private val fineLocationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION
  private val backgroundLocationPermission = android.Manifest.permission.ACCESS_BACKGROUND_LOCATION

  // Codes
  private val INIT_REQUEST_CODE = 1
  private val SECOND_REQUEST_CODE = 2
  private val THIRD_REQUEST_CODE = 3

  // Contexts
  private val locationContext = LocationContext.instance

  // Val
  val mainHandler = Handler(Looper.getMainLooper())

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    setActivitySettings()
  }

  override fun onBackPressed() {}

  private fun setActivitySettings () {
    addListeners()
    validatePermissions()
  }

  private fun validatePermissions () {
    if (ContextCompat.checkSelfPermission(this, coarseLocationPermission) != PackageManager.PERMISSION_GRANTED) requestCoarsePermission()
  }

  private fun requestCoarsePermission () {
    val permissionsList = listOf(coarseLocationPermission).toTypedArray()
    ActivityCompat.requestPermissions(this, permissionsList, INIT_REQUEST_CODE)
  }

  private fun requestFineLocationPermissions () {
    val permissionsList = listOf(fineLocationPermission).toTypedArray()
    ActivityCompat.requestPermissions(this, permissionsList, SECOND_REQUEST_CODE)
  }

  private fun requestBackgroundLocationPermissions () {
    val permissionsList = listOf(backgroundLocationPermission).toTypedArray()
    ActivityCompat.requestPermissions(this, permissionsList, THIRD_REQUEST_CODE)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    when (requestCode) {
      INIT_REQUEST_CODE -> validateInitPermissions(grantResults)
      SECOND_REQUEST_CODE -> validateSecondPermissions(grantResults)
      THIRD_REQUEST_CODE -> validateThirdPermissions(grantResults)
    }
  }

  private fun validateInitPermissions(grantResults: IntArray) {
    if (grantResults.isEmpty()) permissionsNotGranted()
    else if (grantResults[0] != PackageManager.PERMISSION_GRANTED) permissionsNotGranted()
    else requestFineLocationPermissions()
  }

  private fun validateSecondPermissions(grantResults: IntArray) {
    val isAndroidVersionHigherEqualThan11 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    if (grantResults.isEmpty()) permissionsNotGranted()
    else if (grantResults[0] != PackageManager.PERMISSION_GRANTED) permissionsNotGranted()
    else if (isAndroidVersionHigherEqualThan11) requestBackgroundLocationPermissions()
    else finalizePermissionsRequests()
  }

  private fun validateThirdPermissions (grantResults: IntArray) {
    if (grantResults.isEmpty()) permissionsNotGranted()
    else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) finalizePermissionsRequests()
    else permissionsNotGranted()
  }

  private fun finalizePermissionsRequests () {
    Toast.makeText(this, resources.getString(R.string.ma_permissions_granted_toast), Toast.LENGTH_SHORT).show()
  }

  private fun permissionsNotGranted () {
    val intent = Intent(this, NoPermissions::class.java)
    startActivity(intent)
  }

  private fun addListeners () {
    binding.maBtnStartLocationService.setOnClickListener { handleLocationService() }
  }

  private fun handleLocationService () {
    if (isLocationServiceRunning(LocationService::class.java)) stopLocationService()
    else initLocationService()
  }

  private fun initLocationService () {
    val intent = Intent(this, LocationService::class.java)
    startService(intent)
    binding.maBtnStartLocationService.setCompoundDrawablesWithIntrinsicBounds(R.drawable.no_location_white, 0, 0, 0)
    binding.maTvLocationAction.text = resources.getString(R.string.ma_stop_service)
  }

  private fun stopLocationService () {
    val intent = Intent(this, LocationService::class.java)
    stopService(intent)
    mainHandler.removeCallbacksAndMessages(null);
    Toast.makeText(this, resources.getString(R.string.ma_service_stopped_toast), Toast.LENGTH_SHORT).show()
    binding.maBtnStartLocationService.setCompoundDrawablesWithIntrinsicBounds(R.drawable.location_white, 0, 0, 0)
    binding.maTvLocationAction.text = resources.getString(R.string.ma_start_service)
  }

  override fun onDestroy() {
    super.onDestroy()
    stopService(Intent(this, LocationService::class.java))
    locationContext.locationList = mutableListOf()
  }

  private fun isLocationServiceRunning (mClass:Class<LocationService>): Boolean {
    val manager:ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service:ActivityManager.RunningServiceInfo in manager.getRunningServices(Integer.MAX_VALUE)) {
      if (mClass.name.equals(service.service.className)) return true
    }
    return false
  }

  private fun redirectToMaps (locationData: LocationData) {
    val gmmIntentUri: Uri = Uri.parse("google.navigation:q=${locationData.latitude},${locationData.longitude}")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    startActivity(mapIntent)
  }

}