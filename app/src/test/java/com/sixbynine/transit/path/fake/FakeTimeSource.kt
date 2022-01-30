package com.sixbynine.transit.path.fake

import com.sixbynine.transit.path.time.TimeSource
import com.sixbynine.transit.path.time.TimeSourceModule
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeTimeSource @Inject constructor() : TimeSource{

  private var now = Instant.EPOCH

  override fun now(): Instant {
    return now
  }

  fun setNow(dateTime: LocalDateTime) {
    this.now = dateTime.atZone(ZoneId.systemDefault()).toInstant()
  }

  fun setNow(now: Instant) {
    this.now = now
  }
}

@TestInstallIn(
  components = [SingletonComponent::class],
  replaces = [TimeSourceModule::class]
)
@Module
interface FakeTimeSourceModule {
  @Binds
  fun bindTimeSource(fake: FakeTimeSource): TimeSource
}
