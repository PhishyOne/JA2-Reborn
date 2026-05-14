package com.ja2.reborn.touch

import android.content.Context
import android.content.res.Configuration
import kotlin.math.max
import kotlin.math.min

object TouchOverlayAdaptiveDefaults {
    fun apply(context: Context, config: TouchOverlayConfig): TouchOverlayConfig {
        val profile = DeviceProfile.from(context)
        val panelScale = when {
            profile.isTablet -> 100
            profile.aspect >= 2.05f -> 130
            profile.aspect >= 1.80f -> 120
            else -> 110
        }

        return config.copy(
            schemaVersion = TOUCH_OVERLAY_CONFIG_VERSION,
            editMode = false,
            layoutLocked = true,
            tacticalActionPanelScalePercent = panelScale,
            buttons = adaptButtons(config.buttons, profile)
        )
    }

    fun isSameButtonLayout(a: TouchOverlayConfig, b: TouchOverlayConfig): Boolean {
        if (a.buttons.size != b.buttons.size) return false
        return a.buttons.zip(b.buttons).all { (left, right) ->
            left.id == right.id &&
                near(left.x, right.x) &&
                near(left.y, right.y) &&
                near(left.size, right.size)
        }
    }

    fun isLegacyV7GeneratedLayout(context: Context, config: TouchOverlayConfig, bundledDefault: TouchOverlayConfig): Boolean {
        val legacy = bundledDefault.copy(
            schemaVersion = config.schemaVersion,
            buttons = adaptButtonsV7(bundledDefault.buttons, DeviceProfile.from(context))
        )
        return isSameButtonLayout(config, legacy)
    }

    private fun adaptButtons(buttons: List<TouchButtonConfig>, profile: DeviceProfile): List<TouchButtonConfig> {
        if (profile.aspect >= 2.05f && !profile.isTablet) return buttons

        val scale = when {
            profile.isTablet -> 0.84f
            profile.aspect < 1.70f -> 0.86f
            else -> 0.92f
        }

        return buttons.map { button ->
            val adapted = button.copy(size = (button.size * scale).coerceIn(0.055f, button.size))
            when (button.icon) {
                "dpad_map" -> adapted.copy(size = (button.size * if (profile.isTablet) 0.82f else 0.88f).coerceAtLeast(0.28f))
                else -> adapted
            }
        }
    }

    private fun adaptButtonsV7(buttons: List<TouchButtonConfig>, profile: DeviceProfile): List<TouchButtonConfig> {
        if (profile.aspect >= 2.05f && !profile.isTablet) return buttons

        val scale = when {
            profile.isTablet -> 0.84f
            profile.aspect < 1.70f -> 0.86f
            else -> 0.92f
        }

        return buttons.map { button ->
            val adapted = button.copy(size = (button.size * scale).coerceIn(0.055f, button.size))
            when (button.icon) {
                "dpad_map" -> adapted.copy(size = (button.size * if (profile.isTablet) 0.82f else 0.88f).coerceAtLeast(0.28f))
                "mouse_left" -> adapted.copy(x = if (profile.isTablet) 0.030f else adapted.x)
                "mouse_right" -> adapted.copy(x = if (profile.isTablet) 0.095f else adapted.x)
                "end_turn" -> adapted.copy(x = if (profile.isTablet) 0.030f else adapted.x)
                "cheats" -> adapted.copy(x = 0.855f, y = 0.890f)
                "quick_save" -> adapted.copy(x = 0.790f, y = 0.770f)
                "quick_load" -> adapted.copy(x = 0.790f, y = 0.890f)
                "map" -> adapted.copy(x = 0.855f, y = 0.770f)
                "cancel_action" -> adapted.copy(x = 0.920f, y = 0.770f)
                "auto_bandage" -> adapted.copy(x = 0.920f, y = 0.890f)
                "look_direction" -> adapted.copy(x = 0.820f, y = 0.145f)
                "cycle_targets" -> adapted.copy(x = 0.885f, y = 0.145f)
                "fire_mode" -> adapted.copy(x = 0.950f, y = 0.145f)
                "stance_stand" -> adapted.copy(x = 0.820f, y = 0.290f)
                "stance_crouch" -> adapted.copy(x = 0.885f, y = 0.290f)
                "stance_prone" -> adapted.copy(x = 0.950f, y = 0.290f)
                "stealth_toggle" -> adapted.copy(x = 0.855f, y = 0.435f)
                "alt_movement_hold" -> adapted.copy(x = 0.920f, y = 0.435f)
                else -> adapted
            }
        }
    }

    private fun near(a: Float, b: Float): Boolean = kotlin.math.abs(a - b) < 0.0005f

    private data class DeviceProfile(
        val aspect: Float,
        val isTablet: Boolean
    ) {
        companion object {
            fun from(context: Context): DeviceProfile {
                val dm = context.resources.displayMetrics
                val width = max(dm.widthPixels, dm.heightPixels).coerceAtLeast(1)
                val height = min(dm.widthPixels, dm.heightPixels).coerceAtLeast(1)
                val config = context.resources.configuration
                val isTablet = config.smallestScreenWidthDp >= 600 ||
                    (config.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
                return DeviceProfile(width.toFloat() / height.toFloat(), isTablet)
            }
        }
    }
}
