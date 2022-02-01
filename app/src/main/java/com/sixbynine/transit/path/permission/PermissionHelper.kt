package com.sixbynine.transit.path.permission

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Build.VERSION.SDK_INT
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PermissionHelper @Inject internal constructor(
  @ApplicationContext private val context: Context
) {
  val locationPermissions = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
  val backgroundLocationPermissions =
    if (SDK_INT >= 29) arrayOf(ACCESS_BACKGROUND_LOCATION) else emptyArray()

  fun hasLocationPermission(): Boolean {
    return locationPermissions.any { isPermissionGranted(it) }
  }

  fun hasBackgroundLocationPermission(): Boolean {
    return backgroundLocationPermissions.all { isPermissionGranted(it) }
  }

  private fun isPermissionGranted(permission: String): Boolean {
    if (SDK_INT < 23) return true
    return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
  }
}