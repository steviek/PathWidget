package com.sixbynine.transit.path.model

import kotlinx.serialization.Serializable

@Serializable
data class Station(
    val id: Int,
    val mRazzaApiName: String,
    val pathApiName: String,
    val displayName: String,
    val coordinates: Coordinates
)
