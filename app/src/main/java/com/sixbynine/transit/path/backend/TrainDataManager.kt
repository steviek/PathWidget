package com.sixbynine.transit.path.backend

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.sixbynine.transit.path.api.PathDataService
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.UpcomingTrain
import com.sixbynine.transit.path.ktx.seconds
import com.sixbynine.transit.path.serialization.JsonFormat
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType
import retrofit2.Retrofit
import javax.inject.Inject

interface TrainDataManager {
  suspend fun getStations(): Result<List<Station>>
  suspend fun getUpcomingTrains(station: Station): Result<List<UpcomingTrain>>
}

@InstallIn(SingletonComponent::class)
@Module
interface TrainDataManagerModule {
  @Binds
  fun bindTrainDataManager(manager: DefaultTrainDataManager): TrainDataManager
}

@InstallIn(SingletonComponent::class)
@Module
object PathDataServiceModule {
  @Provides
  fun providePathDataService(): PathDataService {
    val contentType = MediaType.get("application/json")
    val retrofit = Retrofit.Builder()
      .baseUrl("https://path.api.razza.dev/v1/")
      .addConverterFactory(JsonFormat.asConverterFactory(contentType))
      .build()
    return retrofit.create(PathDataService::class.java)
  }
}

/** Wrapper around retrofit to provide coroutines. */
// TODO: Handle cancellation properly
class DefaultTrainDataManager @Inject internal constructor(
  private val service: PathDataService
): TrainDataManager {

  /** Returns the list of [Station]s according to the API. */
  override suspend fun getStations(): Result<List<Station>> = withContext(Dispatchers.IO) {
    val response = try {
      withTimeout(2.seconds.toMillis()) {
        service.getStations().execute()
      }
    } catch (t: Throwable) {
      return@withContext Result.failure(t)
    }

    val body = response.body()
    if (!response.isSuccessful || body == null) {
      return@withContext Result.failure(RuntimeException(response.errorBody().toString()))
    }

    Result.success(body.stations)
  }

  /** Returns the [UpcomingTrain]s for [station]. */
  override suspend fun getUpcomingTrains(station: Station): Result<List<UpcomingTrain>> =
    withContext(Dispatchers.IO) {
      val response = try {
        service.getRealtimeArrivals(station.apiName).execute()
      } catch (t: Throwable) {
        return@withContext Result.failure(t)
      }

      val body = response.body()
      if (!response.isSuccessful || body == null) {
        return@withContext Result.failure(RuntimeException(response.errorBody().toString()))
      }

      Result.success(body.upcomingTrains)
    }
}
