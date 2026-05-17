package com.ja2.reborn

import android.content.Context

object LocalizationHelper {
    @JvmStatic
    fun getVanillaVersionLabel(context: Context, version: VanillaVersion): String {
        return context.getString(
            when (version) {
                VanillaVersion.DUTCH -> R.string.vanilla_version_dutch
                VanillaVersion.ENGLISH -> R.string.vanilla_version_english
                VanillaVersion.FRENCH -> R.string.vanilla_version_french
                VanillaVersion.GERMAN -> R.string.vanilla_version_german
                VanillaVersion.ITALIAN -> R.string.vanilla_version_italian
                VanillaVersion.POLISH -> R.string.vanilla_version_polish
                VanillaVersion.RUSSIAN -> R.string.vanilla_version_russian
                VanillaVersion.RUSSIAN_GOLD -> R.string.vanilla_version_russian_gold
                VanillaVersion.SIMPLIFIED_CHINESE -> R.string.vanilla_version_simplified_chinese
            }
        )
    }

    @JvmStatic
    fun getScalingQualityLabel(context: Context, quality: ScalingQuality): String {
        return context.getString(
            when (quality) {
                ScalingQuality.LINEAR -> R.string.scaling_quality_linear
                ScalingQuality.NEAR_PERFECT -> R.string.scaling_quality_near_perfect
                ScalingQuality.PERFECT -> R.string.scaling_quality_perfect
            }
        )
    }

    @JvmStatic
    fun getResolutionModeLabel(context: Context, mode: ResolutionMode): String {
        return context.getString(
            when (mode) {
                ResolutionMode.MODERN -> R.string.resolution_mode_modern
                ResolutionMode.HIGH_RES -> R.string.resolution_mode_high_res
                ResolutionMode.RETRO -> R.string.resolution_mode_retro
            }
        )
    }

    @JvmStatic
    fun getMouseModeLabel(context: Context, mode: MouseMode): String {
        return context.getString(
            when (mode) {
                MouseMode.TOUCHPAD -> R.string.mouse_mode_touchpad
                MouseMode.ABSOLUTE -> R.string.mouse_mode_absolute
                MouseMode.TOUCHSCREEN -> R.string.mouse_mode_touchscreen
            }
        )
    }
}
