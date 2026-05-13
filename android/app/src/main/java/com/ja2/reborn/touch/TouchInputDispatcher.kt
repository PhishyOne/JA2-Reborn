package com.ja2.reborn.touch

import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import org.libsdl.app.SDLActivity
import org.libsdl.app.SDLSurface

class TouchInputDispatcher(private val surface: SDLSurface) {

    private val heldMouseButtons = mutableSetOf<Int>()
    private val heldKeyCodes = mutableSetOf<Int>()
    private val heldComboKeys = mutableMapOf<String, MutableList<Int>>()
    private var heldDpadDirection: String? = null

    fun performAction(action: TouchButtonAction, pressed: Boolean) {
        when (action.type) {
            "mouse_button" -> dispatchMouseAction(action, pressed)
            "key" -> dispatchKeyAction(action, pressed)
            "key_combo" -> dispatchKeyComboAction(action, pressed)
            "text" -> dispatchTextAction(action, pressed)
            "dpad" -> Unit
            "cheat_menu" -> Unit
            "sector_exit" -> dispatchSectorExitAction(action, pressed)
            else -> Log.w(TAG, "Unknown action type: ${action.type}")
        }
    }

    fun releaseAll() {
        heldMouseButtons.toList().forEach { button ->
            surface.performOverlayMouseButton(button, false)
        }
        heldMouseButtons.clear()

        heldComboKeys.values.toList().forEach { codes ->
            codes.reversed().forEach { keyCode ->
                SDLActivity.onNativeKeyUp(keyCode)
                heldKeyCodes.remove(keyCode)
            }
        }
        heldComboKeys.clear()

        heldKeyCodes.toList().forEach { keyCode ->
            SDLActivity.onNativeKeyUp(keyCode)
        }
        heldKeyCodes.clear()
        heldDpadDirection = null
    }

    fun performDpadDirection(direction: String?) {
        if (heldDpadDirection == direction) return

        heldDpadDirection?.let { oldDirection ->
            val oldCode = keyNameToCode(oldDirection)
            if (oldCode != null) {
                SDLActivity.onNativeKeyUp(oldCode)
                heldKeyCodes.remove(oldCode)
            }
        }

        heldDpadDirection = null

        if (direction != null) {
            val newCode = keyNameToCode(direction)
            if (newCode != null) {
                SDLActivity.onNativeKeyDown(newCode)
                heldKeyCodes.add(newCode)
                heldDpadDirection = direction
            } else {
                Log.w(TAG, "Unknown dpad direction: $direction")
            }
        }
    }

    private fun dispatchMouseAction(action: TouchButtonAction, pressed: Boolean) {
        val mouseButton = toMouseButton(action.button)
        surface.performOverlayMouseButton(mouseButton, pressed)
        if (pressed) {
            heldMouseButtons.add(mouseButton)
        } else {
            heldMouseButtons.remove(mouseButton)
        }
    }

    private fun dispatchKeyAction(action: TouchButtonAction, pressed: Boolean) {
        val keyCode = resolveKeyCode(action)
        if (keyCode == null) {
            Log.w(TAG, "Cannot resolve keyCode for action: $action")
            return
        }

        val modifiers = action.modifiers

        if (pressed) {
            sendModifiersDown(modifiers)
            SDLActivity.onNativeKeyDown(keyCode)
            heldKeyCodes.add(keyCode)
        } else {
            SDLActivity.onNativeKeyUp(keyCode)
            heldKeyCodes.remove(keyCode)
            sendModifiersUp(modifiers)
        }
    }

    private fun dispatchKeyComboAction(action: TouchButtonAction, pressed: Boolean) {
        val comboKeys = resolveComboKeys(action)
        if (comboKeys == null || comboKeys.isEmpty()) {
            Log.w(TAG, "Cannot resolve key combo for action: $action")
            return
        }

        if (comboKeys.size > MAX_COMBO_KEYS) {
            Log.w(TAG, "Key combo exceeds max ${MAX_COMBO_KEYS} keys: ${comboKeys.size}, truncating")
            return
        }

        val distinctKeys = comboKeys.distinct()
        if (distinctKeys.size != comboKeys.size) {
            Log.w(TAG, "Duplicate keys in combo, using distinct set")
        }

        val comboId = comboId(action)

        if (pressed) {
            if (heldComboKeys.containsKey(comboId)) {
                Log.w(TAG, "Combo $comboId already held, releasing first")
                releaseCombo(comboId)
            }
            val held = mutableListOf<Int>()
            for (keyCode in distinctKeys) {
                SDLActivity.onNativeKeyDown(keyCode)
                heldKeyCodes.add(keyCode)
                held.add(keyCode)
            }
            heldComboKeys[comboId] = held
        } else {
            releaseCombo(comboId)
        }
    }

    private fun releaseCombo(comboId: String) {
        val held = heldComboKeys.remove(comboId) ?: return
        for (keyCode in held.reversed()) {
            SDLActivity.onNativeKeyUp(keyCode)
            heldKeyCodes.remove(keyCode)
        }
    }

    private fun resolveComboKeys(action: TouchButtonAction): List<Int>? {
        val codes = mutableListOf<Int>()

        for (kc in action.keyCodes) {
            codes.add(kc)
        }
        for (name in action.keyNames) {
            val code = keyNameToCode(name)
            if (code != null) {
                codes.add(code)
            } else {
                Log.w(TAG, "Unknown key_name in combo: $name")
                return null
            }
        }
        if (codes.isNotEmpty()) return codes

        val mainKey = resolveKeyCode(action) ?: return null
        val modKeys = action.modifiers.mapNotNull { modifierKeyCode(it) }
        return modKeys + mainKey
    }

    private fun comboId(action: TouchButtonAction): String {
        val parts = mutableListOf<String>()
        parts.addAll(action.keyCodes.map { it.toString() })
        parts.addAll(action.keyNames)
        if (parts.isEmpty()) {
            action.keyCode?.let { parts.add(it.toString()) }
            action.keyName?.let { parts.add(it) }
        }
        return parts.joinToString("+")
    }

    private fun sendModifiersDown(modifiers: List<String>) {
        modifiers.forEach { mod ->
            val code = modifierKeyCode(mod)
            if (code != null) {
                SDLActivity.onNativeKeyDown(code)
                heldKeyCodes.add(code)
            }
        }
    }

    private fun sendModifiersUp(modifiers: List<String>) {
        modifiers.reversed().forEach { mod ->
            val code = modifierKeyCode(mod)
            if (code != null) {
                SDLActivity.onNativeKeyUp(code)
                heldKeyCodes.remove(code)
            }
        }
    }

    private fun dispatchTextAction(action: TouchButtonAction, pressed: Boolean) {
        if (!pressed) return
        val text = action.text ?: return
        SDLActivity.commitOverlayText(text)
    }

    private fun dispatchSectorExitAction(action: TouchButtonAction, pressed: Boolean) {
        if (!pressed) return
        val direction = sectorExitDirection(action.button ?: action.keyName)
        if (direction == null) {
            Log.w(TAG, "Unknown sector exit direction: ${action.button ?: action.keyName}")
            return
        }
        SDLActivity.showSectorExitMenu(direction)
    }

    private fun resolveKeyCode(action: TouchButtonAction): Int? {
        action.keyCode?.let { return it }
        action.keyName?.let { name -> return keyNameToCode(name) }
        return null
    }

    companion object {
        private const val TAG = "TouchInputDispatcher"
        private const val MAX_COMBO_KEYS = 3

        fun toMouseButton(name: String?): Int = when (name?.lowercase()) {
            "right" -> MotionEvent.BUTTON_SECONDARY
            "middle" -> MotionEvent.BUTTON_TERTIARY
            else -> MotionEvent.BUTTON_PRIMARY
        }

        fun keyNameToCode(name: String): Int? = when (name.uppercase()) {
            "ESCAPE", "ESC" -> KeyEvent.KEYCODE_ESCAPE
            "ENTER", "RETURN" -> KeyEvent.KEYCODE_ENTER
            "SPACE" -> KeyEvent.KEYCODE_SPACE
            "TAB" -> KeyEvent.KEYCODE_TAB
            "PAUSE", "BREAK" -> KeyEvent.KEYCODE_BREAK
            "BACKSPACE" -> KeyEvent.KEYCODE_DEL
            "DELETE" -> KeyEvent.KEYCODE_FORWARD_DEL
            "UP" -> KeyEvent.KEYCODE_DPAD_UP
            "DOWN" -> KeyEvent.KEYCODE_DPAD_DOWN
            "LEFT" -> KeyEvent.KEYCODE_DPAD_LEFT
            "RIGHT" -> KeyEvent.KEYCODE_DPAD_RIGHT
            "F1" -> KeyEvent.KEYCODE_F1
            "F2" -> KeyEvent.KEYCODE_F2
            "F3" -> KeyEvent.KEYCODE_F3
            "F4" -> KeyEvent.KEYCODE_F4
            "F5" -> KeyEvent.KEYCODE_F5
            "F6" -> KeyEvent.KEYCODE_F6
            "F7" -> KeyEvent.KEYCODE_F7
            "F8" -> KeyEvent.KEYCODE_F8
            "F9" -> KeyEvent.KEYCODE_F9
            "F10" -> KeyEvent.KEYCODE_F10
            "F11" -> KeyEvent.KEYCODE_F11
            "F12" -> KeyEvent.KEYCODE_F12
            "MINUS", "-" -> KeyEvent.KEYCODE_MINUS
            "EQUALS", "=" -> KeyEvent.KEYCODE_EQUALS
            "COMMA", "," -> KeyEvent.KEYCODE_COMMA
            "PERIOD", "." -> KeyEvent.KEYCODE_PERIOD
            "SLASH", "/" -> KeyEvent.KEYCODE_SLASH
            "BACKSLASH", "\\" -> KeyEvent.KEYCODE_BACKSLASH
            "SEMICOLON", ";" -> KeyEvent.KEYCODE_SEMICOLON
            "APOSTROPHE", "'" -> KeyEvent.KEYCODE_APOSTROPHE
            "LEFT_BRACKET", "[" -> KeyEvent.KEYCODE_LEFT_BRACKET
            "RIGHT_BRACKET", "]" -> KeyEvent.KEYCODE_RIGHT_BRACKET
            "GRAVE", "`" -> KeyEvent.KEYCODE_GRAVE
            "SHIFT" -> KeyEvent.KEYCODE_SHIFT_LEFT
            "CTRL" -> KeyEvent.KEYCODE_CTRL_LEFT
            "ALT" -> KeyEvent.KEYCODE_ALT_LEFT
            "A" -> KeyEvent.KEYCODE_A
            "B" -> KeyEvent.KEYCODE_B
            "C" -> KeyEvent.KEYCODE_C
            "D" -> KeyEvent.KEYCODE_D
            "E" -> KeyEvent.KEYCODE_E
            "F" -> KeyEvent.KEYCODE_F
            "G" -> KeyEvent.KEYCODE_G
            "H" -> KeyEvent.KEYCODE_H
            "I" -> KeyEvent.KEYCODE_I
            "J" -> KeyEvent.KEYCODE_J
            "K" -> KeyEvent.KEYCODE_K
            "L" -> KeyEvent.KEYCODE_L
            "M" -> KeyEvent.KEYCODE_M
            "N" -> KeyEvent.KEYCODE_N
            "O" -> KeyEvent.KEYCODE_O
            "P" -> KeyEvent.KEYCODE_P
            "Q" -> KeyEvent.KEYCODE_Q
            "R" -> KeyEvent.KEYCODE_R
            "S" -> KeyEvent.KEYCODE_S
            "T" -> KeyEvent.KEYCODE_T
            "U" -> KeyEvent.KEYCODE_U
            "V" -> KeyEvent.KEYCODE_V
            "W" -> KeyEvent.KEYCODE_W
            "X" -> KeyEvent.KEYCODE_X
            "Y" -> KeyEvent.KEYCODE_Y
            "Z" -> KeyEvent.KEYCODE_Z
            "0" -> KeyEvent.KEYCODE_0
            "1" -> KeyEvent.KEYCODE_1
            "2" -> KeyEvent.KEYCODE_2
            "3" -> KeyEvent.KEYCODE_3
            "4" -> KeyEvent.KEYCODE_4
            "5" -> KeyEvent.KEYCODE_5
            "6" -> KeyEvent.KEYCODE_6
            "7" -> KeyEvent.KEYCODE_7
            "8" -> KeyEvent.KEYCODE_8
            "9" -> KeyEvent.KEYCODE_9
            else -> null
        }

        fun modifierKeyCode(name: String): Int? = when (name.uppercase()) {
            "SHIFT" -> KeyEvent.KEYCODE_SHIFT_LEFT
            "CTRL" -> KeyEvent.KEYCODE_CTRL_LEFT
            "ALT" -> KeyEvent.KEYCODE_ALT_LEFT
            else -> null
        }

        fun sectorExitDirection(name: String?): Int? = when (name?.uppercase()) {
            "NORTH", "UP", "N" -> 0
            "EAST", "RIGHT", "E" -> 1
            "SOUTH", "DOWN", "S" -> 2
            "WEST", "LEFT", "W" -> 3
            else -> null
        }
    }
}
