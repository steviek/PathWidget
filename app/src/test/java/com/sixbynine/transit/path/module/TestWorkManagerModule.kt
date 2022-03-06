package com.sixbynine.transit.path.module

import android.content.Context
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@TestInstallIn(components = [SingletonComponent::class], replaces = [WorkManagerModule::class])
@Module
object TestWorkManagerModule {
    @Singleton
    @Provides
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        return WorkManager.getInstance(context)
    }
}
