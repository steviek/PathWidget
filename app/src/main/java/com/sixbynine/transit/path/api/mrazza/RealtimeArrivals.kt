package com.sixbynine.transit.path.api.mrazza

import android.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class RealtimeArrivals(val upcomingTrains: List<UpcomingTrain>)

@Serializable
data class UpcomingTrain(
    val headsign: String,
    @Serializable(with = InstantAsIsoStringSerializer::class) val projectedArrival: Instant,
    @SerialName("lineColors") private val rawLineColors: List<String>
) {

    @Transient
    val lineColors = rawLineColors.map { Color.parseColor(it) }
}

private object InstantAsIsoStringSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        return ZonedDateTime.parse(decoder.decodeString(), DATE_TIME_FORMATTER).toInstant()
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        return encoder.encodeString(
            DATE_TIME_FORMATTER.format(value.atZone(ZoneId.systemDefault()))
        )
    }
}

private const val TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ssz"
private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN)!!
