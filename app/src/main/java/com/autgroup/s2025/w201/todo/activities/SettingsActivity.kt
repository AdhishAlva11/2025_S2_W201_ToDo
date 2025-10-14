package com.autgroup.s2025.w201.todo.activities

import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.autgroup.s2025.w201.todo.R
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Load language BEFORE inflating layout
        loadSavedLanguage()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // --- Back button ---
        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.setOnClickListener { finish() }

        // --- Theme switch ---
        val themeSwitch = findViewById<Switch>(R.id.switchTheme)
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // --- Language spinner ---
        val languageSpinner = findViewById<Spinner>(R.id.spinnerLanguage)
        val languages = listOf("English", "中文", "हिन्दी", "Español", "Français")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
        languageSpinner.adapter = adapter

        // Pre-select saved language
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        when (prefs.getString("language_code", "en")) {
            "en" -> languageSpinner.setSelection(0)
            "zh" -> languageSpinner.setSelection(1)
            "hi" -> languageSpinner.setSelection(2)
            "es" -> languageSpinner.setSelection(3)
            "fr" -> languageSpinner.setSelection(4)
        }

        // --- Spinner listener ---
        var isUserAction = false

        languageSpinner.post {
            // Set saved language AFTER spinner is ready
            val langCode = prefs.getString("language_code", "en")
            val position = when (langCode) {
                "en" -> 0
                "zh" -> 1
                "hi" -> 2
                "es" -> 3
                "fr" -> 4
                else -> 0
            }
            languageSpinner.setSelection(position, false)
            isUserAction = true
        }

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (!isUserAction) return  // ignore automatic selection at startup

                val langCode = when (position) {
                    0 -> "en"
                    1 -> "zh"
                    2 -> "hi"
                    3 -> "es"
                    4 -> "fr"
                    else -> "en"
                }

                // Avoid unnecessary reloads if same language
                val currentCode = prefs.getString("language_code", "en")
                if (langCode != currentCode) {
                    setLocale(langCode)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // --- Change app language ---
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        // Save preference
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().putString("language_code", languageCode).apply()

        Toast.makeText(this, "Language changed to ${locale.displayLanguage}", Toast.LENGTH_SHORT).show()

        // Refresh page cleanly
        recreate()
    }

    // --- Apply saved language before layout loads ---
    private fun loadSavedLanguage() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val langCode = prefs.getString("language_code", "en")
        val locale = Locale(langCode!!)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
}