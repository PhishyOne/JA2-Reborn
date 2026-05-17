package com.ja2.reborn

import kotlin.math.abs
import kotlin.math.roundToInt

object ResolutionPolicy {

    private const val MIN_WIDTH = 640
    private const val MIN_HEIGHT = 480
    private const val ASPECT_ERROR_TOLERANCE = 0.002
    private const val INTEGRAL_SCALE_TOLERANCE = 0.002
    private const val TABLET_HEIGHT_THRESHOLD = 1200

    data class Candidate(
        val width: Int,
        val height: Int,
        val divisor: Int = -1,
        val isPreferred: Boolean = false
    )

    fun calculate(mode: ResolutionMode, nativeWidth: Int, nativeHeight: Int): Resolution {
        if (mode == ResolutionMode.RETRO) {
            return Resolution(640u, 480u)
        }

        val nativeW = maxOf(nativeWidth, nativeHeight)
        val nativeH = minOf(nativeWidth, nativeHeight)
        val aspect = nativeW.toDouble() / nativeH.toDouble()
        val isTablet = nativeH >= TABLET_HEIGHT_THRESHOLD && aspect <= 2.0

        val config = modeConfig(mode, isTablet, nativeH)
        val candidates = mutableListOf<Candidate>()

        for (divisor in 2..5) {
            val cW = (nativeW.toDouble() / divisor).roundToInt()
            val cH = (nativeH.toDouble() / divisor).roundToInt()
            if (isValidCandidate(cW, cH, nativeW, nativeH, config.minH, config.maxH, aspect)) {
                candidates.add(Candidate(cW, cH, divisor = divisor))
            }
        }

        val prefW = (config.targetH * aspect).roundToInt()
        val cappedW = if (prefW > nativeW) nativeW else prefW
        val finalW = cappedW - (cappedW % 2)
        val finalH = if (prefW > nativeW) {
            val recomputed = (nativeW.toDouble() / aspect).roundToInt()
            recomputed - (recomputed % 2)
        } else {
            val even = config.targetH - (config.targetH % 2)
            even
        }

        candidates.add(
            Candidate(
                width = finalW,
                height = finalH,
                isPreferred = true
            )
        )

        return selectBest(candidates, config.targetH, nativeW, nativeH)
    }

    private fun modeConfig(mode: ResolutionMode, isTablet: Boolean, nativeH: Int): ModeConfig {
        val preferredH: Int
        val minH: Int
        val maxH: Int
        val capH: Int

        when {
            mode == ResolutionMode.MODERN && isTablet -> {
                preferredH = 600; minH = 520; maxH = 640; capH = nativeH / 2
            }
            mode == ResolutionMode.MODERN -> {
                preferredH = 540; minH = 480; maxH = 560; capH = nativeH / 2
            }
            mode == ResolutionMode.HIGH_RES && isTablet -> {
                preferredH = 800; minH = 680; maxH = 800; capH = nativeH
            }
            mode == ResolutionMode.HIGH_RES -> {
                preferredH = 720; minH = 600; maxH = 720; capH = nativeH
            }
            else -> {
                preferredH = 540; minH = 480; maxH = 560; capH = nativeH / 2
            }
        }

        val targetH = minOf(preferredH, capH).coerceIn(MIN_HEIGHT, maxH)
        return ModeConfig(targetH, minH, maxH, capH)
    }

    private data class ModeConfig(
        val targetH: Int,
        val minH: Int,
        val maxH: Int,
        val capH: Int
    )

    private fun isValidCandidate(
        cW: Int, cH: Int,
        nativeW: Int, nativeH: Int,
        minH: Int, maxH: Int,
        aspect: Double
    ): Boolean {
        if (cW < MIN_WIDTH || cH < MIN_HEIGHT) return false
        if (cW > nativeW || cH > nativeH) return false
        if (cH < minH || cH > maxH) return false
        val candidateAspect = cW.toDouble() / cH.toDouble()
        val aspectError = abs(candidateAspect - aspect) / aspect
        if (aspectError > ASPECT_ERROR_TOLERANCE) return false
        return true
    }

    private fun selectBest(
        candidates: List<Candidate>,
        targetH: Int,
        nativeW: Int,
        nativeH: Int
    ): Resolution {
        val integerScale = candidates.filter { c ->
            !c.isPreferred && hasNearIntegralScale(c, nativeW, nativeH)
        }

        val pool = if (integerScale.isNotEmpty()) integerScale else candidates

        val best = pool.minWith(
            compareBy<Candidate> { abs(it.height - targetH) }
                .thenBy { -(it.width * it.height) }
        )

        return Resolution(best.width.toUInt(), best.height.toUInt())
    }

    private fun hasNearIntegralScale(c: Candidate, nativeW: Int, nativeH: Int): Boolean {
        val scaleW = nativeW.toDouble() / c.width.toDouble()
        val scaleH = nativeH.toDouble() / c.height.toDouble()
        return abs(scaleW - scaleW.roundToInt()) <= INTEGRAL_SCALE_TOLERANCE &&
                abs(scaleH - scaleH.roundToInt()) <= INTEGRAL_SCALE_TOLERANCE
    }
}
