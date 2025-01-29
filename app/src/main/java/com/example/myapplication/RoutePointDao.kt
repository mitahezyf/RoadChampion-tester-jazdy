package com.example.myapplication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RoutePointDao {
    @Query("SELECT * FROM route_points WHERE routeId = :routeId")
    fun getRoutePoints(routeId: Int): List<RoutePoint>

    @Insert
    fun insert(routePoint: RoutePoint)

    @Insert
    fun insertRoutePoint(point: RoutePoint): Long

    @Insert
    fun insertAll(routePoints: List<RoutePoint>)

    @Query("DELETE FROM route_points WHERE routeId = :routeId")
    fun deleteRoutePoints(routeId: Int)


}
