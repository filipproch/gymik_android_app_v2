package com.jacktech.gymik.fragments;


import android.app.ListFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jacktech.gymik.DataWorker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NewsListFragment extends ListFragment {

	private NewsFragment frag;
	
	public NewsListFragment(NewsFragment newsFragment) {
		this.frag = newsFragment;
	}
	
	public NewsListFragment(){
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onListItemClick(ListView lv, View v, int pos, long id){
		frag.click(pos);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
        try{
            ArrayList<String> newsList = parseArrayList((List<JSONObject>) DataWorker.getInstance().getNews().get("news"));
            ArrayAdapter<String> newsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, newsList);
            setListAdapter(newsAdapter);
        }catch (JSONException e){
            e.printStackTrace();
        }
        getListView().setBackgroundColor(Color.WHITE);
	}
	
	public interface IItemClick{
		public void click(int position);
	}
	
	private ArrayList<String> parseArrayList(List<JSONObject> list) throws JSONException {
		ArrayList<String> retList = new ArrayList<String>();
		for(JSONObject o : list){
			String sS = o.getString("title");
			if(sS.length() > 20)
				sS = sS.substring(0, 20)+"...";
			retList.add(sS);
		}
		return retList;
	}
	
}
