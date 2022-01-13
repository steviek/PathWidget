
package com.sixbynine.transit.path.time

import android.os.Build.VERSION
import android.os.SystemClock
import android.provider.Settings
import com.sixbynine.transit.path.PathWidgetApplication
import com.sixbynine.transit.path.application
import com.sixbynine.transit.path.boot.getPre24BootCount
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.Duration

/** Snapshot of the system clock and boot count, which can be used for keeping track of durations. */
@Serializable
data class BootTimestamp(private val elapsedSinceBootMillis: Long, val bootCount: Int) {

  @Transient
  val elapsedSinceBoot = Duration.ofMillis(elapsedSinceBootMillis)!!

  /** Returns the age of the [BootTimestamp], or null if it's from a previous boot. */
  fun getAge(): Duration? {
    val now = now()
    if (bootCount != now.bootCount) return null
    return now.elapsedSinceBoot - elapsedSinceBoot
  }

  companion object {
    fun now(): BootTimestamp {
      val contentResolver = application.contentResolver
      val bootCount = if (VERSION.SDK_INT >= 24) {
        Settings.Global.getInt(contentResolver, Settings.Global.BOOT_COUNT)
      } else {
        getPre24BootCount()
      }
      return BootTimestamp(SystemClock.elapsedRealtime(), bootCount)
    }
  }
}
