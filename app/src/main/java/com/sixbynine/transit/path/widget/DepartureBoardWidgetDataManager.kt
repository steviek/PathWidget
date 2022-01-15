package com.sixbynine.transit.path.widget

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.sixbynine.transit.path.PathWidgetApplication
import com.sixbynine.transit.path.application
import com.sixbynine.transit.path.backend.TrainDataManager
import com.sixbynine.transit.path.ktx.seconds
import com.sixbynine.transit.path.location.LocationCheckResult.Failure
import com.sixbynine.transit.path.location.LocationCheckResult.NoPermission
import com.sixbynine.transit.path.location.LocationCheckResult.NoProvider
import com.sixbynine.transit.path.location.LocationCheckResult.Success
import com.sixbynine.transit.path.location.tryToGetLocation
import com.sixbynine.transit.path.serialization.JsonFormat
import com.sixbynine.transit.path.station.StationLister
import com.sixbynine.transit.path.util.logDebug
import com.sixbynine.transit.path.util.logWarning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/** Manages updating the configuration or displayed data for a widget. */
object DepartureBoardWidgetDataManager {

  private val trainDataManager = TrainDataManager()

  private val context: Context
    get() = application

  private suspend fun getGlanceIds(): List<GlanceId> {
    return GlanceAppWidgetManager(context).getGlanceIds(DepartureBoardWidget::class.java)
  }

  private var loadingJob: Job? = null

  suspend fun updateData() {
    withContext(Dispatchers.IO) {
      synchronized(this) {
        if (loadingJob == null || loadingJob?.isCompleted == true) {
          loadingJob = launch { updateDataInner() }
        }
      }
    }

    loadingJob?.join()
  }

  private suspend fun updateDataInner() {
    var useClosestStation = false
    var isInitialDataLoad = false
    val stationsToCheck = mutableSetOf<String>()
    // Update the widget with the loading indicator, which will re-render the widget with the
    // progress bar spinning while we load the data.
    updateEachWidget { previousData ->
      isInitialDataLoad = previousData?.loadedData == null
      useClosestStation = previousData?.useClosestStation == true
      stationsToCheck += previousData?.fixedStations ?: emptySet()
      // Check for the station that was closest last time, in case we fail to get location below.
      previousData?.loadedData?.closestStation?.let(stationsToCheck::add)
      previousData?.copy(isLoading = true) ?: DepartureBoardWidgetData(isLoading = true)
    }

    // Try to get the user's location if they chose the closest station option.
    var closestStationName: String? = null
    if (useClosestStation) {
      val locationResult =
        tryToGetLocation(timeout = if (isInitialDataLoad) 1.seconds else 3.seconds)
      logDebug("Location was retrieved as $locationResult")
      when (locationResult) {
        is Failure -> {}
        NoPermission -> {
          logWarning("Permission location was lost in background")
          // TODO: Bug the user about this?
        }
        NoProvider -> {
          logWarning("There's no longer a location provider")
        }
        is Success -> {
          val location = locationResult.location
          val closestStation =
            StationLister.getClosestStation(location.latitude, location.longitude)
          logDebug("The closest station is ${closestStation.displayName}")
          closestStationName = closestStation.apiName
          stationsToCheck += closestStationName
        }
      }.let {}
    }

    // Call the API to get the upcoming train data for each relevant station. Keep track of whether
    // we're connected to the internet so we can use that to choose an error message to display.
    val hasInternet = isOnline
    var anyError = false
    val stations = StationLister.getStations()
    val stationsAndTrains =
      stations
        .filter { it.apiName in stationsToCheck }
        .distinctBy { it.apiName }
        .mapNotNull { station ->
          if (!isOnline) {
            anyError = true
            return@mapNotNull null
          }

          val upcomingTrains = trainDataManager.getUpcomingTrains(station).getOrNull()
          if (upcomingTrains == null) {
            anyError = true
            null
          } else {
            station to upcomingTrains
          }
        }

    // Update each widget with the data we just loaded.
    updateEachWidget { previousData ->
      when {
        anyError && previousData == null -> {
          DepartureBoardWidgetData(
            loadedData = null,
            lastRefresh = LastRefreshData(wasSuccess = false, hadInternet = hasInternet),
            isLoading = false
          )
        }
        anyError && previousData != null -> {
          previousData.copy(
            lastRefresh = LastRefreshData(wasSuccess = false, hadInternet = hasInternet),
            isLoading = false
          )
        }
        previousData == null -> {
          DepartureBoardWidgetData(
            loadedData = LoadedWidgetData(stationsAndTrains, closestStation = closestStationName),
            lastRefresh = LastRefreshData(wasSuccess = true),
            isLoading = false,
          )
        }
        else -> {
          previousData.copy(
            loadedData = LoadedWidgetData(
              stationsAndTrains,
              closestStation = closestStationName ?: previousData.loadedData?.closestStation
            ),
            lastRefresh = LastRefreshData(wasSuccess = true),
            isLoading = false,
          )
        }
      }
    }
  }

  private suspend fun updateEachWidget(
    function: (DepartureBoardWidgetData?) -> DepartureBoardWidgetData
  ) {
    val ids = getGlanceIds()
    logDebug("updateEachWidget: $ids")
    ids.forEach { id ->
      updateWidget(id, true, function)
    }
  }

  /** Returns the [DepartureBoardWidgetData] for the widget with [id], if it's been bound. */
  suspend fun getWidgetData(id: GlanceId): DepartureBoardWidgetData? {
    val state = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
    return state[DEPARTURE_WIDGET_PREFS_KEY]?.let {
      JsonFormat.decodeFromString<DepartureBoardWidgetData>(it)
    }
  }

  /** Update the stored data for the widget with [id], and update its UI. */
  suspend fun updateWidget(
    id: GlanceId,
    doUpdate: Boolean = true,
    function: (DepartureBoardWidgetData?) -> DepartureBoardWidgetData
  ) {
    logDebug("update widget: $id")
    val previousData = getWidgetData(id)
    updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { newState ->
      newState.toMutablePreferences()
        .apply {
          this[DEPARTURE_WIDGET_PREFS_KEY] = JsonFormat.encodeToString(function(previousData))
        }
    }
    if (doUpdate) {
      DepartureBoardWidget().update(context, id)
    }
  }

  private val isOnline: Boolean
    get() {
      val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
      return connectivityManager.activeNetworkInfo?.isConnectedOrConnecting ?: false
    }
}
