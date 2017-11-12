package com.venki.weatherapp.weatherapp.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.venki.weatherapp.weatherapp.entity.ListJsonObject;

import java.lang.reflect.Type;
import java.util.List;

public class CustomSharedPreference {

    private SharedPreferences sharedPref;

    private Gson gson;

    public CustomSharedPreference(Context context) {
        sharedPref = context.getSharedPreferences(Helper.PREFS_TAG, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void setDataFromSharedPreferences(String key, List<ListJsonObject> listObjects) {
        String json = gson.toJson(listObjects);
        sharedPref.edit().putString(key, json).apply();
    }

    public List<ListJsonObject> getAllDataObject(String key){
        String stringObjects = sharedPref.getString(key, "");
        Type type = new TypeToken<List<ListJsonObject>>(){}.getType();
        return gson.fromJson(stringObjects, type);
    }

    public void setDataSourceIfPresent(boolean isData){
        sharedPref.edit().putBoolean(Helper.IS_DATA_PRESENT, isData).apply();
    }

    public boolean getDataSourceIfPresent(){
        return sharedPref.getBoolean(Helper.IS_DATA_PRESENT, false);
    }

    public void setLocationInPreference(String cityName){
        sharedPref.edit().putString(Helper.LOCATION_PREFS, cityName).apply();
    }

    public String getLocationInPreference(){
        return sharedPref.getString(Helper.LOCATION_PREFS, "");
    }

    public static class Helper {

        public static final String MANAGER_LOCATION = "Manager Location";

        public static final String LOCATION_LIST = "Location List";

        public static final String LOCATION_ERROR_MESSAGE = "Input field must be filled";

        public static final String PREFS_TAG = "prefs";

        public static final String STORED_DATA_FIRST = "data_first";

        public static final String STORED_DATA_SECOND = "data_second";

        public static final String IS_DATA_PRESENT = "isData";

        public static final String LOCATION_PREFS = "location_prefs";

        public static final String API_KEY = "";

        public static String capitalizeFirstLetter(String original) {
            if (original == null || original.length() == 0) {
                return original;
            }
            return original.substring(0, 1).toUpperCase() + original.substring(1);
        }
    }
}
