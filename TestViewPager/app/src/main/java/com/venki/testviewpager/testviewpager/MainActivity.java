package com.venki.testviewpager.testviewpager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    ViewPager mViewPager;
    int current_index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewPager = (ViewPager) findViewById(R.id.pager);
/** set the adapter for ViewPager */
        mViewPager.setAdapter(new SamplePagerAdapter(
                getSupportFragmentManager()));
        mViewPager.setCurrentItem(1);
    }
    public int getCurrentIndex()
    {
        return current_index;
    }

    public class SamplePagerAdapter extends FragmentPagerAdapter {

        public SamplePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            /** Show a Fragment based on the position of the current screen */
            SampleFragment fragment = new SampleFragment();
            Bundle args = new Bundle();
            args.putInt("page_position", position + 1);
            fragment.setArguments(args);
            return fragment;
            /*if (position == 0) {
                return new SampleFragment();
            } else
                return new SampleFragmentTwo();
                */
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 4;
        }
    }
}
