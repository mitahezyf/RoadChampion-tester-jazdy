package com.example.myapplication

import android.os.Parcelable
import androidx.room.Entity

import androidx.room.PrimaryKey

import kotlinx.parcelize.Parcelize



@Parcelize
@Entity(tableName = "routes")
data class Route(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,
    val endTime: Long,
    val averageSpeed: Float,

) : Parcelable