package com.ja2.reborn.touch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val TOUCH_OVERLAY_CONFIG_VERSION = 16
const val MAP_SCREEN_INPUT_MODE_BOTH = "both"
const val MAP_SCREEN_INPUT_MODE_DIRECT_TOUCH = "direct_touch"
const val MAP_SCREEN_INPUT_MODE_TOUCHPAD_MOUSE = "touchpad_mouse"
const val DEFAULT_TOUCH_PRESET_VERSION = 20260624
const val TOUCH_PRESET_V104_RESET_VERSION = 20260619
const val TOUCH_OVERLAY_EXAMINE_NOTICE_VERSION = 20260628

@Serializable
data class TouchOverlayConfig(
    @SerialName("schema_version") val schemaVersion: Int = TOUCH_OVERLAY_CONFIG_VERSION,
    @SerialName("default_preset_version") val defaultPresetVersion: Int = 0,
    val enabled: Boolean = true,
    @SerialName("edit_mode") val editMode: Boolean = false,
    @SerialName("layout_locked") val layoutLocked: Boolean = true,
    @SerialName("relative_mouse_speed") val relativeMouseSpeed: Float = 1.45f,
    @SerialName("scroll_speed_ms") val scrollSpeedMs: Int = 35,
    @SerialName("disable_mouse_scrolling") val disableMouseScrolling: Boolean = false,
    @SerialName("hide_overlay_on_non_game_screens") val hideOverlayOnNonGameScreens: Boolean = true,
    @SerialName("tactical_map_fov_percent") val tacticalMapFovPercent: Int = 100,
    @SerialName("tactical_action_panel_scale_percent") val tacticalActionPanelScalePercent: Int = 130,
    @SerialName("direct_touch_arbitration_ms") val directTouchArbitrationMs: Int = 2500,
    @SerialName("map_screen_input_mode") val mapScreenInputMode: String = MAP_SCREEN_INPUT_MODE_BOTH,
    val buttons: List<TouchButtonConfig> = defaultButtons(),
    @SerialName("map_screen_buttons") val mapScreenButtons: List<TouchButtonConfig> = emptyList()
)

@Serializable
data class TouchButtonConfig(
    val id: String,
    val label: String = "",
    val icon: String? = null,
    val shape: String = BUTTON_SHAPE_CIRCLE,
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Float = 0.45f,
    @SerialName("icon_fill") val iconFill: Float = -1f,
    val visible: Boolean = true,
    val actions: List<TouchButtonAction> = emptyList()
)

@Serializable
data class TouchButtonAction(
    val type: String,
    val button: String? = null,
    val mode: String = "hold",
    @SerialName("key_code") val keyCode: Int? = null,
    @SerialName("key_name") val keyName: String? = null,
    @SerialName("key_codes") val keyCodes: List<Int> = emptyList(),
    @SerialName("key_names") val keyNames: List<String> = emptyList(),
    @SerialName("text") val text: String? = null,
    @SerialName("modifiers") val modifiers: List<String> = emptyList()
)

fun defaultButtons(): List<TouchButtonConfig> = listOf(
    TouchButtonConfig(id = "mouse_left", label = "Linke Maustaste", icon = "mouse_left", x = 0.035f, y = 0.2333333f, size = 0.18f, iconFill = 0.9900001f, actions = listOf(TouchButtonAction(type = "mouse_button", button = "left"))),
    TouchButtonConfig(id = "mouse_right", label = "Rechte Maustaste", icon = "mouse_right", x = 0.0875f, y = 0.1555556f, size = 0.187f, actions = listOf(TouchButtonAction(type = "mouse_button", button = "right"))),
    TouchButtonConfig(id = "button_1778182255890", label = "Zug beenden", icon = "end_turn", x = 0.035f, y = 0.1166667f, size = 0.18f, iconFill = 0.9900001f, actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "D"))),
    TouchButtonConfig(id = "button_1778182274729", label = "DPAD Kartensteuerung", icon = "dpad_map", x = 0.0175f, y = 0.3888889f, size = 0.4f, actions = listOf(TouchButtonAction(type = "dpad"))),
    TouchButtonConfig(id = "button_1778184454959", label = "Cheats", icon = "cheats", x = 0f, y = 0f, size = 0.16f, iconFill = 0.9900001f, actions = listOf(TouchButtonAction(type = "cheat_menu", mode = "tap"))),
    TouchButtonConfig(id = "button_1778184514061", label = "Schnellspeichern", icon = "quick_save", x = 0.8575f, y = 0.8166667f, size = 0.18f, iconFill = 0.9900001f, actions = listOf(TouchButtonAction(type = "key_combo", mode = "tap", keyNames = listOf("ALT", "S")))),
    TouchButtonConfig(id = "button_1778184539401", label = "Schnellladen", icon = "quick_load", x = 0.91f, y = 0.8166667f, size = 0.18f, iconFill = 0.9900001f, actions = listOf(TouchButtonAction(type = "key_combo", mode = "tap", keyNames = listOf("ALT", "L")))),
    TouchButtonConfig(id = "button_1778184559365", label = "Kartenbildschirm öffnen", icon = "map", x = 0.8575f, y = 0.7f, size = 0.18f, iconFill = 0.9900001f, actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "M"))),
    TouchButtonConfig(id = "button_1778184579274", label = "Aktuelle Aktion abbrechen", icon = "cancel_action", x = 0.91f, y = 0.7f, size = 0.18f, iconFill = 0.9900001f, actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "ESCAPE"))),
    TouchButtonConfig(id = "button_1778184596324", label = "Automatisches Verbinden", icon = "auto_bandage", x = 0.7f, y = 0f, size = 0.18f, iconFill = 0.7900001f, actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "A"))),
    TouchButtonConfig(id = "button_1778184614235", label = "Aufstehen", icon = "stance_stand", x = 0.8225f, y = 0.2333333f, size = 0.18f, iconFill = 1.14f, actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "S"))),
    TouchButtonConfig(id = "button_1778184624719", label = "Ducken", icon = "stance_crouch", x = 0.8395833f, y = 0.35f, size = 0.18f, iconFill = 0.9f, actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "C"))),
    TouchButtonConfig(id = "button_1778184641238", label = "Hinlegen", icon = "stance_prone", x = 0.8575f, y = 0.4666667f, size = 0.18f, iconFill = 1.1f, actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "P"))),
    TouchButtonConfig(id = "button_1778184648490", label = "Schleichmodus an/aus", icon = "stealth_toggle", x = 0.8925f, y = 0.3111111f, size = 0.18f, iconFill = 0.9900001f, actions = listOf(TouchButtonAction(type = "key", mode = "toggle_tap", keyName = "Z"))),
    TouchButtonConfig(id = "button_1778184658218", label = "Seitwärts-/Rückwärtsschritte", icon = "alt_movement_hold", x = 0.91f, y = 0.4277778f, size = 0.18f, iconFill = 0.9400001f, actions = listOf(TouchButtonAction(type = "key", mode = "toggle", keyName = "ALT"))),
    TouchButtonConfig(id = "button_1778184673846", label = "Feuermodus wechseln", icon = "fire_mode", x = 0.7525f, y = 0f, size = 0.18f, iconFill = 0.9400001f, actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "B"))),
    TouchButtonConfig(id = "button_1778184682442", label = "Blickrichtung ändern", icon = "look_direction", x = 0.8575f, y = 0f, size = 0.18f, iconFill = 1.2f, actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "L"))),
    TouchButtonConfig(id = "button_1778184701058", label = "Durch Ziele schalten", icon = "cycle_targets", x = 0.91f, y = 0f, size = 0.18f, iconFill = 1.1f, actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "N"))),
    TouchButtonConfig(id = "button_1781876669884", label = "Auswahl nachladen", icon = "reload_selected", x = 0.875f, y = 0.1944444f, size = 0.18f, iconFill = 0.9900001f, actions = listOf(TouchButtonAction(type = "key_combo", mode = "tap", keyNames = listOf("ALT", "R")))),
    TouchButtonConfig(id = "button_1781877119546", label = "Item Stacking", icon = "map_shift", x = 0.1225f, y = 0f, size = 0.18f, iconFill = 0.9900001f, actions = listOf(TouchButtonAction(type = "key", mode = "toggle", keyName = "SHIFT"))),
    TouchButtonConfig(id = "button_1719600000001", label = "Examine (CTRL)", icon = "ctrl_examine", x = 0.91f, y = 0.5444445f, size = 0.18f, iconFill = 0.90000004f, actions = listOf(TouchButtonAction(type = "key", mode = "toggle", keyName = "CTRL"))),
    TouchButtonConfig(id = "button_1781877195847", label = "Ebene wechseln", icon = "level_toggle", x = 0.175f, y = 0f, size = 0.18f, iconFill = 0.9900001f, actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "TAB"))),
    TouchButtonConfig(id = "button_1781879071413", label = "Plätze tauschen", icon = "swap_places", x = 0.805f, y = 0f, size = 0.18f, iconFill = 0.9900001f, actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "X")))
)

fun defaultMapScreenButtons(): List<TouchButtonConfig> = listOf(
    TouchButtonConfig(id = "mouse_left", label = "Linke Maustaste", icon = "mouse_left", x = 0.035f, y = 0.2333333f, size = 0.18f, iconFill = 0.9900001f, actions = listOf(TouchButtonAction(type = "mouse_button", button = "left"))),
    TouchButtonConfig(id = "mouse_right", label = "Rechte Maustaste", icon = "mouse_right", x = 0.0875f, y = 0.1555556f, size = 0.187f, actions = listOf(TouchButtonAction(type = "mouse_button", button = "right"))),
    TouchButtonConfig(id = "map_shift", label = "Item Stacking", icon = "map_shift", shape = BUTTON_SHAPE_SQUARE, x = 0.105f, y = 0.5055556f, size = 0.16f, iconFill = 0.9900001f, actions = listOf(TouchButtonAction(type = "key", mode = "toggle", keyName = "SHIFT"))),
    TouchButtonConfig(id = "map_options", label = "Opt", icon = "map_options", shape = BUTTON_SHAPE_RECTANGLE, x = 0.91f, y = 0.03888889f, size = 0.09f, actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "O"))),
    TouchButtonConfig(id = "map_time_minus", label = "Slower Time", icon = "map_time_minus", shape = BUTTON_SHAPE_RECTANGLE, x = 0.035f, y = 0.03888889f, size = 0.09f, iconFill = 0.9900001f, actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "MINUS"))),
    TouchButtonConfig(id = "map_time_plus", label = "Faster Time", icon = "map_time_plus", shape = BUTTON_SHAPE_RECTANGLE, x = 0.105f, y = 0.03888889f, size = 0.09f, iconFill = 0.9900001f, actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "EQUALS"))),
    TouchButtonConfig(id = "map_inventory", label = "Inv", icon = "map_inventory", shape = BUTTON_SHAPE_RECTANGLE, x = 0.84f, y = 0.1555556f, size = 0.09f, actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "ENTER"))),
    TouchButtonConfig(id = "map_laptop", label = "Lap", icon = "map_laptop", shape = BUTTON_SHAPE_RECTANGLE, x = 0.84f, y = 0.03888889f, size = 0.09f, actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "L"))),
    TouchButtonConfig(id = "button_1781881696617", label = "Leave Map", icon = "map_tactical", shape = BUTTON_SHAPE_RECTANGLE, x = 0.91f, y = 0.1555556f, size = 0.09f, iconFill = 0.9900001f, actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "ESCAPE")))
)

const val BUTTON_SHAPE_CIRCLE = "circle"
const val BUTTON_SHAPE_SQUARE = "square"
const val BUTTON_SHAPE_RECTANGLE = "rectangle"

fun normalizeTouchOverlayConfig(config: TouchOverlayConfig): TouchOverlayConfig {
    val tacticalButtons = config.buttons
        .filterNot { it.id == "range_cursor" || it.icon == "range_cursor" }
        .map { it.migrateStealthToggleTapMode() }
    val mapButtons = (config.mapScreenButtons.ifEmpty { defaultMapScreenButtons() })
        .map { it.migrateMapInventoryEnterKey() }
        .filterNot { it.id in setOf("map_ctrl", "map_alt") }
        .let { buttons ->
            if (config.schemaVersion < 14 && buttons.none { it.id == "map_tactical" || it.icon == "map_tactical" }) {
                buttons + defaultMapScreenButtons().first { it.icon == "map_tactical" }
            } else {
                buttons
            }
        }

    return config.copy(
        schemaVersion = TOUCH_OVERLAY_CONFIG_VERSION,
        defaultPresetVersion = DEFAULT_TOUCH_PRESET_VERSION,
        buttons = tacticalButtons,
        mapScreenButtons = mapButtons
    )
}

internal fun needsDefaultPresetReset(config: TouchOverlayConfig): Boolean =
    config.defaultPresetVersion < TOUCH_PRESET_V104_RESET_VERSION

private fun TouchButtonConfig.migrateMapInventoryEnterKey(): TouchButtonConfig {
    if (id != "map_inventory") return this

    val migratedActions = actions.map { action ->
        if (action.type == "key" && action.keyName == "I") {
            action.copy(keyName = "ENTER")
        } else {
            action
        }
    }

    return if (migratedActions == actions) this else copy(actions = migratedActions)
}

private fun TouchButtonConfig.migrateStealthToggleTapMode(): TouchButtonConfig {
    if (id != "stealth_toggle" && icon != "stealth_toggle") return this

    val migratedActions = actions.map { action ->
        if (action.type == "key" && action.keyName == "Z" && action.mode == "tap") {
            action.copy(mode = "toggle_tap")
        } else {
            action
        }
    }

    return if (migratedActions == actions) this else copy(actions = migratedActions)
}
