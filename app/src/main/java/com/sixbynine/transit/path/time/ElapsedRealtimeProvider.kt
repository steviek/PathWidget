package com.sixbynine.transit.path.time

import android.os.SystemClock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Duration

interface ElapsedRealtimeProvider {
    fun elapsedRealtime(): Duration
}

@InstallIn(SingletonComponent::class)
@Module
object ElapsedRealtimeProviderModule {
    @Provides
    fun provideProvider() = object : ElapsedRealtimeProvider {
        override fun elapsedRealtime(): Duration {
            return Duration.ofNanos(SystemClock.elapsedRealtimeNanos())
        }
    }
}