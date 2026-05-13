package com.ja2.reborn

import android.app.Application
import android.content.Context

class Ja2Application : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LanguageManager.wrapContext(base))
    }

    override fun onCreate() {
        super.onCreate()
        LanguageManager.applyLanguage(this)
    }
}
