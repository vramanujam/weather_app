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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.pavlospt.CircleView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.venki.weatherapp.weatherapp.adapter.RecyclerViewAdapter;
import com.venki.weatherapp.weatherapp.adapter.SpacesItemDecoration;
import com.venki.weatherapp.weatherapp.adapter.ThreedayViewAdapter;
import com.venki.weatherapp.weatherapp.database.DatabaseQuery;
import com.venki.weatherapp.weatherapp.entity.WeatherObject;
import com.venki.weatherapp.weatherapp.helpers.CustomSharedPreference;
import com.venki.weatherapp.weatherapp.helpers.Helper;
import com.venki.weatherapp.weatherapp.helpers.ImageLoadTask;
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
import android.view.View;
import android.view.ViewGroup;

import org.joda.time.DateTimeComparator;
import org.joda.time.DateTimeUtils;

public class SampleFragment extends Fragment implements LocationListener {
    private static final String TAG = WeatherActivity.class.getSimpleName();

    private RecyclerView recyclerView;

    private RecyclerViewAdapter recyclerViewAdapter;

    private RecyclerView threehourView;

    private ThreedayViewAdapter threehourViewAdapter;

    private TextView cityCountry;

    private TextView currentDate;

    private ImageView weatherImage;

    private TextView circleTitle;

    private TextView windResult;

    private TextView humidityResult;

    private RequestQueue queue;

    private LocationMapObject locationMapObject;

    private LocationManager locationManager;

    private Location location;

    private final int REQUEST_LOCATION = 200;

    private CustomSharedPreference sharedPreference;


    private TextView tempMinMaxView;

    private TextView weatherResultDescription;

    private String degreeMetric;

    //private String isLocationSaved;

    private DatabaseQuery query;

    private String apiUrl;

    private FiveDaysForecast fiveDaysForecast;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_one, container,
                false);

        queue = Volley.newRequestQueue(getActivity());
        query = new DatabaseQuery(getActivity());
        sharedPreference = new CustomSharedPreference(getActivity());
        //isLocationSaved = sharedPreference.getLocationInPreference();


        return rootView;
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        cityCountry = (TextView)getView().findViewById(R.id.city_country);
        currentDate = (TextView)getView().findViewById(R.id.current_date);
        weatherImage = (ImageView)getView().findViewById(R.id.weather_icon);
        circleTitle = (TextView) getView().findViewById(R.id.weather_result);
        windResult = (TextView)getView().findViewById(R.id.wind_result);
        humidityResult = (TextView)getView().findViewById(R.id.humidity_result);
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
                // make API call with longitude and latitude
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 2, this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    apiUrl = "http://api.openweathermap.org/data/2.5/weather?lat="+location.getLatitude()+"&lon="+location.getLongitude()+"&APPID="+Helper.API_KEY+"&units=metric";
                    makeJsonObject(apiUrl);
                }
            }else{
                // make API call with city name
                //String storedCityName = sharedPreference.getLocationInPreference();
                //String storedCityName = "Enugu";
                System.out.println("Stored city " + currentLocation);
                String[] city = currentLocation.split(",");
                if(!TextUtils.isEmpty(city[0])){
                    System.out.println("Stored city " + city[0]);
                    String url ="http://api.openweathermap.org/data/2.5/weather?q="+city[0]+"&APPID="+Helper.API_KEY+"&units=metric";
                    makeJsonObject(url);
                }
            }
        }

        /*ImageButton addLocation = (ImageButton) findViewById(R.id.add_location);
        addLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addLocationIntent = new Intent(WeatherActivity.this, AddLocationActivity.class);
                startActivity(addLocationIntent);
            }
        });*/

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 5,GridLayoutManager.VERTICAL, false);

        recyclerView = (RecyclerView)getView().findViewById(R.id.weather_daily_list);
        recyclerView.setLayoutManager(gridLayoutManager);
        //recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true));
        recyclerView.setHasFixedSize(true);


        GridLayoutManager threehourgrid = new GridLayoutManager(getActivity(), 1,GridLayoutManager.HORIZONTAL, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen._1sdp);
        threehourView = (RecyclerView)getView().findViewById(R.id.threehour_layout);
        threehourView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        threehourView.setLayoutManager(layoutManager);
        threehourView.setHasFixedSize(true);
        /*threehourView = (RecyclerView)getView().findViewById(R.id.threehour_layout);
        threehourView.setLayoutManager(gridLayoutManager);
        //recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true));
        threehourView.setHasFixedSize(true);*/


    }
    private void makeJsonObject(final String apiUrl){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response " + response);
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
                    circleTitle.setText(Html.fromHtml(weatherTemp).toString());
                    //circleTitle.setSubtitleText(Html.fromHtml(weatherDescription).toString());
                    weatherResultDescription.setText("(" + Html.fromHtml(weatherDescription).toString() + ")");
                    windResult.setText(Html.fromHtml(windSpeed) + " m/s");
                    humidityResult.setText(Html.fromHtml(humidityValue) + " %");
                    tempMinMaxView.setText(Html.fromHtml(tempMinMax));
                    new ImageLoadTask("http://openweathermap.org/img/w/" + icon + ".png", weatherImage).execute();
                    fiveDaysApiJsonObjectCall(locationMapObject.getName());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error " + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //make api call
                    /*locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 2, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        apiUrl = "http://api.openweathermap.org/data/2.5/weather?lat="+location.getLatitude()+"&lon="+location.getLongitude()+"&APPID="+Helper.API_KEY+"&units=metric";
                        makeJsonObject(apiUrl);
                    }else{
                        apiUrl = "http://api.openweathermap.org/data/2.5/weather?lat=51.5074&lon=0.1278&APPID="+Helper.API_KEY+"&units=metric";
                        makeJsonObject(apiUrl);
                    }*/
                }
            }else{
                Toast.makeText(getActivity(), getString(R.string.permission_notice), Toast.LENGTH_LONG).show();
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

    private void fiveDaysApiJsonObjectCall(String city){
        String apiUrl = "http://api.openweathermap.org/data/2.5/forecast?q="+city+ "&APPID="+Helper.API_KEY+"&units=metric";
        final List<WeatherObject> daysOfTheWeek = new ArrayList<WeatherObject>();
        final List<WeatherObject> threeHourForecast = new ArrayList<WeatherObject>();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response 5 days" + response);
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                Forecast forecast = gson.fromJson(response, Forecast.class);
                if (null == forecast) {
                    Toast.makeText(getActivity().getApplicationContext(), "Nothing was returned", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Response Good", Toast.LENGTH_LONG).show();

                    int[] everyday = new int[]{0,0,0,0,0,0,0};

                    List<FiveWeathers> weatherInfo = forecast.getList();
                    if(null != weatherInfo){
                        for(int i = 0; i < weatherInfo.size(); i++){
                            String time = weatherInfo.get(i).getDt_txt();
                            String shortDay = convertTimeToDay(time);
                            String temp = weatherInfo.get(i).getMain().getTemp();
                            String tempMin = weatherInfo.get(i).getMain().getTemp_min();
                            String tempMaximum = weatherInfo.get(i).getMain().getTemp_max();
                            System.out.println("Fivedays time" + time);

                            if(isCurrentDate(time) == 0){
                                String hour[] = time.split(" ");

                                if(weatherInfo.get(i).getConditions().get(0).getDescription() != null)
                                    System.out.println("Weather info is null " + weatherInfo.get(i).getConditions().get(0).getDescription());
                                String icon = weatherInfo.get(i).getConditions().get(0).getIcon();
                                threeHourForecast.add(new WeatherObject(hour[1], icon, temp, weatherInfo.get(i).getConditions().get(0).getMain(),tempMaximum));
                                /*System.out.println("Current Day");
                                System.out.println("time " + time);
                                System.out.println("temp "+ temp);*/
                            }

                            if(convertTimeToDay(time).equals("Mon") && everyday[0] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin,tempMaximum));
                                everyday[0] = 1;
                            }
                            if(convertTimeToDay(time).equals("Tue") && everyday[1] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin,tempMaximum));
                                everyday[1] = 1;
                            }
                            if(convertTimeToDay(time).equals("Wed") && everyday[2] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin,tempMaximum));
                                everyday[2] = 1;
                            }
                            if(convertTimeToDay(time).equals("Thu") && everyday[3] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin,tempMaximum));
                                everyday[3] = 1;
                            }
                            if(convertTimeToDay(time).equals("Fri") && everyday[4] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin,tempMaximum));
                                everyday[4] = 1;
                            }
                            if(convertTimeToDay(time).equals("Sat") && everyday[5] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin,tempMaximum));
                                everyday[5] = 1;
                            }
                            if(convertTimeToDay(time).equals("Sun") && everyday[6] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin,tempMaximum));
                                everyday[6] = 1;
                            }
                            recyclerViewAdapter = new RecyclerViewAdapter(getActivity(), daysOfTheWeek);
                            recyclerView.setAdapter(recyclerViewAdapter);

                            threehourViewAdapter = new ThreedayViewAdapter(getActivity(), threeHourForecast);
                            threehourView.setAdapter(threehourViewAdapter);
                        }
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error " + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    private String convertTimeToDay(String time){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:SSSS", Locale.getDefault());
        String days = "";
        try {
            Date date = format.parse(time);
            //System.out.println("Our time " + date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            days = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
            //System.out.println("Our time " + days);
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
            //System.out.println("isCurrentDate");
            System.out.println(inputdate.toString() + " " + currentDate.toString());
            return DateTimeComparator.getDateOnlyInstance().compare(inputdate,cal.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }
}