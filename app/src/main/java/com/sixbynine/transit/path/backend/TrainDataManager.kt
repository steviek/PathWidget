package com.sixbynine.transit.path.backend

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.sixbynine.transit.path.api.PathDataService
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.api.UpcomingTrain
import com.sixbynine.transit.path.serialization.JsonFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import retrofit2.Retrofit

/** Wrapper around retrofit to provide coroutines. */
// TODO: Handle cancellation properly
class TrainDataManager {

  private val contentType = MediaType.get("application/json")
  private val retrofit = Retrofit.Builder()
    .baseUrl("https://path.api.razza.dev/v1/")
    .addConverterFactory(JsonFormat.asConverterFactory(contentType))
    .build()
  private val service = retrofit.create(PathDataService::class.java)

  /** Returns the list of [Station]s according to the API. */
  suspend fun getStations(): Result<List<Station>> = withContext(Dispatchers.IO) {
    val response = try {
      service.getStations().execute()
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
  suspend fun getUpcomingTrains(station: Station): Result<List<UpcomingTrain>> =
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
