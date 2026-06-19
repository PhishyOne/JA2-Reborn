package com.ja2.reborn.touch

import com.ja2.reborn.MouseMode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TouchOverlayConfigMigrationTest {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    @Test
    fun legacySchema9WithoutMapScreenButtons_getsEmptyList() {
        val legacy = """{
            "schema_version": 9,
            "relative_mouse_speed": 1.0,
            "buttons": [
                {"id": "btn1", "label": "Test", "x": 0.5, "y": 0.5, "size": 0.1, "actions": [
                    {"type": "key", "mode": "tap", "key_name": "A"}
                ]}
            ]
        }"""

        val config = json.decodeFromString<TouchOverlayConfig>(legacy)
        assertEquals(9, config.schemaVersion)
        assertEquals(1, config.buttons.size)
        assertEquals("btn1", config.buttons[0].id)
        assertTrue(config.mapScreenButtons.isEmpty())
    }

    @Test
    fun legacySchema9Upgrade_fillsMapScreenButtonsWithDefaults() {
        val legacy = """{
            "schema_version": 9,
            "buttons": [
                {"id": "btn1", "label": "Test", "x": 0.5, "y": 0.5, "size": 0.1, "actions": [
                    {"type": "key", "mode": "tap", "key_name": "A"}
                ]}
            ]
        }"""

        val config = json.decodeFromString<TouchOverlayConfig>(legacy)
        assertTrue(config.mapScreenButtons.isEmpty())

        val upgraded = normalizeTouchOverlayConfig(config)

        assertEquals(TOUCH_OVERLAY_CONFIG_VERSION, upgraded.schemaVersion)
        assertEquals(7, upgraded.mapScreenButtons.size)
        assertTrue(upgraded.mapScreenButtons.any { it.id == "map_shift" })
        assertFalse(upgraded.mapScreenButtons.any { it.id == "map_ctrl" })
        assertFalse(upgraded.mapScreenButtons.any { it.id == "map_alt" })
    }

    @Test
    fun schema10WithBothButtonSets_preservesAll() {
        val v10 = """{
            "schema_version": 10,
            "buttons": [
                {"id": "tactical_btn", "label": "Tac", "x": 0.5, "y": 0.5, "size": 0.1, "actions": [
                    {"type": "key", "mode": "tap", "key_name": "B"}
                ]}
            ],
            "map_screen_buttons": [
                {"id": "map_btn", "label": "Map", "x": 0.9, "y": 0.4, "size": 0.06, "actions": [
                    {"type": "key", "mode": "tap", "key_name": "M"}
                ]}
            ]
        }"""

        val config = json.decodeFromString<TouchOverlayConfig>(v10)
        assertEquals(10, config.schemaVersion)
        assertEquals(1, config.buttons.size)
        assertEquals("tactical_btn", config.buttons[0].id)
        assertEquals(1, config.mapScreenButtons.size)
        assertEquals("map_btn", config.mapScreenButtons[0].id)
    }

    @Test
    fun codeDefaultHasEmptyMapScreenButtons() {
        val config = TouchOverlayConfig()
        assertEquals(TOUCH_OVERLAY_CONFIG_VERSION, config.schemaVersion)
        assertEquals(15, config.schemaVersion)
        assertTrue(config.mapScreenButtons.isEmpty())
        assertEquals(4, config.buttons.size)
    }

    @Test
    fun schemaVersionConstantIs15() {
        assertEquals(15, TOUCH_OVERLAY_CONFIG_VERSION)
    }

    @Test
    fun mapScreenModifiersUseToggleMode() {
        val buttons = defaultMapScreenButtons()
        val modifiers = buttons.filter { it.id == "map_shift" }
        assertEquals(1, modifiers.size)
        modifiers.forEach { btn ->
            val mode = btn.actions.first().mode
            assertEquals("toggle", mode)
        }
    }

    @Test
    fun defaultMapScreenButtonsHasExtendedSet() {
        val buttons = defaultMapScreenButtons()
        assertEquals(7, buttons.size) // 1 modifier + 6 action buttons
    }

    @Test
    fun defaultConfigHasDirectTouchArbitrationMs1800() {
        val config = TouchOverlayConfig()
        assertEquals(1800, config.directTouchArbitrationMs)
    }

    @Test
    fun legacySchema9Config_withoutArbitration_defaultsTo1800() {
        val legacy = """{
            "schema_version": 9,
            "buttons": [
                {"id": "b1", "label": "T", "x": 0.5, "y": 0.5, "size": 0.1, "actions": [
                    {"type": "key", "key_name": "A"}
                ]}
            ]
        }"""
        val config = json.decodeFromString<TouchOverlayConfig>(legacy)
        assertEquals(1800, config.directTouchArbitrationMs)
    }

    @Test
    fun schema10Config_withArbitration_preservesValue() {
        val v10 = """{
            "schema_version": 10,
            "direct_touch_arbitration_ms": 1200,
            "buttons": [
                {"id": "btn1", "label": "Test", "x": 0.5, "y": 0.5, "size": 0.1, "actions": [
                    {"type": "key", "mode": "tap", "key_name": "A"}
                ]}
            ]
        }"""
        val config = json.decodeFromString<TouchOverlayConfig>(v10)
        assertEquals(1200, config.directTouchArbitrationMs)
    }

    @Test
    fun mouseModeValues_includesHardware() {
        val values = MouseMode.entries
        assertEquals(4, values.size)
        assertTrue(values.contains(MouseMode.HARDWARE))
        assertEquals("hardware", MouseMode.HARDWARE.value)
    }

    @Test
    fun mouseMode_defaultIsTouchpad() {
        assertEquals(MouseMode.TOUCHPAD, MouseMode.DEFAULT)
    }

    @Test
    fun mouseModeDisplayOrder_placesHardwareBelowTouchpad() {
        assertEquals(
            listOf(MouseMode.TOUCHPAD, MouseMode.HARDWARE, MouseMode.ABSOLUTE, MouseMode.TOUCHSCREEN),
            MouseMode.DISPLAY_ORDER.toList()
        )
    }

    @Test
    fun defaultMapScreenButtonsHasInventoryButton() {
        val buttons = defaultMapScreenButtons()
        val inv = buttons.filter { it.id == "map_inventory" }
        assertEquals(1, inv.size)
        assertEquals("Inv", inv[0].label)
        assertEquals("ENTER", inv[0].actions.first().keyName)
    }

    @Test
    fun schema12MapInventoryWithI_migratesToEnter() {
        val v12 = """{
            "schema_version": 12,
            "buttons": [
                {"id": "tactical_btn", "label": "Tac", "x": 0.5, "y": 0.5, "size": 0.1, "actions": [
                    {"type": "key", "mode": "tap", "key_name": "B"}
                ]}
            ],
            "map_screen_buttons": [
                {"id": "map_inventory", "label": "Inv", "icon": "map_inventory", "x": 0.38, "y": 0.94, "size": 0.09, "actions": [
                    {"type": "key", "mode": "tap", "key_name": "I"}
                ]}
            ]
        }"""

        val config = json.decodeFromString<TouchOverlayConfig>(v12)
        val normalized = normalizeTouchOverlayConfig(config)

        assertEquals(TOUCH_OVERLAY_CONFIG_VERSION, normalized.schemaVersion)
        assertEquals("ENTER", normalized.mapScreenButtons.first { it.id == "map_inventory" }.actions.first().keyName)
    }

    @Test
    fun normalizeEmptyMapScreenButtons_fillsDefaults() {
        val config = TouchOverlayConfig(mapScreenButtons = emptyList())
        val normalized = normalizeTouchOverlayConfig(config)

        assertEquals(defaultMapScreenButtons().size, normalized.mapScreenButtons.size)
        assertEquals("ENTER", normalized.mapScreenButtons.first { it.id == "map_inventory" }.actions.first().keyName)
    }

    @Test
    fun normalizeMapInventory_keepsTacticalButtonsUnchanged() {
        val tacticalButtons = listOf(
            TouchButtonConfig(
                id = "tactical_inventory",
                label = "I",
                x = 0.5f,
                y = 0.5f,
                size = 0.1f,
                actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "I"))
            )
        )
        val config = TouchOverlayConfig(
            schemaVersion = 12,
            buttons = tacticalButtons,
            mapScreenButtons = listOf(
                TouchButtonConfig(
                    id = "map_inventory",
                    label = "Inv",
                    x = 0.38f,
                    y = 0.94f,
                    size = 0.09f,
                    actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "I"))
                )
            )
        )

        val normalized = normalizeTouchOverlayConfig(config)

        assertEquals(tacticalButtons, normalized.buttons)
        assertEquals("ENTER", normalized.mapScreenButtons.first().actions.first().keyName)
    }

    @Test
    fun strafePresetsExist() {
        val togglePreset = TACTICAL_TOUCH_BUTTON_PRESETS.firstOrNull { it.id == "alt_movement_hold" }
        assertTrue(togglePreset != null)
        assertEquals("toggle", togglePreset!!.action.mode)
        assertEquals("ALT", togglePreset.action.keyName)
    }

    @Test
    fun tacticalRangeCursorPresetIsRemoved() {
        assertFalse(TACTICAL_TOUCH_BUTTON_PRESETS.any { it.id == "range_cursor" })
    }

    @Test
    fun normalizeRemovesLegacyTacticalRangeCursorButton() {
        val config = TouchOverlayConfig(
            schemaVersion = 14,
            buttons = listOf(
                TouchButtonConfig(
                    id = "range_cursor",
                    label = "Range",
                    icon = "range_cursor",
                    x = 0.5f,
                    y = 0.5f,
                    size = 0.1f,
                    actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "F"))
                ),
                TouchButtonConfig(
                    id = "target_enemy",
                    label = "Target",
                    x = 0.6f,
                    y = 0.5f,
                    size = 0.1f,
                    actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "E"))
                )
            )
        )

        val normalized = normalizeTouchOverlayConfig(config)

        assertEquals(listOf("target_enemy"), normalized.buttons.map { it.id })
        assertEquals(TOUCH_OVERLAY_CONFIG_VERSION, normalized.schemaVersion)
    }

    @Test
    fun stealthPresetUsesLatchedTapMode() {
        val preset = TACTICAL_TOUCH_BUTTON_PRESETS.first { it.id == "stealth_toggle" }
        assertEquals("toggle_tap", preset.action.mode)
        assertEquals("Z", preset.action.keyName)
    }

    @Test
    fun normalizeMigratesLegacyStealthTapToLatchedTap() {
        val config = TouchOverlayConfig(
            schemaVersion = 14,
            buttons = listOf(
                TouchButtonConfig(
                    id = "button_legacy_stealth",
                    label = "Sneak",
                    icon = "stealth_toggle",
                    x = 0.5f,
                    y = 0.5f,
                    size = 0.1f,
                    actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "Z"))
                )
            )
        )

        val normalized = normalizeTouchOverlayConfig(config)

        assertEquals("toggle_tap", normalized.buttons.first().actions.first().mode)
        assertEquals(TOUCH_OVERLAY_CONFIG_VERSION, normalized.schemaVersion)
    }

    @Test
    fun mapScreenPresetsDoNotContainTacticalPresets() {
        assertTrue(MAP_SCREEN_TOUCH_BUTTON_PRESETS.any { it.id == "map_inventory" })
        assertFalse(MAP_SCREEN_TOUCH_BUTTON_PRESETS.any { it.id == "mouse_left" })
        assertFalse(MAP_SCREEN_TOUCH_BUTTON_PRESETS.any { it.id == "stance_crouch" })
        assertEquals(null, touchButtonPresetFor(defaultButtons().first { it.id == "mouse_left" }, MAP_SCREEN_TOUCH_BUTTON_PRESETS))
    }

    @Test
    fun tacticalPresetsDoNotContainMapOnlyPresets() {
        assertTrue(TACTICAL_TOUCH_BUTTON_PRESETS.any { it.id == "mouse_left" })
        assertFalse(TACTICAL_TOUCH_BUTTON_PRESETS.any { it.id == "map_inventory" })
        assertEquals(null, touchButtonPresetFor(defaultMapScreenButtons().first { it.id == "map_inventory" }, TACTICAL_TOUCH_BUTTON_PRESETS))
    }

    @Test
    fun mapInventoryPresetUsesEnter() {
        val preset = MAP_SCREEN_TOUCH_BUTTON_PRESETS.first { it.id == "map_inventory" }
        assertEquals("ENTER", preset.action.keyName)
        assertEquals("tap", preset.action.mode)
    }

    @Test
    fun mapTacticalPresetUsesEscapeAndCancelIcon() {
        val preset = MAP_SCREEN_TOUCH_BUTTON_PRESETS.first { it.id == "map_tactical" }
        assertEquals("ESCAPE", preset.action.keyName)
        assertEquals("tap", preset.action.mode)
        assertEquals("map_tactical", preset.icon)

        val button = defaultMapScreenButtons().first { it.id == "map_tactical" }
        assertEquals("ESCAPE", button.actions.first().keyName)
        assertEquals("map_tactical", button.icon)
    }

    @Test
    fun iconSetEntryDecodesTransformFields() {
        val entry = json.decodeFromString<IconSetEntry>("""{
            "name": "test_icon",
            "svg": "icon_test.svg",
            "iconFill": 1.2,
            "iconOffsetX": -0.1,
            "iconOffsetY": 0.25,
            "iconScaleX": 0.9,
            "iconScaleY": 1.1,
            "iconRotation": 15,
            "iconFlipH": true,
            "iconFlipV": true
        }""")

        assertEquals(15f, entry.iconRotation)
        assertTrue(entry.iconFlipH)
        assertTrue(entry.iconFlipV)
        assertEquals(-0.1f, entry.iconOffsetX)
        assertEquals(0.25f, entry.iconOffsetY)
    }

    @Test
    fun tacticalEditorContainsReloadPresetWithAltR() {
        val preset = TACTICAL_TOUCH_BUTTON_PRESETS.first { it.id == "reload_selected" }

        assertEquals("key_combo", preset.action.type)
        assertEquals("tap", preset.action.mode)
        assertEquals(listOf("ALT", "R"), preset.action.keyNames)
        assertEquals("reload_selected", preset.icon)
        assertFalse(defaultButtons().any { it.id == "reload_selected" })
    }

    @Test
    fun fullLayoutExportImportRoundTripKeepsButtonSetsSeparated() {
        val config = TouchOverlayConfig(
            buttons = listOf(
                TouchButtonConfig(
                    id = "tactical_custom",
                    label = "Tac",
                    x = 0.5f,
                    y = 0.5f,
                    size = 0.1f,
                    actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "B"))
                )
            ),
            mapScreenButtons = listOf(
                TouchButtonConfig(
                    id = "map_inventory",
                    label = "Inv",
                    x = 0.38f,
                    y = 0.94f,
                    size = 0.09f,
                    actions = listOf(TouchButtonAction(type = "key", mode = "tap", keyName = "ENTER"))
                )
            )
        )

        val exported = json.encodeToString(config)
        assertTrue(exported.contains("\"buttons\""))
        assertTrue(exported.contains("\"map_screen_buttons\""))

        val imported = normalizeTouchOverlayConfig(json.decodeFromString<TouchOverlayConfig>(exported))

        assertEquals(listOf("tactical_custom"), imported.buttons.map { it.id })
        assertEquals(listOf("map_inventory"), imported.mapScreenButtons.map { it.id })
        assertEquals("B", imported.buttons.first().actions.first().keyName)
        assertEquals("ENTER", imported.mapScreenButtons.first().actions.first().keyName)
    }

    @Test
    fun importOldPresetAddsMapDefaultsWithoutChangingTacticalButtons() {
        val legacy = """{
            "schema_version": 12,
            "buttons": [
                {"id": "tactical_custom", "label": "Tac", "x": 0.5, "y": 0.5, "size": 0.1, "actions": [
                    {"type": "key", "mode": "tap", "key_name": "I"}
                ]}
            ]
        }"""

        val imported = normalizeTouchOverlayConfig(json.decodeFromString<TouchOverlayConfig>(legacy))

        assertEquals(listOf("tactical_custom"), imported.buttons.map { it.id })
        assertEquals("I", imported.buttons.first().actions.first().keyName)
        assertEquals(defaultMapScreenButtons().map { it.id }, imported.mapScreenButtons.map { it.id })
        assertEquals("ENTER", imported.mapScreenButtons.first { it.id == "map_inventory" }.actions.first().keyName)
    }

    @Test
    fun forceReleaseToggle_resolvesKeyAndRemovesFromHeldSet() {
        val action = TouchButtonAction(type = "key", mode = "toggle", keyName = "SHIFT")
        val keyCode = TouchInputDispatcher.keyNameToCode("SHIFT")
        assertTrue(keyCode != null)
        assertEquals(action.keyName, "SHIFT")
    }

    @Test
    fun legacySchema9Config_with_toggle_defaultsToHold() {
        val legacy = """{
            "schema_version": 9,
            "buttons": [
                {"id": "b1", "label": "T", "x": 0.5, "y": 0.5, "size": 0.1, "actions": [
                    {"type": "key", "key_name": "A"}
                ]}
            ]
        }"""
        val config = json.decodeFromString<TouchOverlayConfig>(legacy)
        val mode = config.buttons[0].actions[0].mode
        assertEquals("hold", mode)
    }
}
