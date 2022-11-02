package com.sixbynine.transit.path.backend

import com.sixbynine.transit.path.api.path.PathTrainDataManager
import com.sixbynine.transit.path.model.DepartureBoardTrain
import com.sixbynine.transit.path.model.Station
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

interface TrainDataManager {
    suspend fun getUpcomingTrains(
        stations: List<Station>
    ): Result<Map<Station, List<DepartureBoardTrain>>>
}

@InstallIn(SingletonComponent::class)
@Module
interface TrainDataManagerModule {
    @Binds
    fun bindTrainDataManager(manager: PathTrainDataManager): TrainDataManager
}
