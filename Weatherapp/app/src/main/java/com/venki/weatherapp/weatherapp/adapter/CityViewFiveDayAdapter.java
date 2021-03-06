package com.venki.weatherapp.weatherapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.venki.weatherapp.weatherapp.R;
import com.venki.weatherapp.weatherapp.database.DatabaseQuery;
import com.venki.weatherapp.weatherapp.entity.CityWeatherData;
import com.venki.weatherapp.weatherapp.helpers.Helper;

import java.util.List;

public class CityViewFiveDayAdapter extends RecyclerView.Adapter<CityViewFiveDayHolders> {

    private List<CityWeatherData> dailyWeather;

    private DatabaseQuery query;

    private String degreeMetric;

    protected Context context;

    public CityViewFiveDayAdapter(Context context, List<CityWeatherData> dailyWeather) {
        this.dailyWeather = dailyWeather;
        this.context = context;
    }

    @Override
    public CityViewFiveDayHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        CityViewFiveDayHolders viewHolder = null;
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.five_day_view, parent, false);
        viewHolder = new CityViewFiveDayHolders(layoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CityViewFiveDayHolders holder, int position) {

        degreeMetric = "C";

        query = new DatabaseQuery(context);

        long mTemp = (long)Double.parseDouble(dailyWeather.get(position).getWeatherResult());
        long mLowTemp = (long)Double.parseDouble(dailyWeather.get(position).getWeatherResultSmall());
        long mHiTemp = (long)Double.parseDouble(dailyWeather.get(position).getWeatherResultBig());

        if(query.getUserDegreeMetric().equals("Fahrenheit")) {
            System.out.println("Degree preference in three hour adapter" + query.getUserDegreeMetric());
            mTemp = Helper.convertCelsiusToFahrenheit(mTemp);
            mLowTemp = Helper.convertCelsiusToFahrenheit(mLowTemp);
            mHiTemp = Helper.convertCelsiusToFahrenheit(mHiTemp);
            degreeMetric = "F";
        }

        holder.dayOfWeek.setText(dailyWeather.get(position).getDayOfWeek());
        holder.weatherResult.setText(String.valueOf(Math.round(mTemp)) + "°" + degreeMetric);
        holder.weatherResultSmall.setText("lo:" + String.valueOf(Math.round(mLowTemp)) + "°" + degreeMetric);
        holder.weatherResultBig.setText("hi:" + String.valueOf(Math.round(mHiTemp)) + "°" + degreeMetric);
    }

    @Override
    public int getItemCount() {
        return dailyWeather.size();
    }

}
