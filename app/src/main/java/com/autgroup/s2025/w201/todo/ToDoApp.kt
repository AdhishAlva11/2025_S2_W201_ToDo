package com.autgroup.s2025.w201.todo

import android.app.Application
import android.content.Context

class ToDoApp : Application() {

    override fun attachBaseContext(base: Context) {
        // Apply saved locale before the app starts
        super.attachBaseContext(LocaleUtils.applySavedLocale(base))
    }
}
