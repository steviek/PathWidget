package com.sixbynine.transit.path.widget

import android.net.ConnectivityManager
import androidx.glance.GlanceId
import com.sixbynine.transit.path.backend.TrainDataManager
import com.sixbynine.transit.path.glance.GlanceIdProvider
import com.sixbynine.transit.path.ktx.seconds
import com.sixbynine.transit.path.location.LocationCheckResult.Failure
import com.sixbynine.transit.path.location.LocationCheckResult.NoPermission
import com.sixbynine.transit.path.location.LocationCheckResult.NoProvider
import com.sixbynine.transit.path.location.LocationCheckResult.Success
import com.sixbynine.transit.path.location.LocationProvider
import com.sixbynine.transit.path.logging.Logging
import com.sixbynine.transit.path.station.StationLister
import com.sixbynine.transit.path.time.BootTimestampProvider
import com.sixbynine.transit.path.time.ElapsedRealtimeProvider
import com.sixbynine.transit.path.time.TimeSource
import com.sixbynine.transit.path.time.millis
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

/** Manages updating the configuration or displayed data for a widget. */
@Singleton
class WidgetUpdater @Inject internal constructor(
    private val trainDataManager: TrainDataManager,
    private val locationProvider: LocationProvider,
    private val bootTimestampProvider: BootTimestampProvider,
    private val elapsedRealtimeProvider: ElapsedRealtimeProvider,
    private val timeSource: TimeSource,
    private val savedWidgetDataManager: SavedWidgetDataManager,
    private val widgetRefresher: WidgetRefresher,
    private val glanceIdProvider: GlanceIdProvider,
    private val connectivityManager: ConnectivityManager,
    private val workerScheduler: WidgetRefreshWorkerScheduler,
    private val logging: Logging
) {

    private suspend fun getGlanceIds(): List<GlanceId> {
        logging.debug("getting glance ids")
        return glanceIdProvider.getGlanceIds(DepartureBoardWidget::class.java)
    }

    @Volatile
    private var loadingJob: LastLoadingJob? = null

    suspend fun updateData(force: Boolean = false): Boolean {
        val jobToJoin = withContext(Dispatchers.IO) {
            val localLoadingJob = loadingJob
            if (localLoadingJob == null || localLoadingJob.isStale || force) {
                loadingJob?.job?.cancel()
                logging.debug("Start new job to fetch train data")
                val job = async { updateDataInner() }
                LastLoadingJob(job).also { loadingJob = it }
            } else {
                logging.debug("Join existing job to fetch train data")
                localLoadingJob
            }
        }

        return kotlin.runCatching { jobToJoin.job.await() }.getOrDefault(false)
    }

    suspend fun failRefreshingWidgets() {
        updateAndRefreshEachWidget { previousData ->
            if (previousData != null && !previousData.isLoading) {
                return@updateAndRefreshEachWidget previousData
            }
            val baseData = previousData ?: DepartureBoardWidgetData()
            baseData.copy(
                lastRefresh = LastRefreshData(
                    bootTimestampProvider.now(),
                    wasSuccess = false,
                    hadInternet = isOnline
                ),
                isLoading = false,
            )
        }
    }

    private suspend fun updateDataInner(): Boolean {
        logging.debug("updateDataInner")
        var useClosestStation = false
        var isInitialDataLoad = false
        val stationsToCheck = mutableSetOf<String>()
        // Update the widget with the loading indicator, which will re-render the widget with the
        // progress bar spinning while we load the data.
        updateAndRefreshEachWidget { previousData ->
            isInitialDataLoad = previousData?.loadedData == null
            useClosestStation = previousData?.useClosestStation == true
            stationsToCheck += previousData?.fixedStations ?: emptySet()
            // Check for the station that was closest last time, in case we fail to get location below.
            if (useClosestStation) {
                previousData?.loadedData?.closestStation?.let(stationsToCheck::add)
            }
            previousData?.copy(isLoading = true) ?: DepartureBoardWidgetData(isLoading = true)
        }

        // Try to get the user's location if they chose the closest station option.
        var closestStationName: String? = null
        if (useClosestStation) {
            val locationResult =
                locationProvider.tryToGetLocation(timeout = if (isInitialDataLoad) 1.seconds else 3.seconds)
            logging.debug("Location was retrieved as $locationResult")
            when (locationResult) {
                is Failure -> {}
                NoPermission -> {
                    logging.warn("Permission location was lost in background")
                    // TODO: Bug the user about this?
                }
                NoProvider -> {
                    logging.warn("There's no longer a location provider")
                }
                is Success -> {
                    val location = locationResult.location
                    val closestStation =
                        StationLister.getClosestStation(location.latitude, location.longitude)
                    logging.debug("The closest station is ${closestStation.displayName}")
                    closestStationName = closestStation.mRazzaApiName
                    stationsToCheck += closestStationName
                }
            }.let {}
        }

        // Call the API to get the upcoming train data for the relevant stations. Keep track of
        // whether we're connected to the internet so we can use that to choose an error message to
        // display.
        val hasInternet = isOnline
        var anyError = false
        val stations = StationLister.getStations()
        val stationsAndTrains =
            stations
                .filter { it.mRazzaApiName in stationsToCheck }
                .distinctBy { it.mRazzaApiName }
                .let {
                    if (!isOnline) {
                        anyError = true
                        return@let emptyMap()
                    }

                    trainDataManager.getUpcomingTrains(it).getOrElse {
                        anyError = true
                        emptyMap()
                    }
                }
                .toList()

        logging.debug("Finished getting trains from train data manager")

        // Update each widget with the data we just loaded.
        val now = bootTimestampProvider.now()
        updateAndRefreshEachWidget { previousData ->
            when {
                anyError && previousData == null -> {
                    DepartureBoardWidgetData(
                        loadedData = null,
                        lastRefresh = LastRefreshData(
                            now,
                            wasSuccess = false,
                            hadInternet = hasInternet
                        ),
                        isLoading = false
                    )
                }
                anyError && previousData != null -> {
                    previousData.copy(
                        lastRefresh = LastRefreshData(
                            now,
                            wasSuccess = false,
                            hadInternet = hasInternet
                        ),
                        isLoading = false
                    )
                }
                previousData == null -> {
                    DepartureBoardWidgetData(
                        loadedData = LoadedWidgetData(
                            stationsAndTrains,
                            updateTimeMillis = timeSource.millis(),
                            closestStation = closestStationName
                        ),
                        lastRefresh = LastRefreshData(now, wasSuccess = true),
                        isLoading = false,
                    )
                }
                else -> {
                    previousData.copy(
                        loadedData = LoadedWidgetData(
                            stationsAndTrains,
                            updateTimeMillis = timeSource.millis(),
                            closestStation = closestStationName
                                ?: previousData.loadedData?.closestStation
                        ),
                        lastRefresh = LastRefreshData(now, wasSuccess = true),
                        isLoading = false,
                    )
                }
            }
        }

        return !anyError
    }

    private suspend fun updateAndRefreshEachWidget(
        function: (DepartureBoardWidgetData?) -> DepartureBoardWidgetData
    ) {
        val ids = getGlanceIds()
        logging.debug("updateEachWidget: $ids")
        ids.forEach { id ->
            savedWidgetDataManager.updateWidgetData(id, function)
            widgetRefresher.refreshWidget(id)
        }

        if (ids.isNotEmpty()) {
            workerScheduler.schedulePeriodicRefresh()
        } else {
            workerScheduler.cancelPeriodicRefresh()
        }
    }

    private val isOnline: Boolean
        get() {
            return connectivityManager.activeNetworkInfo?.isConnectedOrConnecting ?: false
        }

    private data class LastLoadingJob(val job: Deferred<Boolean>, val elapsedRealtime: Duration)

    private fun LastLoadingJob(job: Deferred<Boolean>) =
        LastLoadingJob(job, elapsedRealtimeProvider.elapsedRealtime())

    private val LastLoadingJob.isStale: Boolean
        get() = job.isCompleted &&
                elapsedRealtimeProvider.elapsedRealtime() - elapsedRealtime > 1.seconds
}
