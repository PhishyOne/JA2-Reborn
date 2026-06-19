package com.ja2.reborn.touch

import android.content.Context
import com.ja2.reborn.R

object TouchButtonLocalization {

    fun getPresetLabel(context: Context, presetId: String): String {
        val resId = when (presetId) {
            "mouse_left" -> R.string.touch_preset_mouse_left
            "mouse_right" -> R.string.touch_preset_mouse_right
            "mouse_middle" -> R.string.touch_preset_mouse_middle
            "dpad_map" -> R.string.touch_preset_dpad_map
            "stance_stand" -> R.string.touch_preset_stance_stand
            "stance_crouch" -> R.string.touch_preset_stance_crouch
            "stance_prone" -> R.string.touch_preset_stance_prone
            "run_toggle" -> R.string.touch_preset_run_toggle
            "stealth_toggle" -> R.string.touch_preset_stealth_toggle
            "swap_places" -> R.string.touch_preset_swap_places
            "alt_movement_hold" -> R.string.touch_preset_alt_movement_hold
            "shift_toggle" -> R.string.touch_preset_shift_toggle
            "fire_mode" -> R.string.touch_preset_fire_mode
            "keyring" -> R.string.touch_preset_keyring
            "auto_bandage" -> R.string.touch_preset_auto_bandage
            "reload_selected" -> R.string.touch_preset_reload_selected
            "target_enemy" -> R.string.touch_preset_target_enemy
            "cycle_targets" -> R.string.touch_preset_cycle_targets
            "level_toggle" -> R.string.touch_preset_level_toggle
            "look_direction" -> R.string.touch_preset_look_direction
            "cancel_action" -> R.string.touch_preset_cancel_action
            "merc_1" -> R.string.touch_preset_merc_1
            "merc_2" -> R.string.touch_preset_merc_2
            "merc_3" -> R.string.touch_preset_merc_3
            "merc_4" -> R.string.touch_preset_merc_4
            "merc_5" -> R.string.touch_preset_merc_5
            "merc_6" -> R.string.touch_preset_merc_6
            "squad_1" -> R.string.touch_preset_squad_1
            "squad_2" -> R.string.touch_preset_squad_2
            "squad_3" -> R.string.touch_preset_squad_3
            "squad_4" -> R.string.touch_preset_squad_4
            "squad_5" -> R.string.touch_preset_squad_5
            "squad_6" -> R.string.touch_preset_squad_6
            "squad_7" -> R.string.touch_preset_squad_7
            "squad_8" -> R.string.touch_preset_squad_8
            "squad_9" -> R.string.touch_preset_squad_9
            "squad_10" -> R.string.touch_preset_squad_10
            "next_merc" -> R.string.touch_preset_next_merc
            "map" -> R.string.touch_preset_map
            "blink_items" -> R.string.touch_preset_blink_items
            "options" -> R.string.touch_preset_options
            "version_info" -> R.string.touch_preset_version_info
            "end_turn" -> R.string.touch_preset_end_turn
            "wireframe" -> R.string.touch_preset_wireframe
            "treetops" -> R.string.touch_preset_treetops
            "pause" -> R.string.touch_preset_pause
            "sector_exit_north" -> R.string.touch_preset_sector_exit_north
            "sector_exit_east" -> R.string.touch_preset_sector_exit_east
            "sector_exit_south" -> R.string.touch_preset_sector_exit_south
            "sector_exit_west" -> R.string.touch_preset_sector_exit_west
            "quick_save" -> R.string.touch_preset_quick_save
            "quick_load" -> R.string.touch_preset_quick_load
            "quit_game" -> R.string.touch_preset_quit_game
            "cheats" -> R.string.touch_preset_cheats
            "map_shift" -> R.string.touch_preset_map_shift
            "map_options" -> R.string.touch_preset_map_options
            "map_time_minus" -> R.string.touch_preset_map_time_minus
            "map_time_plus" -> R.string.touch_preset_map_time_plus
            "map_inventory" -> R.string.touch_preset_map_inventory
            "map_laptop" -> R.string.touch_preset_map_laptop
            "map_tactical" -> R.string.touch_preset_map_tactical
            else -> return presetId
        }
        return context.getString(resId)
    }

    fun getCategoryLabel(context: Context, category: String): String {
        val resId = when (category.lowercase()) {
            "maus", "mouse" -> R.string.touch_preset_category_mouse
            "steuerung", "controls" -> R.string.touch_preset_category_controls
            "bewegung", "movement" -> R.string.touch_preset_category_movement
            "kampf", "combat" -> R.string.touch_preset_category_combat
            "ui" -> R.string.touch_preset_category_ui
            "map screen" -> R.string.touch_preset_category_map_screen
            else -> return category
        }
        return context.getString(resId)
    }

    fun getActionTypeDisplayName(context: Context, action: TouchButtonAction): String {
        return when (action.type) {
            "mouse_button" -> when (action.button?.lowercase()) {
                "right" -> context.getString(R.string.touch_action_right)
                "middle" -> context.getString(R.string.touch_action_middle)
                else -> context.getString(R.string.touch_action_left)
            }
            "key" -> getKeyShortName(context, action.keyName ?: action.keyCode?.toString())
            "key_combo" -> {
                val parts = mutableListOf<String>()
                parts.addAll(action.keyNames.map { getKeyShortName(context, it) })
                parts.addAll(action.keyCodes.map { it.toString() })
                parts.joinToString("+").ifEmpty { context.getString(R.string.touch_action_combo) }
            }
            "cheat_menu" -> context.getString(R.string.touch_action_cheats)
            else -> action.type.ifEmpty { context.getString(R.string.touch_action_unknown) }
        }
    }

    fun getKeyShortName(context: Context, name: String?): String {
        val resId = when (name?.uppercase()) {
            null, "" -> R.string.touch_key_unknown
            "ESCAPE" -> R.string.touch_key_escape
            "SPACE" -> R.string.touch_key_space
            "ENTER" -> R.string.touch_key_enter
            "BACKSPACE" -> R.string.touch_key_backspace
            "DELETE" -> R.string.touch_key_delete
            "UP" -> R.string.touch_key_up
            "DOWN" -> R.string.touch_key_down
            "LEFT" -> R.string.touch_key_left
            "RIGHT" -> R.string.touch_key_right
            "TAB" -> R.string.touch_key_tab
            "LEFT_BRACKET" -> return "["
            "RIGHT_BRACKET" -> return "]"
            "SEMICOLON" -> return ";"
            "APOSTROPHE" -> return "'"
            "COMMA" -> return ","
            "PERIOD" -> return "."
            "SLASH" -> return "/"
            "BACKSLASH" -> return "\\"
            "MINUS" -> return "-"
            "EQUALS" -> return "="
            "GRAVE" -> return "`"
            else -> return name
        }
        return context.getString(resId)
    }

    fun getIconFallback(context: Context, icon: String?): String = when (icon) {
        "mouse_left" -> context.getString(R.string.touch_key_left)
        "mouse_right" -> context.getString(R.string.touch_key_right)
        "mouse_middle" -> context.getString(R.string.touch_key_middle)
        "arrow_up" -> "▲"
        "arrow_down" -> "▼"
        "arrow_left" -> "◀"
        "arrow_right" -> "▶"
        "escape" -> context.getString(R.string.touch_key_escape)
        "space" -> "␣"
        "enter" -> "↵"
        "tab" -> context.getString(R.string.touch_key_tab)
        "keyboard" -> "⌨"
        "inventory" -> context.getString(R.string.touch_icon_inventory)
        "map" -> context.getString(R.string.touch_icon_map)
        "pause" -> "⏸"
        else -> icon ?: context.getString(R.string.touch_action_unknown)
    }
}
