package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        mapView = view.findViewById(R.id.mapView)
        mapView.setMultiTouchControls(true)



        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager


        initializeMapWithDefaultLocation()


        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setupLocationUpdates()
        } else {

            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }


        val stopButton = view.findViewById<Button>(R.id.stopButton)
        stopButton.setOnClickListener {
            stopTracking(requireContext())
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

    override fun onLocationChanged(location: Location) {
        if (!isMapInitialized) {
            initializeMapAtLocation(location)
            isMapInitialized = true
            startTime = System.currentTimeMillis()
        }


        val geoPoint = GeoPoint(location.latitude, location.longitude)
        geoPoints.add(geoPoint)


        updateLocationOnMap(location)
    }

    private fun initializeMapWithDefaultLocation() {
        val defaultGeoPoint = GeoPoint(52.2297, 21.0122) // Warszawa
        mapView.controller.apply {
            setCenter(defaultGeoPoint)
            setZoom(15.0)
        }
        Log.d("Map", "Mapa zainicjalizowana na domyślnej lokalizacji.")
    }

    private fun initializeMapAtLocation(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)


        mapView.controller.apply {
            setCenter(geoPoint)
            setZoom(18.0)
        }

        Log.d("Map", "Mapa zainicjalizowana na lokalizacji: $geoPoint")
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

    private fun stopTracking(context: Context) {
        endTime = System.currentTimeMillis()


        val totalDistance = calculateTotalDistance(geoPoints)
        val durationInHours = (endTime - startTime) / 3600000.0
        val averageSpeed = if (durationInHours > 0) (totalDistance / 1000) / durationInHours else 0f


        DatabaseHelper.saveRouteToDatabase(
            context = context,
            startTime = startTime,
            endTime = endTime,
            geoPoints = geoPoints,
            averageSpeed = averageSpeed.toFloat()
        )

        Log.d("MapFragment", "Trasa zakończona i zapisana w bazie danych.")
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
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
