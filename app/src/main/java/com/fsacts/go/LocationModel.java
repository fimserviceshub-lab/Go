package com.fsacts.go;

public class LocationModel {
    private final long id;
    private final long capturedAtMillis;
    private final String locationDate;
    private final String locationTime;
    private final double latitude;
    private final double longitude;
    private final String address;
    private final String title;
    private final String note;

    public LocationModel(
            long id,
            long capturedAtMillis,
            String locationDate,
            String locationTime,
            double latitude,
            double longitude,
            String address,
            String title,
            String note
    ) {
        this.id = id;
        this.capturedAtMillis = capturedAtMillis;
        this.locationDate = locationDate;
        this.locationTime = locationTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.title = title;
        this.note = note;
    }

    public long getId() {
        return id;
    }

    public long getCapturedAtMillis() {
        return capturedAtMillis;
    }

    public String getLocationDate() {
        return locationDate;
    }

    public String getLocationTime() {
        return locationTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public String getTitle() {
        return title;
    }

    public String getNote() {
        return note;
    }
}
