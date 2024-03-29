package com.sixbynine.transit.path.widget

import com.sixbynine.transit.path.model.DepartureBoardTrain
import com.sixbynine.transit.path.model.Station
import com.sixbynine.transit.path.time.BootTimestamp
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.Instant

/**
 * The persisted data for a widget. This should probably be two classes to separate the
 * configuration from the data, but here we are.
 */
@Serializable
data class DepartureBoardWidgetData(
    val loadedData: LoadedWidgetData? = null,
    val lastRefresh: LastRefreshData? = null,
    val isLoading: Boolean = false,
    val fixedStations: Set<String>? = null,
    val useClosestStation: Boolean = false
)

/** Details about the last time we attempted to refresh the widget's data. */
@Serializable
data class LastRefreshData(
    val time: BootTimestamp,
    val wasSuccess: Boolean = true,
    val hadInternet: Boolean = true
)

@Serializable
data class LoadedWidgetData(
    private val stationAndTrains: List<Pair<Station, List<DepartureBoardTrain>>>,
    private val updateTimeMillis: Long,
    val closestStation: String? = null
) {
    @Transient
    val updateTime = Instant.ofEpochMilli(updateTimeMillis)!!

    @Transient
    val stationToTrains: Map<Station, List<DepartureBoardTrain>> = stationAndTrains.toMap()
}
