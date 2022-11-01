package com.sixbynine.transit.path.api.mrazza

import android.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class RealtimeArrivals(val upcomingTrains: List<UpcomingTrain>)

@Serializable
data class UpcomingTrain(
    val lineName: String,
    val headsign: String,
    val route: String,
    val routeDisplayName: String,
    val direction: String,
    @SerialName("projectedArrival") private val rawProjectedArrival: String,
    @SerialName("lineColors") private val rawLineColors: List<String>
) {

  @Transient
  val projectedArrival = ZonedDateTime.parse(rawProjectedArrival, DATE_TIME_FORMATTER).toInstant()!!

  @Transient
  val lineColors = rawLineColors.map { Color.parseColor(it) }
}

const val TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ssz"
val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN)
