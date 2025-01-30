package com.example.myapplication

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.sqrt

class AccelerationDetector(
    context: Context,
    private val threshold: Float = 2.0f, //(2 m/s²)
    private val minSpeed: Float = 1.0f,
    private val callback: (isAcceleration: Boolean) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private var lastTimestamp: Long = 0

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTimestamp < 3000) return // Pomiar nie częściej niż co 3 sekundy

            val acceleration = sqrt((it.values[0] * it.values[0]) + (it.values[1] * it.values[1]) + (it.values[2] * it.values[2]))

            if (acceleration > threshold) {
                callback(true)  // Gwałtowne przyspieszenie
            } else if (acceleration < -threshold) {
                callback(false) // Gwałtowne hamowanie
            }

            lastTimestamp = currentTime
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
