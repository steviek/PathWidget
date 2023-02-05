package com.sixbynine.transit.path.api.path

import com.sixbynine.transit.path.backend.TrainDataManager
import com.sixbynine.transit.path.ktx.awaitWithTimeoutAndCatchingErrors
import com.sixbynine.transit.path.logging.Logging
import com.sixbynine.transit.path.model.DepartureBoardTrain
import com.sixbynine.transit.path.model.Station
import java.time.Instant
import javax.inject.Inject

class PathTrainDataManager @Inject constructor(
    private val service: PathService,
    private val logging: Logging
) : TrainDataManager {
    override suspend fun getUpcomingTrains(
        stations: List<Station>
    ): Result<Map<Station, List<DepartureBoardTrain>>> {
        val results = service.getResults().awaitWithTimeoutAndCatchingErrors(logging).getOrElse {
            return Result.failure(it)
        }

        logging.debug("Finished getting response from path service")

        val stationsToCheck = stations.associateBy { it.pathApiName }
        logging.debug("building stations to check done")
        return kotlin.runCatching {
            results.results.mapNotNull { result ->
                val station = stationsToCheck[result.consideredStation] ?: return@mapNotNull null

                val trains = result.destinations
                    .flatMap { it.messages }
                    .map {
                        DepartureBoardTrain.withRawColors(
                            headsign = it.headSign,
                            projectedArrival = (it.lastUpdated + it.durationToArrival)
                                .coerceAtLeast(Instant.now()),
                            lineColors = it.lineColor.split(',').map { "#$it" }
                        )
                    }

                station to trains
            }
                .toMap()
                .also {
                    logging.debug("returning from getUpcoming trains")
                }
        }
            .onFailure {
                logging.warn("error", it)
            }
    }
}
