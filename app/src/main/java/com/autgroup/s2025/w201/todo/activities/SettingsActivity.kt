package com.autgroup.s2025.w201.todo.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.ThemeUtils
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var themeSwitch: Switch
    private lateinit var languageSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme and language before layout inflation
        applySavedLanguage()
        ThemeUtils.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Back button
        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.setOnClickListener {
            finish()
        }

        themeSwitch = findViewById(R.id.switchTheme)
        languageSpinner = findViewById(R.id.spinnerLanguage)

        val pref = getSharedPreferences("app_settings", MODE_PRIVATE)
        val darkMode = pref.getBoolean("dark_mode", false)
        themeSwitch.isChecked = darkMode
        applyTheme(darkMode)

        // Handle theme switch
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            applyTheme(isChecked)
            pref.edit().putBoolean("dark_mode", isChecked).apply()
        }

        // --- Supported languages ---
        val languages = arrayOf("English", "Hindi", "Chinese")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
        languageSpinner.adapter = adapter

        // --- Preselect saved language ---
        val savedLang = pref.getString("language", "English")
        val savedIndex = languages.indexOf(savedLang)
        if (savedIndex >= 0) languageSpinner.setSelection(savedIndex)

        // --- Language selection logic ---
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLang = languages[position]
                if (selectedLang != savedLang) {
                    pref.edit().putString("language", selectedLang).apply()
                    setLocale(selectedLang)

                    // Delay refresh slightly for configuration to apply cleanly
                    Handler(Looper.getMainLooper()).postDelayed({
                        restartApp()
                    }, 300)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // --- Applies dark/light theme ---
    private fun applyTheme(isDark: Boolean) {
        if (isDark)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    // --- Reads saved language and applies on startup ---
    private fun applySavedLanguage() {
        val pref = getSharedPreferences("app_settings", MODE_PRIVATE)
        val language = pref.getString("language", "English")
        setLocale(language ?: "English")
    }

    // --- Applies locale for given language ---
    private fun setLocale(language: String) {
        val localeCode = when (language) {
            "Hindi" -> "hi"
            "Chinese" -> "zh"
            else -> "en"
        }

        val locale = Locale(localeCode)
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        // Update configuration for both app and base context
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
        applicationContext.createConfigurationContext(config)
    }

    // --- Restarts entire app so all screens (Profile, Home, etc.) update language ---
    private fun restartApp() {
        val intent = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
