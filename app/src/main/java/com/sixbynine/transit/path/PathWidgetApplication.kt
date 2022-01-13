package com.sixbynine.transit.path

import android.app.Application
import android.content.Context

class PathWidgetApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    application = this
  }
}

lateinit var application: Application
  private set

val context: Context
  get() = application
