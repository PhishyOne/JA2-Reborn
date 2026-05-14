package com.ja2.reborn.cheat

import android.app.AlertDialog
import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.StringRes
import com.ja2.reborn.CheatConfig
import com.ja2.reborn.Ja2GuiStyle
import com.ja2.reborn.R
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.libsdl.app.SDLActivity
import java.io.File
import java.io.IOException

const val CHEAT_ENABLED                  = 0
const val CHEAT_GOD_MODE                 = 1
const val CHEAT_NON_LETHAL_PLAYER_DAMAGE = 2
const val CHEAT_FULL_MEDICAL_HEALING     = 3
const val CHEAT_UNLIMITED_AMMO           = 4
const val CHEAT_NO_WEAPON_JAM            = 5
const val CHEAT_UNLIMITED_AP             = 6
const val CHEAT_UNLIMITED_BREATH         = 7
const val CHEAT_REVEAL_ENEMIES           = 8
const val CHEAT_REVEAL_ITEMS             = 9
const val CHEAT_ONE_HIT_KILL             = 10
const val CHEAT_PERFECT_HIT_CHANCE       = 11

private const val CHEAT_ACTION_HEAL_TEAM = 0
private const val CHEAT_ACTION_RELOAD_TEAM = 1
private const val CHEAT_ACTION_RELOAD_SELECTED = 2
private const val CHEAT_ACTION_GRANT_MONEY = 3
private const val DEFAULT_MONEY_GRANT = 100000

class CheatOverlayDialog(
    private val context: Context,
    private val filesDir: File
) {
    private val jsonFormat = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val cheatsJsonPath: String
        get() = "${filesDir.absolutePath}/.ja2/cheats.json"

    private data class CheatEntry(
        val id: Int,
        @StringRes val labelResId: Int,
        val getter: (CheatConfig) -> Boolean,
        val setter: (CheatConfig, Boolean) -> CheatConfig
    )

    private val cheatEntries = listOf(
        CheatEntry(CHEAT_ENABLED, R.string.cheats_enabled_label,
            getter = { it.enabled },
            setter = { c, v -> c.copy(enabled = v) }),
        CheatEntry(CHEAT_GOD_MODE, R.string.cheat_god_mode_label,
            getter = { it.godMode },
            setter = { c, v -> c.copy(godMode = v) }),
        CheatEntry(CHEAT_NON_LETHAL_PLAYER_DAMAGE, R.string.cheat_non_lethal_player_damage_label,
            getter = { it.nonLethalPlayerDamage },
            setter = { c, v -> c.copy(nonLethalPlayerDamage = v) }),
        CheatEntry(CHEAT_FULL_MEDICAL_HEALING, R.string.cheat_full_medical_healing_label,
            getter = { it.fullMedicalHealing },
            setter = { c, v -> c.copy(fullMedicalHealing = v) }),
        CheatEntry(CHEAT_UNLIMITED_AMMO, R.string.cheat_unlimited_ammo_label,
            getter = { it.unlimitedAmmo },
            setter = { c, v -> c.copy(unlimitedAmmo = v) }),
        CheatEntry(CHEAT_NO_WEAPON_JAM, R.string.cheat_no_weapon_jam_label,
            getter = { it.noWeaponJam },
            setter = { c, v -> c.copy(noWeaponJam = v) }),
        CheatEntry(CHEAT_UNLIMITED_AP, R.string.cheat_unlimited_ap_label,
            getter = { it.unlimitedAP },
            setter = { c, v -> c.copy(unlimitedAP = v) }),
        CheatEntry(CHEAT_UNLIMITED_BREATH, R.string.cheat_unlimited_breath_label,
            getter = { it.unlimitedBreath },
            setter = { c, v -> c.copy(unlimitedBreath = v) }),
        CheatEntry(CHEAT_REVEAL_ENEMIES, R.string.cheat_reveal_enemies_label,
            getter = { it.revealEnemies },
            setter = { c, v -> c.copy(revealEnemies = v) }),
        CheatEntry(CHEAT_REVEAL_ITEMS, R.string.cheat_reveal_items_label,
            getter = { it.revealItems },
            setter = { c, v -> c.copy(revealItems = v) }),
        CheatEntry(CHEAT_ONE_HIT_KILL, R.string.cheat_one_hit_kill_label,
            getter = { it.oneHitKill },
            setter = { c, v -> c.copy(oneHitKill = v) }),
        CheatEntry(CHEAT_PERFECT_HIT_CHANCE, R.string.cheat_perfect_hit_chance_label,
            getter = { it.perfectHitChance },
            setter = { c, v -> c.copy(perfectHitChance = v) })
    )

    private var currentConfig: CheatConfig = CheatConfig.DEFAULT
    private val actionButtons = mutableListOf<Button>()

    fun show() {
        loadCurrentConfig()

        val scrollView = ScrollView(context)
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22.dp(), 18.dp(), 22.dp(), 16.dp())
            background = Ja2GuiStyle.panelBackground(context)
        }

        layout.addView(title(R.string.cheat_overlay_title))
        layout.addView(section(R.string.cheat_section_toggles))
        for (entry in cheatEntries) {
            layout.addView(cheatCheckBox(entry))
        }

        layout.addView(section(R.string.cheat_section_actions))
        layout.addView(actionRow(
            actionButton(R.string.cheat_action_heal_team) { runAction(CHEAT_ACTION_HEAL_TEAM) },
            actionButton(R.string.cheat_action_reload_team) { runAction(CHEAT_ACTION_RELOAD_TEAM) }
        ))
        layout.addView(actionRow(
            actionButton(R.string.cheat_action_reload_selected) { runAction(CHEAT_ACTION_RELOAD_SELECTED) },
            actionButton(R.string.cheat_action_grant_money, DEFAULT_MONEY_GRANT) {
                runAction(CHEAT_ACTION_GRANT_MONEY, DEFAULT_MONEY_GRANT)
            }
        ))
        updateActionButtonsEnabled()

        scrollView.addView(layout)

        val dialog = AlertDialog.Builder(context)
            .setView(scrollView)
            .create()
        layout.addView(Ja2GuiStyle.styledButton(
            context,
            context.getString(R.string.touch_close),
            fillColor = 0xAA1D2A36.toInt(),
            strokeColor = Ja2GuiStyle.STROKE
        ) { dialog.dismiss() }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 40.dp()).apply {
            topMargin = 14.dp()
        })
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
        dialog.show()
    }

    private fun cheatCheckBox(entry: CheatEntry): CheckBox =
        CheckBox(context).apply {
            text = context.getString(entry.labelResId)
            textSize = 14f
            setTextColor(0xFFE7EEF6.toInt())
            buttonTintList = android.content.res.ColorStateList.valueOf(0xFF6FA8DC.toInt())
            isChecked = entry.getter(currentConfig)
            setPadding(0, 2.dp(), 0, 2.dp())
            setOnCheckedChangeListener { _, isChecked ->
                onCheatToggled(entry, isChecked)
            }
        }

    private fun title(@StringRes textResId: Int): TextView =
        TextView(context).apply {
            text = context.getString(textResId)
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 0, 0, 8.dp())
        }

    private fun section(@StringRes textResId: Int): TextView =
        TextView(context).apply {
            text = context.getString(textResId)
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(0xFFB5C0CC.toInt())
            setPadding(0, 12.dp(), 0, 4.dp())
        }

    private fun actionRow(vararg buttons: Button): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            for ((index, button) in buttons.withIndex()) {
                addView(button, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    if (index > 0) leftMargin = 8.dp()
                })
            }
        }

    private fun actionButton(@StringRes labelResId: Int, formatArg: Int = 0, onClick: () -> Unit): Button =
        Ja2GuiStyle.styledButton(
            context,
            if (formatArg > 0) context.getString(labelResId, formatArg) else context.getString(labelResId),
            textColor = 0xFFFFFFFF.toInt(),
            fillColor = 0xAA1D2A36.toInt(),
            strokeColor = Ja2GuiStyle.STROKE,
            minHeightDp = 40,
            textSizeSp = 12f,
            onClick = onClick
        ).apply {
            actionButtons.add(this)
        }

    private fun runAction(actionId: Int, amount: Int = 0) {
        if (!currentConfig.enabled) return
        SDLActivity.runCheatAction(actionId, amount)
    }

    private fun loadCurrentConfig() {
        currentConfig = try {
            val text = File(cheatsJsonPath).readText()
            jsonFormat.decodeFromString<CheatConfig>(text)
        } catch (e: SerializationException) {
            CheatConfig.DEFAULT
        } catch (e: IOException) {
            CheatConfig.DEFAULT
        }
    }

    private fun onCheatToggled(entry: CheatEntry, checked: Boolean) {
        currentConfig = entry.setter(currentConfig, checked)
        SDLActivity.setCheat(entry.id, checked)
        saveToFile()
        updateActionButtonsEnabled()
    }

    private fun updateActionButtonsEnabled() {
        actionButtons.forEach { button ->
            button.isEnabled = currentConfig.enabled
            button.alpha = if (currentConfig.enabled) 1.0f else 0.45f
        }
    }

    private fun saveToFile() {
        try {
            val parentDir = File(cheatsJsonPath).parentFile
            if (parentDir?.exists() != true) {
                parentDir?.mkdirs()
            }
            File(cheatsJsonPath).writeText(jsonFormat.encodeToString(currentConfig))
        } catch (e: IOException) {
            android.util.Log.w("CheatOverlayDialog", "Failed to save cheats.json: ${e.message}")
        }
    }

    private fun Int.dp(): Int =
        (this * context.resources.displayMetrics.density).toInt()
}
