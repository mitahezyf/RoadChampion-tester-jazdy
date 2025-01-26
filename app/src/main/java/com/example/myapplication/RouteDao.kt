package com.example.myapplication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RouteDao {

    @Insert
    suspend fun insertRoute(route: Route): Long

    @Query("SELECT * FROM routes ORDER BY startTime DESC")
    suspend fun getAllRoutes(): List<Route>
}
