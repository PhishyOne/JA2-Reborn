package com.ja2.reborn.touch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val TOUCH_OVERLAY_CONFIG_VERSION = 8

@Serializable
data class TouchOverlayConfig(
    @SerialName("schema_version") val schemaVersion: Int = TOUCH_OVERLAY_CONFIG_VERSION,
    val enabled: Boolean = true,
    @SerialName("edit_mode") val editMode: Boolean = false,
    @SerialName("layout_locked") val layoutLocked: Boolean = true,
    @SerialName("relative_mouse_speed") val relativeMouseSpeed: Float = 1.0f,
    @SerialName("scroll_speed_ms") val scrollSpeedMs: Int = 27,
    @SerialName("hide_overlay_on_non_game_screens") val hideOverlayOnNonGameScreens: Boolean = true,
    @SerialName("tactical_map_fov_percent") val tacticalMapFovPercent: Int = 100,
    @SerialName("tactical_action_panel_scale_percent") val tacticalActionPanelScalePercent: Int = 100,
    val buttons: List<TouchButtonConfig> = defaultButtons()
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
    TouchButtonConfig(
        id = "mouse_left",
        label = "L",
        icon = "mouse_left",
        shape = BUTTON_SHAPE_CIRCLE,
        x = 0.42f,
        y = 0.82f,
        size = 0.085f,
        alpha = 0.45f,
        visible = true,
        actions = listOf(
            TouchButtonAction(
                type = "mouse_button",
                button = "left",
                mode = "hold"
            )
        )
    ),
    TouchButtonConfig(
        id = "mouse_right",
        label = "R",
        icon = "mouse_right",
        shape = BUTTON_SHAPE_CIRCLE,
        x = 0.58f,
        y = 0.82f,
        size = 0.085f,
        alpha = 0.45f,
        visible = true,
        actions = listOf(
            TouchButtonAction(
                type = "mouse_button",
                button = "right",
                mode = "hold"
            )
        )
    )
)

const val BUTTON_SHAPE_CIRCLE = "circle"
const val BUTTON_SHAPE_SQUARE = "square"
const val BUTTON_SHAPE_RECTANGLE = "rectangle"
