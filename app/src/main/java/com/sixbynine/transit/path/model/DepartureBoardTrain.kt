package com.sixbynine.transit.path.model

import android.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind.LONG
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

@Serializable
data class DepartureBoardTrain(
    val headsign: String,
    @Serializable(with = InstantAsEpochMilliSerializer::class) val projectedArrival: Instant,
    val lineColors: List<Int>
) {

    companion object {
        fun withRawColors(
            headsign: String,
            projectedArrival: Instant,
            lineColors: List<String>
        ) = DepartureBoardTrain(headsign, projectedArrival, lineColors.map { Color.parseColor(it) })
    }
}

private object InstantAsEpochMilliSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", LONG)

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.ofEpochMilli(decoder.decodeLong())
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        return encoder.encodeLong(value.toEpochMilli())
    }
}
