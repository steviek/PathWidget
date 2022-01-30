package com.sixbynine.transit.path.fake

import com.sixbynine.transit.path.time.ElapsedRealtimeProvider
import com.sixbynine.transit.path.time.ElapsedRealtimeProviderModule
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeElapsedRealtimeProvider @Inject constructor(): ElapsedRealtimeProvider {
  var elapsedRealtime: Duration = Duration.ZERO

  override fun elapsedRealtime() = elapsedRealtime
}

@TestInstallIn(
  components = [SingletonComponent::class],
  replaces = [ElapsedRealtimeProviderModule::class]
)
@Module
interface FakeElapsedRealtimeProviderModule {
  @Binds
  fun bindElapsedRealtimeProvider(provider: FakeElapsedRealtimeProvider): ElapsedRealtimeProvider
}
