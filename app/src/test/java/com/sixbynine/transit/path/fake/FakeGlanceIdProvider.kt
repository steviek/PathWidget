package com.sixbynine.transit.path.fake

import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import com.sixbynine.transit.path.glance.GlanceIdProvider
import com.sixbynine.transit.path.glance.GlanceIdProviderModule
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeGlanceIdProvider @Inject constructor() : GlanceIdProvider {

  private val providerToIds = mutableMapOf<Class<out GlanceAppWidget>, MutableSet<TestGlanceId>>()

  override suspend fun <T : GlanceAppWidget> getGlanceIds(provider: Class<T>): List<GlanceId> {
    return providerToIds[provider]?.toList() ?: emptyList()
  }

  fun <T : GlanceAppWidget> addGlanceId(provider: Class<T>, glanceId: TestGlanceId) {
    providerToIds.computeIfAbsent(provider) { mutableSetOf() } += glanceId
  }
}

@TestInstallIn(components = [SingletonComponent::class], replaces = [GlanceIdProviderModule::class])
@Module
interface FakeGlanceIdProviderModule {
  @Binds
  fun bindGlanceIdProvider(provider: FakeGlanceIdProvider): GlanceIdProvider
}
