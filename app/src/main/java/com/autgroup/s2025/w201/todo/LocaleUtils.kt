package com.autgroup.s2025.w201.todo

import android.content.Context
import android.content.res.Configuration
import java.util.*

object LocaleUtils {

    private const val PREFS_NAME = "app_settings"
    private const val KEY_LANGUAGE = "language"

    // Save and apply the selected language (stores "en", "hi", "zh")
    fun setLocale(context: Context, languageCode: String): Context {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()

        return updateLocale(context, languageCode)
    }

    // Apply the saved language when app starts
    fun applySavedLocale(context: Context): Context {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val languageCode = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        return updateLocale(context, languageCode)
    }

    // Internal helper to apply locale
    private fun updateLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}
