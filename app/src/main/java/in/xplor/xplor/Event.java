package in.xplor.xplor;

public class Event {

    private String title, description;
    private long start, finish;
    private double latitude, longitude;

    private String venue;

    public Event() {

    }

    public Event(String title, String description, long start, long finish, double latitude, double longitude, String venue) {
        this.title = title;
        this.description = description;
        this.start = start;
        this.finish = finish;
        this.latitude = latitude;
        this.longitude = longitude;
        this.venue = venue;
    }

    public void setfinish(long finish) {
        this.finish = finish;
    }

    public void setstart(long start) {
        this.start = start;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getTitle() { return title; }
    public String getDescription() {
        return description;
    }
    public long getStart() {
        return start;
    }
    public long getFinish() {
        return finish;
    }
    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }

    public String getVenue() {
        return venue;
    }
}