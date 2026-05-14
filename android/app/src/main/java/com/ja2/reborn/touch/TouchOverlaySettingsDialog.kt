package com.ja2.reborn.touch

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.CompoundButton
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.annotation.StringRes
import com.ja2.reborn.Ja2GuiStyle
import com.ja2.reborn.R
import org.libsdl.app.SDLActivity
import org.libsdl.app.SDLSurface

class TouchOverlaySettingsDialog(
    private val context: Context,
    private val onResetAll: () -> Unit,
    private val onDeleteAll: () -> Unit = {},
    private val onExportPreset: () -> Unit = {},
    private val onImportPreset: () -> Unit = {},
    private val autoHideEnabled: Boolean = true,
    private val onAutoHideToggled: (Boolean) -> Unit = {},
    private val onScrollSpeedChanged: (Int) -> Unit = {},
    private val onMouseSpeedChanged: (Float) -> Unit = {},
    private val mapFovPercent: Int = 100,
    private val onMapFovChanged: (Int) -> Unit = {},
    private val panelScalePercent: Int = 100,
    private val onPanelScaleChanged: (Int) -> Unit = {}
) {
    // Inverted: left=slow (80ms), right=fast (5ms)
    private val scrollSpeedValues = intArrayOf(80, 60, 45, 35, 27, 20, 15, 10, 5)
    private val mouseSpeedValues = floatArrayOf(0.50f, 0.65f, 0.80f, 1.00f, 1.20f, 1.45f, 1.75f, 2.10f, 2.50f)
    private val panelScaleValues = intArrayOf(100, 110, 120, 130)

    fun show() {
        val scrollView = ScrollView(context).apply {
            isFillViewport = false
        }
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22.dp(), 18.dp(), 22.dp(), 16.dp())
            background = Ja2GuiStyle.panelBackground(context)
        }

        val currentMs = try {
            SDLActivity.getScrollSpeed()
        } catch (e: Exception) {
            27
        }
        val scrollIndex = findClosestScrollIndex(currentMs)
        val scrollSeekBar = SeekBar(context).apply {
            max = scrollSpeedValues.size - 1
            progress = scrollIndex
            thumbTintList = ColorStateList.valueOf(ACCENT)
            progressTintList = ColorStateList.valueOf(ACCENT)
            progressBackgroundTintList = ColorStateList.valueOf(0x667D8DA0)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val ms = scrollSpeedValues[progress.coerceIn(0, scrollSpeedValues.size - 1)]
                        SDLActivity.setScrollSpeed(ms)
                        onScrollSpeedChanged(ms)
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
        }

        val currentMouseSpeed = try {
            SDLSurface.getTouchpadMouseSpeed()
        } catch (e: Exception) {
            1.0f
        }
        val mouseIndex = findClosestMouseIndex(currentMouseSpeed)
        val mouseSeekBar = SeekBar(context).apply {
            max = mouseSpeedValues.size - 1
            progress = mouseIndex
            thumbTintList = ColorStateList.valueOf(ACCENT)
            progressTintList = ColorStateList.valueOf(ACCENT)
            progressBackgroundTintList = ColorStateList.valueOf(0x667D8DA0)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val speed = mouseSpeedValues[progress.coerceIn(0, mouseSpeedValues.size - 1)]
                        SDLSurface.setTouchpadMouseSpeed(speed)
                        onMouseSpeedChanged(speed)
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
        }

        layout.addView(title(R.string.touch_settings_title))
        layout.addView(section(R.string.touch_section_scroll_speed))
        layout.addView(sliderWithLabels(scrollSeekBar))
        layout.addView(section(R.string.touch_section_mouse_speed))
        layout.addView(sliderWithLabels(mouseSeekBar))

        val panelScaleIndex = findClosestIndex(panelScaleValues, panelScalePercent)
        val panelSeekBar = SeekBar(context).apply {
            max = panelScaleValues.size - 1
            progress = panelScaleIndex
            thumbTintList = ColorStateList.valueOf(ACCENT)
            progressTintList = ColorStateList.valueOf(ACCENT)
            progressBackgroundTintList = ColorStateList.valueOf(0x667D8DA0)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val v = panelScaleValues[progress.coerceIn(0, panelScaleValues.size - 1)]
                        SDLActivity.setTacticalActionPanelScalePercent(v)
                        onPanelScaleChanged(v)
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
        }

        layout.addView(section(R.string.touch_section_panel_scale))
        layout.addView(sliderWithDirectionalLabels(panelSeekBar,
            R.string.touch_panel_scale_normal, R.string.touch_panel_scale_larger))

        layout.addView(section(R.string.touch_section_auto_hide))
        layout.addView(autoHideSwitch())

        layout.addView(section(R.string.touch_section_presets))
        layout.addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 2.dp(), 0, 0)
            addView(presetButton(R.string.touch_preset_export, 0xFFB9F6C2.toInt(), 0xFF245B32.toInt()) { onExportPreset() },
                LinearLayout.LayoutParams(0, 42.dp(), 1f).apply { rightMargin = 6.dp() })
            addView(presetButton(R.string.touch_preset_import, 0xFFFFC17A.toInt(), 0xFF3B2C18.toInt()) { onImportPreset() },
                LinearLayout.LayoutParams(0, 42.dp(), 1f).apply { rightMargin = 6.dp() })
            addView(dangerButton(R.string.touch_reset_layout) { showResetConfirmation() },
                LinearLayout.LayoutParams(0, 42.dp(), 1f))
        })

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

    private fun showResetConfirmation() {
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22.dp(), 18.dp(), 22.dp(), 16.dp())
            background = Ja2GuiStyle.panelBackground(context)
            addView(title(R.string.touch_reset_confirm_title))
            addView(TextView(context).apply {
                setText(R.string.touch_reset_confirm_choose)
                textSize = 13f
                setTextColor(Ja2GuiStyle.MUTED)
                setPadding(0, 2.dp(), 0, 12.dp())
            })
        }

        val dialog = AlertDialog.Builder(context).setView(content).create()
        content.addView(Ja2GuiStyle.styledButton(
            context,
            context.getString(R.string.touch_reset_restore_default),
            textColor = Ja2GuiStyle.SUCCESS_TEXT,
            fillColor = Ja2GuiStyle.SUCCESS_FILL,
            strokeColor = Ja2GuiStyle.SUCCESS_TEXT
        ) {
            dialog.dismiss()
            onResetAll()
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 42.dp()))
        content.addView(Ja2GuiStyle.styledButton(
            context,
            context.getString(R.string.touch_reset_delete_all),
            textColor = 0xFFFFB0A8.toInt(),
            fillColor = 0xFF3B1818.toInt(),
            strokeColor = Ja2GuiStyle.DANGER
        ) {
            dialog.dismiss()
            onDeleteAll()
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 42.dp()).apply { topMargin = 8.dp() })
        content.addView(Ja2GuiStyle.styledButton(
            context,
            context.getString(R.string.touch_reset_confirm_negative),
            textColor = Ja2GuiStyle.TEXT,
            fillColor = 0xAA1D2A36.toInt(),
            strokeColor = Ja2GuiStyle.STROKE
        ) {
            dialog.dismiss()
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 40.dp()).apply { topMargin = 8.dp() })

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
        dialog.show()
    }

    private fun findClosestScrollIndex(target: Int): Int {
        return findClosestIndex(scrollSpeedValues, target)
    }

    private fun findClosestMouseIndex(target: Float): Int {
        var best = 3
        var bestDist = Float.MAX_VALUE
        for (i in mouseSpeedValues.indices) {
            val dist = kotlin.math.abs(mouseSpeedValues[i] - target)
            if (dist < bestDist) {
                bestDist = dist
                best = i
            }
        }
        return best
    }

    private fun findClosestIndex(values: IntArray, target: Int): Int {
        var best = 0
        var bestDist = Int.MAX_VALUE
        for (i in values.indices) {
            val dist = kotlin.math.abs(values[i] - target)
            if (dist < bestDist) {
                bestDist = dist
                best = i
            }
        }
        return best
    }

    private fun title(@StringRes resId: Int): TextView =
        TextView(context).apply {
            setText(resId)
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 0, 0, 8.dp())
        }

    private fun section(@StringRes resId: Int): TextView =
        TextView(context).apply {
            setText(resId)
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(0xFFB5C0CC.toInt())
            setPadding(0, 12.dp(), 0, 4.dp())
        }

    private fun sliderWithLabels(seekBar: SeekBar): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 4.dp(), 0, 4.dp())
            addView(seekBar, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ))
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(speedLabel(R.string.touch_speed_slow, Gravity.START), LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
                ))
                addView(speedLabel(R.string.touch_speed_fast, Gravity.END), LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
                ))
            })
        }

    private fun sliderWithDirectionalLabels(seekBar: SeekBar, @StringRes leftLabel: Int, @StringRes rightLabel: Int): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 4.dp(), 0, 4.dp())
            addView(seekBar, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ))
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(speedLabel(leftLabel, Gravity.START), LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
                ))
                addView(speedLabel(rightLabel, Gravity.END), LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
                ))
            })
        }

    private fun speedLabel(@StringRes resId: Int, labelGravity: Int): TextView =
        TextView(context).apply {
            setText(resId)
            gravity = labelGravity
            textSize = 10f
            setTextColor(0x8899A8B0.toInt())
        }

    private fun dangerButton(@StringRes resId: Int, onClick: () -> Unit): Button =
        Ja2GuiStyle.styledButton(
            context,
            context.getString(resId),
            textColor = 0xFFFFB0A8.toInt(),
            fillColor = 0xFF3B1818.toInt(),
            strokeColor = Ja2GuiStyle.DANGER,
            minHeightDp = 36,
            textSizeSp = 10f,
            onClick = onClick
        )

    private fun presetButton(@StringRes resId: Int, textColor: Int, fillColor: Int, onClick: () -> Unit): Button =
        Ja2GuiStyle.styledButton(
            context,
            context.getString(resId),
            textColor = textColor,
            fillColor = fillColor,
            strokeColor = textColor,
            minHeightDp = 36,
            textSizeSp = 10f,
            onClick = onClick
        )

    private fun autoHideSwitch(): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 2.dp(), 0, 0)
            addView(TextView(context).apply {
                setText(R.string.touch_auto_hide_label)
                textSize = 12f
                setTextColor(0xFFB5C0CC.toInt())
                setPadding(0, 0, 10.dp(), 0)
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(Switch(context).apply {
                isChecked = autoHideEnabled
                setOnCheckedChangeListener { _: CompoundButton?, checked: Boolean ->
                    onAutoHideToggled(checked)
                }
            })
        }

    private fun Int.dp(): Int =
        (this * context.resources.displayMetrics.density).toInt()

    companion object {
        private const val ACCENT = 0xFFFFC17A.toInt()
    }
}
