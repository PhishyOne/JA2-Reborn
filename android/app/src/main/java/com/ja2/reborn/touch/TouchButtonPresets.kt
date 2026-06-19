package com.ja2.reborn.touch

data class TouchButtonPreset(
    val id: String,
    val label: String,
    val icon: String,
    val action: TouchButtonAction,
    val category: String
) {
    fun applyTo(config: TouchButtonConfig): TouchButtonConfig =
        config.copy(
            label = label,
            icon = icon,
            actions = listOf(action)
        )
}

val TACTICAL_TOUCH_BUTTON_PRESETS: List<TouchButtonPreset> = listOf(
    mousePreset("mouse_left", "Linke Maustaste", "mouse_left", "left"),
    mousePreset("mouse_right", "Rechte Maustaste", "mouse_right", "right"),
    mousePreset("mouse_middle", "Mittlere Maustaste", "mouse_middle", "middle"),
    TouchButtonPreset("dpad_map", "DPAD Kartensteuerung", "dpad_map", TouchButtonAction(type = "dpad", mode = "hold"), "Steuerung"),

    keyPreset("stance_stand", "Aufstehen", "stance_stand", "S", "Bewegung"),
    keyPreset("stance_crouch", "Ducken", "stance_crouch", "C", "Bewegung"),
    keyPreset("stance_prone", "Hinlegen", "stance_prone", "P", "Bewegung"),
    keyPreset("run_toggle", "Laufen an/aus", "run_toggle", "R", "Bewegung"),
    keyPreset("stealth_toggle", "Schleichmodus an/aus", "stealth_toggle", "Z", "Bewegung"),
    keyPreset("swap_places", "Pl\u00e4tze tauschen", "swap_places", "X", "Bewegung"),
    keyPreset("alt_movement_hold", "R\u00fcckw\u00e4rtsgang / Seitw\u00e4rtsschritte", "alt_movement_hold", "ALT", "Bewegung", mode = "hold"),
    keyPreset("strafe_hold", "Strafe (Halten)", "alt_movement_hold", "CTRL", "Bewegung", mode = "hold"),
    keyPreset("strafe_toggle", "Strafe (An/Aus)", "alt_movement_hold", "CTRL", "Bewegung", mode = "toggle"),

    keyPreset("fire_mode", "Feuermodus wechseln", "fire_mode", "B", "Kampf"),
    keyPreset("range_cursor", "Reichweite zum Cursor anzeigen", "range_cursor", "F", "Kampf"),
    keyPreset("keyring", "Schl\u00fcsselbund \u00f6ffnen", "keyring", "K", "Kampf"),
    keyPreset("auto_bandage", "Automatisches Verbinden", "auto_bandage", "A", "Kampf"),
    keyPreset("target_enemy", "N\u00e4chsten sichtbaren Feind fixieren", "target_enemy", "E", "Kampf"),
    keyPreset("cycle_targets", "Durch Ziele schalten", "cycle_targets", "N", "Kampf"),
    keyPreset("level_toggle", "Ebene wechseln", "level_toggle", "TAB", "Kampf"),
    keyPreset("look_direction", "Blickrichtung \u00e4ndern", "look_direction", "L", "Kampf"),
    keyPreset("cancel_action", "Aktuelle Aktion abbrechen", "cancel_action", "ESCAPE", "Kampf"),

    keyPreset("merc_1", "S\u00f6ldner 1 ausw\u00e4hlen", "merc_1", "F1", "UI"),
    keyPreset("merc_2", "S\u00f6ldner 2 ausw\u00e4hlen", "merc_2", "F2", "UI"),
    keyPreset("merc_3", "S\u00f6ldner 3 ausw\u00e4hlen", "merc_3", "F3", "UI"),
    keyPreset("merc_4", "S\u00f6ldner 4 ausw\u00e4hlen", "merc_4", "F4", "UI"),
    keyPreset("merc_5", "S\u00f6ldner 5 ausw\u00e4hlen", "merc_5", "F5", "UI"),
    keyPreset("merc_6", "S\u00f6ldner 6 ausw\u00e4hlen", "merc_6", "F6", "UI"),
    keyPreset("squad_1", "Squad 1 ausw\u00e4hlen", "squad_1", "1", "UI"),
    keyPreset("squad_2", "Squad 2 ausw\u00e4hlen", "squad_2", "2", "UI"),
    keyPreset("squad_3", "Squad 3 ausw\u00e4hlen", "squad_3", "3", "UI"),
    keyPreset("squad_4", "Squad 4 ausw\u00e4hlen", "squad_4", "4", "UI"),
    keyPreset("squad_5", "Squad 5 ausw\u00e4hlen", "squad_5", "5", "UI"),
    keyPreset("squad_6", "Squad 6 ausw\u00e4hlen", "squad_6", "6", "UI"),
    keyPreset("squad_7", "Squad 7 ausw\u00e4hlen", "squad_7", "7", "UI"),
    keyPreset("squad_8", "Squad 8 ausw\u00e4hlen", "squad_8", "8", "UI"),
    keyPreset("squad_9", "Squad 9 ausw\u00e4hlen", "squad_9", "9", "UI"),
    keyPreset("squad_10", "Squad 10 ausw\u00e4hlen", "squad_10", "0", "UI"),
    keyPreset("next_merc", "N\u00e4chsten S\u00f6ldner im Squad w\u00e4hlen", "next_merc", "SPACE", "UI"),
    keyPreset("map", "Kartenbildschirm \u00f6ffnen", "map", "M", "UI"),
    keyPreset("blink_items", "Gegenst\u00e4nde am Boden blinken lassen", "blink_items", "I", "UI"),
    keyPreset("options", "Optionen-Men\u00fc", "options", "O", "UI"),
    keyPreset("version_info", "Versions-Info anzeigen", "version_info", "V", "UI"),
    keyPreset("end_turn", "Zug beenden", "end_turn", "D", "UI"),
    keyPreset("wireframe", "Drahtgittermodell an/aus", "wireframe", "W", "UI"),
    keyPreset("treetops", "Baumkronen an/aus", "treetops", "T", "UI"),
    keyPreset("pause", "Spiel pausieren", "pause", "PAUSE", "UI"),
    sectorExitPreset("sector_exit_north", "Sektor nach Norden verlassen", "sector_exit_north", "north"),
    sectorExitPreset("sector_exit_east", "Sektor nach Osten verlassen", "sector_exit_east", "east"),
    sectorExitPreset("sector_exit_south", "Sektor nach S\u00fcden verlassen", "sector_exit_south", "south"),
    sectorExitPreset("sector_exit_west", "Sektor nach Westen verlassen", "sector_exit_west", "west"),

    comboPreset("quick_save", "Schnellspeichern", "quick_save", listOf("ALT", "S"), "UI"),
    comboPreset("quick_load", "Schnellladen", "quick_load", listOf("ALT", "L"), "UI"),
    comboPreset("quit_game", "Spiel beenden", "quit_game", listOf("ALT", "X"), "UI"),
    TouchButtonPreset("cheats", "Cheats", "cheats", TouchButtonAction(type = "cheat_menu", mode = "tap"), "UI")
)

val MAP_SCREEN_TOUCH_BUTTON_PRESETS: List<TouchButtonPreset> = listOf(
    keyPreset("map_shift", "Map Shift", "map_shift", "SHIFT", "Map Screen", mode = "toggle"),
    keyPreset("map_ctrl", "Map Ctrl", "map_ctrl", "CTRL", "Map Screen", mode = "toggle"),
    keyPreset("map_alt", "Map Alt", "map_alt", "ALT", "Map Screen", mode = "toggle"),
    keyPreset("map_options", "Map Options", "map_options", "O", "Map Screen"),
    keyPreset("map_time_minus", "Slower Time", "map_time_minus", "MINUS", "Map Screen"),
    keyPreset("map_time_plus", "Faster Time", "map_time_plus", "EQUALS", "Map Screen"),
    keyPreset("map_inventory", "Sector Inventory", "map_inventory", "ENTER", "Map Screen"),
    keyPreset("map_laptop", "Laptop", "map_laptop", "L", "Map Screen")
)

val TOUCH_BUTTON_PRESETS: List<TouchButtonPreset> = TACTICAL_TOUCH_BUTTON_PRESETS

fun touchButtonPresetById(id: String?, presets: List<TouchButtonPreset> = TACTICAL_TOUCH_BUTTON_PRESETS): TouchButtonPreset? =
    presets.firstOrNull { it.id == id }

fun touchButtonPresetFor(config: TouchButtonConfig, presets: List<TouchButtonPreset>): TouchButtonPreset? =
    touchButtonPresetById(config.icon, presets)
        ?: presets.firstOrNull { preset ->
            config.actions.firstOrNull()?.let { actionMatches(preset.action, it) } == true
        }

fun touchButtonPresetFor(config: TouchButtonConfig): TouchButtonPreset? =
    touchButtonPresetFor(config, TACTICAL_TOUCH_BUTTON_PRESETS)

fun touchButtonPresetFor(action: TouchButtonAction, presets: List<TouchButtonPreset>): TouchButtonPreset? =
    presets.firstOrNull { actionMatches(it.action, action) }

fun touchButtonPresetFor(action: TouchButtonAction): TouchButtonPreset? =
    touchButtonPresetFor(action, TACTICAL_TOUCH_BUTTON_PRESETS)

private fun mousePreset(id: String, label: String, icon: String, button: String): TouchButtonPreset =
    TouchButtonPreset(
        id = id,
        label = label,
        icon = icon,
        action = TouchButtonAction(type = "mouse_button", button = button, mode = "hold"),
        category = "Maus"
    )

private fun keyPreset(
    id: String,
    label: String,
    icon: String,
    keyName: String,
    category: String,
    mode: String = "tap"
): TouchButtonPreset =
    TouchButtonPreset(
        id = id,
        label = label,
        icon = icon,
        action = TouchButtonAction(type = "key", mode = mode, keyName = keyName),
        category = category
    )

private fun comboPreset(
    id: String,
    label: String,
    icon: String,
    keyNames: List<String>,
    category: String
): TouchButtonPreset =
    TouchButtonPreset(
        id = id,
        label = label,
        icon = icon,
        action = TouchButtonAction(type = "key_combo", mode = "tap", keyNames = keyNames),
        category = category
    )

private fun sectorExitPreset(id: String, label: String, icon: String, direction: String): TouchButtonPreset =
    TouchButtonPreset(
        id = id,
        label = label,
        icon = icon,
        action = TouchButtonAction(type = "sector_exit", button = direction, mode = "tap"),
        category = "UI"
    )

private fun actionMatches(expected: TouchButtonAction, actual: TouchButtonAction): Boolean {
    if (expected.type != actual.type) return false
    if (expected.mode != actual.mode) return false

    return when (expected.type) {
        "mouse_button" -> expected.button == actual.button
        "key" -> normalizedKeyName(expected.keyName) == normalizedKeyName(actual.keyName) &&
            expected.keyCode == actual.keyCode &&
            expected.modifiers.map(::normalizedKeyName) == actual.modifiers.map(::normalizedKeyName)
        "key_combo" -> expected.keyCodes == actual.keyCodes &&
            expected.keyNames.map(::normalizedKeyName) == actual.keyNames.map(::normalizedKeyName)
        "dpad" -> true
        "cheat_menu" -> true
        "sector_exit" -> expected.button == actual.button
        else -> false
    }
}

private fun normalizedKeyName(name: String?): String =
    name?.uppercase().orEmpty()
