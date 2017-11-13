package com.venki.weatherapp.weatherapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.venki.weatherapp.weatherapp.MainActivity;
import com.venki.weatherapp.weatherapp.R;
import com.venki.weatherapp.weatherapp.database.DatabaseQuery;
import com.venki.weatherapp.weatherapp.entity.WeatherObject;
import com.venki.weatherapp.weatherapp.helpers.Helper;
import com.venki.weatherapp.weatherapp.helpers.ImageLoadTask;

import java.util.List;

/**
 * Created by vigneshwarichandrasekaran on 11/12/17.
 */

public class ThreedayViewAdapter extends RecyclerView.Adapter<ThreedayViewHolders>{
    private List<WeatherObject> dailyWeather;

    private DatabaseQuery query;

    private String degreeMetric;

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

        degreeMetric = "C";

        holder.current_time.setText(dailyWeather.get(position).getDayOfWeek());
        //holder.weatherIcon.setImageResource(dailyWeather.get(position).getWeatherIcon());
        new ImageLoadTask("http://openweathermap.org/img/w/" + dailyWeather.get(position).getWeatherUrl() + ".png", holder.weatherIcon).execute();
        long mTemp = (long)Double.parseDouble(dailyWeather.get(position).getWeatherResult());

        query = new DatabaseQuery(context);

        if(query.getUserDegreeMetric().equals("Fahrenheit")) {
            System.out.println("Degree preference in three hour adapter" + query.getUserDegreeMetric());
            mTemp = Helper.convertCelsiusToFahrenheit(mTemp);
            degreeMetric = "F";
        }

        holder.weatherResult.setText(String.valueOf(Math.round(mTemp)) + "°" + degreeMetric);

        holder.weatherStatus.setText(dailyWeather.get(position).getWeatherResultSmall());
    }

    @Override
    public int getItemCount() {
        return dailyWeather.size();
    }
}
