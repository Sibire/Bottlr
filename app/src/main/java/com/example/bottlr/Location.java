public class Location {
    private String timeDateAdded;
    private String gpsCoordinates;

    public Location(String timeDateAdded, String gpsCoordinates) {
        this.timeDateAdded = timeDateAdded;
        this.gpsCoordinates = gpsCoordinates;
    }

    public String getTimeDateAdded() {
        return timeDateAdded;
    }

    public String getGpsCoordinates() {
        return gpsCoordinates;
    }
}