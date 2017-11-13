package com.venki.weatherapp.weatherapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.venki.weatherapp.weatherapp.R;
import com.venki.weatherapp.weatherapp.entity.WeatherObject;

import java.util.List;

/**
 * Created by vigneshwarichandrasekaran on 11/12/17.
 */

public class ThreedayViewAdapter extends RecyclerView.Adapter<ThreedayViewHolders>{
    private List<WeatherObject> dailyWeather;

    protected Context context;
    public ThreedayViewAdapter(Context context, List<WeatherObject> dailyWeather) {
        this.dailyWeather = dailyWeather;
        this.context = context;
    }

    @Override
    public ThreedayViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        ThreedayViewHolders viewHolder = null;
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.threehour_layout, parent, false);
        viewHolder = new ThreedayViewHolders(layoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ThreedayViewHolders holder, int position) {

        holder.current_time.setText(dailyWeather.get(position).getDayOfWeek());
        holder.weatherIcon.setImageResource(dailyWeather.get(position).getWeatherIcon());

        double mTemp = Double.parseDouble(dailyWeather.get(position).getWeatherResult());
        holder.weatherResult.setText(String.valueOf(Math.round(mTemp)) + "°");

        holder.weatherStatus.setText(dailyWeather.get(position).getWeatherResultSmall());
//            holder.weatherResultSmall.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return dailyWeather.size();
    }
}
