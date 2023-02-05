package com.sixbynine.transit.path.module

import android.appwidget.AppWidgetManager
import android.content.ContentResolver
import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object SystemServiceModule {
    @Provides
    fun provideGlanceAppWidgetManager(
        @ApplicationContext context: Context
    ): GlanceAppWidgetManager {
        return GlanceAppWidgetManager(context)
    }

    @Provides
    fun provideAppWidgetManager(@ApplicationContext context: Context): AppWidgetManager {
        return AppWidgetManager.getInstance(context)
    }

    @Provides
    fun provideLocationManager(@ApplicationContext context: Context): LocationManager? {
        return context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    }

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
}

@InstallIn(SingletonComponent::class)
@Module
object WorkManagerModule {
    @Provides
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}
