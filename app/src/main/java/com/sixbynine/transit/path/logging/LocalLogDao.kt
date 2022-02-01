package com.sixbynine.transit.path.logging

import android.content.Context
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Dao
interface LocalLogDao {
  @Query("SELECT * FROM ${Tables.LOCAL_LOGS}")
  suspend fun getAll(): List<LocalLogEntry>

  @Insert
  suspend fun insert(vararg entries: LocalLogEntry)
}

@InstallIn(SingletonComponent::class)
@Module
object LocalLogDaoProvisionModule {
  @Provides
  fun provideLocalLogDao(@ApplicationContext context: Context): LocalLogDao {
    if (!IsLocalLoggingEnabled) {
      // We only use this database in debug builds. Return a no-op version of the DAO and avoid
      // creating the db.
      return object: LocalLogDao {
        override suspend fun getAll(): List<LocalLogEntry> {
          return listOf()
        }

        override suspend fun insert(vararg entries: LocalLogEntry) {}
      }
    }
    return Room.databaseBuilder(context, LocalLogDatabase::class.java, Databases.DEBUG_LOG_DB)
      .build()
      .localLogDao()
  }
}
