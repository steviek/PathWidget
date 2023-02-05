package com.sixbynine.transit.path.logging

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sixbynine.transit.path.time.RoomConverters

@Database(entities = [LocalLogEntry::class], version = 1)
@TypeConverters(RoomConverters::class)
abstract class LocalLogDatabase : RoomDatabase() {
    abstract fun localLogDao(): LocalLogDao
}

object Databases {
    const val DEBUG_LOG_DB = "debug_logs"
}

object Tables {
    const val LOCAL_LOGS = "local_logs"
}
