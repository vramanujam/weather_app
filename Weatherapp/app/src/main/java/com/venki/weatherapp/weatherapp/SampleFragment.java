package com.venki.weatherapp.weatherapp;

import android.Manifest;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.venki.weatherapp.weatherapp.adapter.CityViewFiveDayAdapter;
import com.venki.weatherapp.weatherapp.adapter.ThreeHourViewAdapter;
import com.venki.weatherapp.weatherapp.database.DatabaseQuery;
import com.venki.weatherapp.weatherapp.entity.CityWeatherData;
import com.venki.weatherapp.weatherapp.helpers.Helper;
import com.venki.weatherapp.weatherapp.helpers.ImageLoader;
import com.venki.weatherapp.weatherapp.json.FiveDaysForecast;
import com.venki.weatherapp.weatherapp.json.FiveWeathers;
import com.venki.weatherapp.weatherapp.json.Forecast;
import com.venki.weatherapp.weatherapp.json.LocationMapObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.joda.time.DateTimeComparator;

public class SampleFragment extends Fragment implements LocationListener {

    private RecyclerView recyclerView;

    private CityViewFiveDayAdapter cityViewFiveDayAdapter;

    private RecyclerView threehourView;

    private ThreeHourViewAdapter threehourViewAdapter;

    private TextView cityCountry;

    private TextView currentDate;

    private ImageView weatherImage;

    private TextView mainTemp;

    private TextView currentWindSpeed;

    private TextView moisture;

    private RequestQueue queue;

    private LocationMapObject locationMapObject;

    private LocationManager locationManager;

    private Location location;

    private final int REQUEST_LOCATION = 200;

    private TextView tempMinMaxView;

    private TextView weatherResultDescription;

    private String degreeMetric;

    private DatabaseQuery query;

    private String apiUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_one, container,
                false);

        queue = Volley.newRequestQueue(getActivity());
        query = new DatabaseQuery(getActivity());
        return rootView;
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        cityCountry = (TextView)getView().findViewById(R.id.city_country);
        currentDate = (TextView)getView().findViewById(R.id.current_date);
        weatherImage = (ImageView)getView().findViewById(R.id.weather_icon);
        mainTemp = (TextView) getView().findViewById(R.id.weather_result);
        currentWindSpeed = (TextView)getView().findViewById(R.id.wind_result);
        moisture = (TextView)getView().findViewById(R.id.humidity_result);
        tempMinMaxView = (TextView) getView().findViewById(R.id.temp_min_max_view);
        weatherResultDescription = (TextView) getView().findViewById(R.id.weather_result_desc);

        locationManager = (LocationManager) getActivity().getSystemService(Service.LOCATION_SERVICE);
        String currentLocation = null;
        /*Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentLocation = extras.getString("city");
            //The key argument here must match that used in the other activity
        }*/
        currentLocation = query.getCityByRowNum(getArguments().getInt("page_position"));
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            if(currentLocation == null){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 2, this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    apiUrl = "http://api.openweathermap.org/data/2.5/weather?lat="+location.getLatitude()+"&lon="+location.getLongitude()+"&APPID="+Helper.API_KEY+"&units=metric";
                    getWeatherData(apiUrl);
                }
            }else{
                // make API call with city name
                System.out.println("Stored city " + currentLocation);
                String[] city = currentLocation.split(",");
                if(!TextUtils.isEmpty(city[0])){
                    System.out.println("Stored city " + city[0]);
                    String url ="http://api.openweathermap.org/data/2.5/weather?q="+city[0]+"&APPID="+Helper.API_KEY+"&units=metric";
                    getWeatherData(url);
                }
            }
        }

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 5,GridLayoutManager.VERTICAL, false);

        recyclerView = (RecyclerView)getView().findViewById(R.id.weather_daily_list);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        threehourView = (RecyclerView)getView().findViewById(R.id.threehour_layout);
        threehourView.setLayoutManager(layoutManager);
        threehourView.setHasFixedSize(true);

    }
    private void getWeatherData(final String apiUrl){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                locationMapObject = gson.fromJson(response, LocationMapObject.class);
                if (null == locationMapObject) {
                    Toast.makeText(getActivity(), "Nothing was returned", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "Response Good", Toast.LENGTH_LONG).show();

                    String city = locationMapObject.getName() + ", " + locationMapObject.getSys().getCountry();
                    if(locationMapObject.getName().equals(query.getCurrentCity()))
                        city += "\n" + "(You are here!)";
                    String todayDate = getTodayDateInStringFormat();
                    Long tempVal = Math.round(Math.floor(Double.parseDouble(locationMapObject.getMain().getTemp())));
                    Long tempMin = Math.round(Math.floor(Double.parseDouble(locationMapObject.getMain().getTemp_min())));
                    Long tempMax = Math.round(Math.floor(Double.parseDouble(locationMapObject.getMain().getTemp_max())));
                    String icon = locationMapObject.getWeather().get(0).getIcon();
                    System.out.println("Current icon is " + icon);

                    System.out.println("Degree preference in weather activity: " + query.getUserDegreeMetric() );

                    degreeMetric = "C";

                    if(query.getUserDegreeMetric().equals("Fahrenheit")) {
                        tempVal = Helper.convertCelsiusToFahrenheit(tempVal);
                        tempMin = Helper.convertCelsiusToFahrenheit(tempMin);
                        tempMax = Helper.convertCelsiusToFahrenheit(tempMax);
                        degreeMetric = "F";
                    }
                    String weatherTemp = String.valueOf(tempVal) + "°"  + degreeMetric;
                    String weatherDescription = Helper.capitalizeFirstLetter(locationMapObject.getWeather().get(0).getDescription());
                    String windSpeed = locationMapObject.getWind().getSpeed();
                    String humidityValue = locationMapObject.getMain().getHumudity();
                    String tempMinMax = "Min Temp: " + String.valueOf(tempMin) + "<sup>o</sup>" + degreeMetric + "," + " " + "Max Temp: " + String.valueOf(tempMax) + "<sup>o</sup>" + degreeMetric + "";


                    //save location in database
                    if(apiUrl.contains("lat")){
                        query.insertNewLocation(locationMapObject.getName());
                    }
                    // populate View data
                    cityCountry.setText(Html.fromHtml(city));
                    currentDate.setText(Html.fromHtml(todayDate));
                    mainTemp.setText(Html.fromHtml(weatherTemp).toString());
                    //circleTitle.setSubtitleText(Html.fromHtml(weatherDescription).toString());
                    weatherResultDescription.setText("(" + Html.fromHtml(weatherDescription).toString() + ")");
                    currentWindSpeed.setText(Html.fromHtml(windSpeed) + " m/s");
                    moisture.setText(Html.fromHtml(humidityValue) + " %");
                    tempMinMaxView.setText(Html.fromHtml(tempMinMax));
                    new ImageLoader("http://openweathermap.org/img/w/" + icon + ".png", weatherImage).execute();
                    getThreeHourAndFiveDaysForecast(locationMapObject.getName());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledAlertToUser();
        }
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                    }
                });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private String getTodayDateInStringFormat(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("E, d MMMM", Locale.getDefault());
        return df.format(c.getTime());
    }

    private void getThreeHourAndFiveDaysForecast(String city){
        String apiUrl = "http://api.openweathermap.org/data/2.5/forecast?q="+city+ "&APPID="+Helper.API_KEY+"&units=metric";
        final List<CityWeatherData> daysOfTheWeek = new ArrayList<CityWeatherData>();
        final List<CityWeatherData> threeHourForecast = new ArrayList<CityWeatherData>();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                Forecast forecast = gson.fromJson(response, Forecast.class);
                if (null == forecast) {
                    Toast.makeText(getActivity().getApplicationContext(), "Nothing was returned", Toast.LENGTH_LONG).show();
                } else {
                    List<FiveWeathers> weatherInfo = forecast.getList();
                    boolean[] flag = new boolean[]{false,false,false,false,false,false,false};
                    if(null != weatherInfo){
                        for(int i = 0; i < weatherInfo.size(); i++){
                            String time = weatherInfo.get(i).getDt_txt();
                            String shortDay = getDay(time);
                            String temp = weatherInfo.get(i).getMain().getTemp();
                            String tempMin = weatherInfo.get(i).getMain().getTemp_min();
                            String tempMaximum = weatherInfo.get(i).getMain().getTemp_max();
                            System.out.println("Fivedays time" + time);

                            if(isCurrentDate(time) == 0){
                                String hour[] = time.split(" ");

                                if(weatherInfo.get(i).getConditions().get(0).getDescription() != null) {
                                    System.out.println("Weather info is null " + weatherInfo.get(i).getConditions().get(0).getDescription());
                                    String icon = weatherInfo.get(i).getConditions().get(0).getIcon();
                                    threeHourForecast.add(new CityWeatherData(hour[1], icon, temp, weatherInfo.get(i).getConditions().get(0).getMain(),tempMaximum));
                                }
                            }

                            if(getDay(time).equals("Mon") && !flag[0]){
                                daysOfTheWeek.add(new CityWeatherData(shortDay, R.drawable.ico_cloud, temp, tempMin,tempMaximum));
                                flag[0] = true;
                            }
                            if(getDay(time).equals("Tue") && !flag[1]){
                                daysOfTheWeek.add(new CityWeatherData(shortDay, R.drawable.ico_cloud, temp, tempMin,tempMaximum));
                                flag[1] = true;
                            }
                            if(getDay(time).equals("Wed") && !flag[2]){
                                daysOfTheWeek.add(new CityWeatherData(shortDay, R.drawable.ico_cloud, temp, tempMin,tempMaximum));
                                flag[2] = true;
                            }
                            if(getDay(time).equals("Thu") && !flag[3]){
                                daysOfTheWeek.add(new CityWeatherData(shortDay, R.drawable.ico_cloud, temp, tempMin,tempMaximum));
                                flag[3] = true;
                            }
                            if(getDay(time).equals("Fri") && !flag[4]){
                                daysOfTheWeek.add(new CityWeatherData(shortDay, R.drawable.ico_cloud, temp, tempMin,tempMaximum));
                                flag[4] = true;
                            }
                            if(getDay(time).equals("Sat") && !flag[5]){
                                daysOfTheWeek.add(new CityWeatherData(shortDay, R.drawable.ico_cloud, temp, tempMin,tempMaximum));
                                flag[5] = true;
                            }
                            if(getDay(time).equals("Sun") && !flag[6]){
                                daysOfTheWeek.add(new CityWeatherData(shortDay, R.drawable.ico_cloud, temp, tempMin,tempMaximum));
                                flag[6] = true;
                            }
                            cityViewFiveDayAdapter = new CityViewFiveDayAdapter(getActivity(), daysOfTheWeek);
                            recyclerView.setAdapter(cityViewFiveDayAdapter);

                            threehourViewAdapter = new ThreeHourViewAdapter(getActivity(), threeHourForecast);
                            threehourView.setAdapter(threehourViewAdapter);
                        }
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                
            }
        });
        queue.add(stringRequest);
    }

    private String getDay(String time){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:SSSS", Locale.getDefault());
        String days = "";
        try {
            Date date = format.parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            days = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return days;
    }

    public int isCurrentDate(String time)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:SSSS", Locale.getDefault());
        try {
            Date inputdate = format.parse(time);
            Date currentDate = format.parse(format.format(new Date() ));
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);
            cal.add(Calendar.DATE, 1);
            return DateTimeComparator.getDateOnlyInstance().compare(inputdate,cal.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }
}