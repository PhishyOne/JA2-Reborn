package com.ja2.reborn.touch

import android.app.AlertDialog
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import com.ja2.reborn.R

class TouchOverlayEditDialog(
    private val context: Context,
    private val buttonConfig: TouchButtonConfig,
    private val onSave: (TouchButtonConfig) -> Unit,
    private val onDelete: (String) -> Unit
) {
    private val presetOptions = TOUCH_BUTTON_PRESETS
    private val presetEntries = buildPresetEntries(presetOptions)
    private fun shapeOptions() = listOf(
        Option(BUTTON_SHAPE_CIRCLE, context.getString(R.string.touch_shape_circle)),
        Option(BUTTON_SHAPE_SQUARE, context.getString(R.string.touch_shape_square)),
        Option(BUTTON_SHAPE_RECTANGLE, context.getString(R.string.touch_shape_rectangle))
    )
    private val sizeValues = floatArrayOf(
        0.030f, 0.035f, 0.040f, 0.045f, 0.055f, 0.065f, 0.075f, 0.090f, 0.105f, 0.120f, 0.140f, 0.160f, 0.180f,
        0.220f, 0.260f, 0.300f, 0.350f, 0.400f, 0.450f
    )
    private val alphaValues = floatArrayOf(
        0.15f, 0.25f, 0.35f, 0.45f, 0.55f, 0.65f, 0.75f, 0.85f, 1.00f
    )

    fun show() {
        val selectedPreset = touchButtonPresetFor(buttonConfig) ?: presetOptions.first()

        val scrollView = ScrollView(context).apply {
            isFillViewport = false
        }
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22.dp(), 18.dp(), 22.dp(), 16.dp())
            background = panelBackground()
        }

        val shapes = shapeOptions()
        val presetSpinner = presetSpinner(selectedPreset)
        val shapeSpinner = spinner(
            shapes.map { it.label },
            selectedIndex(shapes, buttonConfig.shape)
        )

        val sizeLabel = valueText()
        val sizeSeekBar = SeekBar(context).apply {
            max = sizeValues.size - 1
            progress = findClosestIndex(sizeValues, buttonConfig.size)
            thumbTintList = tint(ACCENT)
            progressTintList = tint(ACCENT)
            progressBackgroundTintList = tint(SURFACE_STROKE)
            setOnSeekBarChangeListener(snapSeekBarListener(sizeValues, sizeLabel, sizeFormat))
        }

        val alphaLabel = valueText()
        val alphaSeekBar = SeekBar(context).apply {
            max = alphaValues.size - 1
            progress = findClosestIndex(alphaValues, buttonConfig.alpha)
            thumbTintList = tint(ACCENT)
            progressTintList = tint(ACCENT)
            progressBackgroundTintList = tint(SURFACE_STROKE)
            setOnSeekBarChangeListener(snapSeekBarListener(alphaValues, alphaLabel, alphaFormat))
        }

        val presetSummary = summaryText(selectedPreset)
        presetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                presetEntries[pos.coerceIn(0, presetEntries.size - 1)].preset?.let { preset ->
                    presetSummary.text = TouchButtonLocalization.getCategoryLabel(context, preset.category)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        container.addView(title(context.getString(R.string.touch_edit_title)))
        container.addView(labeledField(context.getString(R.string.touch_edit_field_button), presetSpinner))
        container.addView(presetSummary)
        container.addView(labeledField(context.getString(R.string.touch_edit_field_size), sliderRow(sizeSeekBar, sizeLabel)))
        container.addView(labeledField(context.getString(R.string.touch_edit_field_shape), shapeSpinner))
        container.addView(labeledField(context.getString(R.string.touch_edit_field_opacity), sliderRow(alphaSeekBar, alphaLabel)))

        scrollView.addView(container)

        sizeLabel.text = sizeFormat(sizeValues[sizeSeekBar.progress])
        alphaLabel.text = alphaFormat(alphaValues[alphaSeekBar.progress])

        val dialog = AlertDialog.Builder(context)
            .setView(scrollView)
            .setPositiveButton(context.getString(R.string.touch_edit_save)) { _, _ ->
                val shapes = shapeOptions()
                val preset = presetEntries[presetSpinner.selectedItemPosition.coerceIn(0, presetEntries.size - 1)].preset
                    ?: selectedPreset
                val updated = preset.applyTo(buttonConfig).copy(
                    shape = shapes[shapeSpinner.selectedItemPosition.coerceIn(0, shapes.size - 1)].value
                        ?: BUTTON_SHAPE_CIRCLE,
                    size = sizeValues[sizeSeekBar.progress],
                    alpha = alphaValues[alphaSeekBar.progress]
                )
                onSave(updated)
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .setNeutralButton(context.getString(R.string.touch_edit_delete)) { _, _ -> onDelete(buttonConfig.id) }
            .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                isAllCaps = false
                setTextColor(ACCENT)
            }
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
                isAllCaps = false
                setTextColor(TEXT_MUTED)
            }
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.apply {
                isAllCaps = false
                setTextColor(0xFFFF8A80.toInt())
            }
        }
        dialog.show()
    }

    private fun presetSpinner(selectedPreset: TouchButtonPreset): Spinner =
        groupedPresetSpinner(
            presetEntries.indexOfFirst { it.preset?.id == selectedPreset.id }.takeIf { it >= 0 } ?: 1
        )

    private fun title(text: String): TextView =
        TextView(context).apply {
            this.text = text
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(TEXT)
            setPadding(0, 0, 0, 12.dp())
        }

    private fun summaryText(preset: TouchButtonPreset): TextView =
        TextView(context).apply {
            text = TouchButtonLocalization.getCategoryLabel(context, preset.category)
            textSize = 12f
            setTextColor(TEXT_MUTED)
            setPadding(0, 0, 0, 8.dp())
        }

    private fun labeledField(label: String, child: View): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 4.dp(), 0, 10.dp())
            addView(TextView(context).apply {
                text = label
                textSize = 12f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(TEXT_MUTED)
                setPadding(0, 0, 0, 5.dp())
            })
            addView(child)
        }

    private fun sliderRow(seekBar: SeekBar, valueLabel: TextView): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(seekBar, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(valueLabel, LinearLayout.LayoutParams(64.dp(), ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                leftMargin = 12.dp()
            })
        }

    private fun valueText(): TextView = TextView(context).apply {
        gravity = Gravity.END
        textSize = 12f
        setTextColor(TEXT)
    }

    private fun spinner(labels: List<String>, selected: Int): Spinner =
        Spinner(context).apply {
            adapter = object : ArrayAdapter<String>(
                context,
                android.R.layout.simple_spinner_item,
                labels
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    return (super.getView(position, convertView, parent) as TextView).apply {
                        setTextColor(TEXT)
                        textSize = 14f
                        setPadding(12.dp(), 8.dp(), 12.dp(), 8.dp())
                    }
                }

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    return (super.getDropDownView(position, convertView, parent) as TextView).apply {
                        setTextColor(TEXT)
                        textSize = 14f
                        setPadding(14.dp(), 10.dp(), 14.dp(), 10.dp())
                        setBackgroundColor(0xFF111820.toInt())
                    }
                }
            }
            setSelection(selected.coerceIn(0, labels.size - 1))
            background = fieldBackground()
            setPopupBackgroundDrawable(GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(0xFF111820.toInt())
                setStroke(1.dp(), SURFACE_STROKE)
            })
        }

    private fun groupedPresetSpinner(selected: Int): Spinner =
        Spinner(context).apply {
            adapter = object : ArrayAdapter<PresetEntry>(
                context,
                android.R.layout.simple_spinner_item,
                presetEntries
            ) {
                override fun areAllItemsEnabled(): Boolean = false
                override fun isEnabled(position: Int): Boolean =
                    getItem(position)?.preset != null

                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val entry = getItem(position) ?: presetEntries.first()
                    return (super.getView(position, convertView, parent) as TextView).apply {
                        text = entry.label
                        setTextColor(TEXT)
                        textSize = 14f
                        setPadding(12.dp(), 8.dp(), 12.dp(), 8.dp())
                    }
                }

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val entry = getItem(position) ?: presetEntries.first()
                    return (super.getDropDownView(position, convertView, parent) as TextView).apply {
                        text = entry.label
                        textSize = if (entry.preset == null) 12f else 14f
                        setTypeface(typeface, if (entry.preset == null) Typeface.BOLD else Typeface.NORMAL)
                        setTextColor(if (entry.preset == null) ACCENT else TEXT)
                        setPadding(
                            if (entry.preset == null) 12.dp() else 22.dp(),
                            if (entry.preset == null) 10.dp() else 9.dp(),
                            14.dp(),
                            if (entry.preset == null) 6.dp() else 9.dp()
                        )
                        setBackgroundColor(0xFF111820.toInt())
                    }
                }
            }
            setSelection(selected.coerceIn(0, presetEntries.size - 1))
            background = fieldBackground()
            setPopupBackgroundDrawable(GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(0xFF111820.toInt())
                setStroke(1.dp(), SURFACE_STROKE)
            })
        }

    private fun snapSeekBarListener(
        values: FloatArray,
        label: TextView,
        format: (Float) -> String
    ): SeekBar.OnSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            label.text = format(values[progress.coerceIn(0, values.size - 1)])
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
        override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
    }

    private fun panelBackground(): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 10.dp().toFloat()
            setColor(0xEE111820.toInt())
            setStroke(1.dp(), SURFACE_STROKE)
        }

    private fun fieldBackground(): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 7.dp().toFloat()
            setColor(0xAA1D2A36.toInt())
            setStroke(1.dp(), SURFACE_STROKE)
        }

    private fun tint(color: Int): android.content.res.ColorStateList =
        android.content.res.ColorStateList.valueOf(color)

    private val sizeFormat: (Float) -> String = { v -> String.format("%.3f", v) }
    private val alphaFormat: (Float) -> String = { v -> "${(v * 100).toInt()}%" }

    private fun findClosestIndex(values: FloatArray, target: Float): Int {
        var best = 0
        var bestDist = Float.MAX_VALUE
        for (i in values.indices) {
            val dist = kotlin.math.abs(values[i] - target)
            if (dist < bestDist) {
                bestDist = dist
                best = i
            }
        }
        return best
    }

    private fun selectedIndex(options: List<Option>, value: String?): Int =
        options.indexOfFirst { it.value == value }.takeIf { it >= 0 } ?: 0

    private fun Int.dp(): Int =
        (this * context.resources.displayMetrics.density).toInt()

    private data class Option(val value: String?, val label: String)
    private data class PresetEntry(val label: String, val preset: TouchButtonPreset?)

    private fun buildPresetEntries(presets: List<TouchButtonPreset>): List<PresetEntry> {
        val result = mutableListOf<PresetEntry>()
        for ((category, categoryPresets) in presets.groupBy { it.category }) {
            result.add(PresetEntry(TouchButtonLocalization.getCategoryLabel(context, category).uppercase(), null))
            categoryPresets.forEach { preset ->
                result.add(PresetEntry(TouchButtonLocalization.getPresetLabel(context, preset.id), preset))
            }
        }
        return result
    }

    companion object {
        private const val TEXT = 0xFFFFFFFF.toInt()
        private const val TEXT_MUTED = 0xFFB5C0CC.toInt()
        private const val ACCENT = 0xFFFFC17A.toInt()
        private const val SURFACE_STROKE = 0x667D8DA0
    }
}
