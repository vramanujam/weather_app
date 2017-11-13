package com.venki.weatherapp.weatherapp.entity;

public class DbLocationObj {

    private int id;

    private String location;

    public DbLocationObj(int id, String location) {
        this.id = id;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}