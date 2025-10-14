package com.autgroup.s2025.w201.todo

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeUtils {
    private const val PREFS = "app_settings"
    private const val KEY_DARK_MODE = "dark_mode"

    // Applies saved theme preference at startup
    fun applySavedTheme(context: Context) {
        val isDark = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_DARK_MODE, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
