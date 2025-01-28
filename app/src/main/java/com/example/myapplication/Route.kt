package com.example.myapplication

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class Route(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,
    val endTime: Long,
    val averageSpeed: Float,
    val coordinates: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readFloat(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeLong(startTime)
        parcel.writeLong(endTime)
        parcel.writeFloat(averageSpeed)
        parcel.writeString(coordinates)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Route> {
        override fun createFromParcel(parcel: Parcel): Route = Route(parcel)
        override fun newArray(size: Int): Array<Route?> = arrayOfNulls(size)
    }
}
