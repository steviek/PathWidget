package com.sixbynine.transit.path.time

import android.content.Context
import android.text.format.DateFormat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale

class DateTimeFormatter private constructor(private val context: Context) {
  /** Formats the [time] to be displayed to the user. */
  fun formatLocalTime(time: LocalTime): String {
    val is24Hr = DateFormat.is24HourFormat(context)
    // This isn't perfect from an i18n standpoint, but it works fine for English and Spanish, which
    // are the only languages we support right now.
    val pattern = if (is24Hr) "HH:mm" else "h:mm"
    return JavaDateTimeFormatter.ofPattern(pattern).withLocale(Locale.getDefault()).format(time)
  }

  /** Formats the [time] to be displayed to the user (using the default time zone). */
  fun formatLocalTime(time: Instant): String {
    val localTime = time.atZone(ZoneId.systemDefault()).toLocalTime()
    return formatLocalTime(localTime)
  }

  @InstallIn(SingletonComponent::class)
  @Module
  object ProvisionModule {
    @Provides
    fun provideDateTimeFormatter(@ApplicationContext context: Context): DateTimeFormatter {
      return DateTimeFormatter(context)
    }
  }

  companion object {
    fun from(context: Context): DateTimeFormatter {
      return DateTimeFormatter(context)
    }
  }
}

private typealias JavaDateTimeFormatter = java.time.format.DateTimeFormatter
