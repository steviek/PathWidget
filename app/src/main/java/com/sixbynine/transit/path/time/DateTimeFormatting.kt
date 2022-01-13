package com.sixbynine.transit.path.time

import android.text.format.DateFormat
import com.sixbynine.transit.path.PathWidgetApplication
import com.sixbynine.transit.path.application
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formats the [time] to be displayed to the user. */
fun formatLocalTime(time: LocalTime): String {
  val is24Hr = DateFormat.is24HourFormat(application)
  // This isn't perfect from an i18n standpoint, but it works fine for English and Spanish, which
  // are the only languages we support right now.
  val pattern = if (is24Hr) "HH:mm" else "h:mm"
  return DateTimeFormatter.ofPattern(pattern).withLocale(Locale.getDefault()).format(time)
}

/** Formats the [time] to be displayed to the user (using the default time zone). */
fun formatLocalTime(time: Instant): String {
  val localTime = time.atZone(ZoneId.systemDefault()).toLocalTime()
  return formatLocalTime(localTime)
}
