package com.sixbynine.transit.path.ktx

import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.sixbynine.transit.path.context

/** Extracts an app widget id [Int] from a [GlanceId]. */
fun GlanceId.toAppWidgetId(): Int {
  // This is a big hack and will probably break in the future ¯\_(ツ)_/¯.
  return toString().filter { it.isDigit() }.toInt()
}

fun GlanceAppWidgetManager() = GlanceAppWidgetManager(context)
