package com.ja2.reborn

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolutionModeSerializationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun encodesResolutionModeAsStableLowercaseValue() {
        val config = Ja2Json(
            resolution = Resolution(1600u, 720u),
            resolutionMode = ResolutionMode.HIGH_RES,
            scalingQuality = ScalingQuality.NEAR_PERFECT
        )

        val encoded = json.encodeToString(config)

        org.junit.Assert.assertTrue(encoded.contains("\"resolution_mode\":\"high_res\""))
    }

    @Test
    fun decodesPlanValuesAndTemporaryUppercaseValues() {
        val planConfig: Ja2Json = json.decodeFromString("""{"resolution_mode":"retro"}""")
        val temporaryConfig: Ja2Json = json.decodeFromString("""{"resolution_mode":"HIGH_RES"}""")

        assertEquals(ResolutionMode.RETRO, planConfig.resolutionMode)
        assertEquals(ResolutionMode.HIGH_RES, temporaryConfig.resolutionMode)
    }
}
