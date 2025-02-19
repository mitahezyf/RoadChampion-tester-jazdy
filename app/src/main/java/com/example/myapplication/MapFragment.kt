package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Suppress("DEPRECATION")
class MapFragment : Fragment(R.layout.fragment_map), LocationListener, AccelerationListener {

    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager
    private lateinit var acceleractionMonitor: AcceleractionMonitor
    private lateinit var database: AppDatabase
    private lateinit var routePointDao: RoutePointDao
    private var currentRouteId: Int? = null
    private var isTracking = false
    private var currentLocationMarker: Marker? = null
    private val geoPoints = mutableListOf<GeoPoint>()
    private var startTime: Long = 0L
    private var endTime: Long = 0L

    private var accelerationCount = 0
    private var brakingCount = 0

    private lateinit var textAccelerationCount: TextView
    private lateinit var textBrakingCount: TextView
    private lateinit var textSpeed: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textSpeed = view.findViewById(R.id.textSpeed)
        textAccelerationCount = view.findViewById(R.id.textAccelerations)
        textBrakingCount = view.findViewById(R.id.textBrakings)

        mapView = view.findViewById(R.id.mapView)
        mapView.setMultiTouchControls(true)

        val defaultGeoPoint = GeoPoint(50.041187, 21.999121) //Rzeszów
        mapView.controller.setCenter(defaultGeoPoint)
        mapView.controller.setZoom(18.0)

        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        database = AppDatabase.getInstance(requireContext())
        routePointDao = database.routePointDao()

        acceleractionMonitor = AcceleractionMonitor(requireContext(), this)

        val startButton = view.findViewById<CardView>(R.id.cardStart)
        val stopButton = view.findViewById<CardView>(R.id.cardStop)
        val backButton = view.findViewById<CardView>(R.id.cardBack)

        startButton.setOnClickListener {
            VibrationHelper.vibrate(requireContext())
            startTracking()
        }

        stopButton.setOnClickListener {
            VibrationHelper.vibrate(requireContext())
            stopTracking()
        }

        backButton.setOnClickListener {
            VibrationHelper.vibrate(requireContext())
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setupLocationUpdates()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setupLocationUpdates()
        } else {
            Log.e("Permission", "Brak uprawnień do lokalizacji!")
        }
    }


    private fun startTracking() {
        if (isTracking) return

        accelerationCount = 0
        brakingCount = 0
        updateAccelerationUI()


        startTime = System.currentTimeMillis()
        isTracking = true
        geoPoints.clear()

        CoroutineScope(Dispatchers.IO).launch {
            val lastRouteId = database.routeDao().getLastRouteId()
            val newRouteId = lastRouteId + 1

            val route = Route(
                id = newRouteId,
                startTime = startTime,
                endTime = 0,
                averageSpeed = 0f,
                distance = 0f,
                duration = 0
            )

            database.routeDao().insertRoute(route)

            withContext(Dispatchers.Main) {
                currentRouteId = newRouteId
                Toast.makeText(requireContext(), "Rozpoczęto nagrywanie trasy", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopTracking() {
        if (!isTracking) return

        isTracking = false
        endTime = System.currentTimeMillis()

        val duration = endTime - startTime
        val totalDistance = calculateTotalDistance(geoPoints)
        val durationInHours = (endTime - startTime) / 3600000.0
        val averageSpeed = if (durationInHours > 0) (totalDistance / 1000) / durationInHours else 0f

        CoroutineScope(Dispatchers.IO).launch {
            database.routeDao().updateRoute(
                currentRouteId ?: 0,
                startTime,
                endTime,
                averageSpeed.toFloat(),
                totalDistance,
                duration
            )

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Zatrzymano nagrywanie trasy", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setupLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                500L,
                5f,
                this
            )
        } catch (e: SecurityException) {
            Log.e("Location", "Brak uprawnień do lokalizacji: ${e.message}")
        }
    }
    override fun onAccelerationDataChanged(x: Float, y: Float, z: Float) {
        Log.d("MapFragment", "Akcelerometr - X: $x, Y: $y, Z: $z")
    }

    @SuppressLint("SetTextI18n")
    override fun onLocationChanged(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)

        val speedKmH = location.speed * 3.6  // Konwersja z m/s na km/h
        textSpeed.text = "Prędkość: %.2f km/h".format(speedKmH)

        if (currentLocationMarker == null) {
            currentLocationMarker = Marker(mapView).apply {
                position = geoPoint
                title = "Jesteś tutaj"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                mapView.overlays.add(this)
            }
        } else {
            currentLocationMarker?.position = geoPoint
        }
        mapView.controller.setZoom(18)
        mapView.controller.animateTo(geoPoint)
        mapView.invalidate()

        if (isTracking) {
            geoPoints.add(geoPoint)

            val newPoint = RoutePoint(
                routeId = currentRouteId ?: 0,
                latitude = location.latitude,
                longitude = location.longitude,
                timestamp = System.currentTimeMillis()
            )

            CoroutineScope(Dispatchers.IO).launch {
                routePointDao.insertRoutePoint(newPoint)
            }
        }
    }

    private fun calculateTotalDistance(points: List<GeoPoint>): Float {
        var totalDistance = 0f
        for (i in 1 until points.size) {
            val start = points[i - 1]
            val end = points[i]
            totalDistance += start.distanceToAsDouble(end).toFloat()
        }
        return totalDistance
    }

    override fun onSuddenAccelerationDetected() {
        Log.d("MapFragment", "Wykryto nagłe przyspieszenie!")

        accelerationCount++
        updateAccelerationUI()


        currentRouteId?.let { routeId ->
            CoroutineScope(Dispatchers.IO).launch {
                database.routeDao().updateAccelerationData(routeId, accelerationCount, brakingCount)
            }
        }
    }

    override fun onSuddenBrakingDetected() {
        Log.d("MapFragment", "Wykryto nagłe hamowanie!")
        brakingCount++
        updateAccelerationUI()


        currentRouteId?.let { routeId ->
            CoroutineScope(Dispatchers.IO).launch {
                database.routeDao().updateAccelerationData(routeId, accelerationCount, brakingCount)
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private fun updateAccelerationUI() {
        requireActivity().runOnUiThread {
            textAccelerationCount.text = accelerationCount.toString()
            textBrakingCount.text = brakingCount.toString()
        }
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
        acceleractionMonitor.startMonitoring()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        acceleractionMonitor.stopMonitoring()
    }
}
