package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AccelerometerTestActivity : AppCompatActivity(), AccelerationListener {

    private lateinit var textStatus: TextView
    private lateinit var textX: TextView
    private lateinit var textY: TextView
    private lateinit var textZ: TextView
    private lateinit var buttonStartStop: LinearLayout
    private lateinit var textStartStop: TextView
    private var isMonitoring = false
    private lateinit var accelerometerMonitor: AcceleractionMonitor

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accelerometer_test)

        val buttonBack: LinearLayout = findViewById(R.id.buttonBack)
        buttonBack.setOnClickListener {
            VibrationHelper.vibrate(this)
            finish()
        }

        textStatus = findViewById(R.id.textStatus)
        textX = findViewById(R.id.textX)
        textY = findViewById(R.id.textY)
        textZ = findViewById(R.id.textZ)
        buttonStartStop = findViewById(R.id.buttonStartStop)
        textStartStop = findViewById(R.id.textStartStop)

        accelerometerMonitor = AcceleractionMonitor(this, this)

        buttonStartStop.setOnClickListener {
            VibrationHelper.vibrate(this)
            if (isMonitoring) {
                accelerometerMonitor.stopMonitoring()
                textStartStop.text = "Rozpocznij test"
                textStatus.text = "Spoczynek"
            } else {
                accelerometerMonitor.startMonitoring()
                textStartStop.text = "Zatrzymaj test"
            }
            isMonitoring = !isMonitoring
        }

    }

    @SuppressLint("SetTextI18n")
    override fun onSuddenAccelerationDetected() {
        runOnUiThread {
            textStatus.text = "Gwałtowne przyspieszenie!"
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onSuddenBrakingDetected() {
        runOnUiThread {
            textStatus.text = "Gwałtowne hamowanie!"
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onAccelerationDataChanged(x: Float, y: Float, z: Float) {
        runOnUiThread {
            textX.text = "X: ${"%.2f".format(x)} m/s²"
            textY.text = "Y: ${"%.2f".format(y)} m/s²"
            textZ.text = "Z: ${"%.2f".format(z)} m/s²"
        }
    }
}
