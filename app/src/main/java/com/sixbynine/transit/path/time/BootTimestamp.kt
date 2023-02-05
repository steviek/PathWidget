package com.sixbynine.transit.path.time

import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.Duration
import javax.inject.Inject

/** Snapshot of the system clock and boot count, which can be used for keeping track of durations. */
@Serializable
data class BootTimestamp(private val elapsedSinceBootMillis: Long, val bootCount: Int) {

    constructor(
        elapsedSinceBoot: Duration,
        bootCount: Int
    ) : this(elapsedSinceBoot.toMillis(), bootCount)

    @Transient
    val elapsedSinceBoot = Duration.ofMillis(elapsedSinceBootMillis)!!

    /** Returns the age of the [BootTimestamp], or null if it's from a previous boot. */
    fun getAge(now: BootTimestamp): Duration? {
        if (bootCount != now.bootCount) return null
        return now.elapsedSinceBoot - elapsedSinceBoot
    }
}

class BootTimestampProvider @Inject internal constructor(
    @ApplicationContext private val context: Context,
    private val contentResolver: ContentResolver,
    private val elapsedRealtimeProvider: ElapsedRealtimeProvider
) {
    fun now(): BootTimestamp {
        val bootCount = try {
            Settings.Global.getInt(contentResolver, Settings.Global.BOOT_COUNT)
        } catch (e: SettingNotFoundException) {
            0
        }
        return BootTimestamp(elapsedRealtimeProvider.elapsedRealtime(), bootCount)
    }
}
