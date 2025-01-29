package com.example.myapplication


import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromRoutePointList(points: List<RoutePoint>): String {
        return gson.toJson(points)
    }

    @TypeConverter
    fun toRoutePointList(data: String): List<RoutePoint> {
        val listType = object : TypeToken<List<RoutePoint>>() {}.type
        return gson.fromJson(data, listType)
    }
}
