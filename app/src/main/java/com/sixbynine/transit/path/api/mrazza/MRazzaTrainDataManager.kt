package com.sixbynine.transit.path.api.mrazza

import com.sixbynine.transit.path.backend.TrainDataManager
import com.sixbynine.transit.path.ktx.awaitWithTimeoutAndCatchingErrors
import com.sixbynine.transit.path.logging.Logging
import com.sixbynine.transit.path.model.DepartureBoardTrain
import com.sixbynine.transit.path.model.Station
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/** Wrapper around retrofit to provide coroutines. */
class MRazzaTrainDataManager @Inject internal constructor(
    private val service: MRazzaService,
    private val logging: Logging
) : TrainDataManager {

    override suspend fun getUpcomingTrains(
        stations: List<Station>
    ): Result<Map<Station, List<DepartureBoardTrain>>> {
        return coroutineScope {
            stations.map { async { it to getUpcomingTrains(it) } }
                .awaitAll()
                .map { (station, result) ->
                    if (result.isFailure) {
                        return@coroutineScope Result.failure(result.exceptionOrNull()!!)
                    }
                    station to result.getOrThrow()
                }
                .toMap()
                .let { Result.success(it) }
        }
    }


    /** Returns the [UpcomingTrain]s for [station]. */
    private suspend fun getUpcomingTrains(station: Station): Result<List<DepartureBoardTrain>> {
        return service.getRealtimeArrivals(station.mRazzaApiName)
            .awaitWithTimeoutAndCatchingErrors(logging)
            .map { realtimeArrivals ->
                realtimeArrivals.upcomingTrains.map { upcomingTrain ->
                    DepartureBoardTrain(
                        upcomingTrain.headsign,
                        upcomingTrain.projectedArrival,
                        upcomingTrain.lineColors
                    )
                }
            }
    }
}
