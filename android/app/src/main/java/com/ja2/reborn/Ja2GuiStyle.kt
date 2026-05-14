package com.ja2.reborn

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.Gravity
import android.widget.Button

object Ja2GuiStyle {
    const val PANEL = 0xEE111820.toInt()
    const val STROKE = 0x667D8DA0
    const val ACCENT = 0xFFFFC17A.toInt()
    const val TEXT = 0xFFE7EEF6.toInt()
    const val MUTED = 0xFFB5C0CC.toInt()
    const val SUCCESS_TEXT = 0xFFB9F6C2.toInt()
    const val SUCCESS_FILL = 0xFF245B32.toInt()
    const val WARNING_FILL = 0xFF3B2C18.toInt()
    const val DANGER = 0xFFE53935.toInt()

    fun panelBackground(context: Context): Drawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = context.dp(10).toFloat()
            setColor(PANEL)
            setStroke(context.dp(1), STROKE)
        }

    fun buttonBackground(
        context: Context,
        fillColor: Int,
        strokeColor: Int,
        pressedFillColor: Int = lighten(fillColor, 0x22),
        disabledFillColor: Int = 0x66111820
    ): Drawable =
        StateListDrawable().apply {
            addState(intArrayOf(-android.R.attr.state_enabled), rounded(context, disabledFillColor, 0x337D8DA0))
            addState(intArrayOf(android.R.attr.state_pressed), rounded(context, pressedFillColor, strokeColor))
            addState(intArrayOf(android.R.attr.state_focused), rounded(context, pressedFillColor, strokeColor))
            addState(intArrayOf(), rounded(context, fillColor, strokeColor))
        }

    fun styledButton(
        context: Context,
        text: String,
        textColor: Int = TEXT,
        fillColor: Int = 0xAA1D2A36.toInt(),
        strokeColor: Int = STROKE,
        minHeightDp: Int = 40,
        textSizeSp: Float = 12f,
        onClick: () -> Unit
    ): Button =
        Button(context).apply {
            this.text = text
            textSize = textSizeSp
            setTextColor(textColor)
            gravity = Gravity.CENTER
            background = buttonBackground(context, fillColor, strokeColor)
            minHeight = context.dp(minHeightDp)
            minWidth = 0
            isAllCaps = false
            includeFontPadding = false
            setTypeface(typeface, Typeface.BOLD)
            setPadding(context.dp(4), context.dp(4), context.dp(4), context.dp(4))
            setOnClickListener { onClick() }
        }

    private fun rounded(context: Context, fillColor: Int, strokeColor: Int): Drawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = context.dp(7).toFloat()
            setColor(fillColor)
            setStroke(context.dp(1), strokeColor)
        }

    private fun lighten(color: Int, amount: Int): Int {
        val a = color and -0x1000000
        val r = ((color shr 16) and 0xFF).plus(amount).coerceAtMost(0xFF)
        val g = ((color shr 8) and 0xFF).plus(amount).coerceAtMost(0xFF)
        val b = (color and 0xFF).plus(amount).coerceAtMost(0xFF)
        return a or (r shl 16) or (g shl 8) or b
    }

    fun Context.dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
