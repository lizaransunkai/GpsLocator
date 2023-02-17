package com.liz.gpslocator.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.liz.gpslocator.R

class NoPermissions : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_no_permissions)
  }

  override fun onBackPressed() {}
}