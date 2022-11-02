package com.sixbynine.transit.path.api.path

import com.sixbynine.transit.path.ktx.seconds
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Serializable
data class PathServiceResults(val results: List<PathServiceResult>)

@Serializable
data class PathServiceResult(
    val consideredStation: String,
    val destinations: List<PathDestination>
)

@Serializable
data class PathDestination(
    val label: String,
    val messages: List<PathDestinationMessage>
)

@Serializable
data class PathDestinationMessage(
    val target: String,
    val secondsToArrival: String,
    val lineColor: String,
    val headSign: String,
    @Serializable(with = InstantStringSerializer::class) val lastUpdated: Instant
) {
    val durationToArrival get() = secondsToArrival.toInt().seconds
}

private object InstantStringSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", STRING)

    override fun deserialize(decoder: Decoder): Instant {
        return ZonedDateTime.parse(decoder.decodeString()).toInstant()
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.atZone(ZoneId.systemDefault()).toString())
    }
}
