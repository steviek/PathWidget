package com.sixbynine.transit.path.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.sixbynine.transit.path.PathWidgetApplication
import com.sixbynine.transit.path.application

/** Listener for boot complete to track boot count on pre-24 devices. */
class BootCompleteReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val prefs = getPrefs(context)
    val previousBootCount = prefs.getInt(BootCountKey, 0)
    prefs.edit().putInt(BootCountKey, previousBootCount + 1).apply()
  }
}

fun getPre24BootCount(): Int {
  val prefs = getPrefs(application)
  return prefs.getInt(BootCountKey, 0)
}

private fun getPrefs(context: Context): SharedPreferences {
  return context.getSharedPreferences(PrefsFilename, Context.MODE_PRIVATE)
}

private const val PrefsFilename = "boot_count"
private const val BootCountKey = "count"