package com.ja2.reborn

import org.junit.Assert.assertEquals
import org.junit.Test

class ResolutionPolicyTest {
    @Test
    fun calculatesExpectedPhoneModes() {
        assertResolution(1200, 540, ResolutionMode.MODERN, 2400, 1080)
        assertResolution(1600, 720, ResolutionMode.HIGH_RES, 2400, 1080)

        assertResolution(1212, 540, ResolutionMode.MODERN, 2424, 1080)
        assertResolution(1616, 720, ResolutionMode.HIGH_RES, 2424, 1080)

        assertResolution(1067, 480, ResolutionMode.MODERN, 3200, 1440)
        assertResolution(1600, 720, ResolutionMode.HIGH_RES, 3200, 1440)

        assertResolution(1280, 548, ResolutionMode.MODERN, 3840, 1644)
        assertResolution(1682, 720, ResolutionMode.HIGH_RES, 3840, 1644)
    }

    @Test
    fun calculatesExpectedTabletAndFourKModes() {
        assertResolution(960, 540, ResolutionMode.MODERN, 3840, 2160)
        assertResolution(1280, 720, ResolutionMode.HIGH_RES, 3840, 2160)

        assertResolution(960, 600, ResolutionMode.MODERN, 1920, 1200)
        assertResolution(1280, 800, ResolutionMode.HIGH_RES, 1920, 1200)

        assertResolution(960, 600, ResolutionMode.MODERN, 3840, 2400)
        assertResolution(1280, 800, ResolutionMode.HIGH_RES, 3840, 2400)
    }

    @Test
    fun retroIsAlwaysClassicResolution() {
        assertResolution(640, 480, ResolutionMode.RETRO, 3840, 2400)
        assertResolution(640, 480, ResolutionMode.RETRO, 2400, 1080)
    }

    private fun assertResolution(
        expectedWidth: Int,
        expectedHeight: Int,
        mode: ResolutionMode,
        nativeWidth: Int,
        nativeHeight: Int
    ) {
        val actual = ResolutionPolicy.calculate(mode, nativeWidth, nativeHeight)
        assertEquals(expectedWidth.toUInt(), actual.width)
        assertEquals(expectedHeight.toUInt(), actual.height)
    }
}
