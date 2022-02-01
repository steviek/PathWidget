package com.sixbynine.transit.path.station

import android.content.Context
import android.content.SharedPreferences
import com.sixbynine.transit.path.api.Coordinates
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.backend.TrainDataManager
import com.sixbynine.transit.path.ktx.hours
import com.sixbynine.transit.path.serialization.JsonFormat
import com.sixbynine.transit.path.time.BootTimestamp
import com.sixbynine.transit.path.time.BootTimestampProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import javax.inject.Inject

class StationLister @Inject internal constructor(
  @ApplicationContext private val context: Context,
  private val trainDataManager: TrainDataManager,
  private val bootTimestampProvider: BootTimestampProvider
) {
  private val prefs: SharedPreferences
    get() = context.getSharedPreferences("stations", Context.MODE_PRIVATE)

  fun getStations(): List<Station> {
    val savedData =
      prefs.getString(PrefsKey, null)?.let { JsonFormat.decodeFromString<SavedStations>(it) }

    if (savedData == null) {
      updateInBackground()
      return DefaultStations
    }

    val age = savedData.timestamp.getAge(bootTimestampProvider.now())
    if (age == null || age > 24.hours) {
      // Fetch stations from the API at most once a day.
      updateInBackground()
    }

    return savedData.stations
  }

  /** Returns the closest station to the lat/long, based on cartesian distance. */
  fun getClosestStation(latitude: Double, longitude: Double): Station {
    return getStations()
      .minByOrNull { station ->
        val dLatitude = station.coordinates.latitude - latitude
        val dLongitude = station.coordinates.longitude - longitude
        dLatitude * dLatitude + dLongitude * dLongitude
      }
      ?: error("No stations")
  }

  /**
   * Updates the store of saved stations in the background. This may fail if the app is killed, but
   * that is fine, as this is low priority.
   */
  private fun updateInBackground() = GlobalScope.launch {
    trainDataManager.getStations().getOrNull()?.let { stations ->
      val savedData = SavedStations(stations, bootTimestampProvider.now())
      prefs.edit().putString(PrefsKey, JsonFormat.encodeToString(savedData)).apply()
    }
  }
}

private const val PrefsKey = "saved_stations"

@Serializable
private data class SavedStations(val stations: List<Station>, val timestamp: BootTimestamp)

// Since stations aren't likely to change, we keep a hardcoded copy of the list to ensure that the
// configuration activity is snappy if we haven't loaded data yet.
private val DefaultStations = listOf(
  Station(
    id = -1,
    apiName = "NEWARK",
    displayName = "Newark",
    coordinates = Coordinates(40.73454, -74.16375)
  ),
  Station(
    id = -1,
    apiName = "HARRISON",
    displayName = "Harrison",
    coordinates = Coordinates(40.73942, -74.15587)
  ),
  Station(
    id = -1,
    apiName = "JOURNAL_SQUARE",
    displayName = "Journal Square",
    coordinates = Coordinates(40.73301, -74.06289)
  ),
  Station(
    id = -1,
    apiName = "GROVE_STREET",
    displayName = "Grove Street",
    coordinates = Coordinates(40.71966, -74.04245)
  ),
  Station(
    id = -1,
    apiName = "EXCHANGE_PLACE",
    displayName = "Exchange Place",
    coordinates = Coordinates(40.71676, -74.03238)
  ),
  Station(
    id = -1,
    apiName = "WORLD_TRADE_CENTER",
    displayName = "World Trade Center",
    coordinates = Coordinates(40.71271, -74.01193)
  ),
  Station(
    id = -1,
    apiName = "NEWPORT",
    displayName = "Newport",
    coordinates = Coordinates(40.72699, -74.03383)
  ),
  Station(
    id = -1,
    apiName = "HOBOKEN",
    displayName = "Hoboken",
    coordinates = Coordinates(40.73586, -74.02922)
  ),
  Station(
    id = -1,
    apiName = "CHRISTOPHER_STREET",
    displayName = "Christopher Street",
    coordinates = Coordinates(40.73295, -74.00707)
  ),
  Station(
    id = -1,
    apiName = "NINTH_STREET",
    displayName = "9th Street",
    coordinates = Coordinates(40.73424, -73.9991)
  ),
  Station(
    id = -1,
    apiName = "FOURTEENTH_STREET",
    displayName = "14th Street",
    coordinates = Coordinates(40.73735, -73.99684)
  ),
  Station(
    id = -1,
    apiName = "TWENTY_THIRD_STREET",
    displayName = "23rd Street",
    coordinates = Coordinates(40.7429, -73.99278)
  ),
  Station(
    id = -1,
    apiName = "THIRTY_THIRD_STREET",
    displayName = "33rd Street",
    coordinates = Coordinates(40.74912, -73.98827)
  )
)
