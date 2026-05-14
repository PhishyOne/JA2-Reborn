package com.ja2.reborn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ja2.reborn.cheat.CheatOverlayDialog
import com.ja2.reborn.touch.TouchOverlayController
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.libsdl.app.SDLActivity
import org.libsdl.app.SDLSurface
import java.io.File
import java.io.IOException

open class RebornActivity : SDLActivity() {
    private val jsonFormat = Json {
        ignoreUnknownKeys = true
    }
    private val ja2JsonFilename = ".ja2/ja2.json"
    private val gameSessionFilename = ".ja2/game_session"
    private var touchOverlayController: TouchOverlayController? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        SDLSurface.setTouchscreenMouseMode(loadMouseMode().value)
        super.onCreate(savedInstanceState)
        SDLActivity.setTutorialLanguage(LanguageManager.getSavedLanguage(this) == LanguageManager.Language.GERMAN)

        touchOverlayController = TouchOverlayController(
            filesDir = applicationContext.filesDir,
            activity = this,
            root = getContentView() as ViewGroup,
            surface = mSurface,
            onCheatButtonTapped = {
                CheatOverlayDialog(this, applicationContext.filesDir).show()
            },
            onImportPreset = {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "*/*"))
                }
                startActivityForResult(intent, REQUEST_CODE_IMPORT_PRESET)
            }
        )
        touchOverlayController?.attach()
        writeGameSessionFile()
    }

    override fun onResume() {
        super.onResume()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }

    override fun onPause() {
        touchOverlayController?.releasePressedInputs()
        super.onPause()
    }

    override fun onDestroy() {
        deleteGameSessionFile()
        touchOverlayController?.detach()
        touchOverlayController = null
        super.onDestroy()
    }

    override fun setOrientationBis(w: Int, h: Int, resizable: Boolean, hint: String?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }

    override fun getLibraries(): Array<String?>? {
        return arrayOf(
            "ja2"
        )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMPORT_PRESET && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                touchOverlayController?.importPresetFromUri(uri)
            }
        }
    }

    private fun writeGameSessionFile() {
        try {
            val dir = File(applicationContext.filesDir, ".ja2")
            if (!dir.exists()) dir.mkdirs()
            File(dir, "game_session").writeText("running")
        } catch (e: Exception) {
            Log.w(TAG, "Could not write game session file: ${e.message}")
        }
    }

    private fun deleteGameSessionFile() {
        try {
            val file = File(applicationContext.filesDir, gameSessionFilename)
            if (file.exists()) file.delete()
        } catch (e: Exception) {
            Log.w(TAG, "Could not delete game session file: ${e.message}")
        }
    }

    private fun loadMouseMode(): MouseMode {
        return try {
            val path = "${applicationContext.filesDir.absolutePath}/$ja2JsonFilename"
            val json: Ja2Json = jsonFormat.decodeFromString(File(path).readText())
            json.mouseMode ?: MouseMode.DEFAULT
        } catch (e: SerializationException) {
            Log.w(TAG, "Could not decode mouse mode from ja2.json: ${e.message}")
            MouseMode.DEFAULT
        } catch (e: IOException) {
            Log.w(TAG, "Could not read mouse mode from ja2.json: ${e.message}")
            MouseMode.DEFAULT
        }
    }

    companion object {
        private const val TAG = "RebornActivity"
        private const val REQUEST_CODE_IMPORT_PRESET = 1001
    }
}
