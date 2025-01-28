package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RouteDetailsActivity : AppCompatActivity() {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_details)

        mapView = findViewById(R.id.mapView)
        mapView.setMultiTouchControls(true)

        val route = intent.getParcelableExtra<Route>("route")
        if (route != null) {
            displayRouteOnMap(route)
        } else {
            Log.e("RouteDetails", "Błąd: Trasa nie została przekazana!")
        }
    }

    private fun displayRouteOnMap(route: Route) {
        val geoPoints = parseCoordinates(route.coordinates)

        if (geoPoints.isNotEmpty()) {
            val polyline = Polyline().apply {
                setPoints(geoPoints)
                color = resources.getColor(android.R.color.holo_blue_dark, theme)
                width = 30.0f

            }

            Log.d("RouteDetails", "Dodaję Polyline na mapę. Liczba punktów: ${geoPoints.size}")
            mapView.overlayManager.add(polyline)
            mapView.controller.setCenter(geoPoints.first())
            mapView.controller.setZoom(15.0)
            mapView.invalidate()
        } else {
            Log.e("RouteDetails", "Nie można dodać Polyline – brak punktów!")
        }
    }


    private fun parseCoordinates(coordinatesJson: String): List<GeoPoint> {
        Log.d("RouteDetails", "Otrzymany JSON: $coordinatesJson")
        return try {
            val type = object : TypeToken<List<Map<String, Double>>>() {}.type
            val points: List<Map<String, Double>> = Gson().fromJson(coordinatesJson, type)

            val geoPoints = points.map { point ->
                GeoPoint(point["mLatitude"] ?: 0.0, point["mLongitude"] ?: 0.0)
            }
            Log.d("RouteDetails", "Poprawnie sparsowane punkty: $geoPoints")
            geoPoints
        } catch (e: Exception) {
            Log.e("RouteDetails", "Błąd parsowania JSON: ${e.message}")
            emptyList()
        }
    }


}
