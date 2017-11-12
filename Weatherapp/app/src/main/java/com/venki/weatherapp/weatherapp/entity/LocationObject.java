package com.venki.weatherapp.weatherapp.entity;


public class LocationObject {

    private int id;

    private String locationCity;

    private String weatherInformation;

    private String temp_min_max;

    private String currTime;

    public LocationObject(int id, String locationCity, String weatherInformation, String temp_min_max, String timeToDisplay) {
        this.id = id;
        this.locationCity = locationCity;
        this.weatherInformation = weatherInformation;
        this.temp_min_max = temp_min_max;
        this.currTime = timeToDisplay;
    }

    public String getLocationCity() {
        return locationCity;
    }

    public String getWeatherInformation() {
        return weatherInformation;
    }

    public  String getTempMinMax() {return temp_min_max;}

    public  String getCurrTime() {return currTime;}

    public int getId() {
        return id;
    }
}
