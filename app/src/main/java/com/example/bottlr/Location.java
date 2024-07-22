package com.example.bottlr;

public class Location {
    private String timeDateAdded;
    private String gpsCoordinates;
    private String name;

    public Location(String timeDateAdded, String gpsCoordinates) {
        this.timeDateAdded = timeDateAdded;
        this.gpsCoordinates = gpsCoordinates;
        this.name = "Placeholder Place Name";
    }

    public String getTimeDateAdded() {
        return timeDateAdded;
    }

    public String getGpsCoordinates() {
        return gpsCoordinates;
    }

    public String getName() {
        return name;
    }
}