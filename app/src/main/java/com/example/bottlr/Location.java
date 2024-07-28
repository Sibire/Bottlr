package com.example.bottlr;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
        final String[] coordinates = new String[1];
        coordinates[0] = "Not available";

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(android.location.Location location) {
                coordinates[0] = location.getLatitude() + ", " + location.getLongitude();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(@NonNull String provider) {}
            public void onProviderDisabled(@NonNull String provider) {}
        };

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }

        return coordinates[0];
    }
}