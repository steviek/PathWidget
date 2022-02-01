package com.sixbynine.transit.path.time

import androidx.room.TypeConverter
import java.time.Instant

class RoomConverters {
  @TypeConverter
  fun fromTimestamp(value: Long?): Instant? {
    return value?.let { Instant.ofEpochMilli(it) }
  }

  @TypeConverter
  fun instantToTimestamp(instant: Instant?): Long? {
    return instant?.toEpochMilli()
  }
}
