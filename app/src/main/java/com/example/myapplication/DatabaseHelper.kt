package com.example.myapplication


import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

object DatabaseHelper {

    fun saveRouteToDatabase(
        context: Context,
        startTime: Long,
        endTime: Long,
        geoPoints: List<GeoPoint>,
        averageSpeed: Float
    ) {
        val gson = Gson()
        val coordinatesJson = gson.toJson(geoPoints)

        val route = Route(
            startTime = startTime,
            endTime = endTime,
            averageSpeed = averageSpeed,
            coordinates = coordinatesJson
        )

        val database = AppDatabase.getInstance(context)
        CoroutineScope(Dispatchers.IO).launch {
            database.routeDao().insertRoute(route)
            Log.d("DatabaseHelper", "Trasa zapisana w bazie danych!")
        }
    }
}
