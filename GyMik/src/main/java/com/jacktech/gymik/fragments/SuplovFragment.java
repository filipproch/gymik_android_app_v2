package com.jacktech.gymik.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jacktech.gymik.Adapters.SuplovAdapter;
import com.jacktech.gymik.DataWorker;
import com.jacktech.gymik.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SuplovFragment extends ExtraFragment {

	private View rootView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.suplov_layout,container, false);
        try{
            JSONObject suplx = DataWorker.getInstance().getSuplovani();
            if(suplx.get("data") != null || suplx.getJSONArray("data").length() <= 0 || (suplx.getJSONArray("data").length() == 1 && suplx.getJSONArray("data").getJSONObject(0).getJSONArray("data") == null)){
                TextView tx = new TextView(getActivity());
                tx.setText("Žádné změny");
                tx.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                tx.setGravity(Gravity.CENTER);
                tx.setTextSize(24f);
                tx.setTextColor(Color.BLACK);
                tx.setPadding(0, 15, 0, 0);
                ((RelativeLayout)rootView).removeAllViews();
                ((RelativeLayout)rootView).addView(tx);
            }else{
                JSONArray suplovani = DataWorker.getInstance().getSuplovani().getJSONArray("data");
                final ArrayList<JSONObject> suplovList = new ArrayList<JSONObject>();
                HashMap<Integer, String> suplovDays= new HashMap<Integer, String>();
                int k = 0;
                for(int a = 0;a<suplovani.length();a++){
                    JSONObject suplov = suplovani.getJSONObject(a);
                    if(suplov != null && suplov.get("data") != null){
                        suplovDays.put(k, suplov.getString("day"));
                        suplovList.addAll(jsonArrayToArrayList(suplov.getJSONArray("data")));
                        k+=suplov.getJSONArray("data").length();
                    }
                }
                SuplovAdapter suplov = new SuplovAdapter(getActivity(), R.layout.suplov_item, suplovList, suplovDays);
                ListView suplovLV = (ListView) rootView.findViewById(R.id.suplov_list_view);
                suplovLV.setAdapter(suplov);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
		return rootView;
	}

    private ArrayList<JSONObject> jsonArrayToArrayList(JSONArray data) throws JSONException {
        ArrayList<JSONObject> objects = new ArrayList<JSONObject>(data.length());
        for(int a = 0;a<data.length();a++)
            objects.add(data.getJSONObject(a));
        return objects;
    }

}
