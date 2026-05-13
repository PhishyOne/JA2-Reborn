package com.ja2.reborn.touch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

class TouchOverlaySettingsButtonView(context: Context) : View(context) {

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
    private val rect = RectF()

    override fun onDraw(canvas: Canvas) {
        rect.set(1f, 1f, width - 1f, height - 1f)
        canvas.drawOval(rect, backgroundPaint)
        canvas.drawOval(rect, borderPaint)

        val cx = width / 2f
        val cy = height / 2f
        val s = minOf(width, height) * 0.24f

        canvas.drawCircle(cx, cy, s * 0.62f, iconPaint)
        canvas.drawCircle(cx, cy, s * 0.23f, iconPaint)
        for (i in 0..7) {
            val a = i * Math.PI.toFloat() / 4f
            val x1 = cx + kotlin.math.cos(a) * s * 0.72f
            val y1 = cy + kotlin.math.sin(a) * s * 0.72f
            val x2 = cx + kotlin.math.cos(a) * s
            val y2 = cy + kotlin.math.sin(a) * s
            canvas.drawLine(x1, y1, x2, y2, iconPaint)
        }
    }
}
