package com.jacktech.gymik.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.jacktech.gymik.GymikActivity;
import com.jacktech.gymik.MapView;
import com.jacktech.gymik.R;

public class MapFragment extends ExtraFragment {

private View rootView;
    private MapView map;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.map_layout,container, false);
		map = (MapView) rootView.findViewById(R.id.map_view);
		return rootView;
	}

    @Override
    public void action(ActionType actionType, Bundle bundle){
        if(map != null){
            if(actionType == ActionType.ACTION_BAR_TAB_SELECTED){
                map.updateLevel(bundle.getInt(GymikActivity.ACTION_BAR_TAB_POSITION));
            }else if(actionType == ActionType.ACTION_BAR_TAB_UNSELECTED){
                //TODO: maybe do something?
            }
        }
    }

}
