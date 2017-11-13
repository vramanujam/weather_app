package com.venki.weatherapp.weatherapp;

import android.Manifest;
import android.app.Service;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.venki.weatherapp.weatherapp.adapter.CityListAdapter;
import com.venki.weatherapp.weatherapp.database.DatabaseQuery;
import com.venki.weatherapp.weatherapp.entity.DatabaseLocationObject;
import com.venki.weatherapp.weatherapp.entity.LocationObject;
import com.venki.weatherapp.weatherapp.helpers.Helper;
import com.venki.weatherapp.weatherapp.json.LocationMapObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private List<DatabaseLocationObject> locationData;

    private List<LocationObject> completeCityWeatherData;

    private CityListAdapter cityListAdapter;

    private LocationMapObject jsonLocationObject;

    private RecyclerView locationRecyclerView;

    private DatabaseQuery dbQuery;

    private RequestQueue requestQueue;

    private LocationManager locationManager;

    private Location location;
	
	private String degreeMetric;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        setTitle("City List View");

        requestQueue = Volley.newRequestQueue(MainActivity.this);
        completeCityWeatherData = new ArrayList<LocationObject>();

        //query = new DatabaseQuery(MainActivity.this);
        //query.insertNewLocation("chennai");
        dbQuery = new DatabaseQuery(MainActivity.this);

        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 2, this);
            if (locationManager != null) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                String apiUrl = "http://api.openweathermap.org/data/2.5/weather?lat="+location.getLatitude()+"&lon="+location.getLongitude()+"&APPID="+Helper.API_KEY+"&units=metric";
                updateLocationinDB(apiUrl);
            }
        }


        //query.deleteAllLocationContent();
        locationData = dbQuery.getStoredDataLocations();

        if(null != locationData){
            for(int i = 0; i < locationData.size(); i++){
                // make volley network call here
                System.out.println("Response printing " + locationData.get(i).getLocation());
                System.out.println("row id:"+dbQuery.getRowNumber(locationData.get(i).getLocation()));
                System.out.println("City by rownum " + dbQuery.getCityByRowNum(dbQuery.getRowNumber(locationData.get(i).getLocation())));
                requestJsonObject(locationData.get(i));
            }
        }

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.add_location);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                completeCityWeatherData = new ArrayList<LocationObject>();
                dbQuery = new DatabaseQuery(MainActivity.this);
                if(dbQuery.insertNewLocation(place.getName().toString()))
                {
                    dbQuery = new DatabaseQuery(MainActivity.this);
                    locationData = dbQuery.getStoredDataLocations();

                    if(null != locationData){
                        for(int i = 0; i < locationData.size(); i++){
                            System.out.println("Response printing " + locationData.get(i).getLocation());
                            requestJsonObject(locationData.get(i));
                        }
                    }
                }
                else
                    Toast.makeText(MainActivity.this, " Location already exists", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(Status status) {
            }
        });
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .build();

        autocompleteFragment.setFilter(typeFilter);

        final Button button = findViewById(R.id.add_current_location);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                completeCityWeatherData = new ArrayList<LocationObject>();
                dbQuery = new DatabaseQuery(MainActivity.this);
                if(dbQuery.insertNewLocation(dbQuery.getCurrentCity()))
                {
                    dbQuery = new DatabaseQuery(MainActivity.this);
                    locationData = dbQuery.getStoredDataLocations();

                    if(null != locationData){
                        for(int i = 0; i < locationData.size(); i++){
                            System.out.println("Response printing " + locationData.get(i).getLocation());
                            requestJsonObject(locationData.get(i));
                        }
                    }
                }
                else
                    Toast.makeText(MainActivity.this, " Location already exists", Toast.LENGTH_LONG).show();
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        locationRecyclerView = (RecyclerView) findViewById(R.id.location_list);
        locationRecyclerView.setLayoutManager(linearLayoutManager);
    }
	
	//--
	 @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
         int current_selected_id = R.id.menu_centigrade;
         if(dbQuery.getUserDegreeMetric().equals("Fahrenheit"))
             current_selected_id = R.id.menu_fahrenheit ;
         MenuItem refresh = menu.findItem(current_selected_id);
         refresh.setChecked(true);
         //refresh.setEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_fahrenheit:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                dbQuery.insertUserDegreeMetric("Fahrenheit");

                completeCityWeatherData = new ArrayList<LocationObject>();
                dbQuery = new DatabaseQuery(MainActivity.this);
                locationData = dbQuery.getStoredDataLocations();

                if (null != locationData) {
                    for (int i = 0; i < locationData.size(); i++) {
                        // make volley network call here
                        System.out.println("Response printing " + locationData.get(i).getLocation());
                        requestJsonObject(locationData.get(i));
                    }
                }
                return true;
            case R.id.menu_centigrade:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                dbQuery.insertUserDegreeMetric("Centigrade");

                completeCityWeatherData = new ArrayList<LocationObject>();
                dbQuery = new DatabaseQuery(MainActivity.this);
                locationData = dbQuery.getStoredDataLocations();

                if (null != locationData) {
                    for (int i = 0; i < locationData.size(); i++) {
                        // make volley network call here
                        System.out.println("Response printing " + locationData.get(i).getLocation());
                        requestJsonObject(locationData.get(i));
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	//--
    @Override
    public void onLocationChanged(Location location) {

    }

    private void updateLocationinDB(String  url){
        //String url ="http://api.openweathermap.org/data/2.5/weather?q="+paramValue.getLocation()+"&APPID="+ Helper.API_KEY+"&units=metric";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("MainActivity", "Response " + response);
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                jsonLocationObject = gson.fromJson(response, LocationMapObject.class);
                if (null == jsonLocationObject) {
                    Toast.makeText(getApplicationContext(), "Nothing was returned", Toast.LENGTH_LONG).show();
                } else {
                    String city = jsonLocationObject.getName() /*+ ", " + locationMapObject.getSys().getCountry()*/;
                    dbQuery.insertCurrentLocation(city);
                    System.out.println("Got the current Location as " + dbQuery.getCurrentCity());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("MainActivity", "Error " + error.getMessage());
            }
        });
        requestQueue.add(stringRequest);
    }

    private void requestJsonObject(final DatabaseLocationObject paramValue){
        String url ="http://api.openweathermap.org/data/2.5/weather?q="+paramValue.getLocation()+"&APPID="+ Helper.API_KEY+"&units=metric";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("MainActivity", "Response " + response);
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                jsonLocationObject = gson.fromJson(response, LocationMapObject.class);
                if (null == jsonLocationObject) {
                    Toast.makeText(getApplicationContext(), "Nothing was returned", Toast.LENGTH_LONG).show();
                } else {
                    final int rowId = paramValue.getId();
                    Long tempVal = Math.round(Math.floor(Double.parseDouble(jsonLocationObject.getMain().getTemp())));
                    Long tempMin = Math.round(Math.floor(Double.parseDouble(jsonLocationObject.getMain().getTemp_min())));
                    Long tempMax = Math.round(Math.floor(Double.parseDouble(jsonLocationObject.getMain().getTemp_max())));


                    System.out.println("Degree preference" + dbQuery.getUserDegreeMetric());

                    degreeMetric = "C";

                    if(dbQuery.getUserDegreeMetric().equals("Fahrenheit")) {
                        System.out.println("Degree preference inside if" + dbQuery.getUserDegreeMetric());
                        tempVal = Helper.convertCelsiusToFahrenheit(tempVal);
                        tempMin = Helper.convertCelsiusToFahrenheit(tempMin);
                        tempMax = Helper.convertCelsiusToFahrenheit(tempMax);
                        degreeMetric = "F";
                    }

                    final String city = jsonLocationObject.getName() + ", " + jsonLocationObject.getSys().getCountry();
                    final String weatherInfo = String.valueOf(tempVal) + "<sup>o</sup>" + degreeMetric + ", " + Helper.capitalizeFirstLetter(jsonLocationObject.getWeather().get(0).getDescription());
                    final String tempMinMax = "Min Temp: " + String.valueOf(tempMin) + "<sup>o</sup>" + degreeMetric + "," + " " + "Max Temp: " + String.valueOf(tempMax) + "<sup>o</sup>" + degreeMetric + "";

                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    final long finalTs = timestamp.getTime()/1000;

                    String loc = jsonLocationObject.getCoord().getLat() + "," + jsonLocationObject.getCoord().getLon();

                    String google_url = "https://maps.googleapis.com/maps/api/timezone/json?location=" + loc + "&timestamp=" + finalTs + "&key=" + Helper.TIMEZONE_API_KEY;

                    JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, google_url, (String)null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("MainActivity", "Response " + response);
                            System.out.println("Response Object" + response);
                            try {
                                // to be fine-tuned by Smitha
                                String timeZoneId = response.getString("timeZoneId");

                                Date currentTime = new Date();
                                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm");
                                sdf.setTimeZone(TimeZone.getTimeZone(timeZoneId));
                                String timeToDisplay = sdf.format(currentTime);
                                completeCityWeatherData.add(new LocationObject(rowId, city, weatherInfo, tempMinMax, timeToDisplay));

                                //to be modified - venkatesh
                                cityListAdapter = new CityListAdapter(MainActivity.this, completeCityWeatherData);
                                locationRecyclerView.setAdapter(cityListAdapter);


                            }
                            catch(JSONException e){
                                Log.e("Android Weather App", "Unexpected JSON exception", e);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("MainActivity", "Error " + error.getMessage());
                        }
                    });
                    requestQueue.add(jsonRequest);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("MainActivity", "Error " + error.getMessage());
            }
        });
        requestQueue.add(stringRequest);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {


    }

}
