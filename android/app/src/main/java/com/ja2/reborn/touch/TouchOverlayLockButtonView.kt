package com.ja2.reborn.touch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

class TouchOverlayLockButtonView(context: Context) : View(context) {
    private var locked = true

    private val density = resources.displayMetrics.density
    private val cornerRadiusPx = 12f * density

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xAA111820.toInt()
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x66FFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 1f * density
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
    }

    fun setLocked(value: Boolean) {
        if (locked != value) {
            locked = value
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val bgRect = RectF(1f, 1f, width - 1f, height - 1f)
        canvas.drawRoundRect(bgRect, cornerRadiusPx, cornerRadiusPx, backgroundPaint)
        canvas.drawRoundRect(bgRect, cornerRadiusPx, cornerRadiusPx, borderPaint)

        val iconName = if (locked) "lock_closed" else "lock_open"
        val iconSize = minOf(width, height) * 0.70f
        val iconBounds = RectF(
            width / 2f - iconSize / 2f,
            height / 2f - iconSize / 2f,
            width / 2f + iconSize / 2f,
            height / 2f + iconSize / 2f
        )

        if (SvgIconManager.renderIcon(canvas, context, iconName, iconBounds, fillPaint, LOCK_ICON_FILL)) {
            return
        }

        // Canvas fallback
        val minDim = minOf(width, height).toFloat()
        val iconStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = minDim * 0.07f
        }
        val cx = width / 2f
        val cy = height / 2f
        val s = minDim * 0.28f
        val body = RectF(cx - s * 0.82f, cy - s * 0.08f, cx + s * 0.82f, cy + s * 0.92f)
        canvas.drawRoundRect(body, s * 0.16f, s * 0.16f, iconStroke)
        canvas.drawCircle(cx, cy + s * 0.36f, s * 0.13f, fillPaint)
        canvas.drawLine(cx, cy + s * 0.46f, cx, cy + s * 0.68f, iconStroke)

        val shackle = RectF(cx - s * 0.55f, cy - s * 0.86f, cx + s * 0.55f, cy + s * 0.32f)
        if (locked) {
            canvas.drawArc(shackle, 205f, 130f, false, iconStroke)
            canvas.drawLine(cx - s * 0.55f, cy - s * 0.18f, cx - s * 0.55f, cy + s * 0.02f, iconStroke)
            canvas.drawLine(cx + s * 0.55f, cy - s * 0.18f, cx + s * 0.55f, cy + s * 0.02f, iconStroke)
        } else {
            canvas.drawArc(shackle, 205f, 112f, false, iconStroke)
            canvas.drawLine(cx - s * 0.55f, cy - s * 0.18f, cx - s * 0.55f, cy + s * 0.02f, iconStroke)
            canvas.drawLine(cx + s * 0.8f, cy - s * 0.08f, cx + s * 0.8f, cy + s * 0.1f, iconStroke)
        }
    }

    companion object {
        private const val LOCK_ICON_FILL = 1.55f
    }
}
