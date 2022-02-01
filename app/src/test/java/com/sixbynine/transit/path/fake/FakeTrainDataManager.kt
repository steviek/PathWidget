package com.sixbynine.transit.path.fake

import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.UpcomingTrain
import com.sixbynine.transit.path.backend.TrainDataManager
import com.sixbynine.transit.path.backend.TrainDataManagerModule
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeTrainDataManager @Inject constructor(): TrainDataManager {
  private val stationToTrain = mutableMapOf<String, Result<List<UpcomingTrain>>>()

  override suspend fun getStations(): Result<List<Station>> {
    return Result.failure(UnsupportedOperationException())
  }

  override suspend fun getUpcomingTrains(station: Station): Result<List<UpcomingTrain>> {
    return stationToTrain[station.apiName] ?: Result.failure(UnsupportedOperationException())
  }

  fun setUpcomingTrains(stationName: String, trains: List<UpcomingTrain>) {
    stationToTrain[stationName] = Result.success(trains)
  }

  fun setUpcomingTrainsFailed(stationName: String) {
    stationToTrain[stationName] = Result.failure(RuntimeException())
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
