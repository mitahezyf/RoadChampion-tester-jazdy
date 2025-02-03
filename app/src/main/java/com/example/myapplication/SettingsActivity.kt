package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchVibration: SwitchCompat
    private lateinit var switchTheme: SwitchCompat
    private lateinit var sharedPreferences: SharedPreferences



    override fun onCreate(savedInstanceState: Bundle?) {

        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false) // Domyślnie jasny tryb
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        switchTheme = findViewById(R.id.switchTheme)
        switchTheme.isChecked = isDarkMode
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            VibrationHelper.vibrate(this)
            setDarkMode(isChecked)
        }


        switchVibration = findViewById(R.id.switchVibration)
        switchVibration.isChecked = sharedPreferences.getBoolean("vibration", true)
        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("vibration", isChecked).apply()
            VibrationHelper.vibrate(this)
        }


        findViewById<CardView>(R.id.cardChangeProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            VibrationHelper.vibrate(this)
        }

        findViewById<CardView>(R.id.cardAccelerometerTest).setOnClickListener {
            startActivity(Intent(this, AccelerometerTestActivity::class.java))
            VibrationHelper.vibrate(this)
        }

        findViewById<CardView>(R.id.cardBack).setOnClickListener {
            VibrationHelper.vibrate(this)
            finish()
        }
    }


    private fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean("dark_mode", false)
    }

    private fun setDarkMode(enabled: Boolean) {
        val sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("dark_mode", enabled).apply()

        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

}
