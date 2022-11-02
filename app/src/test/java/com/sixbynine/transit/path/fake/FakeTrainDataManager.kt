package com.sixbynine.transit.path.fake

import com.sixbynine.transit.path.backend.TrainDataManager
import com.sixbynine.transit.path.backend.TrainDataManagerModule
import com.sixbynine.transit.path.model.DepartureBoardTrain
import com.sixbynine.transit.path.model.Station
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeTrainDataManager @Inject constructor(): TrainDataManager {
  private val stationToTrain = mutableMapOf<String,List<DepartureBoardTrain>>()
  private var hasFailure = false

  override suspend fun getUpcomingTrains(
    stations: List<Station>
  ): Result<Map<Station, List<DepartureBoardTrain>>> {
    if (hasFailure) {
      return Result.failure(RuntimeException())
    }
    return stations.associateWith {
      stationToTrain[it.mRazzaApiName] ?: return Result.failure(RuntimeException())
    }.let { Result.success(it) }
  }

  fun setUpcomingTrains(stationName: String, trains: List<DepartureBoardTrain>) {
    stationToTrain[stationName] = trains
    hasFailure = false
  }

  fun setUpcomingTrainsFailed() {
    hasFailure = true
  }
}

@TestInstallIn(
  components = [SingletonComponent::class],
  replaces = [TrainDataManagerModule::class]
)
@Module
interface FakeTrainDataManagerModule {
  @Binds
  fun bindTrainDataManager(fake: FakeTrainDataManager): TrainDataManager
}
