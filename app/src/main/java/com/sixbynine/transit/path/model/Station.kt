package com.sixbynine.transit.path.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Station(
    val id: Int,
    @SerialName("station") val mRazzaApiName: String,
    @SerialName("name") val displayName: String,
    val coordinates: Coordinates
)
