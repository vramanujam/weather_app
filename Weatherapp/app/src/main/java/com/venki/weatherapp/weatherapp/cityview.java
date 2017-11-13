package com.venki.weatherapp.weatherapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.venki.weatherapp.weatherapp.database.DatabaseQuery;

public class cityview extends AppCompatActivity {

    ViewPager mViewPager;
    private DatabaseQuery query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cityview);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.show();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle("City View");

        query = new DatabaseQuery(this);
        String currentLocation = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentLocation = extras.getString("city");
        }
        String[] city = currentLocation.split(",");
        int row = query.getRowNumber(city[0]);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new SamplePagerAdapter(getSupportFragmentManager()));
        mViewPager.setCurrentItem(row);
    }
    public class SamplePagerAdapter extends FragmentPagerAdapter {

        public SamplePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            SampleFragment fragment = new SampleFragment();
            Bundle args = new Bundle();
            args.putInt("page_position", position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return query.countAllStoredLocations();
        }
    }
}
