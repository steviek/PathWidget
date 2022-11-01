package com.sixbynine.transit.path.api.mrazza

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Really awesome to get data about PATH trains.
 *
 * See [https://github.com/mrazza/path-data](https://github.com/mrazza/path-data)
 */
interface MRazzaService {
  @GET("stations/{station}/realtime")
  fun getRealtimeArrivals(@Path("station") station: String): Call<RealtimeArrivals>
}
