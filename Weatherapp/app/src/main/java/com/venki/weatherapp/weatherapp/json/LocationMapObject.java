package com.venki.weatherapp.weatherapp.json;


import java.util.List;

public class LocationMapObject {

    private Coord coord;

    private List<WeatherResults> weather;

    private Main main;

    private Wind wind;

    private Sys sys;

    private String id;

    private String name;


    public LocationMapObject(Coord coord, List<WeatherResults> weather, String base, Main main, String visibility, Wind wind, Rain rain, Clouds clouds, String dt, Sys sys, String id, String name, String cod) {
        this.coord = coord;
        this.weather = weather;
        this.main = main;
        this.wind = wind;
        this.sys = sys;
        this.id = id;
        this.name = name;
    }

    public Coord getCoord() {
        return coord;
    }

    public List<WeatherResults> getWeather() {
        return weather;
    }

    public Main getMain() {
        return main;
    }

    public Wind getWind() {
        return wind;
    }

    public Sys getSys() {
        return sys;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
