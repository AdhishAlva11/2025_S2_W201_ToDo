package com.autgroup.s2025.w201.todo

import android.content.Context
import android.content.res.Configuration
import java.util.*

object LocaleUtils {

    // Applies the language saved in SharedPreferences to the given context
    fun applySavedLocale(context: Context): Context {
        val pref = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val language = pref.getString("language", "English")

        val localeCode = when (language) {
            "Hindi" -> "hi"
            "Chinese" -> "zh"
            else -> "en"
        }

        val locale = Locale(localeCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}
