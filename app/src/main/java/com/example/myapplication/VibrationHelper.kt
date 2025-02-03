package com.example.myapplication

import android.content.Context
import android.os.VibrationEffect

import android.os.VibratorManager

import android.util.Log

object VibrationHelper {

    private fun isVibrationEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        return prefs.getBoolean("vibration", true) // Domyślnie włączone
    }

    fun vibrate(context: Context, duration: Long = 50) {
        if (!isVibrationEnabled(context)) return

        val vibratorManager = context.getSystemService(VibratorManager::class.java)
        val effect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
        vibratorManager.defaultVibrator.vibrate(effect)

        Log.d("VibrationHelper", "Wibracja przez $duration ms")
    }
}
