package com.ja2.reborn.touch

import android.content.Context
import android.util.Log
import com.ja2.reborn.ResolutionMode
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TouchOverlayLoadResult(
    val config: TouchOverlayConfig,
    val defaultPresetWasReset: Boolean
)

class TouchButtonStore(
    private val filesDir: File,
    private val context: Context,
    private val resolutionMode: ResolutionMode = ResolutionMode.DEFAULT
) {

    private val jsonFormat = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private val configFile: File
        get() = File(filesDir, "$CONFIG_PATH")

    private val invalidFile: File
        get() = File(filesDir, "$CONFIG_PATH.invalid.json")

    fun loadOrDefault(): TouchOverlayConfig = loadOrDefaultWithResult().config

    fun loadOrDefaultWithResult(): TouchOverlayLoadResult {
        if (!configFile.exists()) {
            Log.i(TAG, "No existing config, loading bundled default preset")
            val defaults = loadDefaultFromRaw()
            save(defaults)
            return TouchOverlayLoadResult(defaults, defaultPresetWasReset = false)
        }

        return try {
            val raw = configFile.readText()
            val config = jsonFormat.decodeFromString<TouchOverlayConfig>(raw)
            if (needsDefaultPresetReset(config)) {
                Log.i(TAG, "Default touch preset version mismatch (${config.defaultPresetVersion} != $DEFAULT_TOUCH_PRESET_VERSION), replacing user layout with bundled default")
                val defaults = loadDefaultFromRaw()
                save(defaults)
                return TouchOverlayLoadResult(defaults, defaultPresetWasReset = true)
            }

            val migratedConfig = normalizeTouchOverlayConfig(config)
            if (config.schemaVersion != TOUCH_OVERLAY_CONFIG_VERSION || migratedConfig != config) {
                Log.i(TAG, "Config version mismatch or legacy touch mapping found (${config.schemaVersion} != $TOUCH_OVERLAY_CONFIG_VERSION), normalizing with defaults")
                var normalized = migratedConfig.copy(
                    editMode = false,
                    layoutLocked = true
                )
                if (isBundledDefaultLayout(normalized) || isLegacyGeneratedDefaultLayout(normalized)) {
                    normalized = TouchOverlayAdaptiveDefaults.apply(context, normalized, resolutionMode)
                }
                save(normalized)
                TouchOverlayLoadResult(normalized, defaultPresetWasReset = false)
            } else if (isBundledDefaultLayout(migratedConfig) || isLegacyGeneratedDefaultLayout(migratedConfig)) {
                val normalized = TouchOverlayAdaptiveDefaults.apply(context, migratedConfig, resolutionMode)
                if (normalized != migratedConfig) {
                    save(normalized)
                }
                TouchOverlayLoadResult(normalized, defaultPresetWasReset = false)
            } else {
                TouchOverlayLoadResult(migratedConfig, defaultPresetWasReset = false)
            }
        } catch (e: SerializationException) {
            Log.w(TAG, "Corrupt config, backing up and loading defaults: ${e.message}")
            try {
                configFile.copyTo(invalidFile, overwrite = true)
            } catch (ioe: Exception) {
                Log.w(TAG, "Could not backup corrupt config: ${ioe.message}")
            }
            val defaults = loadDefaultFromRaw()
            save(defaults)
            TouchOverlayLoadResult(defaults, defaultPresetWasReset = false)
        } catch (e: Exception) {
            Log.w(TAG, "Could not read config, loading defaults: ${e.message}")
            TouchOverlayLoadResult(loadDefaultFromRaw(), defaultPresetWasReset = false)
        }
    }

    fun loadDefaultFromRaw(): TouchOverlayConfig {
        return TouchOverlayAdaptiveDefaults.apply(context, loadBundledDefaultFromRaw(), resolutionMode)
    }

    private fun loadBundledDefaultFromRaw(): TouchOverlayConfig {
        return try {
            val raw = context.resources.openRawResource(
                context.resources.getIdentifier(
                    "default_touch_preset", "raw", context.packageName
                )
            ).bufferedReader().readText()
            val config = jsonFormat.decodeFromString<TouchOverlayConfig>(raw)
            normalizeTouchOverlayConfig(config).copy(
                editMode = false,
                layoutLocked = true
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not load bundled default preset, falling back to code defaults: ${e.message}")
            normalizeTouchOverlayConfig(TouchOverlayConfig())
        }
    }

    private fun isBundledDefaultLayout(config: TouchOverlayConfig): Boolean {
        return try {
            TouchOverlayAdaptiveDefaults.isSameButtonLayout(config, loadBundledDefaultFromRaw())
        } catch (e: Exception) {
            false
        }
    }

    private fun isLegacyGeneratedDefaultLayout(config: TouchOverlayConfig): Boolean {
        return try {
            TouchOverlayAdaptiveDefaults.isLegacyV7GeneratedLayout(context, config, loadBundledDefaultFromRaw())
        } catch (e: Exception) {
            false
        }
    }

    fun save(config: TouchOverlayConfig) {
        try {
            val parent = configFile.parentFile
            if (parent != null && !parent.exists()) {
                parent.mkdirs()
            }
            configFile.writeText(jsonFormat.encodeToString(stampForPersistence(config)))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save config: ${e.message}")
        }
    }

    fun importFromJson(raw: String): TouchOverlayConfig {
        return normalizeTouchOverlayConfig(jsonFormat.decodeFromString<TouchOverlayConfig>(raw))
    }

    fun exportConfigToDir(config: TouchOverlayConfig, targetDir: File): File {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val timestamp = sdf.format(Date())
        val targetFile = File(targetDir, "ja2_touch_layout_$timestamp.json")
        targetFile.writeText(jsonFormat.encodeToString(stampForPersistence(config)))
        return targetFile
    }

    fun exportConfigToDir(config: TouchOverlayConfig, targetDir: File, baseName: String): File {
        val targetFile = File(targetDir, "$baseName.json")
        targetFile.writeText(jsonFormat.encodeToString(stampForPersistence(config)))
        return targetFile
    }

    private fun stampForPersistence(config: TouchOverlayConfig): TouchOverlayConfig =
        config.copy(
            schemaVersion = TOUCH_OVERLAY_CONFIG_VERSION,
            defaultPresetVersion = DEFAULT_TOUCH_PRESET_VERSION
        )

    companion object {
        private const val TAG = "TouchButtonStore"
        private const val CONFIG_PATH = ".ja2/touch_buttons.json"
    }
}
