package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class MainMenuActivity : AppCompatActivity() {

    private lateinit var imageProfile: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {

        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false) // Domyślnie jasny
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        imageProfile = findViewById(R.id.imageProfile)
        loadProfileImage()


        findViewById<LinearLayout>(R.id.cardNewRoute).setOnClickListener {
            VibrationHelper.vibrate(this)
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardHistory).setOnClickListener {
            VibrationHelper.vibrate(this)
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardSettings).setOnClickListener {
            VibrationHelper.vibrate(this)
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        imageProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun loadProfileImage() {
        val sharedPreferences = getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        val uriString = sharedPreferences.getString("profile_image_uri", null)
        if (uriString != null) {
            val uri = Uri.parse(uriString)
            imageProfile.setImageURI(uri)
        }
    }


    override fun onResume() {
        super.onResume()
        loadProfileImage()
    }
}
