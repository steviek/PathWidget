package com.sixbynine.transit.path.logging

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = Tables.LOCAL_LOGS)
data class LocalLogEntry(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  @ColumnInfo(name = "timestamp") val timestamp: Instant,
  @ColumnInfo(name = "message") val message: String,
  @ColumnInfo(name = "level") val level: Int
)
