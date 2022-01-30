package com.sixbynine.transit.path.module

import android.appwidget.AppWidgetManager
import android.content.ContentResolver
import android.content.Context
import android.location.LocationManager
import androidx.glance.appwidget.GlanceAppWidgetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object SystemServiceModule {
  @Provides
  fun provideGlanceAppWidgetManager(@ApplicationContext context: Context): GlanceAppWidgetManager {
    return GlanceAppWidgetManager(context)
  }

  @Provides
  fun provideAppWidgetManager(@ApplicationContext context: Context): AppWidgetManager {
    return AppWidgetManager.getInstance(context)
  }

  @Provides
  fun provideLocationManager(@ApplicationContext context: Context): LocationManager {
    return context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
  }

  @Provides
  fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
    return context.contentResolver
  }
}