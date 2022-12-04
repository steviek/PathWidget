package com.sixbynine.transit.path.logging

import android.util.Log
import com.sixbynine.transit.path.BuildConfig
import com.sixbynine.transit.path.time.TimeSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

interface Logging {
    fun debug(message: String)
    fun warn(message: String, t: Throwable? = null)
}

class DefaultLogging @Inject constructor(
    private val localLogDao: LocalLogDao,
    private val timeSource: TimeSource
) : Logging {
    override fun debug(message: String) {
        if (BuildConfig.DEBUG) Log.d(LogTag, message)
        recordLogInBackground(Log.DEBUG, message)
    }

    override fun warn(message: String, t: Throwable?) {
        Log.w(LogTag, message, t)
        val resolvedMessage = if (t == null) message else "$message [error=$t]"
        recordLogInBackground(Log.WARN, resolvedMessage)
    }

    private fun recordLogInBackground(level: Int, message: String) {
        if (!IsLocalLoggingEnabled) return

        // Log the message on a background thread. If the app dies before the logging completes, it's
        // not the end of the world.
        GlobalScope.launch(Dispatchers.IO) {
            val logEntry =
                LocalLogEntry(timestamp = timeSource.now(), message = message, level = level)
            localLogDao.insert(logEntry)
        }
    }
}

@InstallIn(SingletonComponent::class)
@Module
interface LoggingModule {
    @Binds
    fun bindLogging(logging: DefaultLogging): Logging
}

const val LogTag = "PathWidgetApplication"
val IsLocalLoggingEnabled = BuildConfig.DEBUG
