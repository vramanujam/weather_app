package com.venki.weatherapp.weatherapp.adapter;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.venki.weatherapp.weatherapp.R;
import com.venki.weatherapp.weatherapp.cityview;
import com.venki.weatherapp.weatherapp.database.DatabaseQuery;
import com.venki.weatherapp.weatherapp.entity.LocationObject;

import java.util.List;

public class CityListAdapter extends RecyclerView.Adapter<CityListHolders> /*implements CompoundButton.OnCheckedChangeListener*/{

    private List<LocationObject> locationObjects;

    protected Context context;

    private DatabaseQuery dbQuery;

    public CityListAdapter(Context context, List<LocationObject> locationObjects) {
        this.locationObjects = locationObjects;
        this.context = context;
        dbQuery = new DatabaseQuery(context);
    }

    @Override
    public CityListHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        CityListHolders viewHolder = null;
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.city_list_view, parent, false);
        viewHolder = new CityListHolders(layoutView, locationObjects);
        return viewHolder;
    }

    public int getLocationIndex(String cityName)
    {
        int index = 0;
        for(LocationObject L: locationObjects){
            System.out.println(L.getLocationCity() + " " + cityName);
            if(L.getLocationCity().equals(cityName)){
                return index;
            }
            index++;
        }
        return -1;
    }
    public void removeLocation(String cityName){
        int Index = getLocationIndex(cityName);
        int databaseIndex = locationObjects.get(Index).getId();
        dbQuery.deleteLocation(databaseIndex);
        locationObjects.remove(Index);
        notifyItemRemoved(Index);
    }
    @Override
    public void onBindViewHolder(final CityListHolders holder, final int position) {

        final String cityName = locationObjects.get(position).getLocationCity();
        System.out.println("cityName:" + cityName + " " + "position:" + position);
        holder.locationCity.setText(Html.fromHtml(locationObjects.get(position).getLocationCity()));
        holder.weatherInformation.setText(Html.fromHtml(locationObjects.get(position).getWeatherInformation()));
        holder.tempMinMax.setText(Html.fromHtml(locationObjects.get(position).getTempMinMax()));
        holder.currTimeView.setText(Html.fromHtml(locationObjects.get(position).getCurrTime()));
        holder.deleteText.setTag(R.id.TAG_KEY, String.valueOf(locationObjects.get(position).getId()));

        holder.deleteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeLocation(cityName);
            }
        });

        holder.locationCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addLocationIntent = new Intent(context, cityview.class);
                addLocationIntent.putExtra("city",cityName);
                context.startActivity(addLocationIntent);
            }
        });
        holder.weatherInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addLocationIntent = new Intent(context, cityview.class);
                addLocationIntent.putExtra("city",cityName);
                context.startActivity(addLocationIntent);
            }
        });;

    }

    @Override
    public int getItemCount() {
        return this.locationObjects.size();
    }



}
