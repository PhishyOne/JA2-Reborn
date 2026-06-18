package com.ja2.reborn.touch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val TOUCH_OVERLAY_CONFIG_VERSION = 12

@Serializable
data class TouchOverlayConfig(
    @SerialName("schema_version") val schemaVersion: Int = TOUCH_OVERLAY_CONFIG_VERSION,
    val enabled: Boolean = true,
    @SerialName("edit_mode") val editMode: Boolean = false,
    @SerialName("layout_locked") val layoutLocked: Boolean = true,
    @SerialName("relative_mouse_speed") val relativeMouseSpeed: Float = 1.0f,
    @SerialName("scroll_speed_ms") val scrollSpeedMs: Int = 27,
    @SerialName("disable_mouse_scrolling") val disableMouseScrolling: Boolean = false,
    @SerialName("hide_overlay_on_non_game_screens") val hideOverlayOnNonGameScreens: Boolean = true,
    @SerialName("tactical_map_fov_percent") val tacticalMapFovPercent: Int = 100,
    @SerialName("tactical_action_panel_scale_percent") val tacticalActionPanelScalePercent: Int = 100,
    @SerialName("direct_touch_arbitration_ms") val directTouchArbitrationMs: Int = 1800,
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
    TouchButtonConfig(
        id = "mouse_left",
        label = "L",
        icon = "mouse_left",
        shape = BUTTON_SHAPE_CIRCLE,
        x = 0.42f,
        y = 0.82f,
        size = 0.140f,
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
        size = 0.140f,
        alpha = 0.45f,
        visible = true,
        actions = listOf(
            TouchButtonAction(
                type = "mouse_button",
                button = "right",
                mode = "hold"
            )
        )
    ),
    TouchButtonConfig(
        id = "strafe_hold",
        label = "STR",
        shape = BUTTON_SHAPE_CIRCLE,
        x = 0.8575f,
        y = 0.6222f,
        size = 0.140f,
        alpha = 0.45f,
        visible = true,
        actions = listOf(
            TouchButtonAction(
                type = "key",
                mode = "hold",
                keyName = "CTRL"
            )
        )
    ),
    TouchButtonConfig(
        id = "strafe_toggle",
        label = "ST",
        shape = BUTTON_SHAPE_SQUARE,
        x = 0.8925f,
        y = 0.6222f,
        size = 0.140f,
        alpha = 0.45f,
        visible = true,
        actions = listOf(
            TouchButtonAction(
                type = "key",
                mode = "toggle",
                keyName = "CTRL"
            )
        )
    ),
    TouchButtonConfig(
        id = "stance_crouch",
        label = "DU",
        icon = "stance_crouch",
        shape = BUTTON_SHAPE_CIRCLE,
        x = 0.70f,
        y = 0.82f,
        size = 0.140f,
        alpha = 0.45f,
        visible = true,
        actions = listOf(
            TouchButtonAction(
                type = "key",
                mode = "tap",
                keyName = "C"
            )
        )
    )
)

fun defaultMapScreenButtons(): List<TouchButtonConfig> = listOf(
    // Right column — sticky modifier toggles
    TouchButtonConfig(
        id = "map_shift", label = "SH", icon = "map_shift", shape = BUTTON_SHAPE_SQUARE,
        x = 0.93f, y = 0.35f, size = 0.090f,
        actions = listOf(TouchButtonAction(type = "key", mode = "toggle", keyName = "SHIFT"))
    ),
    TouchButtonConfig(
        id = "map_ctrl", label = "CT", icon = "map_ctrl", shape = BUTTON_SHAPE_SQUARE,
        x = 0.93f, y = 0.44f, size = 0.090f,
        actions = listOf(TouchButtonAction(type = "key", mode = "toggle", keyName = "CTRL"))
    ),
    TouchButtonConfig(
        id = "map_alt", label = "AL", icon = "map_alt", shape = BUTTON_SHAPE_SQUARE,
        x = 0.93f, y = 0.53f, size = 0.090f,
        actions = listOf(TouchButtonAction(type = "key", mode = "toggle", keyName = "ALT"))
    ),
    // Bottom row — map actions
    TouchButtonConfig(
        id = "map_options", label = "Opt", icon = "map_options", shape = BUTTON_SHAPE_RECTANGLE,
        x = 0.02f, y = 0.94f, size = 0.090f,
        actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "O"))
    ),
    TouchButtonConfig(
        id = "map_time_minus", label = "−", icon = "map_time_minus", shape = BUTTON_SHAPE_SQUARE,
        x = 0.14f, y = 0.94f, size = 0.090f,
        actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "MINUS"))
    ),
    TouchButtonConfig(
        id = "map_time_plus", label = "+", icon = "map_time_plus", shape = BUTTON_SHAPE_SQUARE,
        x = 0.26f, y = 0.94f, size = 0.090f,
        actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "EQUALS"))
    ),
    TouchButtonConfig(
        id = "map_inventory", label = "Inv", icon = "map_inventory", shape = BUTTON_SHAPE_RECTANGLE,
        x = 0.38f, y = 0.94f, size = 0.090f,
        actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "I"))
    ),
    TouchButtonConfig(
        id = "map_laptop", label = "Lap", icon = "map_laptop", shape = BUTTON_SHAPE_RECTANGLE,
        x = 0.50f, y = 0.94f, size = 0.090f,
        actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "L"))
    )
)

const val BUTTON_SHAPE_CIRCLE = "circle"
const val BUTTON_SHAPE_SQUARE = "square"
const val BUTTON_SHAPE_RECTANGLE = "rectangle"
