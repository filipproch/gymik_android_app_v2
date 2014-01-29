package com.jacktech.gymik.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.jacktech.gymik.Adapters;
import com.jacktech.gymik.R;
import com.jacktech.gymik.meals.MealManager;

public class JidloFragment extends ExtraFragment{

    public static final String TAG = JidloFragment.class.getName();
    private View rootView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        MealManager.getInstance().load();
		if (rootView != null) {
	        ViewGroup parent = (ViewGroup) rootView.getParent();
	        if (parent != null)
	            parent.removeView(rootView);
	    }
		try {
			rootView = inflater.inflate(R.layout.jidlo_layout,container, false);
            Adapters.JidloAdapter jidloAdapter = new Adapters.JidloAdapter(getActivity(), MealManager.getInstance().getMeals());
            ListView listView = (ListView) rootView.findViewById(R.id.jidlo_layout_list);
            listView.setAdapter(jidloAdapter);
            listView.setSelection(MealManager.getInstance().getTodayId());
            listView.setDivider(getResources().getDrawable(R.drawable.transperent_color));
	    } catch (Exception e) {
            e.printStackTrace();
	    }

		return rootView;
	}
	
}
