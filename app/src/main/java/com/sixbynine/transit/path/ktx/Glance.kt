package com.sixbynine.transit.path.ktx

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.background
import androidx.glance.unit.ColorProvider
import com.sixbynine.transit.path.context

/** Extracts an app widget id [Int] from a [GlanceId]. */
fun GlanceId.toAppWidgetId(): Int {
  // This is a big hack and will probably break in the future ¯\_(ツ)_/¯.
  return toString().filter { it.isDigit() }.toInt()
}

fun GlanceAppWidgetManager() = GlanceAppWidgetManager(context)

fun GlanceModifier.drawableBackground(@DrawableRes drawable: Int): GlanceModifier =
  background(ImageProvider(drawable))

