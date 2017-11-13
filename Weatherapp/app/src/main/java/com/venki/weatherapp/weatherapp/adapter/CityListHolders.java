package com.venki.weatherapp.weatherapp.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.venki.weatherapp.weatherapp.R;
import com.venki.weatherapp.weatherapp.entity.CityLocationData;

import java.util.List;

public class CityListHolders extends RecyclerView.ViewHolder{

    public TextView locationCity;

    public TextView weatherInformation;

    public TextView tempMinMax;

    public TextView deleteText;

    public TextView currTimeView;

    public CityListHolders(final View itemView, final List<CityLocationData> cityLocationData) {
        super(itemView);
        locationCity = (TextView) itemView.findViewById(R.id.city_location);
        weatherInformation = (TextView)itemView.findViewById(R.id.temp_info);
        tempMinMax = (TextView) itemView.findViewById(R.id.temp_min_max);
        currTimeView = (TextView) itemView.findViewById(R.id.curr_time);
        deleteText = (TextView)itemView.findViewById(R.id.delete_row);
        deleteText.setTextColor(Color.RED);

    }
}
