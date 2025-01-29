package com.example.myapplication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface RouteDao {
    @Insert
    fun insertRoute(route: Route): Long

    @Insert
    fun insertPoints(points: List<RoutePoint>)

    @Transaction
    @Query("SELECT * FROM routes WHERE id = :routeId")
    fun getRouteWithPoints(routeId: Int): RouteWithPoints?

    @Query("SELECT * FROM route_points WHERE routeId = :routeId")
    fun getRoutePoints(routeId: Int): List<RoutePoint>

    @Query("SELECT * FROM routes")
    fun getAllRoutes(): List<Route>

    @Query("SELECT * FROM routes WHERE id = :routeId LIMIT 1")
    fun getRouteById(routeId: Int): Route?

    @Insert
    fun insertRoutePoint(routePoint: RoutePoint)

    @Query("UPDATE routes SET startTime = :startTime, endTime = :endTime, averageSpeed = :averageSpeed WHERE id = :routeId")
    suspend fun updateRoute(routeId: Int, startTime: Long, endTime: Long, averageSpeed: Float)

    @Query("SELECT COALESCE(MAX(id), 0) FROM routes")
    fun getLastRouteId(): Int




}


