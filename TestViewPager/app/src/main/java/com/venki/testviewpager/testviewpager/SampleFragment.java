package com.venki.testviewpager.testviewpager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SampleFragment extends Fragment {
    public int index = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_one, container,
                false);
        MainActivity m = (MainActivity)getActivity();
        index = m.getCurrentIndex();
        return rootView;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        TextView v = (TextView)getView().findViewById(R.id.textView1);
        //MainActivity m = (MainActivity)getActivity();
        v.setText("current Index" +  String.valueOf(getArguments().getInt("page_position")));
    }
}