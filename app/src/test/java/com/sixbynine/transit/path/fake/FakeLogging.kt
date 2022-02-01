package com.sixbynine.transit.path.fake

import com.sixbynine.transit.path.logging.Logging
import com.sixbynine.transit.path.logging.LoggingModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@TestInstallIn(components = [SingletonComponent::class], replaces = [LoggingModule::class])
@Module
object FakeLoggingModule {
  @Provides
  fun provideLogging() = object: Logging {
    override fun debug(message: String) {}

    override fun warn(message: String, t: Throwable?) {}
  }
}
