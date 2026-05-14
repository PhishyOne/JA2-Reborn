package com.ja2.reborn

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LanguageManager {
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "app_language"

    enum class Language(val code: String, val javaLocale: java.util.Locale) {
        ENGLISH("en", java.util.Locale.ENGLISH),
        GERMAN("de", java.util.Locale.GERMAN);

        companion object {
            fun fromCode(code: String): Language =
                entries.firstOrNull { it.code == code } ?: ENGLISH
        }
    }

    fun getSavedLanguage(context: Context): Language {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_LANGUAGE, null) ?: Language.ENGLISH.code
        return Language.fromCode(code)
    }

    fun hasSavedLanguage(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .contains(KEY_LANGUAGE)
    }

    fun setLanguage(context: Context, language: Language): Boolean {
        val previousLanguage = getSavedLanguage(context)
        val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language.code)
            .commit()
        applyLanguageIfNeeded(language)
        return saved && previousLanguage != language
    }

    fun applyLanguage(context: Context) {
        applyLanguageIfNeeded(getSavedLanguage(context))
    }

    fun wrapContext(context: Context): Context {
        val language = getSavedLanguage(context)
        Locale.setDefault(language.javaLocale)

        val configuration = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(android.os.LocaleList(language.javaLocale))
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = language.javaLocale
        }
        return context.createConfigurationContext(configuration)
    }

    private fun applyLanguageIfNeeded(language: Language) {
        Locale.setDefault(language.javaLocale)
        val localeList = LocaleListCompat.create(language.javaLocale)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}
