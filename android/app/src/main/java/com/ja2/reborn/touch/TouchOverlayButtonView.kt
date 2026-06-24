package com.ja2.reborn.touch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout

class TouchOverlayButtonView(
    context: Context,
    initialConfig: TouchButtonConfig,
    private val dispatcher: TouchInputDispatcher,
    private val dragCallback: (String) -> Unit,
    private val longPressCallback: (TouchButtonConfig) -> Unit,
    private val specialActionCallback: (TouchButtonAction) -> Boolean = { false },
    draggable: Boolean = false
) : View(context) {

    private var buttonConfig: TouchButtonConfig = initialConfig
    internal val config: TouchButtonConfig get() = buttonConfig

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xAA111820.toInt()
        style = Paint.Style.FILL
    }

    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xDD2F5F86.toInt()
        style = Paint.Style.FILL
    }

    private val toggleActivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xDD3D7A45.toInt()
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x80FFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * resources.displayMetrics.density
    }

    private val toggleBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF5CBF60.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * resources.displayMetrics.density
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 3f * resources.displayMetrics.density
    }

    private val iconFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
    }

    private var activePointerId = -1
    private var isPressed = false
    private var isDragging = false
    private var isLongPress = false
    private var initialTouchTime = 0L
    private var downX = 0f
    private var downY = 0f
    private var viewStartLeft = 0
    private var viewStartTop = 0
    private var hasMovedPastThreshold = false
    private var isDraggable = draggable
    private var isHoldMode: Boolean = false
    private var isToggleMode: Boolean = false
    private var isToggleTapMode: Boolean = false
    private var isToggled: Boolean = false
    private var isDpad: Boolean = false
    private var currentDpadDirection: String? = null
    private var snapGridSizePx: Int = 0

    init {
        updateMode()
    }

    fun updateConfig(newConfig: TouchButtonConfig) {
        buttonConfig = newConfig
        updateMode()
        invalidate()
    }

    fun syncToggleTapState(active: Boolean) {
        if (!isToggleTapMode || activePointerId != -1 || isToggled == active) return
        isToggled = active
        setPressedState(false)
        invalidate()
    }

    private fun updateMode() {
        val action = buttonConfig.actions.firstOrNull() ?: TouchButtonAction(type = "", mode = "hold")
        isHoldMode = action.mode == "hold"
        isToggleMode = action.mode == "toggle"
        isToggleTapMode = action.mode == "toggle_tap"
        isDpad = buttonConfig.actions.any { it.type == "dpad" }
        if (!isToggleMode && !isToggleTapMode) {
            isToggled = false
        }
    }

    internal fun isPointInsideShape(localX: Float, localY: Float): Boolean {
        if (isDpad) return true
        val bounds = computeOuterShapeBounds()
        if (localX < bounds.left || localX > bounds.right || localY < bounds.top || localY > bounds.bottom) return false
        return when (buttonConfig.shape.lowercase()) {
            BUTTON_SHAPE_CIRCLE -> {
                val cx = bounds.centerX(); val cy = bounds.centerY()
                val radius = bounds.width() / 2f
                val dx = localX - cx; val dy = localY - cy
                dx * dx + dy * dy <= radius * radius
            }
            BUTTON_SHAPE_SQUARE -> isInsideRoundRect(bounds, localX, localY, cornerRadius(0.14f))
            BUTTON_SHAPE_RECTANGLE -> isInsideRoundRect(bounds, localX, localY, cornerRadius(0.18f))
            else -> true
        }
    }

    private fun isInsideRoundRect(bounds: RectF, x: Float, y: Float, cr: Float): Boolean {
        if (x >= bounds.left + cr && x <= bounds.right - cr) return true
        if (y >= bounds.top + cr && y <= bounds.bottom - cr) return true
        val cx = floatArrayOf(bounds.left + cr, bounds.right - cr, bounds.left + cr, bounds.right - cr)
        val cy = floatArrayOf(bounds.top + cr, bounds.top + cr, bounds.bottom - cr, bounds.bottom - cr)
        for (i in 0..3) {
            val dx = x - cx[i]; val dy = y - cy[i]
            if (dx * dx + dy * dy <= cr * cr) return true
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                if (activePointerId != -1) return false
                if (!isPointInsideShape(event.x, event.y)) return false
                activePointerId = event.getPointerId(0)
                setPressedState(true)
                initialTouchTime = System.currentTimeMillis()
                downX = event.rawX
                downY = event.rawY
                viewStartLeft = left
                viewStartTop = top
                hasMovedPastThreshold = false
                isDragging = false
                isLongPress = false

                if (canDispatchInput() && isDpad && isHoldMode) {
                    updateDpadDirection(event.x, event.y)
                } else if (canDispatchInput() && isHoldMode) {
                    dispatchActions(true)
                }
                return true
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (activePointerId != -1) return false
                val index = event.actionIndex
                if (!isPointInsideShape(event.getX(index), event.getY(index))) return false
                activePointerId = event.getPointerId(index)
                setPressedState(true)
                initialTouchTime = System.currentTimeMillis()
                downX = getRawXCompat(event, index)
                downY = getRawYCompat(event, index)
                viewStartLeft = left
                viewStartTop = top
                hasMovedPastThreshold = false
                isDragging = false
                isLongPress = false

                if (canDispatchInput() && isDpad && isHoldMode) {
                    updateDpadDirection(event.getX(index), event.getY(index))
                } else if (canDispatchInput() && isHoldMode) {
                    dispatchActions(true)
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (activePointerId == -1) return true

                val pointerIndex = event.findPointerIndex(activePointerId)
                if (pointerIndex < 0) return true

                val dx = getRawXCompat(event, pointerIndex) - downX
                val dy = getRawYCompat(event, pointerIndex) - downY
                val dist = Math.sqrt((dx * dx + dy * dy).toDouble())

                if (!hasMovedPastThreshold && dist > TOUCH_SLOP) {
                    hasMovedPastThreshold = true
                }

                if (!isDragging) {
                    val elapsed = System.currentTimeMillis() - initialTouchTime
                    if (elapsed > LONG_PRESS_MS) {
                        isLongPress = true
                    }
                    if (hasMovedPastThreshold && isDraggable) {
                        isDragging = true
                        if (canDispatchInput() && isHoldMode) {
                            dispatchActions(false)
                            setPressedState(false)
                        }
                    }
                }

                if (isDragging) {
                    moveWithinParent(viewStartLeft + dx.toInt(), viewStartTop + dy.toInt())
                    dragCallback(config.id)
                } else if (canDispatchInput() && isDpad && isHoldMode) {
                    updateDpadDirection(event.getX(pointerIndex), event.getY(pointerIndex))
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                val pointerIndex = event.findPointerIndex(activePointerId)
                if (pointerIndex < 0) return true
                activePointerId = -1

                if (isDragging) {
                    setPressedState(false)
                    notifyPositionChanged()
                } else if (isLongPress && !hasMovedPastThreshold && isDraggable) {
                    setPressedState(false)
                    longPressCallback(config)
                } else if (canDispatchInput() && isDpad && isHoldMode) {
                    releaseDpadDirection()
                    setPressedState(false)
                } else if (canDispatchInput() && isHoldMode) {
                    dispatchActions(false)
                    setPressedState(false)
                } else if (canDispatchInput() && isToggleMode) {
                    val nowActive = dispatcher.performToggle(buttonConfig.actions)
                    isToggled = nowActive
                    setPressedState(nowActive)
                } else if (canDispatchInput() && isToggleTapMode) {
                    dispatchTap()
                    isToggled = !isToggled
                    setPressedState(isToggled)
                } else if (canDispatchInput()) {
                    dispatchTap()
                    setPressedState(false)
                } else {
                    setPressedState(false)
                }
                isDragging = false
                isLongPress = false
                return true
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)
                if (pointerId == activePointerId) {
                    if (isDragging) {
                        setPressedState(false)
                        notifyPositionChanged()
                    } else if (isLongPress && !hasMovedPastThreshold && isDraggable) {
                        longPressCallback(config)
                    } else if (canDispatchInput() && isDpad && isHoldMode) {
                        releaseDpadDirection()
                    } else if (canDispatchInput() && isHoldMode) {
                        dispatchActions(false)
                    } else if (canDispatchInput() && isToggleMode) {
                        val nowActive = dispatcher.performToggle(buttonConfig.actions)
                        isToggled = nowActive
                    } else if (canDispatchInput() && isToggleTapMode) {
                        dispatchTap()
                        isToggled = !isToggled
                    }
                    setPressedState(false)
                    activePointerId = -1
                    isDragging = false
                    isLongPress = false
                }
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                if (activePointerId != -1) {
                    if (isDragging) {
                        notifyPositionChanged()
                    } else if (canDispatchInput() && isDpad && isHoldMode) {
                        releaseDpadDirection()
                    } else if (canDispatchInput() && isHoldMode) {
                        dispatchActions(false)
                    }
                }
                setPressedState(false)
                activePointerId = -1
                isDragging = false
                isLongPress = false
                return true
            }
        }
        return false
    }

    private fun notifyPositionChanged() {
        dragCallback(config.id)
    }

    fun getNormalizedPosition(parentWidth: Int, parentHeight: Int): Pair<Float, Float> {
        val lp = layoutParams as? FrameLayout.LayoutParams
        val currentLeft = lp?.leftMargin ?: left
        val currentTop = lp?.topMargin ?: top
        val xNorm = if (parentWidth > 0) currentLeft.toFloat() / parentWidth else config.x
        val yNorm = if (parentHeight > 0) currentTop.toFloat() / parentHeight else config.y
        return Pair(clampFloat(xNorm, 0f, 1f), clampFloat(yNorm, 0f, 1f))
    }

    fun releaseIfHeld() {
        if (canDispatchInput() && isToggleMode && isToggled) {
            buttonConfig.actions.forEach { dispatcher.forceReleaseToggle(it) }
            isToggled = false
            setPressedState(false)
            activePointerId = -1
        } else if (canDispatchInput() && isPressed && isDpad && isHoldMode && !isDragging) {
            releaseDpadDirection()
            setPressedState(false)
            activePointerId = -1
        } else if (canDispatchInput() && isPressed && isHoldMode && !isDragging) {
            dispatchActions(false)
            setPressedState(false)
            activePointerId = -1
        }
    }

    fun updateAppearance(sizePx: Int, alpha: Float) {
        updateAppearance(sizePx, sizePx, alpha)
    }

    fun updateAppearance(widthPx: Int, heightPx: Int, alpha: Float) {
        layoutParams?.let {
            it.width = widthPx
            it.height = heightPx
        }
        this.alpha = if (!isPressed) alpha else (alpha * 1.6f).coerceAtMost(1.0f)
        invalidate()
    }

    fun setDraggable(enabled: Boolean) {
        isDraggable = enabled
    }

    fun setSnapGridSize(gridSizePx: Int) {
        snapGridSizePx = gridSizePx.coerceAtLeast(0)
    }

    private fun setPressedState(pressed: Boolean) {
        if (isPressed != pressed) {
            isPressed = pressed
            alpha = if (pressed) {
                (buttonConfig.alpha * 1.6f).coerceAtMost(1.0f)
            } else {
                buttonConfig.alpha
            }
            invalidate()
        }
    }

    private fun dispatchActions(pressed: Boolean) {
        for (action in buttonConfig.actions) {
            if (!specialActionCallback(action)) {
                dispatcher.performAction(action, pressed)
            }
        }
    }

    private fun dispatchTap() {
        for (action in buttonConfig.actions) {
            if (!specialActionCallback(action)) {
                dispatcher.performAction(action, true)
                dispatcher.performAction(action, false)
            }
        }
    }

    private fun canDispatchInput(): Boolean = !isDraggable

    private fun updateDpadDirection(x: Float, y: Float) {
        val direction = dpadDirection(x, y) ?: return
        if (direction != currentDpadDirection) {
            dispatcher.performDpadDirection(direction)
            currentDpadDirection = direction
        }
    }

    private fun releaseDpadDirection() {
        dispatcher.performDpadDirection(null)
        currentDpadDirection = null
    }

    private fun dpadDirection(x: Float, y: Float): String? {
        val cx = width / 2f
        val cy = height / 2f
        val dx = x - cx
        val dy = y - cy
        val deadZone = minOf(width, height) * 0.15f
        if (kotlin.math.abs(dx) < deadZone && kotlin.math.abs(dy) < deadZone) return null

        return if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
            if (dx > 0) "RIGHT" else "LEFT"
        } else {
            if (dy > 0) "DOWN" else "UP"
        }
    }

    private fun moveWithinParent(requestedLeft: Int, requestedTop: Int) {
        val parentView = parent as? View
        val parentWidth = parentView?.width ?: 0
        val parentHeight = parentView?.height ?: 0
        val snappedLeft = snapToGrid(requestedLeft)
        val snappedTop = snapToGrid(requestedTop)

        val clampedLeft = if (parentWidth > 0) {
            snappedLeft.coerceIn(0, (parentWidth - width).coerceAtLeast(0))
        } else {
            snappedLeft
        }
        val clampedTop = if (parentHeight > 0) {
            snappedTop.coerceIn(0, (parentHeight - height).coerceAtLeast(0))
        } else {
            snappedTop
        }

        val lp = layoutParams as? FrameLayout.LayoutParams
        if (lp != null) {
            lp.leftMargin = clampedLeft
            lp.topMargin = clampedTop
            layoutParams = lp
        } else {
            layout(clampedLeft, clampedTop, clampedLeft + width, clampedTop + height)
        }
    }

    private fun snapToGrid(value: Int): Int {
        if (!isDraggable || snapGridSizePx <= 1) return value
        return ((value + snapGridSizePx / 2) / snapGridSizePx) * snapGridSizePx
    }

    override fun onDraw(canvas: Canvas) {
        val (fillPaint, strokePaint) = when {
            isToggled -> Pair(toggleActivePaint, toggleBorderPaint)
            isPressed -> Pair(activePaint, borderPaint)
            else -> Pair(backgroundPaint, borderPaint)
        }
        val isDpad = buttonConfig.icon == "dpad_map"
        if (!isDpad) {
            drawShape(canvas, fillPaint)
            drawShape(canvas, strokePaint)
        }

        val icon = buttonConfig.icon
        if (icon != null) {
            drawIcon(canvas, icon)
        } else {
            drawCenteredText(canvas, buttonConfig.label.ifEmpty { actionDisplayName() })
        }
    }

    private fun actionDisplayName(): String =
        TouchButtonLocalization.getActionTypeDisplayName(context, buttonConfig.actions.firstOrNull() ?: TouchButtonAction(type = "", mode = "tap"))

    private fun drawShape(canvas: Canvas, paint: Paint) {
        val bounds = computeOuterShapeBounds()
        when (buttonConfig.shape.lowercase()) {
            BUTTON_SHAPE_SQUARE -> canvas.drawRoundRect(bounds, cornerRadius(0.14f), cornerRadius(0.14f), paint)
            BUTTON_SHAPE_RECTANGLE -> canvas.drawRoundRect(bounds, cornerRadius(0.18f), cornerRadius(0.18f), paint)
            else -> canvas.drawOval(bounds, paint)
        }
    }

    private fun cornerRadius(factor: Float): Float = minOf(width, height) * factor

    private fun drawCenteredText(canvas: Canvas, text: String) {
        val outerBounds = computeOuterShapeBounds()
        val shapeDim = minOf(outerBounds.width(), outerBounds.height())
        val maxWidth = shapeDim * 0.78f
        val maxHeight = shapeDim * 0.52f
        textPaint.textSize = shapeDim * 0.42f
        while (textPaint.textSize > 8f * resources.displayMetrics.density &&
            (textPaint.measureText(text) > maxWidth || textPaint.fontSpacing > maxHeight)) {
            textPaint.textSize -= resources.displayMetrics.density
        }
        val textY = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(text, width / 2f, textY, textPaint)
    }

    private fun computeOuterShapeBounds(): RectF {
        val w = width.toFloat()
        val h = height.toFloat()
        val buttonHeight = minOf(h, w / 1.8f)
        return when (buttonConfig.shape.lowercase()) {
            BUTTON_SHAPE_SQUARE -> {
                val side = buttonHeight
                RectF(w / 2 - side / 2, h / 2 - side / 2, w / 2 + side / 2, h / 2 + side / 2)
            }
            BUTTON_SHAPE_RECTANGLE -> {
                val rh = buttonHeight
                val rw = minOf(w * 0.9f, rh * 1.8f)
                RectF(w / 2 - rw / 2, h / 2 - rh / 2, w / 2 + rw / 2, h / 2 + rh / 2)
            }
            else -> {
                val radius = minOf(h / 2f, buttonHeight / 2f)
                RectF(w / 2 - radius, h / 2 - radius, w / 2 + radius, h / 2 + radius)
            }
        }
    }

    private fun computeIconShapeBounds(): RectF {
        val w = width.toFloat()
        val h = height.toFloat()
        val buttonHeight = minOf(h, w / 1.8f)
        return when (buttonConfig.shape.lowercase()) {
            BUTTON_SHAPE_CIRCLE -> {
                val r = buttonHeight / 2f * 0.85f
                RectF(w / 2 - r, h / 2 - r, w / 2 + r, h / 2 + r)
            }
            else -> computeOuterShapeBounds()
        }
    }

    private fun drawIcon(canvas: Canvas, icon: String) {
        val shapeBounds = computeIconShapeBounds()
        canvas.save()
        canvas.clipPath(iconClipPath(computeOuterShapeBounds()))
        if (SvgIconManager.renderIcon(canvas, context, icon, shapeBounds, iconFillPaint,
                iconFillOverride = buttonConfig.iconFill)) {
            canvas.restore()
            return
        }
        canvas.restore()
        val outerBounds = computeOuterShapeBounds()
        val shapeDim = minOf(outerBounds.width(), outerBounds.height())
        iconPaint.strokeWidth = shapeDim * 0.07f
        val cx = width / 2f
        val cy = height / 2f
        val s = shapeDim * 0.28f
        when (icon) {
            "dpad_map" -> drawDpad(canvas, cx, cy, s)
            "arrow_up" -> drawArrow(canvas, cx, cy + s, cx, cy - s)
            "arrow_down" -> drawArrow(canvas, cx, cy - s, cx, cy + s)
            "arrow_left" -> drawArrow(canvas, cx + s, cy, cx - s, cy)
            "arrow_right" -> drawArrow(canvas, cx - s, cy, cx + s, cy)
            "arrow_up_left" -> drawArrow(canvas, cx + s * 0.75f, cy + s * 0.75f, cx - s * 0.75f, cy - s * 0.75f)
            "arrow_up_right" -> drawArrow(canvas, cx - s * 0.75f, cy + s * 0.75f, cx + s * 0.75f, cy - s * 0.75f)
            "arrow_down_left" -> drawArrow(canvas, cx + s * 0.75f, cy - s * 0.75f, cx - s * 0.75f, cy + s * 0.75f)
            "arrow_down_right" -> drawArrow(canvas, cx - s * 0.75f, cy - s * 0.75f, cx + s * 0.75f, cy + s * 0.75f)
            "crosshair" -> drawCrosshair(canvas, cx, cy, s)
            "keyboard" -> drawKeyboard(canvas, cx, cy, s)
            "escape" -> drawCenteredText(canvas, "Esc")
            "space" -> drawSpace(canvas, cx, cy, s)
            "enter" -> drawEnter(canvas, cx, cy, s)
            "tab" -> drawTab(canvas, cx, cy, s)
            "inventory" -> drawCenteredText(canvas, "Inv")
            "pause" -> drawPause(canvas, cx, cy, s)
            "next_merc" -> drawPersonBadge(canvas, cx, cy, s, ">")
            "options" -> drawGear(canvas, cx, cy, s)
            "version_info" -> drawInfo(canvas, cx, cy, s)
            "quit_game" -> drawQuit(canvas, cx, cy, s)
            "sector_exit_north" -> drawSectorExit(canvas, cx, cy, s, "north")
            "sector_exit_east" -> drawSectorExit(canvas, cx, cy, s, "east")
            "sector_exit_south" -> drawSectorExit(canvas, cx, cy, s, "south")
            "sector_exit_west" -> drawSectorExit(canvas, cx, cy, s, "west")
            else -> {
                val merc = numberedSuffix(icon, "merc_")
                val squad = numberedSuffix(icon, "squad_")
                when {
                    merc != null -> drawPersonBadge(canvas, cx, cy, s, merc)
                    squad != null -> drawSquadBadge(canvas, cx, cy, s, squad)
                    else -> drawCenteredText(canvas, icon)
                }
            }
        }
    }

    private fun iconClipPath(bounds: RectF): Path =
        Path().apply {
            when (buttonConfig.shape.lowercase()) {
                BUTTON_SHAPE_SQUARE -> addRect(bounds, Path.Direction.CW)
                BUTTON_SHAPE_RECTANGLE -> addRoundRect(
                    bounds,
                    cornerRadius(0.18f),
                    cornerRadius(0.18f),
                    Path.Direction.CW
                )
                else -> addOval(bounds, Path.Direction.CW)
            }
        }

    private fun numberedSuffix(icon: String, prefix: String): String? =
        if (icon.startsWith(prefix)) icon.removePrefix(prefix).takeIf { it.isNotEmpty() } else null

    private fun drawArrow(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float) {
        canvas.drawLine(x1, y1, x2, y2, iconPaint)
        val angle = Math.atan2((y2 - y1).toDouble(), (x2 - x1).toDouble()).toFloat()
        val head = minOf(width, height) * 0.16f
        val a1 = angle + Math.toRadians(145.0).toFloat()
        val a2 = angle - Math.toRadians(145.0).toFloat()
        canvas.drawLine(x2, y2, x2 + kotlin.math.cos(a1) * head, y2 + kotlin.math.sin(a1) * head, iconPaint)
        canvas.drawLine(x2, y2, x2 + kotlin.math.cos(a2) * head, y2 + kotlin.math.sin(a2) * head, iconPaint)
    }

    private fun drawCrosshair(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawCircle(cx, cy, s * 0.82f, iconPaint)
        canvas.drawLine(cx - s * 1.15f, cy, cx - s * 0.35f, cy, iconPaint)
        canvas.drawLine(cx + s * 0.35f, cy, cx + s * 1.15f, cy, iconPaint)
        canvas.drawLine(cx, cy - s * 1.15f, cx, cy - s * 0.35f, iconPaint)
        canvas.drawLine(cx, cy + s * 0.35f, cx, cy + s * 1.15f, iconPaint)
        canvas.drawCircle(cx, cy, s * 0.12f, iconFillPaint)
    }

    private fun drawMouse(canvas: Canvas, cx: Float, cy: Float, s: Float, activeSide: Int) {
        val mouse = RectF(cx - s * 0.72f, cy - s * 1.05f, cx + s * 0.72f, cy + s * 1.05f)
        canvas.drawRoundRect(mouse, s * 0.55f, s * 0.55f, iconPaint)
        canvas.drawLine(cx, mouse.top + s * 0.1f, cx, cy - s * 0.2f, iconPaint)
        canvas.drawLine(cx - s * 0.72f, cy - s * 0.2f, cx + s * 0.72f, cy - s * 0.2f, iconPaint)
        if (activeSide == 0) {
            canvas.drawCircle(cx, cy - s * 0.56f, s * 0.12f, iconFillPaint)
        } else {
            val dotX = cx + activeSide * s * 0.34f
            canvas.drawCircle(dotX, cy - s * 0.58f, s * 0.16f, iconFillPaint)
        }
    }

    private fun drawKeyboard(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawRoundRect(RectF(cx - s * 1.15f, cy - s * 0.65f, cx + s * 1.15f, cy + s * 0.65f), s * 0.14f, s * 0.14f, iconPaint)
        for (row in 0..1) {
            for (col in 0..3) {
                canvas.drawCircle(cx - s * 0.63f + col * s * 0.42f, cy - s * 0.24f + row * s * 0.42f, s * 0.04f, iconFillPaint)
            }
        }
    }

    private fun drawSpace(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawLine(cx - s, cy + s * 0.35f, cx + s, cy + s * 0.35f, iconPaint)
        canvas.drawLine(cx - s, cy + s * 0.35f, cx - s, cy, iconPaint)
        canvas.drawLine(cx + s, cy + s * 0.35f, cx + s, cy, iconPaint)
    }

    private fun drawEnter(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawLine(cx + s * 0.75f, cy - s * 0.75f, cx + s * 0.75f, cy + s * 0.35f, iconPaint)
        drawArrow(canvas, cx + s * 0.75f, cy + s * 0.35f, cx - s * 0.75f, cy + s * 0.35f)
    }

    private fun drawTab(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        drawArrow(canvas, cx - s, cy, cx + s * 0.55f, cy)
        canvas.drawLine(cx + s * 0.85f, cy - s * 0.65f, cx + s * 0.85f, cy + s * 0.65f, iconPaint)
    }

    private fun drawPause(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawRoundRect(RectF(cx - s * 0.55f, cy - s, cx - s * 0.2f, cy + s), s * 0.08f, s * 0.08f, iconFillPaint)
        canvas.drawRoundRect(RectF(cx + s * 0.2f, cy - s, cx + s * 0.55f, cy + s), s * 0.08f, s * 0.08f, iconFillPaint)
    }

    private fun drawMapIcon(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawLine(cx - s, cy - s * 0.72f, cx - s * 0.35f, cy - s, iconPaint)
        canvas.drawLine(cx - s * 0.35f, cy - s, cx + s * 0.28f, cy - s * 0.72f, iconPaint)
        canvas.drawLine(cx + s * 0.28f, cy - s * 0.72f, cx + s, cy - s * 0.98f, iconPaint)
        canvas.drawLine(cx - s, cy - s * 0.72f, cx - s, cy + s * 0.8f, iconPaint)
        canvas.drawLine(cx - s * 0.35f, cy - s, cx - s * 0.35f, cy + s * 0.55f, iconPaint)
        canvas.drawLine(cx + s * 0.28f, cy - s * 0.72f, cx + s * 0.28f, cy + s * 0.78f, iconPaint)
        canvas.drawLine(cx + s, cy - s * 0.98f, cx + s, cy + s * 0.45f, iconPaint)
        canvas.drawLine(cx - s, cy + s * 0.8f, cx - s * 0.35f, cy + s * 0.55f, iconPaint)
        canvas.drawLine(cx - s * 0.35f, cy + s * 0.55f, cx + s * 0.28f, cy + s * 0.78f, iconPaint)
        canvas.drawLine(cx + s * 0.28f, cy + s * 0.78f, cx + s, cy + s * 0.45f, iconPaint)
        canvas.drawCircle(cx + s * 0.15f, cy, s * 0.13f, iconFillPaint)
    }

    private fun drawDpad(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        val rawDim = minOf(width, height).toFloat()
        val dpadScale = rawDim * 0.38f
        val minDim = dpadScale / 0.28f
        val savedStrokeWidth = iconPaint.strokeWidth
        val reach = dpadScale * 1.18f
        val arm = dpadScale * 0.34f
        val notch = dpadScale * 0.28f
        val outline = Path().apply {
            moveTo(cx - arm, cy - reach)
            lineTo(cx + arm, cy - reach)
            lineTo(cx + arm, cy - arm)
            lineTo(cx + reach, cy - arm)
            lineTo(cx + reach, cy + arm)
            lineTo(cx + arm, cy + arm)
            lineTo(cx + arm, cy + reach)
            lineTo(cx - arm, cy + reach)
            lineTo(cx - arm, cy + arm)
            lineTo(cx - reach, cy + arm)
            lineTo(cx - reach, cy - arm)
            lineTo(cx - arm, cy - arm)
            close()
        }
        val fillPaint = Paint(iconFillPaint).apply { alpha = 22 }
        val outlinePaint = Paint(iconPaint).apply {
            alpha = 150
            strokeWidth = minDim * 0.032f
            style = Paint.Style.STROKE
        }
        val activePaint = Paint(iconPaint).apply {
            alpha = 230
            strokeWidth = minDim * 0.045f
            style = Paint.Style.STROKE
        }

        canvas.drawPath(outline, fillPaint)
        canvas.drawPath(outline, outlinePaint)
        canvas.drawLine(cx - notch, cy, cx + notch, cy, outlinePaint)
        canvas.drawLine(cx, cy - notch, cx, cy + notch, outlinePaint)

        when (currentDpadDirection) {
            "UP" -> canvas.drawLine(cx, cy - reach * 0.78f, cx, cy - arm * 1.15f, activePaint)
            "DOWN" -> canvas.drawLine(cx, cy + arm * 1.15f, cx, cy + reach * 0.78f, activePaint)
            "LEFT" -> canvas.drawLine(cx - reach * 0.78f, cy, cx - arm * 1.15f, cy, activePaint)
            "RIGHT" -> canvas.drawLine(cx + arm * 1.15f, cy, cx + reach * 0.78f, cy, activePaint)
        }
        iconPaint.strokeWidth = savedStrokeWidth
    }

    private fun drawSectorExit(canvas: Canvas, cx: Float, cy: Float, s: Float, direction: String) {
        val inset = s * 0.9f
        canvas.drawRoundRect(RectF(cx - inset, cy - inset, cx + inset, cy + inset), s * 0.14f, s * 0.14f, iconPaint)
        when (direction) {
            "north" -> drawArrow(canvas, cx, cy + s * 0.55f, cx, cy - s * 0.78f)
            "east" -> drawArrow(canvas, cx - s * 0.55f, cy, cx + s * 0.78f, cy)
            "south" -> drawArrow(canvas, cx, cy - s * 0.55f, cx, cy + s * 0.78f)
            "west" -> drawArrow(canvas, cx + s * 0.55f, cy, cx - s * 0.78f, cy)
        }
    }

    private fun drawStance(canvas: Canvas, cx: Float, cy: Float, s: Float, stance: Int) {
        canvas.drawCircle(cx, cy - s * 0.95f + stance * s * 0.22f, s * 0.22f, iconPaint)
        when (stance) {
            0 -> {
                canvas.drawLine(cx, cy - s * 0.7f, cx, cy + s * 0.2f, iconPaint)
                canvas.drawLine(cx, cy - s * 0.35f, cx - s * 0.48f, cy + s * 0.05f, iconPaint)
                canvas.drawLine(cx, cy - s * 0.35f, cx + s * 0.48f, cy + s * 0.05f, iconPaint)
                canvas.drawLine(cx, cy + s * 0.2f, cx - s * 0.42f, cy + s * 0.95f, iconPaint)
                canvas.drawLine(cx, cy + s * 0.2f, cx + s * 0.42f, cy + s * 0.95f, iconPaint)
            }
            1 -> {
                canvas.drawLine(cx, cy - s * 0.48f, cx - s * 0.42f, cy + s * 0.1f, iconPaint)
                canvas.drawLine(cx - s * 0.42f, cy + s * 0.1f, cx + s * 0.45f, cy + s * 0.12f, iconPaint)
                canvas.drawLine(cx + s * 0.45f, cy + s * 0.12f, cx + s * 0.8f, cy + s * 0.75f, iconPaint)
                canvas.drawLine(cx - s * 0.2f, cy + s * 0.12f, cx - s * 0.7f, cy + s * 0.7f, iconPaint)
            }
            else -> {
                canvas.drawLine(cx - s, cy + s * 0.2f, cx + s, cy + s * 0.2f, iconPaint)
                canvas.drawLine(cx - s * 0.35f, cy, cx + s * 0.45f, cy + s * 0.2f, iconPaint)
                canvas.drawLine(cx + s * 0.1f, cy + s * 0.2f, cx + s * 0.9f, cy + s * 0.65f, iconPaint)
            }
        }
    }

    private fun drawRun(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawCircle(cx - s * 0.12f, cy - s * 0.78f, s * 0.18f, iconPaint)
        canvas.drawLine(cx - s * 0.02f, cy - s * 0.55f, cx - s * 0.45f, cy, iconPaint)
        canvas.drawLine(cx - s * 0.45f, cy, cx + s * 0.15f, cy + s * 0.18f, iconPaint)
        canvas.drawLine(cx - s * 0.18f, cy - s * 0.22f, cx + s * 0.58f, cy - s * 0.52f, iconPaint)
        canvas.drawLine(cx - s * 0.22f, cy + s * 0.02f, cx - s * 0.82f, cy + s * 0.82f, iconPaint)
        canvas.drawLine(cx + s * 0.12f, cy + s * 0.18f, cx + s * 0.82f, cy + s * 0.82f, iconPaint)
        drawSpeedLines(canvas, cx - s * 0.95f, cy + s * 0.2f, s)
    }

    private fun drawStealth(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawOval(RectF(cx - s * 0.85f, cy - s * 0.1f, cx + s * 0.45f, cy + s * 0.55f), iconPaint)
        canvas.drawCircle(cx + s * 0.68f, cy - s * 0.25f, s * 0.12f, iconFillPaint)
        canvas.drawCircle(cx + s * 0.9f, cy - s * 0.55f, s * 0.08f, iconFillPaint)
        canvas.drawLine(cx - s, cy + s * 0.88f, cx + s, cy + s * 0.88f, iconPaint)
    }

    private fun drawSwap(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        drawSmallPerson(canvas, cx - s * 0.55f, cy, s * 0.58f)
        drawSmallPerson(canvas, cx + s * 0.55f, cy, s * 0.58f)
        drawArrow(canvas, cx - s * 0.55f, cy - s * 0.8f, cx + s * 0.55f, cy - s * 0.8f)
        drawArrow(canvas, cx + s * 0.55f, cy + s * 0.8f, cx - s * 0.55f, cy + s * 0.8f)
    }

    private fun drawBackstep(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        drawArrow(canvas, cx + s * 0.85f, cy - s * 0.35f, cx - s * 0.85f, cy - s * 0.35f)
        canvas.drawOval(RectF(cx - s * 0.45f, cy, cx + s * 0.5f, cy + s * 0.65f), iconPaint)
        drawCenteredText(canvas, "Alt")
    }

    private fun drawFireMode(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawLine(cx - s, cy + s * 0.55f, cx + s * 0.3f, cy - s * 0.15f, iconPaint)
        canvas.drawRoundRect(RectF(cx - s * 0.1f, cy - s * 0.35f, cx + s * 0.95f, cy + s * 0.05f), s * 0.08f, s * 0.08f, iconPaint)
        for (i in 0..2) canvas.drawCircle(cx - s * 0.55f + i * s * 0.55f, cy - s * 0.8f, s * 0.11f, iconFillPaint)
    }

    private fun drawRange(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        drawCrosshair(canvas, cx - s * 0.25f, cy - s * 0.12f, s * 0.62f)
        canvas.drawLine(cx - s * 0.75f, cy + s * 0.85f, cx + s * 0.95f, cy + s * 0.85f, iconPaint)
        for (i in 0..3) {
            val x = cx - s * 0.75f + i * s * 0.55f
            canvas.drawLine(x, cy + s * 0.7f, x, cy + s, iconPaint)
        }
    }

    private fun drawKeyring(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawCircle(cx - s * 0.35f, cy - s * 0.25f, s * 0.38f, iconPaint)
        canvas.drawLine(cx - s * 0.05f, cy + s * 0.02f, cx + s * 0.85f, cy + s * 0.72f, iconPaint)
        canvas.drawLine(cx + s * 0.42f, cy + s * 0.38f, cx + s * 0.24f, cy + s * 0.65f, iconPaint)
        canvas.drawLine(cx + s * 0.62f, cy + s * 0.52f, cx + s * 0.44f, cy + s * 0.82f, iconPaint)
    }

    private fun drawAutoBandage(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawRoundRect(RectF(cx - s * 0.88f, cy - s * 0.38f, cx + s * 0.45f, cy + s * 0.5f), s * 0.18f, s * 0.18f, iconPaint)
        canvas.drawLine(cx - s * 0.22f, cy - s * 0.18f, cx - s * 0.22f, cy + s * 0.3f, iconPaint)
        canvas.drawLine(cx - s * 0.48f, cy + s * 0.05f, cx + s * 0.05f, cy + s * 0.05f, iconPaint)
        drawArrow(canvas, cx + s * 0.35f, cy - s * 0.6f, cx + s * 0.85f, cy - s * 0.15f)
    }

    private fun drawTargetEnemy(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        drawCrosshair(canvas, cx, cy, s * 0.76f)
        canvas.drawCircle(cx, cy - s * 0.22f, s * 0.18f, iconPaint)
        canvas.drawArc(RectF(cx - s * 0.38f, cy - s * 0.02f, cx + s * 0.38f, cy + s * 0.65f), 200f, 140f, false, iconPaint)
    }

    private fun drawCycleTargets(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawCircle(cx - s * 0.45f, cy + s * 0.1f, s * 0.42f, iconPaint)
        canvas.drawCircle(cx + s * 0.22f, cy - s * 0.18f, s * 0.42f, iconPaint)
        drawArrow(canvas, cx - s * 0.75f, cy - s * 0.78f, cx + s * 0.55f, cy - s * 0.78f)
    }

    private fun drawLevelToggle(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawRoundRect(RectF(cx - s, cy + s * 0.2f, cx + s * 0.4f, cy + s), s * 0.08f, s * 0.08f, iconPaint)
        canvas.drawRoundRect(RectF(cx - s * 0.4f, cy - s * 0.55f, cx + s, cy + s * 0.25f), s * 0.08f, s * 0.08f, iconPaint)
        drawArrow(canvas, cx + s * 0.78f, cy + s * 0.75f, cx + s * 0.78f, cy - s * 0.35f)
    }

    private fun drawLookDirection(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawOval(RectF(cx - s, cy - s * 0.48f, cx + s, cy + s * 0.48f), iconPaint)
        canvas.drawCircle(cx, cy, s * 0.22f, iconFillPaint)
        drawArrow(canvas, cx + s * 0.55f, cy + s * 0.72f, cx + s * 0.95f, cy + s * 0.25f)
    }

    private fun drawCancel(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawLine(cx - s * 0.82f, cy - s * 0.82f, cx + s * 0.82f, cy + s * 0.82f, iconPaint)
        canvas.drawLine(cx + s * 0.82f, cy - s * 0.82f, cx - s * 0.82f, cy + s * 0.82f, iconPaint)
    }

    private fun drawPersonBadge(canvas: Canvas, cx: Float, cy: Float, s: Float, badge: String) {
        drawSmallPerson(canvas, cx - s * 0.25f, cy - s * 0.05f, s * 0.85f)
        drawBadge(canvas, cx + s * 0.58f, cy + s * 0.48f, s * 0.44f, badge)
    }

    private fun drawSquadBadge(canvas: Canvas, cx: Float, cy: Float, s: Float, badge: String) {
        drawSmallPerson(canvas, cx - s * 0.62f, cy + s * 0.05f, s * 0.58f)
        drawSmallPerson(canvas, cx, cy - s * 0.05f, s * 0.72f)
        drawSmallPerson(canvas, cx + s * 0.58f, cy + s * 0.08f, s * 0.58f)
        drawBadge(canvas, cx + s * 0.55f, cy + s * 0.58f, s * 0.42f, badge)
    }

    private fun drawBlinkItems(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawRoundRect(RectF(cx - s * 0.7f, cy - s * 0.15f, cx + s * 0.45f, cy + s * 0.75f), s * 0.1f, s * 0.1f, iconPaint)
        canvas.drawLine(cx - s * 0.7f, cy - s * 0.15f, cx - s * 0.1f, cy - s * 0.55f, iconPaint)
        canvas.drawLine(cx - s * 0.1f, cy - s * 0.55f, cx + s * 0.45f, cy - s * 0.15f, iconPaint)
        drawSpark(canvas, cx + s * 0.75f, cy - s * 0.45f, s * 0.35f)
    }

    private fun drawGear(canvas: Canvas, cx: Float, cy: Float, s: Float) {
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

    private fun drawInfo(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawCircle(cx, cy, s, iconPaint)
        drawCenteredText(canvas, "i")
    }

    private fun drawEndTurn(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawLine(cx - s * 0.85f, cy, cx - s * 0.18f, cy + s * 0.62f, iconPaint)
        canvas.drawLine(cx - s * 0.18f, cy + s * 0.62f, cx + s * 0.9f, cy - s * 0.65f, iconPaint)
    }

    private fun drawWireframe(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        val a = RectF(cx - s * 0.85f, cy - s * 0.45f, cx + s * 0.25f, cy + s * 0.65f)
        val b = RectF(cx - s * 0.25f, cy - s * 0.85f, cx + s * 0.85f, cy + s * 0.25f)
        canvas.drawRect(a, iconPaint)
        canvas.drawRect(b, iconPaint)
        canvas.drawLine(a.left, a.top, b.left, b.top, iconPaint)
        canvas.drawLine(a.right, a.top, b.right, b.top, iconPaint)
        canvas.drawLine(a.right, a.bottom, b.right, b.bottom, iconPaint)
    }

    private fun drawTreetops(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawCircle(cx, cy - s * 0.35f, s * 0.62f, iconPaint)
        canvas.drawCircle(cx - s * 0.48f, cy - s * 0.05f, s * 0.45f, iconPaint)
        canvas.drawCircle(cx + s * 0.48f, cy - s * 0.05f, s * 0.45f, iconPaint)
        canvas.drawLine(cx, cy + s * 0.2f, cx, cy + s, iconPaint)
        canvas.drawLine(cx, cy + s * 0.55f, cx - s * 0.38f, cy + s * 0.9f, iconPaint)
        canvas.drawLine(cx, cy + s * 0.55f, cx + s * 0.38f, cy + s * 0.9f, iconPaint)
    }

    private fun drawSaveLoad(canvas: Canvas, cx: Float, cy: Float, s: Float, save: Boolean) {
        canvas.drawRoundRect(RectF(cx - s * 0.75f, cy - s * 0.85f, cx + s * 0.55f, cy + s * 0.7f), s * 0.1f, s * 0.1f, iconPaint)
        canvas.drawRect(RectF(cx - s * 0.42f, cy - s * 0.62f, cx + s * 0.22f, cy - s * 0.2f), iconPaint)
        canvas.drawRect(RectF(cx - s * 0.45f, cy + s * 0.2f, cx + s * 0.25f, cy + s * 0.48f), iconPaint)
        if (save) {
            drawArrow(canvas, cx + s * 0.82f, cy - s * 0.65f, cx + s * 0.82f, cy + s * 0.2f)
        } else {
            drawArrow(canvas, cx + s * 0.82f, cy + s * 0.2f, cx + s * 0.82f, cy - s * 0.65f)
        }
    }

    private fun drawQuit(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawRect(RectF(cx - s * 0.9f, cy - s, cx + s * 0.15f, cy + s), iconPaint)
        canvas.drawLine(cx + s * 0.15f, cy - s, cx + s * 0.55f, cy - s * 0.72f, iconPaint)
        canvas.drawLine(cx + s * 0.55f, cy - s * 0.72f, cx + s * 0.55f, cy + s * 0.72f, iconPaint)
        canvas.drawLine(cx + s * 0.55f, cy + s * 0.72f, cx + s * 0.15f, cy + s, iconPaint)
        drawArrow(canvas, cx - s * 0.22f, cy, cx + s, cy)
    }

    private fun drawCheats(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawCircle(cx, cy, s * 0.92f, iconPaint)
        drawSpark(canvas, cx - s * 0.25f, cy - s * 0.38f, s * 0.22f)
        textPaint.textSize = s * 1.15f
        canvas.drawText("C", cx + s * 0.08f, cy - (textPaint.descent() + textPaint.ascent()) / 2f, textPaint)
    }

    private fun drawSmallPerson(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawCircle(cx, cy - s * 0.52f, s * 0.22f, iconPaint)
        canvas.drawLine(cx, cy - s * 0.3f, cx, cy + s * 0.35f, iconPaint)
        canvas.drawLine(cx, cy - s * 0.05f, cx - s * 0.35f, cy + s * 0.18f, iconPaint)
        canvas.drawLine(cx, cy - s * 0.05f, cx + s * 0.35f, cy + s * 0.18f, iconPaint)
        canvas.drawLine(cx, cy + s * 0.35f, cx - s * 0.28f, cy + s * 0.78f, iconPaint)
        canvas.drawLine(cx, cy + s * 0.35f, cx + s * 0.28f, cy + s * 0.78f, iconPaint)
    }

    private fun drawBadge(canvas: Canvas, cx: Float, cy: Float, s: Float, text: String) {
        canvas.drawCircle(cx, cy, s, iconFillPaint)
        val oldColor = textPaint.color
        textPaint.color = 0xFF111820.toInt()
        drawCenteredMiniText(canvas, text, cx, cy, s * 1.45f)
        textPaint.color = oldColor
    }

    private fun drawCenteredMiniText(canvas: Canvas, text: String, cx: Float, cy: Float, max: Float) {
        textPaint.textSize = max * 0.5f
        while (textPaint.textSize > max * 0.25f && textPaint.measureText(text) > max) {
            textPaint.textSize -= resources.displayMetrics.density
        }
        canvas.drawText(text, cx, cy - (textPaint.descent() + textPaint.ascent()) / 2f, textPaint)
    }

    private fun drawSpark(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        canvas.drawLine(cx - s, cy, cx + s, cy, iconPaint)
        canvas.drawLine(cx, cy - s, cx, cy + s, iconPaint)
        canvas.drawLine(cx - s * 0.6f, cy - s * 0.6f, cx + s * 0.6f, cy + s * 0.6f, iconPaint)
        canvas.drawLine(cx + s * 0.6f, cy - s * 0.6f, cx - s * 0.6f, cy + s * 0.6f, iconPaint)
    }

    private fun drawSpeedLines(canvas: Canvas, x: Float, y: Float, s: Float) {
        canvas.drawLine(x, y - s * 0.35f, x + s * 0.45f, y - s * 0.35f, iconPaint)
        canvas.drawLine(x - s * 0.2f, y, x + s * 0.35f, y, iconPaint)
        canvas.drawLine(x, y + s * 0.35f, x + s * 0.5f, y + s * 0.35f, iconPaint)
    }

    private fun getRawXCompat(event: MotionEvent, pointerIndex: Int): Float {
        if (Build.VERSION.SDK_INT >= 29) return event.getRawX(pointerIndex)
        val loc = IntArray(2)
        getLocationOnScreen(loc)
        return event.getX(pointerIndex) + loc[0]
    }

    private fun getRawYCompat(event: MotionEvent, pointerIndex: Int): Float {
        if (Build.VERSION.SDK_INT >= 29) return event.getRawY(pointerIndex)
        val loc = IntArray(2)
        getLocationOnScreen(loc)
        return event.getY(pointerIndex) + loc[1]
    }

    companion object {
        private const val LONG_PRESS_MS = 400L
        private const val TOUCH_SLOP = 16.0

        fun clampFloat(value: Float, min: Float, max: Float): Float =
            value.coerceIn(min, max)

        fun iconFallback(context: android.content.Context, icon: String?): String =
            TouchButtonLocalization.getIconFallback(context, icon)
    }
}
