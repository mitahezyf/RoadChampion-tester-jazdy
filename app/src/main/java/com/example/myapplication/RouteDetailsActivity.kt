package com.example.myapplication


import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline





@Suppress("DEPRECATION")
class RouteDetailsActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var database: AppDatabase
    private var routeId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_details)

        org.osmdroid.config.Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        org.osmdroid.config.Configuration.getInstance().userAgentValue = packageName

        findViewById<CardView>(R.id.cardBack).setOnClickListener {
            finish()
        }


        routeId = intent.getIntExtra("routeId", -1)
        if (routeId == -1) {
            finish()
            return
        }

        mapView = findViewById(R.id.mapView)
        database = AppDatabase.getInstance(this)


        loadRouteDetails()
    }

    @SuppressLint("SetTextI18n")
    private fun loadRouteDetails() {
        Thread {
            val routeWithPoints = database.routeDao().getRouteWithPoints(routeId)

            runOnUiThread {
                if (routeWithPoints != null) {
                    findViewById<TextView>(R.id.textStartTime).text =
                        formatTime(routeWithPoints.route.startTime)

                    findViewById<TextView>(R.id.textEndTime).text =
                        formatTime(routeWithPoints.route.endTime)

                    findViewById<TextView>(R.id.textDistance).text =
                        "%.2f km".format(routeWithPoints.route.distance / 1000)

                    findViewById<TextView>(R.id.textDuration).text =
                        formatDuration(routeWithPoints.route.duration)

                    findViewById<TextView>(R.id.textAvgSpeed).text =
                        "%.2f km/h".format(routeWithPoints.route.averageSpeed)

                    findViewById<TextView>(R.id.textAccelerations).text =
                        routeWithPoints.route.suddenAccelerations.toString()

                    findViewById<TextView>(R.id.textBrakings).text =
                        routeWithPoints.route.suddenBrakings.toString()

                    drawRouteOnMap(routeWithPoints.points)
                }
            }
        }.start()
    }

    private fun drawRouteOnMap(points: List<RoutePoint>) {
        if (points.isEmpty()) return

        val mapController = mapView.controller


        mapView.postDelayed({
            val startPoint = GeoPoint(points.first().latitude, points.first().longitude)
            mapController.setCenter(startPoint)
            mapController.setZoom(18.0)

            val polyline = Polyline().apply {
                color = Color.BLUE
                width = 5f
                setPoints(points.map { GeoPoint(it.latitude, it.longitude) })
            }

            val startMarker = Marker(mapView).apply {
                position = startPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Start"
            }

            val endMarker = Marker(mapView).apply {
                position = GeoPoint(points.last().latitude, points.last().longitude)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Koniec"
            }


            mapView.overlays.clear()
            mapView.overlays.add(polyline)
            mapView.overlays.add(startMarker)
            mapView.overlays.add(endMarker)
            mapView.invalidate()


            mapController.animateTo(startPoint)

        }, 1000)
    }


    @SuppressLint("SimpleDateFormat")
    private fun formatTime(timestamp: Long): String {
        return if (timestamp > 0) java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date(timestamp)) else "-"
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(durationMillis: Long): String {
        val minutes = (durationMillis / 60000).toInt()
        val seconds = ((durationMillis % 60000) / 1000).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }
}
