package com.ja2.reborn.touch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

class TouchOverlayLockButtonView(context: Context) : View(context) {
    private var locked = true

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xAA111820.toInt()
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x80FFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * resources.displayMetrics.density
    }
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 3f * resources.displayMetrics.density
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
    }
    private val rect = RectF()

    fun setLocked(value: Boolean) {
        if (locked != value) {
            locked = value
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val minDim = minOf(width, height).toFloat()
        rect.set(1f, 1f, width - 1f, height - 1f)
        canvas.drawOval(rect, backgroundPaint)
        canvas.drawOval(rect, borderPaint)

        val cx = width / 2f
        val cy = height / 2f
        val s = minDim * 0.28f
        val body = RectF(cx - s * 0.82f, cy - s * 0.08f, cx + s * 0.82f, cy + s * 0.92f)
        canvas.drawRoundRect(body, s * 0.16f, s * 0.16f, iconPaint)
        canvas.drawCircle(cx, cy + s * 0.36f, s * 0.13f, fillPaint)
        canvas.drawLine(cx, cy + s * 0.46f, cx, cy + s * 0.68f, iconPaint)

        val shackle = RectF(cx - s * 0.55f, cy - s * 0.86f, cx + s * 0.55f, cy + s * 0.32f)
        if (locked) {
            canvas.drawArc(shackle, 205f, 130f, false, iconPaint)
            canvas.drawLine(cx - s * 0.55f, cy - s * 0.18f, cx - s * 0.55f, cy + s * 0.02f, iconPaint)
            canvas.drawLine(cx + s * 0.55f, cy - s * 0.18f, cx + s * 0.55f, cy + s * 0.02f, iconPaint)
        } else {
            canvas.drawArc(shackle, 205f, 112f, false, iconPaint)
            canvas.drawLine(cx - s * 0.55f, cy - s * 0.18f, cx - s * 0.55f, cy + s * 0.02f, iconPaint)
            canvas.drawLine(cx + s * 0.8f, cy - s * 0.08f, cx + s * 0.8f, cy + s * 0.1f, iconPaint)
        }
    }
}
