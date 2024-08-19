package com.example.bottlr;

import android.content.Context;
import android.location.LocationManager;
import android.util.Log;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Location {
    private String timeDateAdded;
    private String gpsCoordinates;
    private String name;
    private Context context; // Add context as a member variable

    // Existing constructor
    public Location(Context context) {
        this.context = context; // Initialize context
    }

    // New constructor to fix the issue
    public Location(String timeDateAdded, String gpsCoordinates, String name) {
        this.timeDateAdded = timeDateAdded;
        this.gpsCoordinates = gpsCoordinates;
        this.name = name;
    }

    public String getTimeDateAdded() {
        return timeDateAdded;
    }

    public void setTimeDateAdded(String timeDateAdded) {
        this.timeDateAdded = timeDateAdded;
    }

    public String getGpsCoordinates() {
        return gpsCoordinates;
    }

    public void setGpsCoordinates(String gpsCoordinates) {
        this.gpsCoordinates = gpsCoordinates;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocationTimeStamp() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    public String getLocationName() {
        return "Test Location Name";
        // TODO: Geocoder decoding and name population
    }

    public String getLocationCoordinates() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            android.location.Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                double latitude = truncateCoordinate(location.getLatitude());
                Log.d("Location Coords", "Raw Latitude: " + location.getLatitude());
                Log.d("Location Coords", "Truncated Latitude: " + latitude);
                double longitude = truncateCoordinate(location.getLongitude());
                Log.d("Location Coords", "Raw Longitude: " + location.getLongitude());
                Log.d("Location Coords", "Truncated Longitude: " + longitude);
                return latitude + ", " + longitude;
            } else {
                return "Location not available";
            }
        } catch (SecurityException e) {
            return "Permissions not granted";
        }
    }
    private double truncateCoordinate(double coordinate) {
        DecimalFormat df = new DecimalFormat("#.#####");
        return Double.parseDouble(df.format(coordinate));
    }
}