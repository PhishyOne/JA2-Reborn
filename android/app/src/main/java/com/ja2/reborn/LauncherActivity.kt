package com.ja2.reborn

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.ja2.reborn.databinding.ActivityLauncherBinding
import com.ja2.reborn.ui.main.SectionsPagerAdapter
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException


class LauncherActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_LEGACY_STORAGE_PERMISSION = 1421
    }

    private lateinit var binding: ActivityLauncherBinding

    private val activityLogTag = "LauncherActivity"
    private val jsonFormat = Json {
        prettyPrint = true
    }
    private val ja2JsonFilename = ".ja2/ja2.json"
    private val cheatsJsonFilename = ".ja2/cheats.json"
    private lateinit var configurationModel: ConfigurationModel
    private var startPendingStoragePermission = false

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        LanguageManager.applyLanguage(this)
        super.onCreate(savedInstanceState)

        configurationModel = ViewModelProvider(this)[ConfigurationModel::class.java]
        loadJA2Json()
        syncGameVersionWithLanguageSelection()
        loadCheatsJson()

        if (hasGameSession() && hasPlayableGameDirectory()) {
            startGameAfterPermissionCheck()
            return
        }

        binding = ActivityLauncherBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)
        setupLanguageFlags()
        val sectionsPagerAdapter = SectionsPagerAdapter(this)
        binding.viewPager.adapter = sectionsPagerAdapter

        binding.fab.setOnClickListener {
            startGame()
        }
        hideSystemBars()
    }

    override fun onResume() {
        super.onResume()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        hideSystemBars()

        val exception = NativeExceptionContainer.getException()
        Log.i(activityLogTag, "Resuming LauncherActivity, previous exception: $exception")
        if (exception != null) {
            Toast.makeText(
                this,
                getString(R.string.crash_exception_toast, exception),
                Toast.LENGTH_LONG
            ).show()
            NativeExceptionContainer.resetException()
        }

        if (startPendingStoragePermission) {
            startPendingStoragePermission = false
            if (hasRequiredStoragePermission()) {
                startGameAfterPermissionCheck()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.storage_permission_missing_toast),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQUEST_LEGACY_STORAGE_PERMISSION) {
            return
        }

        if (hasRequiredStoragePermission()) {
            startGameAfterPermissionCheck()
        } else {
            Toast.makeText(
                this,
                getString(R.string.storage_permission_missing_toast),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun setupLanguageFlags() {
        val flagDE = binding.languageFlags.findViewById<android.widget.ImageView>(
            com.ja2.reborn.R.id.flagDE
        )
        val flagGB = binding.languageFlags.findViewById<android.widget.ImageView>(
            com.ja2.reborn.R.id.flagGB
        )

        fun updateFlagHighlight() {
            val current = LanguageManager.getSavedLanguage(this)
            flagDE.alpha = if (current == LanguageManager.Language.GERMAN) 1.0f else 0.35f
            flagGB.alpha = if (current == LanguageManager.Language.ENGLISH) 1.0f else 0.35f
            flagDE.isSelected = current == LanguageManager.Language.GERMAN
            flagGB.isSelected = current == LanguageManager.Language.ENGLISH
        }
        updateFlagHighlight()

        fun selectLanguage(language: LanguageManager.Language) {
            if (LanguageManager.setLanguage(this, language)) {
                configurationModel.setVanillaGameVersion(language.toVanillaVersion())
                saveJA2Json()
                LanguageManager.applyLanguage(this)
                recreate()
            } else {
                configurationModel.setVanillaGameVersion(language.toVanillaVersion())
                saveJA2Json()
                updateFlagHighlight()
            }
        }

        flagDE.setOnClickListener {
            selectLanguage(LanguageManager.Language.GERMAN)
        }
        flagGB.setOnClickListener {
            selectLanguage(LanguageManager.Language.ENGLISH)
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    fun getRecommendedResolution(): Resolution {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealMetrics(metrics)

        val nativeWidth = Integer.max(metrics.widthPixels, metrics.heightPixels)
        val nativeHeight = Integer.min(metrics.widthPixels, metrics.heightPixels)
        val halfWidth = (nativeWidth / 2).toUInt()
        val halfHeight = (nativeHeight / 2).toUInt()
        val width = halfWidth - (halfWidth % 2u)
        val height = halfHeight - (halfHeight % 2u)

        return if (width > Resolution.DEFAULT.width && height > Resolution.DEFAULT.height) {
            Resolution(width, height)
        } else {
            Resolution.DEFAULT
        }
    }

    fun persistJA2Configuration() {
        saveJA2Json()
    }

    private fun startGame() {
        if (!hasRequiredStoragePermission()) {
            showStoragePermissionDialog()
            return
        }
        startGameAfterPermissionCheck()
    }

    private fun startGameAfterPermissionCheck() {
        try {
            GameDir.checkGameDirectoryForCommonMistakes(
                this,
                configurationModel.vanillaGameDir.value
            ) {
                saveJA2Json()
                saveCheatsJson()
                NativeExceptionContainer.resetException()
                val intent = Intent(this@LauncherActivity, RebornActivity::class.java)
                startActivity(intent)
            }
        } catch (e: IOException) {
            val message = "Could not write ${ja2JsonPath}: ${e.message}"
            Log.e(activityLogTag, message)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasRequiredStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager()
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        val readGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val writeGranted = Build.VERSION.SDK_INT > Build.VERSION_CODES.P ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED

        return readGranted && writeGranted
    }

    private fun showStoragePermissionDialog() {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = Ja2GuiStyle.panelBackground(this@LauncherActivity)
            setPadding(dp(20), dp(18), dp(20), dp(18))
        }

        content.addView(TextView(this).apply {
            text = getString(R.string.storage_permission_title)
            setTextColor(Ja2GuiStyle.ACCENT)
            textSize = 18f
            gravity = Gravity.CENTER
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        content.addView(TextView(this).apply {
            text = getString(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    R.string.storage_permission_message
                } else {
                    R.string.storage_permission_legacy_message
                }
            )
            setTextColor(Ja2GuiStyle.TEXT)
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, dp(16))
        }, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        val dialog = AlertDialog.Builder(this).create()
        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        buttonRow.addView(Ja2GuiStyle.styledButton(
            this,
            getString(R.string.cancel),
            minHeightDp = 42
        ) {
            dialog.dismiss()
        }, LinearLayout.LayoutParams(0, dp(44), 1f).apply {
            marginEnd = dp(6)
        })
        buttonRow.addView(Ja2GuiStyle.styledButton(
            this,
            getString(R.string.storage_permission_open_settings),
            textColor = Ja2GuiStyle.ACCENT,
            minHeightDp = 42
        ) {
            dialog.dismiss()
            openStoragePermissionSettings()
        }, LinearLayout.LayoutParams(0, dp(44), 1f).apply {
            marginStart = dp(6)
        })
        content.addView(buttonRow, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        dialog.setView(content)
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog.show()
    }

    private fun openStoragePermissionSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            requestLegacyStoragePermission()
            return
        }
        startPendingStoragePermission = true
        val intent = Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            Uri.parse("package:$packageName")
        )
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.w(activityLogTag, "Could not open app file access settings: ${e.message}")
            startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        }
    }

    private fun requestLegacyStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startGameAfterPermissionCheck()
            return
        }

        val permissions = mutableListOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        ActivityCompat.requestPermissions(
            this,
            permissions.toTypedArray(),
            REQUEST_LEGACY_STORAGE_PERMISSION
        )
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private val ja2JsonPath: String
        get() {
            return "${applicationContext.filesDir.absolutePath}/$ja2JsonFilename"
        }

    private fun loadJA2Json() {
        try {
            val text = File(ja2JsonPath).readText()
            val json: Ja2Json = jsonFormat.decodeFromString(text)

            configurationModel.setVanillaGameDir(json.vanillaGameDir)
            configurationModel.setSaveGameDir(json.saveGameDir)

            if (json.vanillaGameVersion != null) {
                configurationModel.setVanillaGameVersion(json.vanillaGameVersion)
            } else {
                configurationModel.setVanillaGameVersion(VanillaVersion.DEFAULT)
            }
            if (json.scalingQuality != null) {
                configurationModel.setScalingQuality(json.scalingQuality)
            } else {
                configurationModel.setScalingQuality(ScalingQuality.DEFAULT)
            }
            if (json.mouseMode != null) {
                configurationModel.setMouseMode(json.mouseMode)
            } else {
                configurationModel.setMouseMode(MouseMode.DEFAULT)
            }
            if (json.resolution != null) {
                configurationModel.setResolution(json.resolution)
            } else {
                configurationModel.setResolution(getRecommendedResolution())
            }
            if (json.debug != null) {
                configurationModel.setDebug(json.debug)
            } else {
                configurationModel.setDebug(false)
            }
        } catch (e: SerializationException) {
            Log.w(activityLogTag, "Could not decode ja2.json: ${e.message}")
            configurationModel.setVanillaGameVersion(VanillaVersion.ENGLISH)
            configurationModel.setScalingQuality(ScalingQuality.DEFAULT)
            configurationModel.setMouseMode(MouseMode.DEFAULT)
            configurationModel.setResolution(getRecommendedResolution())
            configurationModel.setDebug(false)
        } catch (e: IOException) {
            Log.w(activityLogTag, "Could not read $ja2JsonPath: ${e.message}")
            configurationModel.setVanillaGameVersion(VanillaVersion.ENGLISH)
            configurationModel.setScalingQuality(ScalingQuality.DEFAULT)
            configurationModel.setMouseMode(MouseMode.DEFAULT)
            configurationModel.setResolution(getRecommendedResolution())
            configurationModel.setDebug(false)
        }
    }

    private fun syncGameVersionWithLanguageSelection() {
        val language = if (LanguageManager.hasSavedLanguage(this)) {
            LanguageManager.getSavedLanguage(this)
        } else {
            LanguageManager.Language.ENGLISH
        }
        val version = language.toVanillaVersion()
        if (configurationModel.vanillaGameVersion.value != version) {
            configurationModel.setVanillaGameVersion(version)
            saveJA2Json()
        }
    }

    private fun LanguageManager.Language.toVanillaVersion(): VanillaVersion {
        return when (this) {
            LanguageManager.Language.ENGLISH -> VanillaVersion.ENGLISH
            LanguageManager.Language.GERMAN -> VanillaVersion.GERMAN
        }
    }

    private fun saveJA2Json() {
        val json = Ja2Json(
            configurationModel.vanillaGameDir.value,
            configurationModel.vanillaGameVersion.value,
            configurationModel.saveGameDir.value,
            configurationModel.resolution.value,
            configurationModel.scalingQuality.value,
            configurationModel.mouseMode.value,
            configurationModel.debug.value
        )
        val parentDir = File(ja2JsonPath).parentFile
        if (parentDir?.exists() != true) {
            parentDir?.mkdirs()
        }
        File(ja2JsonPath).writeText(jsonFormat.encodeToString(json))
    }

    private val cheatsJsonPath: String
        get() {
            return "${applicationContext.filesDir.absolutePath}/$cheatsJsonFilename"
        }

    private fun loadCheatsJson() {
        try {
            val text = File(cheatsJsonPath).readText()
            val cheats: CheatConfig = jsonFormat.decodeFromString(text)
            configurationModel.cheatConfig.value = cheats
        } catch (e: SerializationException) {
            Log.w(activityLogTag, "Could not decode cheats.json: ${e.message}")
            configurationModel.cheatConfig.value = CheatConfig.DEFAULT
        } catch (e: IOException) {
            Log.i(activityLogTag, "No cheats.json found, using defaults")
            configurationModel.cheatConfig.value = CheatConfig.DEFAULT
        }
    }

    private fun hasGameSession(): Boolean {
        return File(applicationContext.filesDir, ".ja2/game_session").exists()
    }

    private fun hasPlayableGameDirectory(): Boolean {
        val dir = configurationModel.vanillaGameDir.value?.trim() ?: return false
        return dir.isNotEmpty() && File(dir).isDirectory
    }

    private fun saveCheatsJson() {
        val cheats = configurationModel.cheatConfig.value ?: CheatConfig.DEFAULT
        val parentDir = File(cheatsJsonPath).parentFile
        if (parentDir?.exists() != true) {
            parentDir?.mkdirs()
        }
        File(cheatsJsonPath).writeText(jsonFormat.encodeToString(cheats))
    }
}
