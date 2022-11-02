package com.sixbynine.transit.path.api.path

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Path JSON service at https://www.panynj.gov/bin/portauthority/ridepath.json
 */
interface PathService {
  @GET("ridepath.json")
  fun getResults(): Call<PathServiceResults>
}
