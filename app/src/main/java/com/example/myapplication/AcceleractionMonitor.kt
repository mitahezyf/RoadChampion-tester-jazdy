package com.example.myapplication

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt



interface AccelerationListener {
    fun onSuddenAccelerationDetected()
    fun onSuddenBrakingDetected()
}

class AcceleractionMonitor(context: Context, private val listener: AccelerationListener) : SensorEventListener {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private var lastAcceleration: Float = 0f
    private val accelerationThreshold = 2.0f  // Próg przyspieszeń w m/s2
    private val brakingThreshold = -2.0f // Próg  hamowanń w m/s2

    fun startMonitoring() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopMonitoring() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val acceleration = sqrt((it.values[0] * it.values[0] +
                    it.values[1] * it.values[1] +
                    it.values[2] * it.values[2]).toDouble()).toFloat()

            val delta = acceleration - lastAcceleration

            if (delta > accelerationThreshold) {
                listener.onSuddenAccelerationDetected()
            } else if (delta < brakingThreshold) {
                listener.onSuddenBrakingDetected()
            }

            lastAcceleration = acceleration
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //NIE WIEM JAK I DLACZEGO ALE BROŃ CIĘ BOŻE WYWALIĆ
    }


}

