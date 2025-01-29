package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RouteDetailsActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var database: AppDatabase
    private var routeId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_details)

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))

        mapView = findViewById(R.id.mapView)
        mapView.setMultiTouchControls(true)

        database = AppDatabase.getInstance(this)

        routeId = intent.getIntExtra("routeId", -1)
        Log.d("RouteDetails", "Otrzymane ID trasy: $routeId")

        if (routeId == -1) {
            Log.e("RouteDetails", "Niepoprawne ID trasy!")
            return
        }

        loadRouteDetails()
    }

    private fun loadRouteDetails() {
        CoroutineScope(Dispatchers.IO).launch {
            val routeWithPoints = database.routeDao().getRouteWithPoints(routeId)

            withContext(Dispatchers.Main) {
                if (routeWithPoints == null) {
                    Log.e("RouteDetails", "Nie znaleziono trasy!")
                    return@withContext
                }

                val route = routeWithPoints.route
                val routePoints = routeWithPoints.points.map { GeoPoint(it.latitude, it.longitude) }

                Log.d("RouteDetails", "Załadowano punkty: ${routePoints.size}")

                if (routePoints.isNotEmpty()) {
                    val firstPoint = routePoints.first()
                    mapView.controller.setCenter(firstPoint)
                    mapView.controller.setZoom(18.0)

                    val overlay = Polyline().apply {
                        setPoints(routePoints)
                        color = Color.BLUE
                        width = 5.0f
                    }

                    mapView.overlayManager.add(overlay)
                    addStartAndEndMarkers(routePoints)
                    mapView.invalidate()
                }
            }
        }
    }

    private fun addStartAndEndMarkers(points: List<GeoPoint>) {
        if (points.isEmpty()) return

        val startMarker = Marker(mapView).apply {
            position = points.first()
            title = "Start trasy"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        val endMarker = Marker(mapView).apply {
            position = points.last()
            title = "Koniec trasy"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        mapView.overlays.add(startMarker)
        mapView.overlays.add(endMarker)
        mapView.invalidate()
    }
}
