package com.bottlr.app

import android.content.Context
import android.location.LocationManager
import android.util.Log
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Location {
    var timeDateAdded: String? = null
    var gpsCoordinates: String? = null
    var name: String? = null
    private var context: Context? = null

    constructor(context: Context) {
        this.context = context
    }

    constructor(timeDateAdded: String, gpsCoordinates: String, name: String) {
        this.timeDateAdded = timeDateAdded
        this.gpsCoordinates = gpsCoordinates
        this.name = name
    }

    fun getLocationTimeStamp(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    fun getLocationName(): String {
        return "Test Location Name"
        // TODO: Geocoder decoding and name population
    }

    fun getLocationCoordinates(): String {
        val localContext = context ?: return "Context not available"
        val locationManager = localContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            @Suppress("MissingPermission")
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                val latitude = truncateCoordinate(location.latitude)
                Log.d("Location Coords", "Raw Latitude: " + location.latitude)
                Log.d("Location Coords", "Truncated Latitude: " + latitude)
                val longitude = truncateCoordinate(location.longitude)
                Log.d("Location Coords", "Raw Longitude: " + location.longitude)
                Log.d("Location Coords", "Truncated Longitude: " + longitude)
                "$latitude, $longitude"
            } else {
                "Location not available"
            }
        } catch (e: SecurityException) {
            "Permissions not granted"
        }
    }

    private fun truncateCoordinate(coordinate: Double): Double {
        val df = DecimalFormat("#.#####")
        return df.format(coordinate).toDouble()
    }
}