package com.fsacts.go;

public class LocationModel {
    private int id;
    private String location_date;
    private String location_time;
    private String location_latitude;
    private String location_longitude;
    private String location_address;
    private String location_title;
    private String location_note;

    //Constructors
    public LocationModel(int id, String location_date, String location_time, String location_latitude, String location_longitude, String location_address, String location_title, String location_note) {
        this.id = id;
        this.location_title = location_title;
        this.location_date = location_date;
        this.location_time = location_time;
        this.location_latitude = location_latitude;
        this.location_longitude = location_longitude;
        this.location_address = location_address;
        this.location_note = location_note;
    }

    public LocationModel() {
    }

    //getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocation_title() {
        return location_title;
    }

    public void setLocation_title(String location_title) {
        this.location_title = location_title;
    }

    public String getLocation_date() {
        return location_date;
    }

    public void setLocation_date(String location_date) {
        this.location_date = location_date;
    }

    public String getLocation_time() {
        return location_time;
    }

    public void setLocation_time(String location_time) {
        this.location_time = location_time;
    }

    public String getLocation_latitude() {
        return location_latitude;
    }

    public void setLocation_latitude(String location_latitude) {
        this.location_latitude = location_latitude;
    }

    public String getLocation_longitude() {
        return location_longitude;
    }

    public void setLocation_longitude(String location_longitude) {
        this.location_longitude = location_longitude;
    }

    public String getLocation_address() {
        return location_address;
    }

    public void setLocation_address(String location_address) {
        this.location_address = location_address;
    }

    public String getLocation_note() {
        return location_note;
    }

    public void setLocation_note(String location_note) {
        this.location_note = location_note;
    }


    //toString is necessary for printing the contents of a class object
    @Override
    public String toString() {
        return "LocationModel{" +
                "id=" + id +
                ", location_title='" + location_title + '\'' +
                ", location_date='" + location_date + '\'' +
                ", location_time='" + location_time + '\'' +
                ", location_latitude='" + location_latitude + '\'' +
                ", location_longitude='" + location_longitude + '\'' +
                ", location_address='" + location_address + '\'' +
                ", location_note='" + location_note + '\'' +
                '}';
    }
}
