package com.ja2.reborn

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = ResolutionModeSerializer::class)
enum class ResolutionMode(val value: String) {
    MODERN("modern"),
    HIGH_RES("high_res"),
    RETRO("retro");

    companion object {
        val DEFAULT = MODERN
    }
}

object ResolutionModeSerializer : KSerializer<ResolutionMode> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ResolutionMode", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ResolutionMode) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): ResolutionMode {
        val raw = decoder.decodeString()
        return ResolutionMode.entries.firstOrNull { it.value == raw || it.name == raw }
            ?: throw SerializationException("unknown resolution mode: $raw")
    }
}
