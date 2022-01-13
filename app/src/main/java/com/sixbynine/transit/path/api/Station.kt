package com.sixbynine.transit.path.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Stations(val stations: List<Station>)

@Serializable
data class Station(
    val id: Int,
    @SerialName("station") val apiName: String,
    @SerialName("name") val displayName: String,
    val coordinates: Coordinates
)

@Serializable
data class Coordinates(val latitude: Double, val longitude: Double)
