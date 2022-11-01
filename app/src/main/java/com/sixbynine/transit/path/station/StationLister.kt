package com.sixbynine.transit.path.station

import com.sixbynine.transit.path.model.Coordinates
import com.sixbynine.transit.path.model.Station

object StationLister  {

  fun getStations(): List<Station> {
    return DefaultStations
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
}

// Since stations aren't likely to change, we keep a hardcoded copy of the list to ensure that the
// configuration activity is snappy if we haven't loaded data yet.
private val DefaultStations = listOf(
  Station(
    id = -1,
    mRazzaApiName = "NEWARK",
    displayName = "Newark",
    coordinates = Coordinates(40.73454, -74.16375)
  ),
  Station(
    id = -1,
    mRazzaApiName = "HARRISON",
    displayName = "Harrison",
    coordinates = Coordinates(40.73942, -74.15587)
  ),
  Station(
    id = -1,
    mRazzaApiName = "JOURNAL_SQUARE",
    displayName = "Journal Square",
    coordinates = Coordinates(40.73301, -74.06289)
  ),
  Station(
    id = -1,
    mRazzaApiName = "GROVE_STREET",
    displayName = "Grove Street",
    coordinates = Coordinates(40.71966, -74.04245)
  ),
  Station(
    id = -1,
    mRazzaApiName = "EXCHANGE_PLACE",
    displayName = "Exchange Place",
    coordinates = Coordinates(40.71676, -74.03238)
  ),
  Station(
    id = -1,
    mRazzaApiName = "WORLD_TRADE_CENTER",
    displayName = "World Trade Center",
    coordinates = Coordinates(40.71271, -74.01193)
  ),
  Station(
    id = -1,
    mRazzaApiName = "NEWPORT",
    displayName = "Newport",
    coordinates = Coordinates(40.72699, -74.03383)
  ),
  Station(
    id = -1,
    mRazzaApiName = "HOBOKEN",
    displayName = "Hoboken",
    coordinates = Coordinates(40.73586, -74.02922)
  ),
  Station(
    id = -1,
    mRazzaApiName = "CHRISTOPHER_STREET",
    displayName = "Christopher Street",
    coordinates = Coordinates(40.73295, -74.00707)
  ),
  Station(
    id = -1,
    mRazzaApiName = "NINTH_STREET",
    displayName = "9th Street",
    coordinates = Coordinates(40.73424, -73.9991)
  ),
  Station(
    id = -1,
    mRazzaApiName = "FOURTEENTH_STREET",
    displayName = "14th Street",
    coordinates = Coordinates(40.73735, -73.99684)
  ),
  Station(
    id = -1,
    mRazzaApiName = "TWENTY_THIRD_STREET",
    displayName = "23rd Street",
    coordinates = Coordinates(40.7429, -73.99278)
  ),
  Station(
    id = -1,
    mRazzaApiName = "THIRTY_THIRD_STREET",
    displayName = "33rd Street",
    coordinates = Coordinates(40.74912, -73.98827)
  )
)
