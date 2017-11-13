package com.venki.weatherapp.weatherapp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.venki.weatherapp.weatherapp.R;

public class ThreeHourViewHolders extends RecyclerView.ViewHolder{
    private static final String TAG = CityViewFiveDayHolders.class.getSimpleName();

    public TextView current_time;

    public ImageView weatherIcon;

    public TextView weatherResult;

    public TextView weatherStatus;


    public ThreeHourViewHolders(final View itemView) {
        super(itemView);
        current_time = (TextView)itemView.findViewById(R.id.time_of_day);
        weatherIcon = (ImageView)itemView.findViewById(R.id.three_hour_weather_icon);
        weatherResult = (TextView) itemView.findViewById(R.id.three_hour_weather_result);
        weatherStatus = (TextView)itemView.findViewById(R.id.threehour_weather_status);
    }
}
