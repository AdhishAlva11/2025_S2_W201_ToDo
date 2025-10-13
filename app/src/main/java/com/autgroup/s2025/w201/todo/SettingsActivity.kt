package com.autgroup.s2025.w201.todo

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Switch
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.autgroup.s2025.w201.todo.R


import java.util.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.setOnClickListener {
            finish()
        }


        val themeSwitch = findViewById<Switch>(R.id.switchTheme)
        val languageSpinner = findViewById<Spinner>(R.id.spinnerLanguage)

        // --- Theme toggle ---
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }


        // --- Language dropdown ---
        val languages = listOf("English", "中文", "हिन्दी", "Español", "Français")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
        languageSpinner.adapter = adapter

        // --- Handle selection ---
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> setLocale("en")  // English
                    1 -> setLocale("zh")  // Mandarin Chinese
                    2 -> setLocale("hi")  // Hindi
                    3 -> setLocale("es")  // Spanish
                    4 -> setLocale("fr")  // French
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

        Toast.makeText(this, "Language changed to ${locale.displayLanguage}", Toast.LENGTH_SHORT)
        recreate()
    }
}
