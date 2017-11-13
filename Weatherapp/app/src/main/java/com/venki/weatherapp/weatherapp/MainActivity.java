package com.venki.weatherapp.weatherapp;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
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
import android.widget.ImageButton;
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
import com.venki.weatherapp.weatherapp.adapter.LocationAdapter;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private DatabaseQuery query;

    private List<DatabaseLocationObject> allLocations;

    private LocationObject locationObject;

    private LocationMapObject locationMapObject;

    private RequestQueue queue;

    private List<LocationObject> allData;

    private LocationAdapter locationAdapter;

    private RecyclerView locationRecyclerView;

    private LocationManager locationManager;

    private String provider;

    private Location location;
	
	private String degreeMetric;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        setTitle(Helper.LOCATION_LIST);

        queue = Volley.newRequestQueue(MainActivity.this);
        allData = new ArrayList<LocationObject>();

        //query = new DatabaseQuery(MainActivity.this);
        //query.insertNewLocation("chennai");
        query = new DatabaseQuery(MainActivity.this);

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
        allLocations = query.getStoredDataLocations();

        if(null != allLocations){
            for(int i = 0; i < allLocations.size(); i++){
                // make volley network call here
                System.out.println("Response printing " + allLocations.get(i).getLocation());
                System.out.println("row id:"+query.getRowNumber(allLocations.get(i).getLocation()));
                System.out.println("City by rownum " + query.getCityByRowNum(query.getRowNumber(allLocations.get(i).getLocation())));
                requestJsonObject(allLocations.get(i));
            }
        }

        Toast.makeText(MainActivity.this, "Count number of locations " + allLocations.size(), Toast.LENGTH_LONG).show();

//        ImageButton addLocation = (ImageButton) findViewById(R.id.add_location);
        /*addLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //venkatesh - to be modified
                //Intent addLocationIntent = new Intent(MainActivity.this, AddLocationActivity.class);
                //startActivity(addLocationIntent);
            }
        });*/

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.add_location);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                allData = new ArrayList<LocationObject>();
                query = new DatabaseQuery(MainActivity.this);
                //String city[] = query.getCurrentCity().split(",");
                /*if(place.getName().toString().equals(city[0])){
                    System.out.println("This is Current City");
                }
                else
                    System.out.println("City entered "+ place.getName().toString());*/
                if(query.insertNewLocation(place.getName().toString()))
                {
                    query = new DatabaseQuery(MainActivity.this);
                    allLocations = query.getStoredDataLocations();

                    if(null != allLocations){
                        for(int i = 0; i < allLocations.size(); i++){
                            System.out.println("Response printing " + allLocations.get(i).getLocation());
                            requestJsonObject(allLocations.get(i));
                        }
                    }

                    Toast.makeText(MainActivity.this, "Count number of locations " + allLocations.size(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        //query.createTablesRequired();
        //query.insertCurrentLocation("Dubai, In");
        //query = new DatabaseQuery(MainActivity.this);
        //System.out.println("current city is" + query.getCurrentCity());
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .build();

        autocompleteFragment.setFilter(typeFilter);

        final Button button = findViewById(R.id.add_current_location);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button allData = new ArrayList<LocationObject>();

                allData = new ArrayList<LocationObject>();
                query = new DatabaseQuery(MainActivity.this);
                if(query.insertNewLocation(query.getCurrentCity()))
                {
                    query = new DatabaseQuery(MainActivity.this);
                    allLocations = query.getStoredDataLocations();

                    if(null != allLocations){
                        for(int i = 0; i < allLocations.size(); i++){
                            System.out.println("Response printing " + allLocations.get(i).getLocation());
                            requestJsonObject(allLocations.get(i));
                        }
                    }

                    Toast.makeText(MainActivity.this, "Count number of locations " + allLocations.size(), Toast.LENGTH_LONG).show();
                }
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
         if(query.getUserDegreeMetric().equals("Fahrenheit"))
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
                query.insertUserDegreeMetric("Fahrenheit");

                allData = new ArrayList<LocationObject>();
                query = new DatabaseQuery(MainActivity.this);
                allLocations = query.getStoredDataLocations();

                if (null != allLocations) {
                    for (int i = 0; i < allLocations.size(); i++) {
                        // make volley network call here
                        System.out.println("Response printing " + allLocations.get(i).getLocation());
                        requestJsonObject(allLocations.get(i));
                    }
                }
                return true;
            case R.id.menu_centigrade:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                query.insertUserDegreeMetric("Centigrade");

                allData = new ArrayList<LocationObject>();
                query = new DatabaseQuery(MainActivity.this);
                allLocations = query.getStoredDataLocations();

                if (null != allLocations) {
                    for (int i = 0; i < allLocations.size(); i++) {
                        // make volley network call here
                        System.out.println("Response printing " + allLocations.get(i).getLocation());
                        requestJsonObject(allLocations.get(i));
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
        //You had this as int. It is advised to have Lat/Loing as double.
        /*double lat = location.getLatitude();
        double lng = location.getLongitude();

        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        StringBuilder builder = new StringBuilder();
        try {
            List<Address> address = geoCoder.getFromLocation(lat, lng, 1);
            int maxLines = address.get(0).getMaxAddressLineIndex();
            for (int i=0; i<maxLines; i++) {
                String addressStr = address.get(0).getAddressLine(i);
                builder.append(addressStr);
                builder.append(" ");
            }

            String fnialAddress = builder.toString(); //This is the complete address.

            latituteField.setText(String.valueOf(lat));
            longitudeField.setText(String.valueOf(lng));
            addressField.setText(fnialAddress); //This will display the final address.

        } catch (IOException e) {
            // Handle IOException
        } catch (NullPointerException e) {
            // Handle NullPointerException
        }*/
    }

    private void updateLocationinDB(String  url){
        //String url ="http://api.openweathermap.org/data/2.5/weather?q="+paramValue.getLocation()+"&APPID="+ Helper.API_KEY+"&units=metric";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("MainActivity", "Response " + response);
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                locationMapObject = gson.fromJson(response, LocationMapObject.class);
                if (null == locationMapObject) {
                    Toast.makeText(getApplicationContext(), "Nothing was returned", Toast.LENGTH_LONG).show();
                } else {
                    //int rowId = paramValue.getId();
                    //Long tempVal = Math.round(Math.floor(Double.parseDouble(locationMapObject.getMain().getTemp())));
                    String city = locationMapObject.getName() /*+ ", " + locationMapObject.getSys().getCountry()*/;
                    query.insertCurrentLocation(city);
                    System.out.println("Got the current Location as " + query.getCurrentCity());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("MainActivity", "Error " + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    private void requestJsonObject(final DatabaseLocationObject paramValue){
        String url ="http://api.openweathermap.org/data/2.5/weather?q="+paramValue.getLocation()+"&APPID="+ Helper.API_KEY+"&units=metric";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("MainActivity", "Response " + response);
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                locationMapObject = gson.fromJson(response, LocationMapObject.class);
                if (null == locationMapObject) {
                    Toast.makeText(getApplicationContext(), "Nothing was returned", Toast.LENGTH_LONG).show();
                } else {
                    final int rowId = paramValue.getId();
                    Long tempVal = Math.round(Math.floor(Double.parseDouble(locationMapObject.getMain().getTemp())));
                    Long tempMin = Math.round(Math.floor(Double.parseDouble(locationMapObject.getMain().getTemp_min())));
                    Long tempMax = Math.round(Math.floor(Double.parseDouble(locationMapObject.getMain().getTemp_max())));


                    System.out.println("Degree preference" + query.getUserDegreeMetric());

                    degreeMetric = "C";

                    if(query.getUserDegreeMetric().equals("Fahrenheit")) {
                        System.out.println("Degree preference inside if" + query.getUserDegreeMetric());
                        tempVal = Helper.convertCelsiusToFahrenheit(tempVal);
                        tempMin = Helper.convertCelsiusToFahrenheit(tempMin);
                        tempMax = Helper.convertCelsiusToFahrenheit(tempMax);
                        degreeMetric = "F";
                    }

                    final String city = locationMapObject.getName() + ", " + locationMapObject.getSys().getCountry();
                    final String weatherInfo = String.valueOf(tempVal) + "<sup>o</sup>" + degreeMetric + ", " + Helper.capitalizeFirstLetter(locationMapObject.getWeather().get(0).getDescription());
                    final String tempMinMax = "Min Temp: " + String.valueOf(tempMin) + "<sup>o</sup>" + degreeMetric + "," + " " + "Max Temp: " + String.valueOf(tempMax) + "<sup>o</sup>" + degreeMetric + "";

                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    final long finalTs = timestamp.getTime()/1000;

                    String loc = locationMapObject.getCoord().getLat() + "," + locationMapObject.getCoord().getLon();

                    String google_url = "https://maps.googleapis.com/maps/api/timezone/json?location=" + loc + "&timestamp=" + finalTs + "&key=" + Helper.TIMEZONE_API_KEY;

                    JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, google_url, (String)null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("MainActivity", "Response " + response);
                            System.out.println("Response Object" + response);

//                            response = gson.toJson(response);
                            try {
                                // to be fine-tuned by Smitha
                                String timeZoneId = response.getString("timeZoneId");

                                Date currentTime = new Date();
                                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM yy dd HH:mm");
                                sdf.setTimeZone(TimeZone.getTimeZone(timeZoneId));
                                String timeToDisplay = sdf.format(currentTime);
                                allData.add(new LocationObject(rowId, city, weatherInfo, tempMinMax, timeToDisplay));

                                //to be modified - venkatesh
                                locationAdapter = new LocationAdapter(MainActivity.this, allData);
                                locationRecyclerView.setAdapter(locationAdapter);


                            }
                            catch(JSONException e){
                                Log.e("Android Weather App", "Unexpected JSON exception", e);
                            }



//                            if(response.status == "OK") {
//                                int dstOffset = response.dstOffset;
//                                int rawOffset = response.rawOffset;
//                                System.out.println("dstOff val: " + dstOffset);
//                                System.out.println("rawOff val: " + rawOffset);
//                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("MainActivity", "Error " + error.getMessage());
                        }
                    });
                    queue.add(jsonRequest);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("MainActivity", "Error " + error.getMessage());
            }
        });
        queue.add(stringRequest);
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
