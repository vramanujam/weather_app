package com.venki.weatherapp.weatherapp.helpers;


public class Helper {

    public static final String MANAGER_LOCATION = "Manager Location";

    public static final String LOCATION_LIST = "Location List";

    public static final String LOCATION_ERROR_MESSAGE = "Input field must be filled";

    public static final String PREFS_TAG = "prefs";

    public static final String STORED_DATA_FIRST = "data_first";

    public static final String STORED_DATA_SECOND = "data_second";

    public static final String IS_DATA_PRESENT = "isData";

    public static final String LOCATION_PREFS = "location_prefs";

    public static final String API_KEY = "";

    public static final String TIMEZONE_API_KEY = "";

    public static String capitalizeFirstLetter(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    public static long convertCelsiusToFahrenheit (long centigrade) {
        long fahrenheit;
        fahrenheit = centigrade * (9/5) + 32;
        return fahrenheit;
    }
}
