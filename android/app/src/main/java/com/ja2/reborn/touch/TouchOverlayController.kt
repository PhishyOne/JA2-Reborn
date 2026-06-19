package com.ja2.reborn.touch

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout
import com.ja2.reborn.Ja2GuiStyle
import com.ja2.reborn.R
import com.ja2.reborn.ResolutionMode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.libsdl.app.SDLActivity
import org.libsdl.app.SDLSurface
import java.io.File

class TouchOverlayController(
    private val filesDir: File,
    private val activity: Activity,
    private val root: ViewGroup,
    private val surface: SDLSurface,
    private val resolutionMode: ResolutionMode = ResolutionMode.DEFAULT,
    private val onCheatButtonTapped: () -> Unit = {},
    private val onImportPreset: () -> Unit = {}
) {
    private val store = TouchButtonStore(filesDir, activity, resolutionMode)
    private val dispatcher = TouchInputDispatcher(surface)
    private var config: TouchOverlayConfig? = null
    private val buttonViews = mutableListOf<TouchOverlayButtonView>()
    private var overlayContainer: FrameLayout? = null
    private var attached = false
    private var containerWidth = 0
    private var containerHeight = 0
    private var saveDebounceRunnable: Runnable? = null
    private var schlossButton: TouchOverlayLockButtonView? = null
    private var plusButton: TextView? = null
    private var gearButton: TouchOverlaySettingsButtonView? = null
    private var helpButton: TextView? = null
    private var gridView: TouchOverlayGridView? = null
    private var autoHideRunnable: Runnable? = null
    private var overlayAutoHidden = false
    private var currentActiveScreen = SAFE_SCREEN_ID

    fun attach() {
        if (attached) return
        attached = true
        overlayAutoHidden = false

        val loadResult = store.loadOrDefaultWithResult()
        config = loadResult.config
        config = config!!.copy(layoutLocked = true, editMode = false)
        applyRuntimeSpeeds(config!!)

        val container = FrameLayout(activity).apply {
            isClickable = false
            isFocusable = false
            clipChildren = false
            clipToPadding = false
        }
        root.clipChildren = false
        root.clipToPadding = false
        root.addView(container, createMatchParentLayoutParams())
        root.bringChildToFront(container)
        overlayContainer = container

        root.addOnLayoutChangeListener(layoutChangeListener)
        root.post {
            captureContainerSize()
            createGridView()
            createSystemButtons()
            createButtonViews()
            updateSchlossButtonState()
        }
        startAutoHidePolling()
    }

    fun detach() {
        releasePressedInputs()
        SDLSurface.setOverlayEditModeActive(false)
        removeAllButtonViews()
        removeSystemButtons()
        removeGridView()

        root.removeOnLayoutChangeListener(layoutChangeListener)

        overlayContainer?.let { container ->
            (container.parent as? ViewGroup)?.removeView(container)
        }
        overlayContainer = null

        saveDebounceRunnable?.let { root.removeCallbacks(it) }
        saveDebounceRunnable = null
        stopAutoHidePolling()
        attached = false
    }

    fun releasePressedInputs() {
        buttonViews.forEach { it.releaseIfHeld() }
        dispatcher.releaseAll()
    }

    fun reloadFromStore() {
        config = store.loadOrDefault()
        config = config!!.copy(layoutLocked = true, editMode = false,
            tacticalActionPanelScalePercent = effectivePanelScale(config!!.tacticalActionPanelScalePercent))
        applyRuntimeSpeeds(config!!)
        root.post {
            removeAllButtonViews()
            captureContainerSize()
            updateGridViewState()
            createButtonViews()
            updateButtonDraggable()
            updateSchlossButtonState()
        }
    }

    fun resetToDefaults() {
        config = store.loadDefaultFromRaw()
        config = config!!.copy(
            tacticalActionPanelScalePercent = effectivePanelScale(config!!.tacticalActionPanelScalePercent))
        applyRuntimeSpeeds(config!!)
        store.save(persistableConfig(config!!))
        root.post {
            removeAllButtonViews()
            captureContainerSize()
            updateGridViewState()
            createButtonViews()
            updateButtonDraggable()
            updateSchlossButtonState()
        }
        Log.i(TAG, "Reset to default config")
    }

    fun deleteAllButtons() {
        val cfg = config ?: return
        config = if (currentActiveScreen == MAP_SCREEN) cfg.copy(mapScreenButtons = emptyList())
        else cfg.copy(buttons = emptyList())
        store.save(persistableConfig(config!!))
        root.post {
            removeAllButtonViews()
            captureContainerSize()
            updateGridViewState()
            updateButtonDraggable()
            updateSchlossButtonState()
        }
        Log.i(TAG, "Deleted all touch overlay buttons")
    }

    private fun captureContainerSize() {
        containerWidth = root.width
        containerHeight = root.height
    }

    private val layoutChangeListener = android.view.View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
        root.post {
            val newWidth = root.width
            val newHeight = root.height
            if (newWidth > 0 && newHeight > 0 &&
                (newWidth != containerWidth || newHeight != containerHeight)) {
                containerWidth = newWidth
                containerHeight = newHeight
                updateGridViewState()
                repositionAllButtons()
                repositionSystemButtons()
            }
        }
    }

    private fun createGridView() {
        val container = overlayContainer ?: return
        if (gridView != null) return

        val view = TouchOverlayGridView(activity).apply {
            isClickable = false
            isFocusable = false
            gridSizePx = gridSizePx()
            visibility = if (config?.layoutLocked == false) View.VISIBLE else View.GONE
        }
        gridView = view
        container.addView(
            view,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun updateGridViewState() {
        gridView?.let { view ->
            view.gridSizePx = gridSizePx()
            view.visibility = if (config?.layoutLocked == false) View.VISIBLE else View.GONE
            view.invalidate()
        }
    }

    private fun removeGridView() {
        gridView?.let { view ->
            (view.parent as? ViewGroup)?.removeView(view)
        }
        gridView = null
    }

    // ---- System buttons ----

    private fun createSystemButtons() {
        val container = overlayContainer ?: return
        val cfg = config ?: return

        schlossButton = TouchOverlayLockButtonView(activity).apply {
            alpha = 0.45f
            setLocked(cfg.layoutLocked)
            setOnClickListener { onSchlossTapped() }
            isClickable = true
            isFocusable = true
        }
        container.addView(schlossButton, bottomStartLayoutParams(0, SYSTEM_BUTTON_SIZE_DP.dpToPx()))

        plusButton = createSystemButtonView("+").apply {
            setOnClickListener { onPlusTapped() }
            setOnLongClickListener { resetToDefaults(); true }
            visibility = if (cfg.layoutLocked) View.GONE else View.VISIBLE
        }
        container.addView(plusButton, bottomStartLayoutParams(1, SYSTEM_BUTTON_SIZE_DP.dpToPx()))

        gearButton = TouchOverlaySettingsButtonView(activity).apply {
            setOnClickListener { onGearTapped() }
            isClickable = true
            isFocusable = true
            visibility = if (cfg.layoutLocked) View.GONE else View.VISIBLE
        }
        container.addView(gearButton, bottomStartLayoutParams(2, SYSTEM_BUTTON_SIZE_DP.dpToPx()))

        helpButton = createSystemButtonView("?").apply {
            setOnClickListener { onHelpTapped() }
            visibility = if (cfg.layoutLocked) View.GONE else View.VISIBLE
        }
        container.addView(helpButton, bottomStartLayoutParams(3, SYSTEM_BUTTON_SIZE_DP.dpToPx()))

        ensureSystemButtonsOnTop()
    }

    private fun createSystemButtonView(text: String): TextView {
        return TextView(activity).apply {
            this.text = text
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(0xFFFFFFFF.toInt())
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 12.dpToPx().toFloat()
                setColor(0xAA111820.toInt())
                setStroke(1.dpToPx(), 0x66FFFFFF)
            }
            isClickable = true
            isFocusable = true
        }
    }

    private fun repositionSystemButtons() {
        schlossButton?.layoutParams = bottomStartLayoutParams(0, SYSTEM_BUTTON_SIZE_DP.dpToPx())
        plusButton?.layoutParams = bottomStartLayoutParams(1, SYSTEM_BUTTON_SIZE_DP.dpToPx())
        gearButton?.layoutParams = bottomStartLayoutParams(2, SYSTEM_BUTTON_SIZE_DP.dpToPx())
        helpButton?.layoutParams = bottomStartLayoutParams(3, SYSTEM_BUTTON_SIZE_DP.dpToPx())
    }

    private fun removeSystemButtons() {
        schlossButton?.let { (it.parent as? ViewGroup)?.removeView(it) }
        plusButton?.let { (it.parent as? ViewGroup)?.removeView(it) }
        gearButton?.let { (it.parent as? ViewGroup)?.removeView(it) }
        helpButton?.let { (it.parent as? ViewGroup)?.removeView(it) }
        schlossButton = null
        plusButton = null
        gearButton = null
        helpButton = null
    }

    private fun updateSchlossButtonState() {
        val cfg = config ?: return
        schlossButton?.setLocked(cfg.layoutLocked)
        plusButton?.visibility = if (cfg.layoutLocked) View.GONE else View.VISIBLE
        gearButton?.visibility = if (cfg.layoutLocked) View.GONE else View.VISIBLE
        helpButton?.visibility = if (cfg.layoutLocked) View.GONE else View.VISIBLE
        syncOverlayEditMode()
    }

    private fun syncOverlayEditMode() {
        val cfg = config
        val active = attached && cfg?.layoutLocked == false && !overlayAutoHidden
        SDLSurface.setOverlayEditModeActive(active)
    }

    private fun onPlusTapped() {
        val cfg = config ?: return
        val container = overlayContainer ?: return

        if (containerWidth <= 0 || containerHeight <= 0) {
            captureContainerSize()
            if (containerWidth <= 0 || containerHeight <= 0) {
                Log.w(TAG, "Cannot create button: container has zero size")
                return
            }
        }

        val newId = "button_${System.currentTimeMillis()}"

        val baseButtonConfig = TouchButtonConfig(
            id = newId,
            label = "",
            icon = null,
            shape = BUTTON_SHAPE_CIRCLE,
            x = clampFloat(0.45f, 0f, 1f),
            y = clampFloat(0.75f, 0f, 1f),
            size = 0.140f,
            alpha = 0.45f,
            visible = true,
            actions = emptyList()
        )
        val tempConfig = activeTouchButtonPresets().first().applyTo(baseButtonConfig)

        val dialog = TouchOverlayEditDialog(
            context = activity,
            buttonConfig = tempConfig,
            presetOptions = activeTouchButtonPresets(),
            onSave = { updatedConfig ->
                addNewButton(updatedConfig)
            },
            onDelete = { }
        )
        dialog.show()
    }

    private fun addNewButton(buttonConfig: TouchButtonConfig) {
        val cfg = config ?: return
        val container = overlayContainer ?: return
        val minDim = minOf(containerWidth, containerHeight)

        val (buttonWidth, buttonHeight) = dimensionsFor(buttonConfig, minDim)
        val leftPx = (buttonConfig.x * containerWidth).toInt()
            .coerceIn(0, (containerWidth - buttonWidth).coerceAtLeast(0))
        val topPx = (buttonConfig.y * containerHeight).toInt()
            .coerceIn(0, (containerHeight - buttonHeight).coerceAtLeast(0))

        val buttonView = TouchOverlayButtonView(
            container.context,
            buttonConfig,
            dispatcher,
            { btnId -> onButtonPositionChanged(btnId) },
            { btnConfig -> onButtonLongPress(btnConfig) },
            { action -> onSpecialAction(action) },
            draggable = !cfg.layoutLocked
        )
        buttonView.alpha = buttonConfig.alpha
        buttonView.setSnapGridSize(if (cfg.layoutLocked) 0 else gridSizePx())

        buttonView.layoutParams = FrameLayout.LayoutParams(buttonWidth, buttonHeight).apply {
            leftMargin = leftPx
            topMargin = topPx
        }
        container.addView(buttonView)
        buttonViews.add(buttonView)

        ensureSystemButtonsOnTop()
        updateActiveButtons(activeButtons() + buttonConfig)
        saveConfig()

        Log.i(TAG, "Created new button: ${buttonConfig.id}")
    }

    private fun onGearTapped() {
        val cfg = config
        val dialog = TouchOverlaySettingsDialog(
            context = activity,
            onResetAll = { resetToDefaults() },
            onDeleteAll = { deleteAllButtons() },
            onExportPreset = { exportPreset() },
            onImportPreset = { onImportPreset() },
            autoHideEnabled = cfg?.hideOverlayOnNonGameScreens ?: true,
            onAutoHideToggled = { enabled -> toggleAutoHide(enabled) },
            disableMouseScrolling = cfg?.disableMouseScrolling ?: false,
            onDisableMouseScrollingToggled = { disabled -> persistDisableMouseScrolling(disabled) },
            onScrollSpeedChanged = { ms -> persistScrollSpeed(ms) },
            onMouseSpeedChanged = { speed -> persistMouseSpeed(speed) },
            mapFovPercent = cfg?.tacticalMapFovPercent ?: 100,
            onMapFovChanged = { v -> persistMapFov(v) },
            panelScalePercent = effectivePanelScale(cfg?.tacticalActionPanelScalePercent ?: 130),
            onPanelScaleChanged = { v -> persistPanelScale(v) },
            directTouchArbitrationMs = cfg?.directTouchArbitrationMs ?: 2500,
            onDirectTouchArbitrationChanged = { ms -> persistDirectTouchArbitration(ms) },
            resolutionMode = resolutionMode
        )
        dialog.show()
    }

    private fun onHelpTapped() {
        val cfg = config ?: return
        if (!cfg.layoutLocked) {
            config = cfg.copy(layoutLocked = true, editMode = false)
            updateSchlossButtonState()
            updateButtonDraggable()
            updateGridViewState()
            saveCurrentPositions()
        }
        overlayAutoHidden = true
        applyAutoHideVisibility(false)
        SDLActivity.showTutorial()
        root.post { autoHidePoll() }
    }

    private fun applyRuntimeSpeeds(cfg: TouchOverlayConfig) {
        SDLSurface.setTouchpadMouseSpeed(cfg.relativeMouseSpeed)
        SDLSurface.setDirectTouchArbitrationMs(cfg.directTouchArbitrationMs)
        try {
            SDLActivity.setScrollSpeed(cfg.scrollSpeedMs)
            SDLActivity.setMouseScrollingDisabled(cfg.disableMouseScrolling)
            SDLActivity.setTacticalActionPanelScalePercent(effectivePanelScale(cfg.tacticalActionPanelScalePercent))
        } catch (e: Exception) {
            Log.w(TAG, "Could not apply runtime settings: ${e.message}")
        }
    }

    private fun currentScrollSpeed(): Int {
        return try {
            SDLActivity.getScrollSpeed()
        } catch (e: Exception) {
            config?.scrollSpeedMs ?: 35
        }
    }

    private fun persistableConfig(cfg: TouchOverlayConfig): TouchOverlayConfig =
        cfg.copy(
            schemaVersion = TOUCH_OVERLAY_CONFIG_VERSION,
            editMode = false,
            layoutLocked = true,
            relativeMouseSpeed = SDLSurface.getTouchpadMouseSpeed(),
            scrollSpeedMs = currentScrollSpeed(),
            disableMouseScrolling = currentDisableMouseScrolling(),
            tacticalMapFovPercent = currentMapFovPercent(),
            tacticalActionPanelScalePercent = effectivePanelScale(currentPanelScalePercent()),
            directTouchArbitrationMs = SDLSurface.getDirectTouchArbitrationMs()
        )

    private fun currentDisableMouseScrolling(): Boolean {
        return try {
            SDLActivity.isMouseScrollingDisabled()
        } catch (e: Exception) {
            config?.disableMouseScrolling ?: false
        }
    }

    private fun currentMapFovPercent(): Int {
        return 100
    }

    private fun currentPanelScalePercent(): Int {
        return try {
            SDLActivity.getTacticalActionPanelScalePercent()
        } catch (e: Exception) {
            config?.tacticalActionPanelScalePercent ?: 130
        }
    }

    private fun effectivePanelScale(raw: Int): Int {
        return when (resolutionMode) {
            ResolutionMode.RETRO -> 100
            ResolutionMode.HIGH_RES -> raw.coerceIn(100, 180)
            else -> raw.coerceIn(100, 130)
        }
    }

    private fun saveConfig() {
        val cfg = config ?: return
        store.save(persistableConfig(cfg))
    }

    private fun persistScrollSpeed(ms: Int) {
        val cfg = config ?: return
        val updated = cfg.copy(scrollSpeedMs = ms)
        config = updated
        store.save(persistableConfig(updated))
    }

    private fun persistMouseSpeed(speed: Float) {
        val cfg = config ?: return
        val updated = cfg.copy(relativeMouseSpeed = speed)
        config = updated
        store.save(persistableConfig(updated))
    }

    private fun persistDisableMouseScrolling(disabled: Boolean) {
        val cfg = config ?: return
        try {
            SDLActivity.setMouseScrollingDisabled(disabled)
        } catch (e: Exception) {
            Log.w(TAG, "Could not set mouse scrolling flag: ${e.message}")
        }
        val updated = cfg.copy(disableMouseScrolling = disabled)
        config = updated
        store.save(persistableConfig(updated))
    }

    private fun persistMapFov(v: Int) {
        val cfg = config ?: return
        val updated = cfg.copy(tacticalMapFovPercent = v)
        config = updated
        store.save(persistableConfig(updated))
    }

    private fun persistPanelScale(v: Int) {
        val cfg = config ?: return
        val updated = cfg.copy(tacticalActionPanelScalePercent = effectivePanelScale(v))
        config = updated
        store.save(persistableConfig(updated))
    }

    private fun persistDirectTouchArbitration(ms: Int) {
        val cfg = config ?: return
        val updated = cfg.copy(directTouchArbitrationMs = ms)
        config = updated
        store.save(persistableConfig(updated))
    }

    fun exportPreset() {
        val cfg = config ?: return
        val jsonFormat = Json { ignoreUnknownKeys = true }

        val ja2JsonFile = File(filesDir, ".ja2/ja2.json")
        val saveGameDir: String? = try {
            if (ja2JsonFile.exists()) {
                val ja2Json = jsonFormat.decodeFromString<com.ja2.reborn.Ja2Json>(ja2JsonFile.readText())
                ja2Json.saveGameDir
            } else null
        } catch (e: Exception) {
            Log.w(TAG, "Could not read ja2.json for export: ${e.message}")
            null
        }

        if (saveGameDir.isNullOrBlank()) {
            showStyledMessage(
                activity.getString(R.string.touch_export_failed_title),
                activity.getString(R.string.touch_export_no_save_dir)
            )
            return
        }

        val targetDir = File(saveGameDir)
        if (!targetDir.exists() || !targetDir.canWrite()) {
            showStyledMessage(
                activity.getString(R.string.touch_export_failed_title),
                activity.getString(R.string.touch_export_dir_invalid).format(saveGameDir)
            )
            return
        }

        showExportNameDialog(targetDir, persistableConfig(cfg))
    }

    fun importPresetFromUri(uri: Uri) {
        val raw: String
        try {
            activity.contentResolver.openInputStream(uri)?.use { stream ->
                raw = stream.bufferedReader().readText()
            } ?: run {
                showImportError(activity.getString(R.string.touch_import_read_error))
                return
            }
        } catch (e: Exception) {
            Log.e(TAG, "Import read failed: ${e.message}")
            showImportError(activity.getString(R.string.touch_import_read_error_detail).format(e.message))
            return
        }

        val importedConfig: TouchOverlayConfig
        try {
            importedConfig = store.importFromJson(raw)
        } catch (e: Exception) {
            Log.e(TAG, "Import validation failed: ${e.message}")
            showImportError(activity.getString(R.string.touch_import_invalid_layout).format(e.message))
            return
        }

        val normalized = importedConfig.copy(
            schemaVersion = TOUCH_OVERLAY_CONFIG_VERSION,
            layoutLocked = true,
            editMode = false,
            tacticalActionPanelScalePercent = effectivePanelScale(importedConfig.tacticalActionPanelScalePercent)
        )
        applyRuntimeSpeeds(normalized)
        store.save(persistableConfig(normalized))
        reloadFromStore()
        Toast.makeText(activity, activity.getString(R.string.touch_import_success, normalized.buttons.size + normalized.mapScreenButtons.size), Toast.LENGTH_LONG).show()
        Log.i(TAG, "Imported touch layout with ${normalized.buttons.size} tactical + ${normalized.mapScreenButtons.size} map screen buttons")
    }

    private fun showImportError(message: String) {
        showStyledMessage(activity.getString(R.string.touch_import_failed_title), message)
    }

    private fun showExportNameDialog(targetDir: File, cfgToExport: TouchOverlayConfig) {
        val content = dialogContent(
            title = activity.getString(R.string.touch_export_filename_title),
            message = activity.getString(R.string.touch_export_filename_message)
        )
        val input = EditText(activity).apply {
            hint = activity.getString(R.string.touch_export_filename_hint)
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0x8899A8B0.toInt())
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            filters = arrayOf(InputFilter.LengthFilter(48))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 7.dpToPx().toFloat()
                setColor(0xAA05080C.toInt())
                setStroke(1.dpToPx(), Ja2GuiStyle.STROKE)
            }
            setPadding(10.dpToPx(), 0, 10.dpToPx(), 0)
        }
        content.addView(input, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 44.dpToPx()).apply {
            topMargin = 4.dpToPx()
            bottomMargin = 12.dpToPx()
        })

        val dialog = AlertDialog.Builder(activity).setView(content).create()
        content.addView(dialogButtonRow(
            positiveText = activity.getString(R.string.touch_export_filename_confirm),
            negativeText = activity.getString(R.string.touch_reset_confirm_negative),
            onPositive = {
                val baseName = sanitizePresetName(input.text.toString())
                if (baseName.isBlank()) {
                    input.error = activity.getString(R.string.touch_export_filename_invalid)
                    return@dialogButtonRow
                }
                try {
                    val exportedFile = store.exportConfigToDir(cfgToExport, targetDir, baseName)
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.touch_export_success_named, exportedFile.nameWithoutExtension),
                        Toast.LENGTH_LONG
                    ).show()
                    Log.i(TAG, "Exported touch layout to ${exportedFile.absolutePath}")
                    dialog.dismiss()
                } catch (e: Exception) {
                    Log.e(TAG, "Export failed: ${e.message}")
                    dialog.dismiss()
                    showStyledMessage(
                        activity.getString(R.string.touch_export_failed_title),
                        e.message ?: activity.getString(R.string.touch_import_unknown_error)
                    )
                }
            },
            onNegative = { dialog.dismiss() }
        ))
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            input.requestFocus()
        }
        dialog.show()
    }

    private fun showStyledMessage(title: String, message: String) {
        val content = dialogContent(title, message)
        val dialog = AlertDialog.Builder(activity).setView(content).create()
        content.addView(Ja2GuiStyle.styledButton(
            activity,
            activity.getString(R.string.ok),
            fillColor = 0xAA1D2A36.toInt(),
            strokeColor = Ja2GuiStyle.STROKE
        ) { dialog.dismiss() }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 40.dpToPx()).apply {
            topMargin = 12.dpToPx()
        })
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
        dialog.show()
    }

    private fun dialogContent(title: String, message: String): LinearLayout =
        LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22.dpToPx(), 18.dpToPx(), 22.dpToPx(), 16.dpToPx())
            background = Ja2GuiStyle.panelBackground(activity)
            addView(TextView(activity).apply {
                text = title
                textSize = 18f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(0, 0, 0, 8.dpToPx())
            })
            addView(TextView(activity).apply {
                text = message
                textSize = 13f
                setTextColor(Ja2GuiStyle.MUTED)
            })
        }

    private fun dialogButtonRow(
        positiveText: String,
        negativeText: String,
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ): LinearLayout =
        LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(Ja2GuiStyle.styledButton(
                activity,
                negativeText,
                fillColor = 0xAA1D2A36.toInt(),
                strokeColor = Ja2GuiStyle.STROKE,
                onClick = onNegative
            ), LinearLayout.LayoutParams(0, 40.dpToPx(), 1f).apply { rightMargin = 6.dpToPx() })
            addView(Ja2GuiStyle.styledButton(
                activity,
                positiveText,
                textColor = Ja2GuiStyle.SUCCESS_TEXT,
                fillColor = Ja2GuiStyle.SUCCESS_FILL,
                strokeColor = Ja2GuiStyle.SUCCESS_TEXT,
                onClick = onPositive
            ), LinearLayout.LayoutParams(0, 40.dpToPx(), 1f))
        }

    private fun sanitizePresetName(raw: String): String =
        raw.trim()
            .removeSuffix(".json")
            .replace(Regex("""[\\/:*?"<>|]"""), "_")
            .trim('.', ' ')

    fun toggleAutoHide(enabled: Boolean) {
        val cfg = config ?: return
        config = cfg.copy(hideOverlayOnNonGameScreens = enabled)
        saveConfig()
        Log.i(TAG, "Auto-hide toggled: $enabled")
        root.post {
            if (enabled) {
                autoHidePoll()
            } else if (overlayAutoHidden) {
                applyAutoHideVisibility(true)
            }
        }
    }

    // ---- Auto-hide polling ----

    private fun startAutoHidePolling() {
        val runnable = object : Runnable {
            override fun run() {
                autoHidePoll()
                root.postDelayed(this, AUTO_HIDE_POLL_MS)
            }
        }
        autoHideRunnable = runnable
        root.postDelayed(runnable, AUTO_HIDE_POLL_MS)
    }

    private fun stopAutoHidePolling() {
        autoHideRunnable?.let { root.removeCallbacks(it) }
        autoHideRunnable = null
    }

    private fun autoHidePoll() {
        val cfg = config ?: return

        val tutorialVisible = try {
            SDLActivity.isTutorialVisible()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to query tutorial visibility: ${e.message}")
            false
        }

        if (tutorialVisible) {
            if (!overlayAutoHidden) {
                overlayAutoHidden = true
                applyAutoHideVisibility(false)
            }
            return
        }

        if (!cfg.hideOverlayOnNonGameScreens) {
            if (overlayAutoHidden) {
                overlayAutoHidden = false
                applyAutoHideVisibility(true)
            }
            return
        }

        val screenId = try {
            SDLActivity.getJa2ScreenId()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to query JA2 screen id: ${e.message}")
            SAFE_SCREEN_ID
        }

        val shouldShowOverlay = !tutorialVisible && (
            !cfg.hideOverlayOnNonGameScreens || screenId in VISIBLE_SCREEN_WHITELIST
        )
        val visibilityChanged = shouldShowOverlay == overlayAutoHidden
        val screenChanged = screenId != currentActiveScreen

        if (!visibilityChanged && !screenChanged) return

        if (shouldShowOverlay && overlayAutoHidden) {
            // Becoming visible — show with correct button set
            overlayAutoHidden = false
            switchToScreen(screenId)
        } else if (!shouldShowOverlay && !overlayAutoHidden) {
            // Becoming hidden
            overlayAutoHidden = true
            currentActiveScreen = screenId
            applyAutoHideVisibility(false)
        } else if (shouldShowOverlay && !overlayAutoHidden && screenChanged) {
            // Screen changed while overlay stays visible — swap button sets
            switchToScreen(screenId)
        }
    }

    private fun switchToScreen(screenId: Int) {
        currentActiveScreen = screenId
        releasePressedInputs()
        removeAllButtonViews()
        if (containerWidth <= 0 || containerHeight <= 0) {
            captureContainerSize()
        }
        createButtonViews()
        updateSchlossButtonState()
        updateGridViewState()
        updateButtonDraggable()
        applyAutoHideVisibility(true)
    }

    private fun applyAutoHideVisibility(visible: Boolean) {
        val vis = if (visible) View.VISIBLE else View.GONE

        buttonViews.forEach { it.visibility = vis }
        schlossButton?.visibility = vis

        if (!visible) {
            plusButton?.visibility = View.GONE
            gearButton?.visibility = View.GONE
            helpButton?.visibility = View.GONE
            gridView?.visibility = View.GONE
            releasePressedInputs()
            syncOverlayEditMode()
        } else {
            updateSchlossButtonState()
            updateGridViewState()
        }
    }

    private fun onSchlossTapped() {
        val cfg = config ?: return
        val newLocked = !cfg.layoutLocked
        config = cfg.copy(layoutLocked = newLocked)
        updateSchlossButtonState()
        updateButtonDraggable()
        updateGridViewState()

        if (newLocked) {
            saveCurrentPositions()
        } else {
            Log.i(TAG, "Layout unlock is runtime-only and will not be persisted")
        }
        Log.i(TAG, "Layout lock toggled: locked=$newLocked")
    }

    private fun ensureSystemButtonsOnTop() {
        schlossButton?.let { overlayContainer?.bringChildToFront(it) }
        plusButton?.let { overlayContainer?.bringChildToFront(it) }
        gearButton?.let { overlayContainer?.bringChildToFront(it) }
        helpButton?.let { overlayContainer?.bringChildToFront(it) }
    }

    private fun updateButtonDraggable() {
        val locked = config?.layoutLocked ?: true
        buttonViews.forEach {
            it.setDraggable(!locked)
            it.setSnapGridSize(if (locked) 0 else gridSizePx())
        }
    }

    // ---- Normal button views ----

    private fun activeButtons(): List<TouchButtonConfig> {
        val cfg = config ?: return emptyList()
        return if (currentActiveScreen == MAP_SCREEN) cfg.mapScreenButtons else cfg.buttons
    }

    private fun updateActiveButtons(newButtons: List<TouchButtonConfig>) {
        val cfg = config ?: return
        config = if (currentActiveScreen == MAP_SCREEN) cfg.copy(mapScreenButtons = newButtons)
        else cfg.copy(buttons = newButtons)
    }

    private fun activeTouchButtonPresets(): List<TouchButtonPreset> =
        if (currentActiveScreen == MAP_SCREEN) MAP_SCREEN_TOUCH_BUTTON_PRESETS else TACTICAL_TOUCH_BUTTON_PRESETS

    private fun createButtonViews() {
        val cfg = config ?: return
        val container = overlayContainer ?: return
        val buttons = activeButtons()

        if (buttons.isEmpty()) {
            Log.w(TAG, "No buttons for screen $currentActiveScreen, nothing to create")
            return
        }

        if (containerWidth <= 0 || containerHeight <= 0) {
            Log.w(TAG, "Container has zero size (${containerWidth}x${containerHeight}), deferring")
            container.post {
                captureContainerSize()
                createButtonViews()
            }
            return
        }

        val minDim = minOf(containerWidth, containerHeight)

        for (btnConfig in buttons) {
            if (!btnConfig.visible) continue

            val buttonView = TouchOverlayButtonView(
                container.context,
                btnConfig,
                dispatcher,
                { btnId -> onButtonPositionChanged(btnId) },
                { btnConfig -> onButtonLongPress(btnConfig) },
                { action -> onSpecialAction(action) },
                draggable = !cfg.layoutLocked
            )
            buttonView.alpha = btnConfig.alpha
            buttonView.setSnapGridSize(if (cfg.layoutLocked) 0 else gridSizePx())

            val (buttonWidth, buttonHeight) = dimensionsFor(btnConfig, minDim)
            val leftPx = (btnConfig.x * containerWidth).toInt()
                .coerceIn(0, (containerWidth - buttonWidth).coerceAtLeast(0))
            val topPx = (btnConfig.y * containerHeight).toInt()
                .coerceIn(0, (containerHeight - buttonHeight).coerceAtLeast(0))

            val lp = FrameLayout.LayoutParams(buttonWidth, buttonHeight).apply {
                leftMargin = leftPx
                topMargin = topPx
            }

            buttonView.layoutParams = lp
            container.addView(buttonView)
            buttonViews.add(buttonView)
        }

        Log.i(TAG, "Created ${buttonViews.size} button views")
        ensureSystemButtonsOnTop()
    }

    private fun repositionAllButtons() {
        val buttons = activeButtons()
        val minDim = minOf(containerWidth, containerHeight)

        for (view in buttonViews) {
            val btnConfig = buttons.firstOrNull { it.id == view.config.id } ?: continue
            val (buttonWidth, buttonHeight) = dimensionsFor(btnConfig, minDim)
            val leftPx = (btnConfig.x * containerWidth).toInt()
                .coerceIn(0, (containerWidth - buttonWidth).coerceAtLeast(0))
            val topPx = (btnConfig.y * containerHeight).toInt()
                .coerceIn(0, (containerHeight - buttonHeight).coerceAtLeast(0))

            val lp = view.layoutParams as? FrameLayout.LayoutParams ?: continue
            lp.width = buttonWidth
            lp.height = buttonHeight
            lp.leftMargin = leftPx
            lp.topMargin = topPx
            view.layoutParams = lp
            view.updateAppearance(buttonWidth, buttonHeight, btnConfig.alpha)
        }
        ensureSystemButtonsOnTop()
    }

    private fun onButtonPositionChanged(buttonId: String) {
        saveDebounceRunnable?.let {
            root.removeCallbacks(it)
        }

        saveDebounceRunnable = Runnable {
            saveCurrentPositions()
        }
        root.postDelayed(saveDebounceRunnable, SAVE_DEBOUNCE_MS)
    }

    private fun onButtonLongPress(btnConfig: TouchButtonConfig) {
        val currentView = buttonViews.firstOrNull { it.config.id == btnConfig.id }
        val capturedX = currentView?.getNormalizedPosition(containerWidth, containerHeight)?.first
        val capturedY = currentView?.getNormalizedPosition(containerWidth, containerHeight)?.second

        val dialog = TouchOverlayEditDialog(
            context = activity,
            buttonConfig = btnConfig,
            presetOptions = activeTouchButtonPresets(),
            onSave = { updatedConfig ->
                val positionPreserved = if (capturedX != null && capturedY != null)
                    updatedConfig.copy(x = capturedX, y = capturedY)
                else
                    updatedConfig
                onButtonEditSaved(positionPreserved)
            },
            onDelete = { buttonId ->
                onButtonDeleted(buttonId)
            }
        )
        dialog.show()
    }

    private fun onButtonEditSaved(updatedConfig: TouchButtonConfig) {
        val updatedButtons = activeButtons().map {
            if (it.id == updatedConfig.id) updatedConfig else it
        }

        updateActiveButtons(updatedButtons)
        saveConfig()

        val view = buttonViews.firstOrNull { it.config.id == updatedConfig.id } ?: return
        view.updateConfig(updatedConfig)
        val container = overlayContainer ?: return
        val minDim = minOf(containerWidth, containerHeight)

        val newConfig = updatedConfig
        val (buttonWidth, buttonHeight) = dimensionsFor(newConfig, minDim)
        val leftPx = (newConfig.x * containerWidth).toInt()
            .coerceIn(0, (containerWidth - buttonWidth).coerceAtLeast(0))
        val topPx = (newConfig.y * containerHeight).toInt()
            .coerceIn(0, (containerHeight - buttonHeight).coerceAtLeast(0))

        val lp = view.layoutParams as? FrameLayout.LayoutParams ?: return
        lp.width = buttonWidth
        lp.height = buttonHeight
        lp.leftMargin = leftPx
        lp.topMargin = topPx
        view.layoutParams = lp
        view.updateAppearance(buttonWidth, buttonHeight, newConfig.alpha)
        ensureSystemButtonsOnTop()
    }

    private fun onButtonDeleted(buttonId: String) {
        val container = overlayContainer ?: return

        val view = buttonViews.firstOrNull { it.config.id == buttonId } ?: return
        view.releaseIfHeld()

        container.removeView(view)
        buttonViews.remove(view)

        updateActiveButtons(activeButtons().filter { it.id != buttonId })
        saveConfig()

        Log.i(TAG, "Deleted button: $buttonId")
    }

    private fun saveCurrentPositions() {
        val cfg = config ?: return
        if (containerWidth <= 0 || containerHeight <= 0) {
            Log.w(TAG, "Skipping position save: container has zero size")
            return
        }

        val currentButtons = activeButtons()
        val updatedButtons = currentButtons.map { btnConfig ->
            val view = buttonViews.firstOrNull { it.config.id == btnConfig.id }
            if (view != null) {
                val (xNorm, yNorm) = view.getNormalizedPosition(containerWidth, containerHeight)
                btnConfig.copy(x = xNorm, y = yNorm)
            } else {
                btnConfig
            }
        }

        updateActiveButtons(updatedButtons)
        config = config!!.copy(
            relativeMouseSpeed = SDLSurface.getTouchpadMouseSpeed(),
            scrollSpeedMs = currentScrollSpeed()
        )
        saveConfig()
    }

    private fun removeAllButtonViews() {
        val container = overlayContainer ?: return
        buttonViews.forEach { container.removeView(it) }
        buttonViews.clear()
    }

    private fun createMatchParentLayoutParams(): ViewGroup.LayoutParams {
        return if (root is RelativeLayout) {
            RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
        } else {
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    private fun bottomStartLayoutParams(index: Int, sizePx: Int): FrameLayout.LayoutParams {
        val margin = 8.dpToPx()
        val gap = 6.dpToPx()
        return FrameLayout.LayoutParams(sizePx, sizePx).apply {
            gravity = Gravity.BOTTOM or Gravity.START
            bottomMargin = margin
            leftMargin = margin + index * (sizePx + gap)
        }
    }

    private fun onSpecialAction(action: TouchButtonAction): Boolean {
        if (action.type == "cheat_menu") {
            onCheatButtonTapped()
            return true
        }
        return false
    }

    private fun Int.dpToPx(): Int =
        (this * activity.resources.displayMetrics.density).toInt()

    private fun gridSizePx(): Int =
        GRID_SIZE_DP.dpToPx().coerceAtLeast(12)

    private fun dimensionsFor(btnConfig: TouchButtonConfig, minDim: Int): Pair<Int, Int> {
        val minPx = MIN_BUTTON_SIZE_DP.dpToPx()
        val height = (btnConfig.size * minDim).toInt().coerceAtLeast(minPx)
        val width = if (btnConfig.shape == BUTTON_SHAPE_RECTANGLE) {
            (height * RECTANGLE_WIDTH_FACTOR).toInt()
        } else {
            height
        }
        return Pair(width, height)
    }

    companion object {
        private const val TAG = "TouchOverlayController"
        private const val SAVE_DEBOUNCE_MS = 250L
        private const val MIN_BUTTON_SIZE_DP = 28
        private const val SYSTEM_BUTTON_SIZE_DP = 44
        private const val RECTANGLE_WIDTH_FACTOR = 1.55f
        private const val GRID_SIZE_DP = 16
        private const val AUTO_HIDE_POLL_MS = 250L

        // Screen IDs from src/game/ScreenIDs.h
        // GAME_SCREEN = 5, MAP_SCREEN = 9, FADE_SCREEN = 13
        private const val GAME_SCREEN = 5
        private const val MAP_SCREEN = 9
        private val VISIBLE_SCREEN_WHITELIST = setOf(GAME_SCREEN, MAP_SCREEN)
        private const val SAFE_SCREEN_ID = GAME_SCREEN

        fun clampFloat(value: Float, min: Float, max: Float): Float =
            value.coerceIn(min, max)
    }
}
