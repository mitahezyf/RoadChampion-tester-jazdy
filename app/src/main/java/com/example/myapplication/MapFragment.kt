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


class MapFragment : Fragment(R.layout.fragment_map), LocationListener {

    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager
    private val geoPoints = mutableListOf<GeoPoint>()
    private var isMapInitialized = false
    private var currentLocationMarker: Marker? = null
    private var startTime: Long = 0L
    private var endTime: Long = 0L
    private lateinit var database: AppDatabase
    private lateinit var routePointDao: RoutePointDao
    private var currentRouteId: Int? = null
    private var isTracking = false

    private lateinit var acceleractionMonitor: AcceleractionMonitor

    private var isFollowingUser = true





    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setupLocationUpdates()
        } else {
            Log.e("Permission", "Permission denied!")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getInstance(requireContext())
        routePointDao = database.routePointDao()

        mapView = view.findViewById(R.id.mapView)
        mapView.setMultiTouchControls(true)

        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        initializeMapWithDefaultLocation()

        acceleractionMonitor = AcceleractionMonitor(requireContext(), object : AccelerationListener {
            override fun onSuddenAccelerationDetected() {
                Log.d("MapFragment", "Wykryto gwałtowne przyspieszenie!")
            }

            override fun onSuddenBrakingDetected() {
                Log.d("MapFragment", "Wykryto gwałtowne hamowanie!")
            }
        })

        val startButton = view.findViewById<CardView>(R.id.cardStart)
        val stopButton = view.findViewById<CardView>(R.id.cardStop)
        val backButton = view.findViewById<CardView>(R.id.cardBack)



        startButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.default_button_color))
        stopButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.default_button_color))


        startButton.setOnClickListener { startTracking() }
        stopButton.setOnClickListener { stopTracking() }
        backButton.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }




        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setupLocationUpdates()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }


        CoroutineScope(Dispatchers.IO).launch {
            val lastRouteId = database.routeDao().getLastRouteId()
            withContext(Dispatchers.Main) {
                currentRouteId = lastRouteId
                Log.d("MapFragment", "Przywrócono ostatnią trasę ID: $currentRouteId")
            }
        }
    }




    private fun startTracking() {
        if (isTracking) return

        startTime = System.currentTimeMillis()
        isTracking = true

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
                Log.d("MapFragment", "Rozpoczęto nową trasę o ID: $currentRouteId")


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
                Log.d("MapFragment", "Trasa ID: $currentRouteId zakończona i zapisana!")


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

    @SuppressLint("DefaultLocale")
    override fun onLocationChanged(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)

        if (isFollowingUser) {
            mapView.controller.animateTo(geoPoint)
        }

        if (!isMapInitialized) {
            initializeMapAtLocation(location)
            isMapInitialized = true
        }
        updateLocationOnMap(location)


        val speedMps = location.speed
        val speedKmh = speedMps * 3.6 // Przeliczenie na km/h


        val speedTextView = view?.findViewById<TextView>(R.id.textSpeed)
        speedTextView?.text = String.format("%.1f km/h", speedKmh)

        if (isTracking) {
            if (currentRouteId == -1) {
                Log.e("MapFragment", "Błąd! currentRouteId jest -1, punkty nie będą zapisywane!")
                return
            }
            geoPoints.add(geoPoint)

            val newPoint = RoutePoint(
                routeId = currentRouteId ?: 0,
                latitude = location.latitude,
                longitude = location.longitude,
                timestamp = System.currentTimeMillis()
            )

            CoroutineScope(Dispatchers.IO).launch {
                routePointDao.insertRoutePoint(newPoint)
                Log.d("Database", "Zapisano punkt: $newPoint")
            }
        }
    }


    private fun initializeMapAtLocation(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)
        mapView.controller.apply {
            setCenter(geoPoint)
            setZoom(18.0)
        }
        Log.d("Map", "Mapa ustawiona na lokalizację: $geoPoint")
    }
    private fun initializeMapWithDefaultLocation() {
        val defaultGeoPoint = GeoPoint(50.0413, 21.9990) // Rzeszow
        mapView.controller.apply {
            setCenter(defaultGeoPoint)
            setZoom(15.0)
        }
        Log.d("Map", "Mapa zainicjalizowana na domyślnej lokalizacji.")
    }

    private fun updateLocationOnMap(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)

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

        mapView.invalidate()
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

    override fun onProviderEnabled(provider: String) {
        Log.d("Location", "Provider $provider enabled")
    }

    override fun onProviderDisabled(provider: String) {
        Log.d("Location", "Provider $provider disabled")
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
