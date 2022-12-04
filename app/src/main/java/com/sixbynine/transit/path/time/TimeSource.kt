package com.sixbynine.transit.path.time

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Instant

interface TimeSource {
    fun now(): Instant
}

fun TimeSource.millis() = now().toEpochMilli()

@InstallIn(SingletonComponent::class)
@Module
object TimeSourceModule {
    @Provides
    fun provideTimeSource() = object : TimeSource {
        override fun now(): Instant {
            return Instant.now()
        }
    }
}
