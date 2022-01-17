package com.sixbynine.transit.path.ktx

import androidx.annotation.DrawableRes
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.background

/** Extracts an app widget id [Int] from a [GlanceId]. */
fun GlanceId.toAppWidgetId(): Int {
  // This is a big hack and will probably break in the future ¯\_(ツ)_/¯.
  return toString().filter { it.isDigit() }.toInt()
}

fun GlanceModifier.drawableBackground(@DrawableRes drawable: Int): GlanceModifier =
  background(ImageProvider(drawable))

