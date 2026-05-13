package com.ja2.reborn.touch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View

class TouchOverlayGridView(context: Context) : View(context) {
    var gridSizePx: Int = 1
        set(value) {
            field = value.coerceAtLeast(1)
            invalidate()
        }

    private val minorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x26FFFFFF
        strokeWidth = 1f * resources.displayMetrics.density
    }

    private val majorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x44FFFFFF
        strokeWidth = 1.25f * resources.displayMetrics.density
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (gridSizePx <= 1) return

        var lineIndex = 0
        var x = 0f
        while (x <= width) {
            canvas.drawLine(x, 0f, x, height.toFloat(), paintFor(lineIndex))
            x += gridSizePx
            lineIndex++
        }

        lineIndex = 0
        var y = 0f
        while (y <= height) {
            canvas.drawLine(0f, y, width.toFloat(), y, paintFor(lineIndex))
            y += gridSizePx
            lineIndex++
        }
    }

    private fun paintFor(lineIndex: Int): Paint =
        if (lineIndex % MAJOR_LINE_INTERVAL == 0) majorPaint else minorPaint

    companion object {
        private const val MAJOR_LINE_INTERVAL = 4
    }
}
