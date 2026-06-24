package com.ja2.reborn

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

object UpdatePrefs {

    private const val PREFS_NAME = "ja2_reborn_update_prefs"
    private const val KEY_OPTED_IN = "auto_update_opted_in"
    private const val KEY_PROMPTED_VERSION = "auto_update_prompted_version"
    private const val KEY_LAST_CHECK_TIME = "last_update_check_time"
    private const val RATE_LIMIT_MS = 24 * 60 * 60 * 1000L

    // -- Opt-in ---------------------------------------------------------------

    fun isOptedIn(context: Context): Boolean? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_OPTED_IN)) return null
        return prefs.getBoolean(KEY_OPTED_IN, false)
    }

    fun setOptedIn(context: Context, optedIn: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_OPTED_IN, optedIn)
            .apply()
    }

    // -- Prompted version (avoid re-prompting on same version) ----------------

    fun getPromptedVersion(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PROMPTED_VERSION, null)
    }

    fun setPromptedVersion(context: Context, version: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PROMPTED_VERSION, version)
            .apply()
    }

    fun shouldShowOptIn(context: Context, currentVersion: String): Boolean {
        val optedIn = isOptedIn(context)
        if (optedIn != null) return false
        val prompted = getPromptedVersion(context)
        return prompted != currentVersion
    }

    // -- Rate limiting --------------------------------------------------------

    fun getLastCheckTime(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_CHECK_TIME, 0L)
    }

    fun setLastCheckTime(context: Context, time: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_CHECK_TIME, time)
            .apply()
    }

    fun setLastCheckTimeNow(context: Context) {
        setLastCheckTime(context, System.currentTimeMillis())
    }

    fun isRateLimited(context: Context, force: Boolean = false): Boolean {
        if (force) return false
        val last = getLastCheckTime(context)
        if (last == 0L) return false
        return (System.currentTimeMillis() - last) < RATE_LIMIT_MS
    }

    // -- Version helpers ------------------------------------------------------

    fun getInstalledVersionName(context: Context): String {
        return try {
            val info: PackageInfo = context.packageManager.getPackageInfo(
                context.packageName, 0
            )
            info.versionName ?: "0.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "0.0.0"
        }
    }

    fun getInstalledVersionCode(context: Context): Long {
        return try {
            val info: PackageInfo = context.packageManager.getPackageInfo(
                context.packageName, 0
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                info.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            0L
        }
    }
}
