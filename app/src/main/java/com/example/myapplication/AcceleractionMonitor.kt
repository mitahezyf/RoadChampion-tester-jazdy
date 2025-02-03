package com.example.myapplication

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlin.math.sqrt

interface AccelerationListener {
    fun onSuddenAccelerationDetected()
    fun onSuddenBrakingDetected()
    fun onAccelerationDataChanged(x: Float, y: Float, z: Float)
}

class AcceleractionMonitor(context: Context, private val listener: AccelerationListener) :
    SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private var lastAcceleration: Float = 0f
    private var filteredAcceleration: Float = 0f
    private val alpha = 0.8f // Współczynnik filtra dolnoprzepustowego

    private val accelerationThreshold = 2.5f  // Próg przyspieszenia w m/s2
    private val brakingThreshold = -2.5f // Próg hamowania w m/s2
    private var lastEventTime: Long = 0
    private val minEventDuration = 1000L // Min czas trwania zdarzenia (ms)

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
            val x = it.values[0]
            val y = it.values[1]
            val z = it.values[2]

            val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()


            filteredAcceleration = alpha * filteredAcceleration + (1 - alpha) * acceleration

            val delta = filteredAcceleration - lastAcceleration
            val currentTime = SystemClock.elapsedRealtime()

            if (currentTime - lastEventTime > minEventDuration) {
                if (delta > accelerationThreshold) {
                    listener.onSuddenAccelerationDetected()
                    lastEventTime = currentTime
                } else if (delta < brakingThreshold) {
                    listener.onSuddenBrakingDetected()
                    lastEventTime = currentTime
                }
            }

            listener.onAccelerationDataChanged(x, y, z)

            lastAcceleration = filteredAcceleration
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}
