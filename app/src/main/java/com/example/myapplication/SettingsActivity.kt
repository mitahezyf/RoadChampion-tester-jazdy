package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchVibration: SwitchCompat
    private lateinit var switchTheme: SwitchCompat
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)


        findViewById<CardView>(R.id.cardChangeProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }


        findViewById<CardView>(R.id.cardAccelerometerTest).setOnClickListener {
            startActivity(Intent(this, AccelerometerTestActivity::class.java))
        }


        switchVibration = findViewById(R.id.switchVibration)
        switchVibration.isChecked = sharedPreferences.getBoolean("vibration", true)
        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("vibration", isChecked).apply()
            VibrationHelper.vibrate(this)
        }


        switchTheme = findViewById(R.id.switchTheme)
        switchTheme.isChecked = isDarkModeEnabled()
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            setDarkMode(isChecked)
        }
        findViewById<CardView>(R.id.cardBack).setOnClickListener {
            finish()
        }
    }

    private fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean("dark_mode", false)
    }

    private fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("dark_mode", enabled).apply()
        recreate()
    }
}
