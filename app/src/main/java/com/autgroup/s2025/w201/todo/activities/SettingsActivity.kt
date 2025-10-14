package com.autgroup.s2025.w201.todo.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import android.widget.ImageButton
import com.autgroup.s2025.w201.todo.R
import com.autgroup.s2025.w201.todo.ThemeUtils

class SettingsActivity : AppCompatActivity() {

    private lateinit var themeSwitch: Switch
    private lateinit var languageSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        // Back button setup
        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.setOnClickListener {
            finish() // closes Settings and returns to Profile
        }

        themeSwitch = findViewById(R.id.switchTheme)
        languageSpinner = findViewById(R.id.spinnerLanguage)

        //Load saved preference
        val pref = getSharedPreferences("app_settings", MODE_PRIVATE)
        val darkMode = pref.getBoolean("dark_mode", false)
        themeSwitch.isChecked = darkMode
        applyTheme(darkMode)

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            applyTheme(isChecked)
            pref.edit().putBoolean("dark_mode", isChecked).apply()
        }

        // Language dropdown (demo)
        val languages = arrayOf("English", "Spanish", "French", "Hindi", "Chinese")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
        languageSpinner.adapter = adapter
    }

    private fun applyTheme(isDark: Boolean) {
        if (isDark)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }


}