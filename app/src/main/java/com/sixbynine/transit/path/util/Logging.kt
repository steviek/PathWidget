package com.sixbynine.transit.path.util

import android.util.Log
import com.sixbynine.transit.path.BuildConfig

fun logDebug(message: String) {
  if (BuildConfig.DEBUG) Log.d(LogTag, message)
}

fun logWarning(message: String, throwable: Throwable? = null) {
  Log.w(LogTag, message, throwable)
}

const val LogTag = "PathWidgetApplication"
