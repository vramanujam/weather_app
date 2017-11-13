package com.venki.weatherapp.weatherapp.entity;


public class WeatherObject {

    private String dayOfWeek;

    private int weatherIcon;

    private String weatherResult;

    private String weatherResultSmall;

    private String weatherResultBig;

    private String weatherUrl;

    public WeatherObject(String dayOfWeek, int weatherIcon, String weatherResult, String weatherResultSmall, String weatherResultBig) {
        this.dayOfWeek = dayOfWeek;
        this.weatherIcon = weatherIcon;
        this.weatherResult = weatherResult;
        this.weatherResultSmall = weatherResultSmall;
        this.weatherResultBig = weatherResultBig;
    }

    public WeatherObject(String dayOfWeek, String weatherIcon, String weatherResult, String weatherResultSmall, String weatherResultBig) {
        this.dayOfWeek = dayOfWeek;
        this.weatherUrl = weatherIcon;
        this.weatherResult = weatherResult;
        this.weatherResultSmall = weatherResultSmall;
        this.weatherResultBig = weatherResultBig;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public int getWeatherIcon() {
        return weatherIcon;
    }

    public String getWeatherResult() {
        return weatherResult;
    }

    public String getWeatherResultSmall() {
        return weatherResultSmall;
    }

    public String getWeatherResultBig() {
        return weatherResultBig;
    }

    public String getWeatherUrl(){ return weatherUrl;}
}
