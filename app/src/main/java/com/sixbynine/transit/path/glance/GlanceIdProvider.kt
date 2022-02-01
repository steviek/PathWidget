package com.sixbynine.transit.path.glance

import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

interface GlanceIdProvider {
  suspend fun <T : GlanceAppWidget> getGlanceIds(provider: Class<T>): List<GlanceId>
}

class DefaultGlanceIdProvider @Inject constructor(
  private val glanceAppWidgetManager: GlanceAppWidgetManager
) : GlanceIdProvider {
  override suspend fun <T : GlanceAppWidget> getGlanceIds(provider: Class<T>): List<GlanceId> {
    return glanceAppWidgetManager.getGlanceIds(provider)
  }
}

@InstallIn(SingletonComponent::class)
@Module
interface GlanceIdProviderModule {
  @Binds
  fun bindGlanceIdProvider(provider: DefaultGlanceIdProvider): GlanceIdProvider
}
